package dev.seeight.dtceditor.popup.impl;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.PopUp;
import org.lwjgl.glfw.GLFW;

public class GridSizePopUp extends PopUp {
	private final int[] sizes = new int[]{
			5, 10, 20, 30, 40, 50
	};

	public GridSizePopUp(DeltaCheapEditor editor) {
		super(editor, 300, 300);
	}

	@Override
	public void render() {
		float a = (100 - this.getClosingProgress()) / 100F;

		int x = this.getX();
		int y = this.getY();

		this.renderer.color(1, 1, 1, a);
		this.renderer.rect2d(x, y, x + this.getWidth(), y + this.getHeight());

		x += 6;
		y += 6;

		this.renderer.color(0, 0, 0, a);
		this.editor.font.drawString("Grid Size", x, y);
		y += this.editor.font.FONT_HEIGHT_FLOAT;

		this.renderer.color(0.8F, 0.8F, 0.8F, a);
		for (int size : sizes) {
			if (this.editor.getGridSize() == size) {
				this.renderer.color(0.25F, 0.75F, 1, a);
			}

			this.editor.font.drawString(String.valueOf(size), x, y);

			if (this.editor.getGridSize() == size) {
				this.renderer.color(0.8F, 0.8F, 0.8F, a);
			}

			y += this.editor.font.FONT_HEIGHT_FLOAT;
		}
	}

	@Override
	public void mouseButton(int button, int action) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_1 || action != GLFW.GLFW_RELEASE) {
			return;
		}

		double mx = editor.mouse.getX();
		double my = editor.mouse.getY();
		int x = this.getX();
		int y = (int) (this.getY() + this.editor.font.FONT_HEIGHT_FLOAT);
		float height = this.editor.font.FONT_HEIGHT_FLOAT;
		for (int size : sizes) {
			if (mx > x && my > y && mx < x + getWidth() && my < y + height) {
				this.editor.setGridSize(size);
				break;
			}
			y += height;
		}
	}
}
