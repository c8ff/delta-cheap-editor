package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

public class AddObject implements IHistoryEntry {
	private final RoomObject object;
	private final Room room;

	public AddObject(RoomObject object, Room room) {
		this.object = object;
		this.room = room;
	}

	@Override
	public void undo() {
		this.room.removeObject(object);
	}

	@Override
	public void redo() {
		this.room.addObject(object);
	}
}
