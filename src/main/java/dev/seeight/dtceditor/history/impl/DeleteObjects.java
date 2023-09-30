package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

import java.util.List;

public class DeleteObjects implements IHistoryEntry {
	private final Room room;
	private final List<RoomObject> objects1;

	public DeleteObjects(Room room, List<RoomObject> objects1) {
		this.room = room;
		this.objects1 = objects1;
	}

	@Override
	public void undo() {
		this.room.addObjects(this.objects1);
	}

	@Override
	public void redo() {
		this.room.removeObjects(this.objects1);
	}
}
