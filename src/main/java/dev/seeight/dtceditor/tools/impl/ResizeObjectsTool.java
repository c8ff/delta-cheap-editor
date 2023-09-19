package dev.seeight.dtceditor.tools.impl;

import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.history.impl.MoveObjects;
import dev.seeight.dtceditor.history.impl.SelectObjects;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.renderer.renderer.Texture;

import java.util.ArrayList;
import java.util.List;

public class ResizeObjectsTool extends Tool {
	private final Texture icon;

	private int clickX;
	private int clickY;
	private int areaX = 0;
	private int areaY = 0;
	private int areaX2 = 0;
	private int areaY2 = 0;
	private int startAreaX = 0;
	private int startAreaY = 0;
	private int startAreaX2 = 0;
	private int startAreaY2 = 0;
	private boolean click;
	private int selectionMode;
	private float expandSize = 2;

	private final List<MoveObjects.PositionChange> positionChanges;
	private final List<RoomObject> selectedObjects;
	private boolean clearPreviousObjects;

	public ResizeObjectsTool(EditorTab tab, Texture icon) {
		super(tab);
		this.icon = icon;
		this.positionChanges = new ArrayList<>();
		this.selectedObjects = new ArrayList<>();
	}

	@Override
	public void render() {
		this.expandSize = 2 * this.tab.zoom;
		if (!this.click) {
			this.calcArea();
		}

		this.tab.editor.getRenderer().color(1, 1, 1, 1);
		this.tab.editor.getRenderer().hollowRect2d(areaX, areaY, areaX2, areaY2, this.tab.zoom);
		this.renderExpand(areaX, areaY);
		this.renderExpand(areaX2, areaY);
		this.renderExpand(areaX, areaY2);
		this.renderExpand(areaX2, areaY2);

		this.renderExpand(areaX + (areaX2 - areaX) / 2, areaY);
		this.renderExpand(areaX2, areaY + (areaY2 - areaY) / 2);
		this.renderExpand(areaX + (areaX2 - areaX) / 2, areaY2);
		this.renderExpand(areaX, areaY + (areaY2 - areaY) / 2);
	}

	@Override
	public void click(int button, int x, int y) {
		this.positionChanges.clear();

		this.click = true;
		this.calcArea();
		this.startAreaX = this.areaX;
		this.startAreaY = this.areaY;
		this.startAreaX2 = this.areaX2;
		this.startAreaY2 = this.areaY2;

		x = translateX(x);
		y = translateY(y);
		clickX = room.snapToGrid(x);
		clickY = room.snapToGrid(y);

		if (isInsideExpand(areaX + (areaX2 - areaX) / 2, areaY, x, y)) {
			selectionMode = 5;
		} else if (isInsideExpand(areaX2, areaY + (areaY2 - areaY) / 2, x, y)) {
			selectionMode = 6;
		} else if (isInsideExpand(areaX + (areaX2 - areaX) / 2, areaY2, x, y)) {
			selectionMode = 7;
		} else if (isInsideExpand(areaX, areaY + (areaY2 - areaY) / 2, x, y)) {
			selectionMode = 8;
		} else if (isInsideExpand(areaX, areaY, x, y)) {
			selectionMode = 1;
		} else if (isInsideExpand(areaX2, areaY, x, y)) {
			selectionMode = 2;
		} else if (isInsideExpand(areaX, areaY2, x, y)) {
			selectionMode = 3;
		} else if (isInsideExpand(areaX2, areaY2, x, y)) {
			selectionMode = 4;
		} else if (x >= areaX && y >= areaY && x <= areaX2 && y <= areaY2) {
			selectionMode = 0;
		} else {
			selectionMode = -1;
			clearPreviousObjects = false;

			RoomObject object = null;

			List<RoomObject> objects = this.room.getObjects();
			for (int i = objects.size() - 1; i >= 0; i--) {
				RoomObject roomObject = objects.get(i);
				if (x > roomObject.x && y > roomObject.y && x < roomObject.x + roomObject.getWidth() && y < roomObject.y + roomObject.getHeight()) {
					object = roomObject;
					break;
				}
			}

			if (object != null) {
				clearPreviousObjects = tab.editor.shift;
				selectedObjects.add(object);
			} else {
				clearPreviousObjects = true;
				selectedObjects.clear();
			}
		}

		System.out.println(selectionMode);
	}

	@Override
	public void drag(int button, int x, int y) {
		x = room.snapToGrid(translateX(x));
		y = room.snapToGrid(translateY(y));

		if (selectionMode == 1) {
			areaX = x;
			areaY = y;
		} else if (selectionMode == 2) {
			areaX2 = x;
			areaY = y;
		} else if (selectionMode == 3) {
			areaX = x;
			areaY2 = y;
		} else if (selectionMode == 4) {
			areaX2 = x;
			areaY2 = y;
		} else if (selectionMode == 5) {
			areaY = y;
		} else if (selectionMode == 6) {
			areaX2 = x;
		} else if (selectionMode == 7) {
			areaY2 = y;
		} else if (selectionMode == 8) {
			areaX = x;
		} else if (selectionMode == 0) {
			// you don't want to know how long this took
			areaX = x + (startAreaX - clickX);
			areaY = y + (startAreaY - clickY);
			areaX2 = x + (startAreaX2 - clickX);
			areaY2 = y + (startAreaY2 - clickY);
		}
	}

