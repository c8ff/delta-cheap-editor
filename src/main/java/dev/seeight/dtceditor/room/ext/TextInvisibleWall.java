package dev.seeight.dtceditor.room.ext;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.obj.TextInvisibleWallPopUp;
import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.renderer.renderer.Texture;

public class TextInvisibleWall extends RoomObject {
	public String[] text;

	public TextInvisibleWall(String... text) {
		this.text = text;
	}

	@Override
	public Texture getTexture(IObjectTexture prov) {
		return prov.getTextInvisibleWallTexture();
	}

	@Override
	public PopUp getOptionsPopUp(DeltaCheapEditor editor, Room room) {
		return new TextInvisibleWallPopUp(editor, this, room);
	}
}
