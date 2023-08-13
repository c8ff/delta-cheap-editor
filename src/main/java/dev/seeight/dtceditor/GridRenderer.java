package dev.seeight.dtceditor;

import dev.seeight.dtceditor.utils.IOUtil;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GridRenderer {
	public GLTexture grid5;
	public GLTexture grid10;
	public GLTexture grid20;
	public GLTexture grid30;
	public GLTexture grid40;
	public GLTexture grid50;

	private final Renderer renderer;

	public GridRenderer(Renderer renderer, String path) {
		try {
			GLTexture.CREATE_MIPMAPS = true;
			GLTexture.DEFAULT_TEXTURE_MAG_FILTER = GL11.GL_NEAREST;
			GLTexture.DEFAULT_TEXTURE_MIN_FILTER = GL11.GL_LINEAR_MIPMAP_LINEAR;
			this.grid5 = IOUtil.textureFromPath(this, path + "grid5.png");
			this.grid10 = IOUtil.textureFromPath(this, path + "grid10.png");
			this.grid20 = IOUtil.textureFromPath(this, path + "grid20.png");
			this.grid30 = IOUtil.textureFromPath(this, path + "grid30.png");
			this.grid40 = IOUtil.textureFromPath(this, path + "grid40.png");
			this.grid50 = IOUtil.textureFromPath(this, path + "grid50.png");
			GLTexture.CREATE_MIPMAPS = false;
			GLTexture.DEFAULT_TEXTURE_MAG_FILTER = GL11.GL_NEAREST;
			GLTexture.DEFAULT_TEXTURE_MIN_FILTER = GL11.GL_NEAREST;
			this.renderer = renderer;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void render(float x, float y, float width, float height, float cameraX, float cameraY, float scale, int windowHeight, int size) {
		GLTexture tex = gridFromSize(size);

		if (tex == null) {
			throw new RuntimeException("Invalid size: " + size);
		}

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) ((x + cameraX) * scale), (int) ((windowHeight - (y + cameraY + height) * scale)), (int) (width * scale), (int) (height * scale));

		int amountX = (int) Math.ceil((double) width / tex.width);
		int amountY = (int) Math.ceil((double) height / tex.height);
		int amount = (amountX * amountY);

		int c = 0;
		float startX = x;
		for (int i = 0; i < amount; i++) {
			renderer.texRect2d(tex, x, y, x + tex.width, y + tex.height);
			c++;

			if (c >= amountX) {
				c = 0;
				x = startX;
				y += tex.height;
			} else {
				x += tex.width;
			}
		}

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public GLTexture gridFromSize(int size) {
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
