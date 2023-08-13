package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.renderer.renderer.Texture;

public class MoveCamera extends Tool {
	private final Texture icon;

	private float previousCameraX;
	private float previousCameraY;
	private float endCameraX;
	private float endCameraY;
	private double tempCameraX;
	private double tempCameraY;
	private boolean finished;

	public MoveCamera(DeltaCheapEditor editor, Texture icon) {
		super(editor);
		this.icon = icon;
	}

	@Override
	public IHistoryEntry getNext() {
		return new History(editor, previousCameraX, previousCameraY, endCameraX, endCameraY);
	}

	@Override
	public Texture getIcon() {
		return this.icon;
	}

	@Override
	public void render() {

	}

	@Override
	public void click(int button, int x, int y) {
		this.previousCameraX = editor.cameraX;
		this.previousCameraY = editor.cameraY;
		this.tempCameraX = editor.snapToGrid(x - editor.cameraX);
		this.tempCameraY = editor.snapToGrid(y - editor.cameraY);
	}

	@Override
	public void drag(int button, int x, int y) {
		editor.cameraX = editor.snapToGrid((float) (x - tempCameraX));
		editor.cameraY = editor.snapToGrid((float) (y - tempCameraY));
		this.editor.dirty = true;
	}

	@Override
	public void lift(int button, int x, int y) {
		this.endCameraX = editor.cameraX;
		this.endCameraY = editor.cameraY;
		finished = true;
	}

	@Override
	public void scroll(double x, double y) {

	}

	@Override
	public boolean isActionFinished() {
		if (!finished) {
			return false;
		}

		finished = false;
		return true;
	}

	public static class History implements IHistoryEntry {
		private final DeltaCheapEditor deltaCheapEditor;
		private final float cX;
		private final float cY;
		private final float ecX;
		private final float ecY;

		public History(DeltaCheapEditor deltaCheapEditor, float cX, float cY, float ecX, float ecY) {
			this.deltaCheapEditor = deltaCheapEditor;
			this.cX = cX;
			this.cY = cY;
			this.ecX = ecX;
			this.ecY = ecY;
		}

		@Override
		public void undo() {
			deltaCheapEditor.cameraX = cX;
			deltaCheapEditor.cameraY = cY;
		}

		@Override
		public void redo() {
			deltaCheapEditor.cameraX = ecX;
			deltaCheapEditor.cameraY = ecY;
		}
	}
}
