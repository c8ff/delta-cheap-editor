package dev.seeight.dtceditor.tools;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.Nullable;

public abstract class Tool {
	protected final DeltaCheapEditor editor;

	public volatile float renderX;
	public volatile float renderY;
	public volatile float renderX2;
	public volatile float renderY2;

	public Tool(DeltaCheapEditor editor) {
		this.editor = editor;
	}

	public boolean contains(double x, double y) {
		return x > renderX && y > renderY && x < renderX2 && y < renderY2;
	}

	protected int translateX(DeltaCheapEditor editor, int x) {
		return (int) ((x - editor.cameraX * editor.zoom) / editor.zoom);
	}

	protected int translateY(DeltaCheapEditor editor, int y) {
		return (int) ((y - editor.cameraY * editor.zoom) / editor.zoom);
	}

	public abstract void render();

	public abstract void click(int button, int x, int y);

	public abstract void drag(int button, int x, int y);

	public abstract void lift(int button, int x, int y);

	public abstract void scroll(double x, double y);

	public abstract boolean isActionFinished();

	public abstract IHistoryEntry getNext();

	public abstract Texture getIcon();

	public @Nullable PopUp getOptionsPopUp() {
		return null;
	}
}
