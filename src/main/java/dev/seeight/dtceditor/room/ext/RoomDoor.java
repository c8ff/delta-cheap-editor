package dev.seeight.dtceditor.room.ext;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.obj.RoomDoorPopUp;
import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.renderer.renderer.Texture;

public class RoomDoor extends RoomObject {
	public String targetRoom;
	public boolean externalRoom;

	public RoomDoor(String targetRoom) {
		this.targetRoom = targetRoom;
	}

	@Override
	public Texture getTexture(IObjectTexture prov) {
		return prov.getRoomDoorTexture();
	}

	@Override
	public PopUp getOptionsPopUp(DeltaCheapEditor editor) {
		return new RoomDoorPopUp(editor, this);
	}
}