	@Override
	public void lift(int button, int x, int y) {
		if (this.selectionMode < 0) {
			this.click = false;
			return;
		}

		int aDiffX = this.areaX - this.startAreaX;
		int aDiffY = this.areaY - this.startAreaY;
		int aDiffX2 = this.areaX2 - this.startAreaX2;
		int aDiffY2 = this.areaY2 - this.startAreaY2;

		for (RoomObject roomObject : this.room.getObjects()) {
			if (!roomObject.selected) {
				continue;
			}

			int diffX = this.startAreaX - roomObject.x;
			int diffY = this.startAreaY - roomObject.y;
			int diffX2 = this.startAreaX2 - roomObject.x - roomObject.getWidth();
			int diffY2 = this.startAreaY2 - roomObject.y - roomObject.getHeight();

			int newX = this.startAreaX + aDiffX - diffX;
			int newY = this.startAreaY + aDiffY - diffY;
			int newX2 = this.startAreaX2 + aDiffX2 - diffX2;
			int newY2 = this.startAreaY2 + aDiffY2 - diffY2;

			this.positionChanges.add(new MoveObjects.PositionChange(roomObject, newX, newY, newX2 - newX, newY2 - newY, roomObject.x, roomObject.y, roomObject.getWidth(), roomObject.getHeight()));
			roomObject.x = room.snapToGrid(newX);
			roomObject.y = room.snapToGrid(newY);
			roomObject.setWidth(room.snapToGrid(newX2 - newX));
			roomObject.setHeight(room.snapToGrid(newY2 - newY));
		}

		this.click = false;
	}

	@Override
	public void scroll(double x, double y) {

	}

	@Override
	public boolean isActionFinished() {
		return !this.click && (!this.positionChanges.isEmpty() || !this.selectedObjects.isEmpty() || this.clearPreviousObjects);
	}

	@Override
	public IHistoryEntry getNext() {
		if (!this.selectedObjects.isEmpty()) {
			List<RoomObject> copy = new ArrayList<>(this.selectedObjects);
			this.selectedObjects.clear();
			this.positionChanges.clear();
			this.selectionMode = 0;

			// TODO: This is wacky. Should be a separate select objects type, like inverting the current selection (aka selecting the unselected and unselecting the selected)
			// Unselect previous objects
			if (this.clearPreviousObjects) {
				this.room.addHistory(SelectObjects.apply(this.room, this.room.getObjects().stream().filter(copy::contains).toList(), false));
				this.clearPreviousObjects = false;
			}

			// Select clicked objects
			return SelectObjects.apply(this.room, copy);
		} else if (this.clearPreviousObjects) {
			this.clearPreviousObjects = false;
			this.positionChanges.clear();
			return SelectObjects.apply(this.room, this.room.getObjects(), false);
		}

		// Apply changes to objects and add to history
		ArrayList<MoveObjects.PositionChange> objects = new ArrayList<>(this.positionChanges);
		this.positionChanges.clear();
		return new MoveObjects(objects);
	}

	@Override
	public Texture getIcon() {
		return icon;
	}

	private void calcArea() {
		int areaX = Integer.MAX_VALUE;
		int areaY = Integer.MAX_VALUE;
		int areaX2 = Integer.MIN_VALUE;
		int areaY2 = Integer.MIN_VALUE;

		if (this.room.getObjects().isEmpty()) {
			this.areaX = 0;
			this.areaY = 0;
			this.areaX2 = 0;
			this.areaY2 = 0;
			return;
		}

		for (RoomObject roomObject : this.room.getObjects()) {
			if (roomObject.selected) {
				if (roomObject.x < areaX) {
					areaX = roomObject.x;
				}
				if (roomObject.y < areaY) {
					areaY = roomObject.y;
				}

				if (roomObject.x + roomObject.getWidth() > areaX2) {
					areaX2 = roomObject.x + roomObject.getWidth();
				}

				if (roomObject.y + roomObject.getHeight() > areaY2) {
					areaY2 = roomObject.y + roomObject.getHeight();
				}
			}
		}

		this.areaX = areaX;
		this.areaY = areaY;
		this.areaX2 = areaX2;
		this.areaY2 = areaY2;
	}

	private void renderExpand(int x, int y) {
		renderer.rect2d(x - expandSize, y - expandSize, x + expandSize, y + expandSize);
	}

	private boolean isInsideExpand(int x, int y, int mx, int my) {
		return mx > x - expandSize && my > y - expandSize && mx < x + expandSize && my < y + expandSize;
	}
}
