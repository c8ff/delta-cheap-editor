package dev.seeight.dtceditor.area;

import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.input.Mouse;
import dev.seeight.renderer.renderer.Renderer;

public class Area {
    public int x;
    public int y;
    public int width;
    public int height;

    protected final DeltaCheapEditor editor;
    protected final FontRenderer font;
    protected final Mouse mouse;
    protected final Renderer renderer;

    public Area(DeltaCheapEditor editor) {
        this.editor = editor;
        this.font = editor.font;
        this.mouse = editor.mouse;
        this.renderer = editor.getRenderer();
    }

    public boolean windowFocus(boolean windowFocus) {
        return false;
    }

    public boolean framebufferSize(int width, int height) {
        return false;
    }

    public boolean mouseButton(int button, int action) {
        return false;
    }

    public boolean cursorPosition(double x, double y, int mods) {
        return false;
    }

    public boolean key(int key, int action, int mods) {
        return false;
    }

    public boolean scroll(double x, double y) {
        return false;
    }

    public boolean character(int codepoint) {
        return false;
    }

    public void render() {

    }

    public void prerender() {

    }

    public boolean isInside(double x, double y) {
        return this.x < x && this.y < y && this.x + this.width > x && this.y + this.height > y;
    }
}
