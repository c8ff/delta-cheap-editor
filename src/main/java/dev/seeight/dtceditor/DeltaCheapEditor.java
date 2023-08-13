package dev.seeight.dtceditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.seeight.common.lwjgl.Window;
import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.history.impl.DeleteObjects;
import dev.seeight.dtceditor.history.impl.SelectObjects;
import dev.seeight.dtceditor.input.Mouse;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.impl.*;
import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.DoorExit;
import dev.seeight.dtceditor.room.ext.InvisibleWall;
import dev.seeight.dtceditor.room.ext.Player;
import dev.seeight.dtceditor.room.ext.RoomDoor;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.dtceditor.tools.impl.AddObjectTool;
import dev.seeight.dtceditor.tools.impl.MoveCamera;
import dev.seeight.dtceditor.tools.impl.ResizeObjectsTool;
import dev.seeight.dtceditor.tools.impl.SelectTool;
import dev.seeight.dtceditor.utils.IOUtil;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.OpenGLRenderer2;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DeltaCheapEditor implements StuffListener {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final Gson gsonRaw = new GsonBuilder().create();

	private Tool selectedTool;
	private final float[] m0 = new float[16];

	private double prevTime;
	/**
	 * The time in seconds between the previous frame and the current frame.
	 * This value is calculated at the end of the loop, before polling GLFW events.
	 */
	private double deltaTime;

	public boolean dirty;
	public float cameraX;
	public float cameraY;
	public float zoom = 1;
	private final Window window;
	private final OpenGLRenderer2 renderer;
	private int gridSize = 20;
	private final GridRenderer gridRenderer;

	public final FontRenderer font;
	public final FontRenderer fontBold;

	private PopUp popUp;
	private PopUp nextPopUp;

	public final Mouse mouse;

	private final List<IHistoryEntry> historyEntries;
	private int historyIndex;

	private final List<Tool> tools;

	private final List<RoomObject> objects;
	public final List<RoomObject> objectsUnmodifiable;

	public int roomWidth = 640;
	public int roomHeight = 480;

	private String loadedBackgroundPath;
	private Texture backgroundTexture;
	private String backgroundToLoad;
	private boolean pendingBackground;
	private List<Layer> layers;
	private List<Layer> layersToLoad;
	private boolean pendingLayers;

	private boolean renderGrid = true;

	public boolean ctrl;
	public boolean shift;
	private boolean ignoreClick;

	private final ObjectTextureProvider objectTextureProvider;

	private String loadedRoomPath = null;

	public DeltaCheapEditor(Window window, OpenGLRenderer2 renderer) {
		this.mouse = new Mouse();
		this.dirty = true;
		this.window = window;
		this.renderer = renderer;
		try {
			this.objectTextureProvider = new ObjectTextureProvider();
			this.font = IOUtil.fontFromPath(renderer, gson, this, "/fonts/inter18/");
			this.fontBold = IOUtil.fontFromPath(renderer, gson, this, "/fonts/interBold24/");
			this.gridRenderer = new GridRenderer(renderer, "/");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.historyEntries = new ArrayList<>();
		this.objects = new ArrayList<>();
		this.objectsUnmodifiable = Collections.unmodifiableList(this.objects);
		this.popUp = null;
		this.tools = new ArrayList<>();
		try {
			this.tools.add(new AddObjectTool(this, IOUtil.textureFromPath(this, "/icons/addObject.png")));
			this.tools.add(new SelectTool(this, IOUtil.textureFromPath(this, "/icons/select.png")));
			this.tools.add(new MoveCamera(this, IOUtil.textureFromPath(this, "/icons/move.png")));
			this.tools.add(new ResizeObjectsTool(this, IOUtil.textureFromPath(this, "/icons/resizeObjects.png")));
			this.selectedTool = this.tools.get(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		window.setDropCallback(new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				String name = GLFWDropCallback.getName(names, 0);
				if (name.endsWith(".json")) {
					if (DeltaCheapEditor.this.popUp instanceof LoadRoomPopUp p) {
						p.setSelectedFile(name);
					} else {
						DeltaCheapEditor.this.setPopUp(new LoadRoomPopUp(DeltaCheapEditor.this, name));
					}
				}
			}
		});
	}

	@Override
	public void windowFocus(boolean windowFocus) {

	}

	@Override
	public void framebufferSize(int width, int height) {

	}

	@Override
	public void mouseButton(int button, int action) {
		this.mouse.mouseButton(button, action);

		if (ignoreClick && button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
			ignoreClick = false;
			return;
		}

		if (this.popUp != null) {
			if (!this.popUp.contains(mouse.getX(), mouse.getY())) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
					if (this.popUp.isClosing()) {
						this.popUp.setClosingProgress(100F);
					} else {
						this.popUp.setClosing(true);
					}
				}
			} else if (!this.popUp.isClosing()) {
				this.popUp.mouseButton(button, action);
			}

			return;
		}

		for (Tool tool : tools) {
			if (tool.contains(mouse.getX(), mouse.getY())) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
					this.selectedTool = tool;
				} else if (button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS) {
					PopUp pop = this.selectedTool.getOptionsPopUp();
					if (pop != null) {
						this.setPopUp(pop);
					}
				}
				ignoreClick = action != GLFW.GLFW_RELEASE;
				return;
			}
		}

		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			boolean down = action != GLFW.GLFW_RELEASE;

			if (down) {
				this.selectedTool.click(0, mouse.getXi(), mouse.getYi());
			} else {
				this.selectedTool.lift(0, mouse.getXi(), mouse.getYi());
			}
		}
	}

	@Override
	public void cursorPosition(double x, double y) {
		this.mouse.cursorPosition(x, y);

		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.cursorPosition(x, y);
			return;
		}

		if (this.mouse.button1.isDown() && !this.ignoreClick) {
			this.selectedTool.drag(0, mouse.getXi(), mouse.getYi());
		}
	}

	@Override
	public void key(int key, int action, int mods) {
		if (key == GLFW.GLFW_KEY_F11) {
			if (action == GLFW.GLFW_PRESS) {
				this.window.setFullscreen(!this.window.isFullscreen());
			}
			return;
		}

		if (this.popUp != null && !this.popUp.isClosing()) {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				if (action == GLFW.GLFW_RELEASE) {
					if (this.popUp.isClosing()) {
						this.popUp.setClosingProgress(100F);
					} else {
						this.popUp.setClosing(true);
					}
				}
			} else if (!this.popUp.isClosing()) {
				this.popUp.key(key, action, mods);
			}

			return;
		}

		if (key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			ctrl = action != GLFW.GLFW_RELEASE;
		}

		if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			shift = action != GLFW.GLFW_RELEASE;
		}

		if (action != GLFW.GLFW_PRESS) {
			return;
		}

		switch (key) {
			case GLFW.GLFW_KEY_R -> {
				this.cameraX = 0;
				this.cameraY = 0;
				this.zoom = 1;
				this.historyEntries.clear();
			}
			case GLFW.GLFW_KEY_S -> {
				if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
					break;
				}

				SaveRoomPopUp.promptSave(this, mods);
			}
			case GLFW.GLFW_KEY_A -> {
				if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
					this.addHistory(new SelectObjects(this, collectSelectedObjects(roomObject -> roomObject.selected = true), true));
				}
			}
			case GLFW.GLFW_KEY_DELETE -> {
				if (this.getSelectedCount() == 0) {
					return;
				}

				List<RoomObject> objects1 = new ArrayList<>();
				for (RoomObject object : this.objects) {
					if (object.selected) {
						objects1.add(object);
					}
				}

				this.objects.removeAll(objects1);
				this.addHistory(new DeleteObjects(this.objects, objects1));
			}
			case GLFW.GLFW_KEY_Z -> {
				if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
					if (this.historyIndex > 0 && this.historyIndex <= this.historyEntries.size()) {
						this.historyEntries.get(--this.historyIndex).undo();
					}
				}
			}
			case GLFW.GLFW_KEY_Y -> {
				if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
					if (this.historyIndex < this.historyEntries.size() && this.historyIndex >= -1) {
						this.historyEntries.get(this.historyIndex++).redo();
					}
				}
			}
			case GLFW.GLFW_KEY_V -> this.setPopUp(new GridSizePopUp(this));
			case GLFW.GLFW_KEY_B -> this.setPopUp(new RoomDetails(this));
			case GLFW.GLFW_KEY_N -> this.setPopUp(new LoadRoomPopUp(this));
			case GLFW.GLFW_KEY_U -> this.setPopUp(new ConsolePopUp(this));
			case GLFW.GLFW_KEY_D -> {
				this.renderGrid = !this.renderGrid;
				this.dirty = true;
			}
			case GLFW.GLFW_KEY_1 -> this.gridSize = 5;
			case GLFW.GLFW_KEY_2 -> this.gridSize = 10;
			case GLFW.GLFW_KEY_3 -> this.gridSize = 20;
			case GLFW.GLFW_KEY_4 -> this.gridSize = 30;
			case GLFW.GLFW_KEY_5 -> this.gridSize = 40;
			case GLFW.GLFW_KEY_6 -> this.gridSize = 50;
		}
	}

	@Override
	public void scroll(double x, double y) {
		this.mouse.scroll(x, y);

		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.scroll(x, y);
			return;
		}

		if (this.ctrl) {
			this.zoom += Math.ceil(Math.floor(y) * 10) / 10 / 4F;
			if (this.zoom <= 0) {
				this.zoom = 0;
			}
			return;
		} else {
			// TODO: Implement 'x' scroll too (for laptops lol)
			if (this.shift) {
				this.cameraX += this.gridSize * (y > 0 ? 1 : -1);
			} else {
				this.cameraY += this.gridSize * (y > 0 ? 1 : -1);
			}
		}

		this.selectedTool.scroll(x, y);
	}

	@Override
	public void character(int codepoint) {
		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.character(codepoint);
		}
	}

	protected void checkNextPopUp() {
		if (this.nextPopUp != null) {
			if (this.popUp == null) {
				this.popUp = this.nextPopUp;
			} else if (this.popUp.isClosing() && this.popUp.getClosingProgress() >= 100) {
				this.popUp.close();
				this.popUp = this.nextPopUp;
			} else {
				this.popUp.setClosing(true);
				return;
			}

			this.nextPopUp = null;
			this.popUp.setClosingProgress(100F);
			this.popUp.setClosing(false);
			this.popUp.init();
		} else if (this.popUp != null && this.popUp.isClosing() && this.popUp.getClosingProgress() >= 100) {
			this.popUp.close();
			this.popUp = this.nextPopUp;
		}
	}

	protected void checkBackgroundTextureQueue() {
		if (this.pendingBackground) {
			if (this.backgroundToLoad == null) {
				if (this.backgroundTexture != null) {
					this.backgroundTexture.delete();
				}
				this.backgroundTexture = null;
				this.loadedBackgroundPath = null;
			} else {
				try {
					if (this.backgroundTexture != null) {
						this.backgroundTexture.delete();
					}
					this.backgroundTexture = GLTexture.fromInputStream(new FileInputStream(this.backgroundToLoad));
					this.loadedBackgroundPath = this.backgroundToLoad;
				} catch (IOException e) {
					System.err.print("Couldn't load texture. ");
					e.printStackTrace();
				}
			}

			this.pendingBackground = false;
		}
	}

	protected void checkLayerQueue() {
		if (!this.pendingLayers) {
			return;
		}

		if (this.layersToLoad == null) {
			if (this.layers != null) {
				for (Layer layer : this.layers) {
					if (layer.texture == null) {
						continue;
					}

					layer.texture.delete();
				}
			}

			this.layers = null;
		} else {
			if (this.layers != null) {
				for (Layer layer : this.layers) {
					if (layer.texture == null) {
						continue;
					}

					layer.texture.delete();
				}
			}

			this.layers = this.layersToLoad;
			for (Layer layer : this.layers) {
				try {
					layer.createTexture();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void render() {
		if (selectedTool.isActionFinished()) {
			IHistoryEntry next = selectedTool.getNext();
			if (next != null) {
				this.addHistory(next);
			}
		}

		if (!this.dirty) {
			return;
		}

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		renderer.frameStart();

		renderer.getViewMatrix4f(m0);
		renderer.scale(zoom, zoom, 1);
		renderer.translate(cameraX, cameraY, 0);

		try {
			renderBackground();
			renderObjects();
			renderGrid();

			selectedTool.render();

			renderer.setViewMatrix4f(m0);

			renderToolIcons();
			renderPopUp();
		} catch (Exception e) {
			e.printStackTrace();
			window.setShouldClose(true);
		}

		renderer.frameEnd();

		window.swapBuffers();

		mouse.frameEnd();

		if (!window.shouldClose()) {
			try {
				this.checkBackgroundTextureQueue();
				this.checkLayerQueue();
				this.checkNextPopUp();
			} catch (Exception e) {
				e.printStackTrace();
				window.setShouldClose(true);
			}
		}

		double time = GLFW.glfwGetTime();
		this.deltaTime = time - this.prevTime;
		this.prevTime = time;
	}

	protected void renderBackground() {
		if (backgroundTexture == null) {
			renderer.color(0.25F, 0.25F, 0.25F, 1);
			renderer.rect2d(0, 0, roomWidth, roomHeight);
		} else {
			renderer.color(1, 1, 1, 1);
			renderer.texRect2d(backgroundTexture, 0, 0, roomWidth, roomHeight);
		}

		if (layers != null) {
			renderer.color(1, 1, 1, 1);
			for (Layer layer : layers) {
				renderer.texRect2d(layer.texture, 0, 0, roomWidth, roomHeight);
			}
		}
	}

	protected void renderGrid() {
		if (!renderGrid) return;
		renderer.color(1, 1, 1, 0.25F); // TODO: Make grid opacity custom
		gridRenderer.render(0, 0, roomWidth, roomHeight, cameraX, cameraY, zoom, window.getHeight(), gridSize);
	}

	protected void renderObjects() {
		renderer.color(1, 1, 1, 0.25F);
		for (RoomObject object : this.objects) {
			float a = object instanceof InvisibleWall ? 0.3F : (object instanceof DoorExit || object instanceof RoomDoor ? 0.6F : 1F);

			if (object.selected) {
				renderer.color(1, 0.25F, 0.25F, a);
			} else {
				renderer.color(1, 1, 1, a);
			}

			renderer.texRect2d(object.getTexture(this.objectTextureProvider), object.x + object.renderOffsetX, object.y + object.renderOffsetY, object.x + object.renderOffsetX + object.getWidth(), object.y + object.renderOffsetY + object.getHeight());
		}
	}

	protected void renderPopUp() {
		if (popUp != null) {
			renderer.color(0, 0, 0, (100 - this.popUp.getClosingProgress()) / 200F);
			renderer.rect2d(0, 0, window.getWidth(), window.getHeight());
			renderer.color(1, 1, 1, 1);
			popUp.animateClosing();
			popUp.render();
		}
	}

	protected void renderToolIcons() {
		float x = 10;
		float y = 10;
		int iconSize = 32;
		int margin = 4;

		// background
		renderer.color(0, 0, 0, 0.75F);
		renderer.rect2d(x + margin, y + margin, x + margin * 2 + iconSize, y + margin * 2 + iconSize * this.tools.size());

		// tools
		renderer.color(0.5F, 0.5F, 0.5F, 1);
		for (Tool tool : this.tools) {
			tool.renderX = x;
			tool.renderY = y;
			tool.renderX2 = x + 32 + margin * 2;
			tool.renderY2 = y + 32 + margin * 2;

			if (this.selectedTool == tool) {
				renderer.color(1, 1, 1, 1);
			}

			renderer.texRect2d(tool.getIcon(), x + margin, y + margin, x + margin + 32, y + margin + 32);

			if (this.selectedTool == tool) {
				renderer.color(0.5F, 0.5F, 0.5F, 1);
			}

			y += 32;
		}
	}

	public void addHistory(IHistoryEntry historyEntry) {
		if (this.historyIndex < this.historyEntries.size() - 1) {
			this.historyEntries.subList(Math.max(this.historyIndex, 0), this.historyEntries.size()).clear();
		}

		if (this.historyIndex == 0 && !this.historyEntries.isEmpty()) {
			this.historyEntries.clear();
		}

		this.historyEntries.add(historyEntry);
		this.historyIndex = this.historyEntries.size();
	}

	public void shutdown() {
		try {
			Server.shutdownServerThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<RoomObject> collectSelectedObjects(Consumer<RoomObject> consumer) {
		List<RoomObject> list = new ArrayList<>();

		for (RoomObject object : this.objects) {
			if (object.selected) {
				try {
					list.add(object);
					consumer.accept(object);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}

		return list;
	}

	public int getSelectedCount() {
		int c = 0;

		for (RoomObject object : this.objects) {
			if (object.selected) {
				c++;
			}
		}

		return c;
	}

	public void addObject(RoomObject object) {
		if (object instanceof Player) {
			if (this.objects.contains(object)) {
				return;
			}
		}

		this.objects.add(object);
	}

	public void removeObject(RoomObject object) {
		this.objects.remove(object);
	}

	public int snapToGrid(float input) {
		double mod = input % gridSize;
		int a = (int) (input / gridSize) * gridSize;
		if (mod > gridSize / 2d) {
			return a + gridSize;
		}

		return a;
	}

	public void setPopUp(PopUp popUp) {
		this.nextPopUp = popUp;
	}

	public OpenGLRenderer2 getRenderer() {
		return renderer;
	}

	public double getDeltaTime() {
		return deltaTime;
	}

	public Window getWindow() {
		return this.window;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int size) {
		this.gridSize = size;
	}

	public void unselectAllObjects() {
		for (RoomObject object : this.objects) {
			object.selected = false;
		}
	}

	public void setBackground(String backgroundToLoad) {
		this.backgroundToLoad = backgroundToLoad;
		this.pendingBackground = true;
	}

	public void setLayers(List<Layer> layers) {
		this.layersToLoad = layers;
		this.pendingLayers = true;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public String getLoadedBackgroundPath() {
		return loadedBackgroundPath;
	}

	public String getLoadedRoomPath() {
		return loadedRoomPath;
	}

	public void setLoadedRoomPath(String loadedRoomPath) {
		this.loadedRoomPath = loadedRoomPath;

		if (loadedRoomPath == null) {
			this.window.setTitle("Editor");
		} else {
			this.window.setTitle("Editor | " + loadedRoomPath);
		}
	}

	public void clearObjects() {
		this.objects.clear();
	}

	public void clearBackground() {
		this.setBackground(null);
	}

	public void clearHistory() {
		this.historyEntries.clear();
		this.historyIndex = 0;
	}

	public static class Layer {
		private transient GLTexture texture;
		public String filename;
		public String order;
		public transient String folder;

		public Layer(String filename, String order, String folder) {
			this.filename = filename;
			this.order = order;
			this.folder = folder;
		}

		public void createTexture() throws IOException {
			texture = GLTexture.fromInputStream(new FileInputStream(folder + File.separator + filename));
		}
	}

	private static class ObjectTextureProvider implements IObjectTexture {
		private final Texture invisibleWall;
		private final Texture roomDoor;
		private final Texture textInvisibleWall;
		private final Texture playerTexture;
		private final Texture doorExit;

		public ObjectTextureProvider() throws IOException {
			this.invisibleWall = IOUtil.textureFromPath(this, "/invisible_wall_debug.jpg");
			this.roomDoor = IOUtil.textureFromPath(this, "/door_debug.jpg");
			this.textInvisibleWall = IOUtil.textureFromPath(this, "/invisible_wall_debug.jpg");
			this.playerTexture = IOUtil.textureFromPath(this, "/kris.png");
			this.doorExit = IOUtil.textureFromPath(this, "/door_exit.png");
		}

		@Override
		public Texture getInvisibleWallTexture() {
			return this.invisibleWall;
		}

		@Override
		public Texture getTextInvisibleWallTexture() {
			return this.textInvisibleWall;
		}

		@Override
		public Texture getRoomDoorTexture() {
			return this.roomDoor;
		}

		@Override
		public Texture getPlayerTexture() {
			return this.playerTexture;
		}

		@Override
		public Texture getDoorExitTexture() {
			return this.doorExit;
		}
	}
}
