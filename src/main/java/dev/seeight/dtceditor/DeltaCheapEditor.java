package dev.seeight.dtceditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.seeight.common.lwjgl.Window;
import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.input.Mouse;
import dev.seeight.dtceditor.mgr.TextureManager;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.impl.AskCloseTabPopUp;
import dev.seeight.dtceditor.popup.impl.LoadRoomPopUp;
import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.dtceditor.tab.ITab;
import dev.seeight.dtceditor.utils.IOUtil;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.OpenGLRenderer2;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class DeltaCheapEditor implements StuffListener {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final Gson gsonRaw = new GsonBuilder().create();

	private double prevTime;
	/**
	 * The time in seconds between the previous frame and the current frame.
	 * This value is calculated at the end of the loop, before polling GLFW events.
	 */
	private double deltaTime;

	public boolean dirty;

	private final Window window;
	private final OpenGLRenderer2 renderer;

	private final GridRenderer gridRenderer;

	public final FontRenderer font;
	public final FontRenderer fontBold;

	private PopUp popUp;
	private PopUp nextPopUp;

	public final Mouse mouse;

	public boolean ctrl;
	public boolean shift;

	private final ObjectTextureProvider objectTextureProvider;


	private final Queue<Runnable> frameStartTasks = new LinkedTransferQueue<>();

	private final TextureManager textureManager;

	private final Texture addTabIcon;

	private final List<ITab> tabs;
	private ITab tab;

	public DeltaCheapEditor(Window window, OpenGLRenderer2 renderer) {
		this.mouse = new Mouse();
		this.dirty = true;
		this.window = window;
		this.renderer = renderer;
		this.textureManager = new TextureManager();
		this.tabs = new ArrayList<>();
		this.tabs.add(new EditorTab(this, new Room(640, 480, new ArrayList<>(), new ArrayList<>())));
		this.setTab(this.tabs.get(this.tabs.size() - 1));

		try {
			this.objectTextureProvider = new ObjectTextureProvider(this.textureManager);
			this.font = IOUtil.fontFromPath(renderer, gson, this, "/fonts/inter18/", this.textureManager);
			this.fontBold = IOUtil.fontFromPath(renderer, gson, this, "/fonts/interBold24/", this.textureManager);
			this.gridRenderer = new GridRenderer(renderer, "/", this.textureManager);
			this.addTabIcon = this.textureManager.get("/icons/add_tab.png");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.popUp = null;

		window.setDropCallback(new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				String name = GLFWDropCallback.getName(names, 0);
				if (name.endsWith(".json")) {
					if (DeltaCheapEditor.this.popUp instanceof LoadRoomPopUp p) {
						p.setSelectedFile(name);
					} else {
						if (tab instanceof EditorTab t) {
							DeltaCheapEditor.this.setPopUp(new LoadRoomPopUp(DeltaCheapEditor.this, name));
						}
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
		if (tab != null) tab.framebufferSize(width, height);
	}

	@Override
	public void mouseButton(int button, int action) {
		this.mouse.mouseButton(button, action);

		if (clickedTabBounds(mouse.getXi(), mouse.getYi())) {
			int i = clickedTab(mouse.getXi(), mouse.getYi());

			// Clicked new tab
			if (i == -2) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
					EditorTab tab1 = new EditorTab(this, new Room(640, 480, new ArrayList<>(), new ArrayList<>()));
					this.addTab(tab1);
					this.setTab(tab1);
				}

				return;
			}

			if (i < 0) return; // Selected nothing

			if (action == GLFW.GLFW_PRESS) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
					this.setTab(this.tabs.get(i));
				} else if (button == GLFW.GLFW_MOUSE_BUTTON_3) {
					ITab iTab = this.tabs.get(i);
					if (!iTab.isSaved()) {
						this.setPopUp(new AskCloseTabPopUp(this, iTab));
					} else {
						this.removeTab(iTab);
					}
				}
			}

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

		if (this.tab != null) this.tab.mouseButton(button, action);
	}

	@Override
	public void cursorPosition(double x, double y) {
		this.mouse.cursorPosition(x, y);

		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.cursorPosition(x, y);
			return;
		}

		if (this.tab != null) this.tab.cursorPosition(x, y);
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

		if (this.tab != null) this.tab.key(key, action, mods);
	}

	@Override
	public void scroll(double x, double y) {
		this.mouse.scroll(x, y);

		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.scroll(x, y);
			return;
		}

		if (this.tab != null) this.tab.scroll(x, y);
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

	public void render() {
		if (this.tab != null) tab.prerender();

		if (!this.dirty) {
			return;
		}

		while (!frameStartTasks.isEmpty()) {
			try {
				frameStartTasks.poll().run();
			} catch (Exception e) {
				System.err.print("An error occurred while executing task. ");
				e.printStackTrace(System.err);
			}
		}

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		renderer.frameStart();

		try {
			if (this.tab != null) {
				tab.render();
			} else {
				renderer.color(1, 1, 1, 1);
				fontBold.drawString("No tabs open.", 20, 20);
				font.drawString("I don't know how you got here.", 20, 20 + fontBold.FONT_HEIGHT_FLOAT + 4);
			}

			renderTabs();

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

	protected void renderTabs() {
		float x = tab instanceof EditorTab ? 32 : 0; // Spacing for EditorTab's tools
		float width = window.getWidth() - x;
		float height = 40;
		float y = window.getHeight() - height;
		float tabWidth = 200;
		float tabHeight = 35;

		renderer.color(0.85F, 0.85F, 0.85F, 1F);
		renderer.rect2d(x, y, x + width, y + height);

		x += 6;

		for (ITab iTab : tabs) {
			float tx = x;
			float ty = y + height - tabHeight;

			if (iTab == tab) {
				renderer.color(1, 1, 1, 1);
			} else {
				renderer.color(1, 1, 1, 0.50F);
			}
			renderer.rect2d(tx, ty, tx + tabWidth, ty + tabHeight);

			renderer.color(0, 0, 0, 1);
			if (iTab instanceof EditorTab t) {
				font.drawString(t.title, tx + 2, ty + 2);
			} else {
				font.drawString(iTab.getClass().getSimpleName(), tx + 2, ty + 2);
			}
			x += tabWidth + 6;
		}

		x += addTabIcon.getWidth();

		int bx = (int) (x - addTabIcon.getWidth() / 2F);
		int by = (int) (y + (height - addTabIcon.getHeight()) / 2F) + 1;
		renderer.color(0, 0, 0, 1);
		renderer.texRect2d(addTabIcon, bx, by, bx + addTabIcon.getWidth(), by + addTabIcon.getHeight());
	}

	protected boolean clickedTabBounds(int mx, int my) {
		float width = window.getWidth();
		float height = 40;
		float x = 0;
		float y = window.getHeight() - height;

		return x < mx && y < my && x + width > mx && y + height > my;
	}

	protected int clickedTab(int mx, int my) {
		float x = tab instanceof EditorTab ? 32 : 0; // Spacing for EditorTab's tools
		float height = 40;
		float y = window.getHeight() - height;
		float tabWidth = 200;
		float tabHeight = 35;

		x += 6;

		for (int i = 0; i < tabs.size(); i++) {
			float tx = x;
			float ty = y + height - tabHeight;

			if (tx < mx && ty < my && tx + tabWidth > mx && ty + tabHeight > my) {
				return i;
			}

			x += tabWidth + 6;
		}

		x += addTabIcon.getWidth();

		int bx = (int) (x - addTabIcon.getWidth() / 2F);
		if (bx < mx && y < my && bx + addTabIcon.getWidth() > mx && y + height > my) {
			return -2;
		}

		return -1;
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

	public void shutdown() {
		try {
			Server.shutdownServerThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public TextureManager getTextureManager() {
		return textureManager;
	}

	public GridRenderer getGridRenderer() {
		return gridRenderer;
	}

	public ObjectTextureProvider getObjectTextureProvider() {
		return objectTextureProvider;
	}

	public void queueLoadBackground(Room room, String backgroundPath) {
		this.frameStartTasks.add(() -> room.loadBackground(backgroundPath));
	}

	public void queueLoadLayerTextures(Room room) {
		this.frameStartTasks.add(room::loadLayerTextures);
	}

	private void setTab(ITab tab) {
		this.tab = tab;
		if (tab != null) {
			if (tab instanceof EditorTab t) {
				window.setTitle("Editor | DEV BUILD | " + t.title);
			} else {
				window.setTitle("Editor | DEV BUILD | " + tab.getClass().getSimpleName());
			}
		} else {
			window.setTitle("Editor | DEV BUILD");
		}
	}

	public void addTab(ITab tab) {
		this.tabs.add(tab);
		if (this.tab == null) {
			this.tab = tabs.get(0);
		}
	}

	public void removeTab(ITab tab) {
		if (this.tabs.remove(tab)) {
			if (this.tab == tab) {
				if (this.tabs.isEmpty()) {
					this.setTab(null);
				} else {
					this.setTab(this.tabs.get(this.tabs.size() - 1));
				}
			}
		}
	}

	public ITab getTab() {
		return this.tab;
	}

	private static class ObjectTextureProvider implements IObjectTexture {
		private final Texture invisibleWall;
		private final Texture roomDoor;
		private final Texture textInvisibleWall;
		private final Texture playerTexture;
		private final Texture doorExit;

		public ObjectTextureProvider(TextureManager textureManager) throws IOException {
			this.invisibleWall = textureManager.get("/invisible_wall_debug.jpg");
			this.roomDoor = textureManager.get("/door_debug.jpg");
			this.textInvisibleWall = textureManager.get("/invisible_wall_debug.jpg");
			this.playerTexture = textureManager.get("/kris.png");
			this.doorExit = textureManager.get("/door_exit.png");
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
