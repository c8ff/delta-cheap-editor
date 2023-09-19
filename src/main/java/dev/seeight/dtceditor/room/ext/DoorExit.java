package dev.seeight.dtceditor.room.ext;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.popup.obj.DoorExitPopUp;
import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.renderer.renderer.Texture;

public class DoorExit extends RoomObject {
	public String target;

	public DoorExit(String target) {
		this.target = target;
	}

	@Override
	public Texture getTexture(IObjectTexture prov) {
		return prov.getDoorExitTexture();
	}

	@Override
	public PopUp getOptionsPopUp(DeltaCheapEditor editor, Room room) {
		return new DoorExitPopUp(editor, this, room);
	}
}
