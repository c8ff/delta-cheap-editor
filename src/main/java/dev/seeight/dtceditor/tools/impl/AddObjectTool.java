package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.history.impl.AddObject;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.tool.AddObjectToolOptions;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.*;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.Contract;
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
	public AddObjectTool(EditorTab tab, Texture icon) {
		super(tab);
		this.icon = icon;
		this.creator = ObjectCreator.INVISIBLE_WALL;
	}

	@Override
	public void render() {
		if (this.click) {
			this.tab.editor.getRenderer().rect2d(Math.min(this.clickX, this.clickX2), Math.min(this.clickY, this.clickY2), Math.max(this.clickX, this.clickX2), Math.max(this.clickY, this.clickY2));
		}
	}

	@Override
	public void click(int button, int x, int y) {
		x = translateX(x);
		y = translateY(y);

		this.clickX = this.room.snapToGrid(x);
		this.clickY = this.room.snapToGrid(y);
		this.clickX2 = this.room.snapToGrid(x);
		this.clickY2 = this.room.snapToGrid(y);
		this.click = true;
	}

	@Override
	public void drag(int button, int x, int y) {
		x = translateX(x);
		y = translateY(y);

		this.clickX2 = this.room.snapToGrid(x);
		this.clickY2 = this.room.snapToGrid(y);
	}

	@Override
	public void lift(int button, int x, int y) {
		room.unselectAllObjects();

		object = creator.createObject(tab.editor, room);

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
		return new AddObjectToolOptions(tab.editor, this);
	}

	public ObjectCreator getCreator() {
		return creator;
	}

	public void setCreator(ObjectCreator creator) {
		this.creator = creator;
	}

	public enum ObjectCreator implements IObjectCreator {
		INVISIBLE_WALL((editor, room) -> new InvisibleWall()),
		ROOM_DOOR((editor, room) -> {
			RoomDoor door = new RoomDoor("unknown");
			editor.setPopUp(door.getOptionsPopUp(editor, room));
			return door;
		}),
		PLAYER((editor, room) -> Player.player),
		TEXT_INVISIBLE_WALL((editor, room) -> {
			TextInvisibleWall obj = new TextInvisibleWall("Skill Issue");
			editor.setPopUp(obj.getOptionsPopUp(editor, room));
			return obj;
		}),
		DOOR_EXIT((editor, room) -> {
			DoorExit obj = new DoorExit("unknown");
			editor.setPopUp(obj.getOptionsPopUp(editor, room));
			return obj;
		});

		private final IObjectCreator creator;

		ObjectCreator(IObjectCreator lambda) {
			this.creator = lambda;
		}

		@Override
		public RoomObject createObject(DeltaCheapEditor editor, Room room) {
			return creator.createObject(editor, room);
		}
	}

	@FunctionalInterface
	interface IObjectCreator {
		RoomObject createObject(DeltaCheapEditor editor, Room room);
	}
}
