package dev.seeight.dtceditor.room;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.impl.EditObjectPopUp;
import dev.seeight.renderer.renderer.Texture;

public abstract class RoomObject {
	protected int width;
	protected int height;
	public int x;
	public int y;
	public boolean selected;

	public volatile int renderOffsetX;
	public volatile int renderOffsetY;

	public void setSize(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public abstract Texture getTexture(IObjectTexture prov);

	public PopUp getOptionsPopUp(DeltaCheapEditor editor) {
		return new EditObjectPopUp(editor, this);
	}
}
