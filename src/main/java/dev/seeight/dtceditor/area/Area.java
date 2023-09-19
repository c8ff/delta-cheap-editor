package dev.seeight.dtceditor.area;

import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.StuffListener;
import dev.seeight.dtceditor.input.Mouse;
import dev.seeight.renderer.renderer.Renderer;

public class Area implements StuffListener {
    public int x;
    public int y;
    public int width;
    public int height;
    public int divisor;
    public DivisorType divisorType;

    public enum DivisorType {
        PERCENT,
        FIXED
    }

    protected final DeltaCheapEditor editor;
    protected final FontRenderer font;
    protected final Mouse mouse;
    protected final Renderer renderer;

    public Area(DeltaCheapEditor editor, int divisor, DivisorType divisorType) {
        this.editor = editor;
        this.font = editor.font;
        this.mouse = editor.mouse;
        this.renderer = editor.getRenderer();
        this.divisor = divisor;
        this.divisorType = divisorType;
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

    public void render() {

    }
}
