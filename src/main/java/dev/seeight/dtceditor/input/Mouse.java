package dev.seeight.dtceditor.input;

import org.lwjgl.glfw.GLFW;

public class Mouse {
	private double x;
	private double y;
	private int xi;
	private int yi;
	private double scrollX;
	private double scrollY;

	public final Key button1 = new Key(GLFW.GLFW_MOUSE_BUTTON_1);
	public final Key button2 = new Key(GLFW.GLFW_MOUSE_BUTTON_2);
	public final Key button3 = new Key(GLFW.GLFW_MOUSE_BUTTON_3);
	private final Key[] buttons = new Key[]{button1, button2, button3};

	public void cursorPosition(double x, double y) {
		this.x = x;
		this.y = y;
		this.xi = (int) x;
		this.yi = (int) y;
	}

	public void scroll(double x, double y) {
		this.scrollX = x;
		this.scrollY = y;
	}

	public void mouseButton(int button, int action) {
		for (Key key : buttons) {
			if (key.isCode(button)) {
				key.down = action != GLFW.GLFW_RELEASE;
				break;
			}
		}
	}

	public void frameEnd() {
		this.scrollX = 0;
		this.scrollY = 0;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getXi() {
		return xi;
	}

	public int getYi() {
		return yi;
	}

	public double getScrollX() {
		return scrollX;
	}

	public double getScrollY() {
		return scrollY;
	}
}
