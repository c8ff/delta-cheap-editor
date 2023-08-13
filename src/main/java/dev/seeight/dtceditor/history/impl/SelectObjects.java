package dev.seeight.dtceditor.history.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;

import java.util.List;

public class SelectObjects implements IHistoryEntry {
	private final List<RoomObject> objects;
	private final boolean state;
	private final DeltaCheapEditor editor;

	public SelectObjects(DeltaCheapEditor editor, List<RoomObject> objects, boolean state) {
		this.editor = editor;
		this.objects = objects;
		this.state = state;
	}

	@Override
	public void undo() {
		editor.unselectAllObjects();
		for (RoomObject roomObject : this.objects) {
			roomObject.selected = !state;
		}
	}

	@Override
	public void redo() {
		editor.unselectAllObjects();
		for (RoomObject roomObject : this.objects) {
			roomObject.selected = state;
		}
	}

	public static SelectObjects apply(DeltaCheapEditor editor, List<RoomObject> objects) {
		return apply(editor, objects, true);
	}

	public static SelectObjects apply(DeltaCheapEditor editor, List<RoomObject> objects, boolean state) {
		for (RoomObject object : objects) {
			object.selected = state;
		}
		return new SelectObjects(editor, objects, state);
	}
}
