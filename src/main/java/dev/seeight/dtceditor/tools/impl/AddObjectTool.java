package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.history.impl.AddObject;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.tool.AddObjectToolOptions;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.*;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddObjectTool extends Tool {
	private final Texture icon;

	private boolean finished;
	private RoomObject object;

	private int clickX;
	private int clickY;
	private int clickX2;
	private int clickY2;

	private boolean click;

	private ObjectCreator creator;

	@Contract(pure = true)
	public AddObjectTool(@NotNull DeltaCheapEditor editor, Room room, Texture icon) {
		super(editor, room);
		this.icon = icon;
		this.creator = ObjectCreator.INVISIBLE_WALL;
	}

	@Override
	public void render() {
		if (this.click) {
			this.editor.getRenderer().rect2d(Math.min(this.clickX, this.clickX2), Math.min(this.clickY, this.clickY2), Math.max(this.clickX, this.clickX2), Math.max(this.clickY, this.clickY2));
		}
	}

	@Override
	public void click(int button, int x, int y) {
		x = translateX(editor, x);
		y = translateY(editor, y);

		this.clickX = this.editor.snapToGrid(x);
		this.clickY = this.editor.snapToGrid(y);
		this.clickX2 = this.editor.snapToGrid(x);
		this.clickY2 = this.editor.snapToGrid(y);
		this.click = true;
		this.editor.dirty = true;
	}

	@Override
	public void drag(int button, int x, int y) {
		x = translateX(editor, x);
		y = translateY(editor, y);

		this.clickX2 = this.editor.snapToGrid(x);
		this.clickY2 = this.editor.snapToGrid(y);
		this.editor.dirty = true;
	}

	@Override
	public void lift(int button, int x, int y) {
		room.unselectAllObjects();

		object = creator.createObject(editor);

		int normX = Math.min(this.clickX, this.clickX2);
		int normY = Math.min(this.clickY, this.clickY2);
		int normX2 = Math.max(this.clickX, this.clickX2);
		int normY2 = Math.max(this.clickY, this.clickY2);
		int width = normX2 - normX;
		int height = normY2 - normY;

		if (width == 0 || height == 0) {
			object = null;
			return;
		}

		object.x = normX;
		object.y = normY;
		object.setWidth(normX2 - normX);
		object.setHeight(normY2 - normY);

		room.addObject(object);

		click = false;
		finished = true;
	}

	@Override
	public void scroll(double x, double y) {

	}

	@Override
	public boolean isActionFinished() {
		if (object == null) {
			finished = false;
			return false;
		}

		if (!finished) {
			return false;
		}

		finished = false;
		return true;
	}

	@Override
	public IHistoryEntry getNext() {
		return new AddObject(object, room);
	}

	@Override
	public Texture getIcon() {
		return this.icon;
	}

	@Override
	public @Nullable PopUp getOptionsPopUp() {
		return new AddObjectToolOptions(editor, this);
	}

	public ObjectCreator getCreator() {
		return creator;
	}

	public void setCreator(ObjectCreator creator) {
		this.creator = creator;
	}

	public enum ObjectCreator implements IObjectCreator {
		INVISIBLE_WALL(editor -> new InvisibleWall()),
		ROOM_DOOR(editor -> {
			RoomDoor door = new RoomDoor("unknown");
			editor.setPopUp(door.getOptionsPopUp(editor));
			return door;
		}),
		PLAYER(editor -> Player.player),
		TEXT_INVISIBLE_WALL(editor -> {
			TextInvisibleWall obj = new TextInvisibleWall("Skill Issue");
			editor.setPopUp(obj.getOptionsPopUp(editor));
			return obj;
		}),
		DOOR_EXIT(editor -> {
			DoorExit obj = new DoorExit("unknown");
			editor.setPopUp(obj.getOptionsPopUp(editor));
			return obj;
		});

		private final IObjectCreator creator;

		ObjectCreator(IObjectCreator lambda) {
			this.creator = lambda;
		}

		@Override
		public RoomObject createObject(DeltaCheapEditor editor) {
			return creator.createObject(editor);
		}
	}

	@FunctionalInterface
	interface IObjectCreator {
		RoomObject createObject(DeltaCheapEditor editor);
	}
}
