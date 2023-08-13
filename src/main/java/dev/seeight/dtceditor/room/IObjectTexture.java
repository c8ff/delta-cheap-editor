package dev.seeight.dtceditor.room;

import dev.seeight.renderer.renderer.Texture;

public interface IObjectTexture {
	Texture getInvisibleWallTexture();

	Texture getTextInvisibleWallTexture();

	Texture getRoomDoorTexture();

	Texture getPlayerTexture();

	Texture getDoorExitTexture();
}
