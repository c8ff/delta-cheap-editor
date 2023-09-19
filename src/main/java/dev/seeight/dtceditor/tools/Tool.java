package dev.seeight.dtceditor.tools;

import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.area.RoomArea;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.Nullable;

public abstract class Tool {
	protected final EditorTab tab;
	protected final Room room;
	protected final RoomArea roomArea;
	protected final Renderer renderer;

	public volatile float renderX;
	public volatile float renderY;
	public volatile float renderX2;
	public volatile float renderY2;

	public Tool(EditorTab tab) {
		this.tab = tab;
		this.room = tab.room;
		this.roomArea = tab.roomArea;
		this.renderer = tab.editor.getRenderer();
	}

	public boolean contains(double x, double y) {
		return x > renderX && y > renderY && x < renderX2 && y < renderY2;
	}

	protected int translateX(int x) {
		return (int) (this.roomArea.x + (x - this.tab.cameraX * this.tab.zoom) / this.tab.zoom);
	}

	protected int translateY(int y) {
		return (int) (this.roomArea.y + (y - this.tab.cameraY * this.tab.zoom) / this.tab.zoom);
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
