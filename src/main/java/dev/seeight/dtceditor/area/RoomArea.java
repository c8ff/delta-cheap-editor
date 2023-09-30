package dev.seeight.dtceditor.area;

import dev.seeight.dtceditor.Layer;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.DoorExit;
import dev.seeight.dtceditor.room.ext.InvisibleWall;
import dev.seeight.dtceditor.room.ext.RoomDoor;
import dev.seeight.dtceditor.tab.EditorTab;
import org.lwjgl.glfw.GLFW;

public class RoomArea extends Area {
    private final EditorTab tab;
    private final Room room;

    public RoomArea(EditorTab tab) {
        super(tab.editor);
        this.tab = tab;
        this.room = tab.room;
    }

    @Override
    public boolean mouseButton(int button, int action) {
        int bt = button == GLFW.GLFW_MOUSE_BUTTON_1 ? GLFW.GLFW_MOUSE_BUTTON_1 : button == GLFW.GLFW_MOUSE_BUTTON_2 ? GLFW.GLFW_MOUSE_BUTTON_2 : -1;
        if (bt != -1) {
            boolean down = action != GLFW.GLFW_RELEASE;

            if (down) {
                tab.selectedTool.click(bt, mouse.getXi(), mouse.getYi());
            } else {
                tab.selectedTool.lift(bt, mouse.getXi(), mouse.getYi());
            }
        }

        return false;
    }

    @Override
    public boolean cursorPosition(double x, double y, int mods) {
        if ((mods & EditorTab.MouseState.LEFT) != 0) {
            tab.selectedTool.drag(0, mouse.getXi(), mouse.getYi());
            return true;
        }

        return false;
    }

    @Override
    public boolean key(int key, int action, int mods) {
        if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
            tab.cameraX = 0;
            tab.cameraY = 0;
            tab.zoom = 1;
            this.room.clearHistory();
            return true;
        }

        return false;
    }

    @Override
    public boolean scroll(double x, double y) {
        if (editor.ctrl) {
            tab.zoom += Math.ceil(Math.floor(y) * 10) / 10 / 4F;
            if (tab.zoom <= 0) {
                tab.zoom = 0;
            }
        } else {
            // TODO: Implement 'x' scroll too (for laptops lol)
            if (editor.shift) {
                tab.cameraX += room.getGridSize() * (y > 0 ? 1 : -1);
            } else {
                tab.cameraY += room.getGridSize() * (y > 0 ? 1 : -1);
            }
        }

        return true;
    }

    @Override
    public void render() {
        renderer.scale(tab.zoom, tab.zoom, 1);
        renderer.translate(tab.cameraX, tab.cameraY, 0);

        renderBackground();
        renderObjects();
        renderGrid();

        tab.selectedTool.render();
    }

    protected void renderBackground() {
        if (this.room.getBackgroundTexture() == null) {
            renderer.color(0.25F, 0.25F, 0.25F, 1);
            renderer.rect2d(0, 0, this.room.getWidth(), this.room.getHeight());
        } else {
            renderer.color(1, 1, 1, 1);
            renderer.texRect2d(this.room.getBackgroundTexture(), 0, 0, this.room.getWidth(), this.room.getHeight());
        }

        if (!this.room.getLayers().isEmpty()) {
            renderer.color(1, 1, 1, 1);
            for (Layer layer : this.room.getLayers()) {
                if (layer.texture == null || layer.texture.isDeleted()) continue;
                renderer.texRect2d(layer.texture, 0, 0, this.room.getWidth(), this.room.getHeight());
            }
        }
    }

    protected void renderGrid() {
        renderer.color(1, 1, 1, 0.25F); // TODO: Make grid opacity custom
        editor.getGridRenderer().render(0, 0, this.room.getWidth(), this.room.getHeight(), tab.cameraX, tab.cameraY, tab.zoom, editor.getWindow().getHeight(), room.getGridSize());
    }

    protected void renderObjects() {
        renderer.color(1, 1, 1, 0.25F);
        for (RoomObject object : this.room.getObjects()) {
            float a = object instanceof InvisibleWall ? 0.3F : (object instanceof DoorExit || object instanceof RoomDoor ? 0.6F : 1F);

            if (object.selected) {
                renderer.color(1, 0.25F, 0.25F, a);
            } else {
                renderer.color(1, 1, 1, a);
            }

            renderer.texRect2d(object.getTexture(this.editor.getObjectTextureProvider()), object.x + object.renderOffsetX, object.y + object.renderOffsetY, object.x + object.renderOffsetX + object.getWidth(), object.y + object.renderOffsetY + object.getHeight());
        }
    }
}
