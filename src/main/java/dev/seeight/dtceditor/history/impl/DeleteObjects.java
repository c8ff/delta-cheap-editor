package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

import java.util.List;

public class DeleteObjects implements IHistoryEntry {
	private final List<RoomObject> objects;
	private final List<RoomObject> objects1;

	public DeleteObjects(List<RoomObject> objects, List<RoomObject> objects1) {
		this.objects = objects;
		this.objects1 = objects1;
	}

	@Override
	public void undo() {
		this.objects.addAll(this.objects1);
	}

	@Override
	public void redo() {
		this.objects.removeAll(this.objects1);
	}
}
