package dev.seeight.dtceditor;

import dev.seeight.dtceditor.mgr.TextureManager;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.lwjgl.opengl.GL11;


public class GridRenderer {
	public Texture grid5;
	public Texture grid10;
	public Texture grid20;
	public Texture grid30;
	public Texture grid40;
	public Texture grid50;

	private final Renderer renderer;

	public GridRenderer(Renderer renderer, String path, TextureManager texMgr) {
		GLTexture.CREATE_MIPMAPS = true;
		GLTexture.DEFAULT_TEXTURE_MAG_FILTER = GL11.GL_NEAREST;
		GLTexture.DEFAULT_TEXTURE_MIN_FILTER = GL11.GL_LINEAR_MIPMAP_LINEAR;
		this.grid5 = texMgr.get(path + "grid5.png");
		this.grid10 = texMgr.get(path + "grid10.png");
		this.grid20 = texMgr.get(path + "grid20.png");
		this.grid30 = texMgr.get(path + "grid30.png");
		this.grid40 = texMgr.get(path + "grid40.png");
		this.grid50 = texMgr.get(path + "grid50.png");
		GLTexture.CREATE_MIPMAPS = false;
		GLTexture.DEFAULT_TEXTURE_MAG_FILTER = GL11.GL_NEAREST;
		GLTexture.DEFAULT_TEXTURE_MIN_FILTER = GL11.GL_NEAREST;
		this.renderer = renderer;
	}

	public void render(float x, float y, float width, float height, float cameraX, float cameraY, float scale, int windowHeight, int size) {
		Texture tex = gridFromSize(size);

		if (tex == null) {
			throw new RuntimeException("Invalid size: " + size);
		}

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) ((x + cameraX) * scale), (int) ((windowHeight - (y + cameraY + height) * scale)), (int) (width * scale), (int) (height * scale));

		int amountX = (int) Math.ceil((double) width / tex.getWidth());
		int amountY = (int) Math.ceil((double) height / tex.getHeight());
		int amount = (amountX * amountY);

		int c = 0;
		float startX = x;
		for (int i = 0; i < amount; i++) {
			renderer.texRect2d(tex, x, y, x + tex.getWidth(), y + tex.getHeight());
			c++;

			if (c >= amountX) {
				c = 0;
				x = startX;
				y += tex.getHeight();
			} else {
				x += tex.getWidth();
			}
		}

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public Texture gridFromSize(int size) {
		if (size == 5) {
			return grid5;
		} else if (size == 10) {
			return grid10;
		} else if (size == 20) {
			return grid20;
		} else if (size == 30) {
			return grid30;
		} else if (size == 40) {
			return grid40;
		} else if (size == 50) {
			return grid50;
		}

		return null;
	}
}
