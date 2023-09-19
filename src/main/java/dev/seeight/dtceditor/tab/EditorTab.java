package dev.seeight.dtceditor.tab;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.area.Area;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class EditorTab implements ITab {
    private final DeltaCheapEditor editor;
    private final Room room;

    private final List<Area> areas;

    public EditorTab(DeltaCheapEditor editor, Room room) {
        this.editor = editor;
        this.room = room;
        this.areas = new ArrayList<>();
        this.areas.add(new Area(editor, 30, Area.DivisorType.FIXED) {
            @Override
            public void render() {
                editor.getRenderer().color(1, 1, 1, 1);
                editor.getRenderer().rect2d(0, 0, width, height);
                editor.getRenderer().color(0, 0, 0, 1);
                editor.font.drawString("Hello World!", 0, 0);
            }
        });
        this.areas.add(new Area(editor, 0, Area.DivisorType.PERCENT) {
            @Override
            public void render() {
                editor.getRenderer().color(0.35F, 0.35F, 0.35F, 1);
                editor.getRenderer().rect2d(0, 0, width, height);
                editor.getRenderer().color(1, 1, 1, 1);
                editor.font.drawString("Hello World! This is another area.", 0, 0);
                editor.getRenderer().rect2d(width - 4, height - 4, width, height);
            }
        });
        this.areas.add(new Area(editor, 30, Area.DivisorType.FIXED) {
            @Override
            public void render() {
                editor.getRenderer().color(1, 1, 1, 1);
                editor.getRenderer().rect2d(0, 0, width, height);

                editor.getRenderer().color(1, 0, 0, 1);
                editor.getRenderer().rect2d(0, 0, 4, 4);
                editor.getRenderer().rect2d(width - 4, height - 4, width, height);
            }
        });

        resizeAreas(editor.getWindow().getWidth(), editor.getWindow().getHeight());
    }

    @Override
    public void framebufferSize(int width, int height) {
        resizeAreas(width, height);
    }

    private void resizeAreas(int width, int height) {
        double remainingPercent;
        int x = 0;
        int y = 0;
        int x1 = 0;

        int fixedWidth = 0;
        for (Area area : this.areas) {
            if (area.divisorType != Area.DivisorType.FIXED) continue;
            fixedWidth += area.divisor;
            area.width = area.divisor;
        }

        x1 += fixedWidth;

        for (Area area : this.areas) {
            if (area.divisorType != Area.DivisorType.FIXED) {
                remainingPercent = (1 - (double) x1 / width);
                area.width = (int) (width * remainingPercent);
                x1 += area.width;
            }

            area.height = height;
            area.x = x;
            area.y = y;
            x += area.width;
        }

        if (x > width) {
            System.out.println("bruh");
        }
    }

    @Override
    public void render() {
        float[] mat = new float[16];

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (Area area : this.areas) {
            GL11.glScissor(area.x, editor.getWindow().getHeight() - (area.height + area.y), area.width, area.height);

            this.editor.getRenderer().getViewMatrix4f(mat);
            this.editor.getRenderer().resetView();
            this.editor.getRenderer().translate(area.x, area.y, 0);

            area.render();

            this.editor.getRenderer().setViewMatrix4f(mat);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
