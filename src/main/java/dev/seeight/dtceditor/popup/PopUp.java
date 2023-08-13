package dev.seeight.dtceditor.popup;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.StuffListener;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.util.MathUtil;
import org.jetbrains.annotations.Range;

public class PopUp implements StuffListener {
	public final DeltaCheapEditor editor;
	@Range(from = 0, to = 100)
	protected float closingProgress;
	protected boolean closing;

	protected final Renderer renderer;

	protected int width;
	protected int height;

	public PopUp(final DeltaCheapEditor editor) {
		this(editor, 300, 200);
	}

	public PopUp(final DeltaCheapEditor editor, int width, int height) {
		this.editor = editor;
		this.renderer = editor.getRenderer();
		this.width = width;
		this.height = height;
	}

	public void render() {
		this.renderer.color(1, 1, 1, (100 - this.closingProgress) / 100F);
		this.renderer.rect2d(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
	}

	public void init() {

	}

	public void close() {

	}

	public void animateClosing() {
		if (this.closing) {
			float diff = 100 - this.closingProgress;
			double v = MathUtil.clamp(this.editor.getDeltaTime() * 20F, 0, 1);
			this.closingProgress += diff * v;
		} else if (this.closingProgress > 0) {
			float diff = 0 - this.closingProgress;
			double v = MathUtil.clamp(this.editor.getDeltaTime() * 20F, 0, 1);
			this.closingProgress += diff * v;
		}
		this.closingProgress = (float) MathUtil.clamp(Math.floor(this.closingProgress * 100F) / 100F, 0, 100F);
		if (this.closingProgress > 99.9) {
			this.closingProgress = 100;
		}
	}

	public void setClosing(boolean closing) {
		this.closing = closing;
	}

	public void setClosingProgress(@Range(from = 0, to = 100) float closingProgress) {
		this.closingProgress = closingProgress;
	}

	public @Range(from = 0, to = 100) float getClosingProgress() {
		return closingProgress;
	}

	public boolean isClosing() {
		return closing;
	}

	@Override
	public void windowFocus(boolean windowFocus) {

	}

	@Override
	public void framebufferSize(int width, int height) {

	}

	@Override
	public void mouseButton(int button, int action) {

	}

	@Override
	public void cursorPosition(double x, double y) {

	}

	@Override
	public void key(int key, int action, int mods) {

	}

	@Override
	public void scroll(double x, double y) {

	}

	@Override
	public void character(int codepoint) {

	}

	public int getX() {
		return (this.editor.getWindow().getWidth() - this.getWidth()) / 2;
	}

	public int getY() {
		return (this.editor.getWindow().getHeight() - this.getHeight()) / 2;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean contains(double x, double y) {
		double x0 = getX();
		double y0 = getY();
		return (x >= x0 &&
				y >= y0 &&
				x < x0 + getWidth() &&
				y < y0 + getHeight());
	}
}
