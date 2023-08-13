package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

import java.util.List;

public class MoveObjects implements IHistoryEntry {
	private final List<PositionChange> objects;

	public MoveObjects(List<PositionChange> objects) {
		this.objects = objects;
	}

	@Override
	public void undo() {
		for (PositionChange positionChange : objects) {
			positionChange.object.x = positionChange.prevX;
			positionChange.object.y = positionChange.prevY;
			positionChange.object.setSize(positionChange.prevWidth, positionChange.prevHeight);
		}
	}

	@Override
	public void redo() {
		for (PositionChange positionChange : objects) {
			positionChange.object.x = positionChange.x;
			positionChange.object.y = positionChange.y;
			positionChange.object.setSize(positionChange.width, positionChange.height);
		}
	}

	public static class PositionChange {
		public final RoomObject object;
		public final int x, y, width, height;
		public final int prevX, prevY, prevWidth, prevHeight;

		public PositionChange(RoomObject object, int x, int y, int width, int height, int prevX, int prevY, int prevWidth, int prevHeight) {
			this.object = object;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.prevX = prevX;
			this.prevY = prevY;
			this.prevWidth = prevWidth;
			this.prevHeight = prevHeight;
		}
	}
}
