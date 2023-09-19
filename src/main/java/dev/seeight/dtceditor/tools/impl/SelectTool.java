package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.history.impl.SelectObjects;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectTool extends Tool {
	private final Texture icon;

	private boolean click;
	private boolean finished;

	private int x;
	private int y;
	private int x2;
	private int y2;

	private RoomObject selObject;
	private List<RoomObject> selObjects;
	private boolean clearAll;
	private double lastTime;

	@Contract(pure = true)
	public SelectTool(@NotNull DeltaCheapEditor editor, Room room, Texture icon) {
		super(editor, room);
		this.icon = icon;
	}

	@Override
	public void render() {
		if (click && selObject == null && !clearAll) {
			this.editor.getRenderer().color(0.25F, 0.75F, 1, 0.25F);
			this.editor.getRenderer().rect2d(Math.min(x, x2), Math.min(y, y2), Math.max(x, x2), Math.max(y, y2));
		}
	}

	@Override
	public void click(int button, int x, int y) {
		x = translateX(editor, x);
		y = translateY(editor, y);

		RoomObject object = this.getObjectAt(x, y);

		// dev
		if (object != null) {
			double time = (GLFW.glfwGetTime() - this.lastTime);
			if (time < 0.225F) {
				this.editor.setPopUp(object.getOptionsPopUp(this.editor));
				this.lastTime = GLFW.glfwGetTime();
				this.selObject = object;
				this.finished = true;
				this.click = false;
				return;
			}
		}
		// dev

		if (object == null) {
			this.x = x;
			this.y = y;
			this.x2 = x;
			this.y2 = y;
			this.selObject = null;
		} else if (this.room.getSelectedCount() > 0 && !this.editor.ctrl) {
			this.clearAll = true;
			this.selObject = null;
			this.finished = true;
		} else {
			this.finished = true;
			this.selObject = object;
		}

		this.click = true;
		this.lastTime = GLFW.glfwGetTime();
	}

	@Override
	public void drag(int button, int x, int y) {
		x = translateX(editor, x);
		y = translateY(editor, y);

		if (!this.finished) {
			this.x2 = x;
			this.y2 = y;
		}
	}

	@Override
	public void lift(int button, int x, int y) {
		if (!this.finished) {
			List<RoomObject> objectsInside = this.getObjectsInside(Math.min(this.x, this.x2), Math.min(this.y, this.y2), Math.max(this.x, this.x2), Math.max(this.y, this.y2), new ArrayList<>());

			if (!objectsInside.isEmpty()) {
				this.selObjects = objectsInside;
			} else {
				this.selObjects = null;
			}
			this.finished = true;
		}

		click = false;
	}

	@Override
	public void scroll(double x, double y) {

	}

	@Override
	public boolean isActionFinished() {
		return finished && !click;
	}

	@Override
	public IHistoryEntry getNext() {
		this.finished = false;

		if (this.clearAll) {
			this.clearAll = false;
			this.room.unselectAllObjects();
			return SelectObjects.apply(this.room, this.room.getObjects(), false);
		}

		if (this.selObject != null) {
			RoomObject selObject = this.selObject;
			this.selObject = null;

			boolean a = !selObject.selected;

			if (!this.editor.ctrl) {
				this.room.unselectAllObjects();
			}

			return SelectObjects.apply(this.room, Collections.singletonList(selObject), a);
		}

		if (this.selObjects == null) {
			this.room.unselectAllObjects();
			return null;
		}

		List<RoomObject> selObjects = this.selObjects;
		this.selObjects = null;
		if (!this.editor.ctrl) {
			this.room.unselectAllObjects();
		}
		return SelectObjects.apply(this.room, selObjects);
	}

	@Override
	public Texture getIcon() {
		return this.icon;
	}

	@Nullable
	public RoomObject getObjectAt(int x, int y) {
		List<RoomObject> objectsUnmodifiable = this.room.getObjects();
		for (int i = objectsUnmodifiable.size() - 1; i >= 0; i--) {
			RoomObject roomObject = objectsUnmodifiable.get(i);
			if (x > roomObject.x && y > roomObject.y && x < roomObject.x + roomObject.getWidth() && y < roomObject.y + roomObject.getHeight()) {
				return roomObject;
			}
		}
		return null;
	}


	@NotNull
	public List<RoomObject> getObjectsInside(float x, float y, float x2, float y2, @NotNull List<RoomObject> output) {
		for (RoomObject roomObject : this.room.getObjects()) {
			if (roomObject.x > x && roomObject.y > y && roomObject.x < x2 && roomObject.y < y2) {
				output.add(roomObject);
			}
		}

		return output;
	}
}
