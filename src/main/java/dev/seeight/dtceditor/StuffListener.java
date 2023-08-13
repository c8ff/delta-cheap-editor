package dev.seeight.dtceditor;

public interface StuffListener {
	void windowFocus(boolean windowFocus);

	void framebufferSize(int width, int height);

	void mouseButton(int button, int action);

	void cursorPosition(double x, double y);

	void key(int key, int action, int mods);

	void scroll(double x, double y);

	void character(int codepoint);
}
