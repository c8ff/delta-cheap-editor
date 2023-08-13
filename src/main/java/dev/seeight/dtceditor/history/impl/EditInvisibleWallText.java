package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.ext.TextInvisibleWall;

public class EditInvisibleWallText implements IHistoryEntry {
	private final TextInvisibleWall obj;
	private final String[] original;
	private final String[] modified;

	public EditInvisibleWallText(TextInvisibleWall obj, String[] original, String[] modified) {
		this.obj = obj;
		this.original = original;
		this.modified = modified;
	}

	public EditInvisibleWallText(TextInvisibleWall obj, String[] modified) {
		this(obj, obj.text, modified);
		obj.text = modified;
	}

	@Override
	public void undo() {
		this.obj.text = original;
	}

	@Override
	public void redo() {
		this.obj.text = modified;
	}
}
