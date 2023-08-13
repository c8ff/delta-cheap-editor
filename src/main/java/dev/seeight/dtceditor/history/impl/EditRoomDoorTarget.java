package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.ext.RoomDoor;

public class EditRoomDoorTarget implements IHistoryEntry {
	private final RoomDoor door;
	private final String original;
	private final boolean bOriginal;
	private final String modified;
	private final boolean bModified;

	public EditRoomDoorTarget(RoomDoor door, String original, boolean bOriginal, String modified, boolean bModified) {
		this.door = door;
		this.original = original;
		this.bOriginal = bOriginal;
		this.modified = modified;
		this.bModified = bModified;
	}

	public EditRoomDoorTarget(RoomDoor door, String modified, boolean externalRoom) {
		this(door, door.targetRoom, door.externalRoom, modified, externalRoom);
		door.targetRoom = modified;
		door.externalRoom = externalRoom;
	}

	@Override
	public void undo() {
		door.targetRoom = original;
		door.externalRoom = bOriginal;
	}

	@Override
	public void redo() {
		door.targetRoom = modified;
		door.externalRoom = bModified;
	}
}
