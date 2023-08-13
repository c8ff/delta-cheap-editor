package dev.seeight.dtceditor.room.ext;

import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.renderer.renderer.Texture;

public class InvisibleWall extends RoomObject {
	@Override
	public Texture getTexture(IObjectTexture prov) {
		return prov.getInvisibleWallTexture();
	}
}
