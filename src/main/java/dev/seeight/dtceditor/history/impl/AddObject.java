package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

public class AddObject implements IHistoryEntry {
	private final RoomObject object;
	private final DeltaCheapEditor deltaCheapEditor;

	public AddObject(RoomObject object, DeltaCheapEditor deltaCheapEditor) {
		this.object = object;
		this.deltaCheapEditor = deltaCheapEditor;
	}

	@Override
	public void undo() {
		this.deltaCheapEditor.removeObject(object);
	}

	@Override
	public void redo() {
		this.deltaCheapEditor.addObject(object);
	}
}
