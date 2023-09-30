package dev.seeight.dtceditor.contextmenu;

import dev.seeight.dtceditor.DeltaCheapEditor;
import org.lwjgl.glfw.GLFW;

public class ListContextMenu implements IContextMenu {
    private final Entry[] entries;
    private final float x;
    private final float y;

    private float width;
    private float height;

    public ListContextMenu(float x, float y, Entry... entries) {
        this.x = x;
        this.y = y;
        this.entries = entries;
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void render(DeltaCheapEditor editor) {
        float tx = x;
        float ty = y;

        for (Entry entry : this.entries) {
            entry.x = tx;
            entry.y = ty;
            entry.width = 100;
            entry.height = editor.font.FONT_HEIGHT_FLOAT + 4;

            editor.getRenderer().color(1, 1, 1, 1);
            editor.getRenderer().rect2d(tx, ty, tx + entry.width, ty + entry.height);
            editor.getRenderer().color(0, 0, 0, 1);
            editor.font.drawString(entry.displayString, tx + 4, ty + (entry.height - editor.font.FONT_HEIGHT_FLOAT) / 2F);

            ty += entry.height;
        }

        width = 100;
        height = ty - y;
    }

    @Override
    public void onClose() {

    }

    @Override
    public boolean mouseButton(int button, int action, int x, int y) {
        if (button == 0) {
            if (action == GLFW.GLFW_RELEASE) {
                for (Entry entry : entries) {
                    if (entry.x < x && entry.y < y && entry.x + entry.width > x && entry.y + entry.height > y) {
                        System.out.println("Entry selected: " + entry.displayString);
                        entry.action.run();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int getX() {
        return (int) x;
    }

    @Override
    public int getY() {
        return (int) y;
    }

    @Override
    public int getWidth() {
        return (int) width;
    }

    @Override
    public int getHeight() {
        return (int) height;
    }

    public static final class Entry {
        private final Runnable action;
        private final String displayString;
        private float x;
        private float y;
        private float width;
        private float height;

        public Entry(Runnable action, String displayString) {
            this.action = action;
            this.displayString = displayString;
        }
    }
}
