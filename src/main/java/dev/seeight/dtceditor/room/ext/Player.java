package dev.seeight.dtceditor.room.ext;

import dev.seeight.dtceditor.room.IObjectTexture;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.renderer.renderer.Texture;

public class Player extends RoomObject {
	public static final Player player = new Player();

	private Player() {
		this.setWidth(0);
		this.setHeight(0);
	}

	@Override
	public void setWidth(int width) {
		this.width = 38;
	}

	@Override
	public void setHeight(int height) {
		this.height = 76;
	}

	@Override
	public Texture getTexture(IObjectTexture prov) {
		return prov.getPlayerTexture();
	}
}
