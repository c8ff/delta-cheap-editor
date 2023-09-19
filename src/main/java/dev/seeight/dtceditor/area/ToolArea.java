package dev.seeight.dtceditor.area;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.dtceditor.tools.Tool;
import org.lwjgl.glfw.GLFW;

public class ToolArea extends Area {
    private final EditorTab tab;

    public ToolArea(EditorTab tab, DeltaCheapEditor editor) {
        super(editor);
        this.tab = tab;
    }

    @Override
    public boolean mouseButton(int button, int action) {
        for (Tool tool : tab.tools) {
            if (tool.contains(mouse.getX(), mouse.getY())) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
                    tab.selectedTool = tool;
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS) {
                    PopUp pop = tab.selectedTool.getOptionsPopUp();
                    if (pop != null) {
                        editor.setPopUp(pop);
                    }
                }
                // TODO: ignore click lol
                return true;
            }
        }

        return false;
    }

    @Override
    public void render() {
        renderToolIcons();
    }

    @Override
    public void prerender() {
        if (tab.selectedTool.isActionFinished()) {
            IHistoryEntry next = tab.selectedTool.getNext();
            if (next != null) {
                tab.room.addHistory(next);
            }
        }
    }

    protected void renderToolIcons() {
        float y = this.y;

        // background
        renderer.color(0, 0, 0, 0.75F);
        renderer.rect2d(0, 0, width, height);

        // tools
        renderer.color(0.5F, 0.5F, 0.5F, 1);
        for (Tool tool : tab.tools) {
            tool.renderX = 0;
            tool.renderY = y;
            tool.renderX2 = 32;
            tool.renderY2 = y + 32;

            if (tab.selectedTool == tool) {
                renderer.color(1, 1, 1, 1);
            }

            renderer.texRect2d(tool.getIcon(), 0, y, 32, y + 32);

            if (tab.selectedTool == tool) {
                renderer.color(0.5F, 0.5F, 0.5F, 1);
            }

            y += 32;
        }
    }
}
