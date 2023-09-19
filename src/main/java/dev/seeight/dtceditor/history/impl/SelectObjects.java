package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

import java.util.List;

public class SelectObjects implements IHistoryEntry {
	private final List<RoomObject> objects;
	private final boolean state;
	private final Room room;

	public SelectObjects(Room room, List<RoomObject> objects, boolean state) {
		this.room = room;
		this.objects = objects;
		this.state = state;
	}

	@Override
	public void undo() {
		room.unselectAllObjects();
		for (RoomObject roomObject : this.objects) {
			roomObject.selected = !state;
		}
	}

	@Override
	public void redo() {
		room.unselectAllObjects();
		for (RoomObject roomObject : this.objects) {
			roomObject.selected = state;
		}
	}

	public static SelectObjects apply(Room room, List<RoomObject> objects) {
		return apply(room, objects, true);
	}

	public static SelectObjects apply(Room room, List<RoomObject> objects, boolean state) {
		for (RoomObject object : objects) {
			object.selected = state;
		}
		return new SelectObjects(room, objects, state);
	}
}
