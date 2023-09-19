package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.tab.EditorTab;
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

	public MoveCamera(EditorTab tab, Texture icon) {
		super(tab);
		this.icon = icon;
	}

	@Override
	public IHistoryEntry getNext() {
		return new History(tab, previousCameraX, previousCameraY, endCameraX, endCameraY);
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
		this.previousCameraX = tab.cameraX;
		this.previousCameraY = tab.cameraY;
		this.tempCameraX = room.snapToGrid(x - tab.cameraX);
		this.tempCameraY = room.snapToGrid(y - tab.cameraY);
	}

	@Override
	public void drag(int button, int x, int y) {
		tab.cameraX = room.snapToGrid((float) (x - tempCameraX));
		tab.cameraY = room.snapToGrid((float) (y - tempCameraY));
	}

	@Override
	public void lift(int button, int x, int y) {
		this.endCameraX = tab.cameraX;
		this.endCameraY = tab.cameraY;
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
		private final EditorTab tab;
		private final float cX;
		private final float cY;
		private final float ecX;
		private final float ecY;

		public History(EditorTab tab, float cX, float cY, float ecX, float ecY) {
			this.tab = tab;
			this.cX = cX;
			this.cY = cY;
			this.ecX = ecX;
			this.ecY = ecY;
		}

		@Override
		public void undo() {
			tab.cameraX = cX;
			tab.cameraY = cY;
		}

		@Override
		public void redo() {
			tab.cameraX = ecX;
			tab.cameraY = ecY;
		}
	}
}
