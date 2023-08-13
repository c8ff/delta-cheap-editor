package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.ext.DoorExit;

public class EditDoorExit implements IHistoryEntry {
	private final DoorExit doorExit;
	private final String original;
	private final String modified;

	public EditDoorExit(DoorExit doorExit, String original, String modified) {
		this.doorExit = doorExit;
		this.original = original;
		this.modified = modified;
	}

	public EditDoorExit(DoorExit doorExit, String modified) {
		this(doorExit, doorExit.target, modified);
		doorExit.target = modified;
	}

	@Override
	public void undo() {
		this.doorExit.target = original;
	}

	@Override
	public void redo() {
		this.doorExit.target = modified;
	}
}
