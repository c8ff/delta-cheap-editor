package dev.seeight.dtceditor.tab;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.area.Area;
import dev.seeight.dtceditor.area.RoomArea;
import dev.seeight.dtceditor.area.ToolArea;
import dev.seeight.dtceditor.history.impl.DeleteObjects;
import dev.seeight.dtceditor.history.impl.SelectObjects;
import dev.seeight.dtceditor.mgr.TextureManager;
import dev.seeight.dtceditor.popup.impl.*;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.tools.Tool;
import dev.seeight.dtceditor.tools.impl.AddObjectTool;
import dev.seeight.dtceditor.tools.impl.MoveCamera;
import dev.seeight.dtceditor.tools.impl.ResizeObjectsTool;
import dev.seeight.dtceditor.tools.impl.SelectTool;
import dev.seeight.util.StringUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorTab implements ITab {
    public final DeltaCheapEditor editor;
    public final Room room;

    private final ToolArea toolArea;
    public final RoomArea roomArea;

    public final List<Tool> tools;
    public Tool selectedTool;

    public float cameraX;
    public float cameraY;
    public float zoom = 1;

    public Area interactedArea;

    public boolean d1;
    public boolean d2;

    public String title;

    public EditorTab(DeltaCheapEditor editor, Room room) {
        this.editor = editor;
        this.room = room;

        this.toolArea = new ToolArea(this, editor);
        this.roomArea = new RoomArea(this);
        this.interactedArea = toolArea;

        this.tools = new ArrayList<>();
        TextureManager textureManager = editor.getTextureManager();
        this.tools.add(new AddObjectTool(this, textureManager.get("/icons/addObject.png")));
        this.tools.add(new SelectTool(this, textureManager.get("/icons/select.png")));
        this.tools.add(new MoveCamera(this, textureManager.get("/icons/move.png")));
        this.tools.add(new ResizeObjectsTool(this, textureManager.get("/icons/resizeObjects.png")));
        this.selectedTool = tools.get(0);

        // TODO: This is not refreshed.
        System.out.println(room.getPath());
        title = room.getPath() != null ? StringUtil.substringFromLastIndexOf(room.getPath(), File.separator) : "(unsaved)";

        resizeAreas(editor.getWindow().getWidth(), editor.getWindow().getHeight());
    }

    @Override
    public void framebufferSize(int width, int height) {
        resizeAreas(width, height);
    }

    @Override
    public void mouseButton(int button, int action) {
        if (this.toolArea.isInside(editor.mouse.getX(), editor.mouse.getY())) {
            this.interactedArea = this.toolArea;
        } else if (this.roomArea.isInside(editor.mouse.getX(), editor.mouse.getY())) {
            this.interactedArea = this.roomArea;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            d1 = action != GLFW.GLFW_RELEASE;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            d2 = action != GLFW.GLFW_RELEASE;
        }

        this.interactedArea.mouseButton(button, action);
    }

    @Override
    public void cursorPosition(double x, double y) {
        this.interactedArea.cursorPosition(x, y, (d1 ? MouseState.LEFT : 0x00) | (d2 ? MouseState.RIGHT : 0x00));
    }

    @Override
    public void key(int key, int action, int mods) {
        if (!this.toolArea.key(key, action, mods)) {
            this.roomArea.key(key, action, mods);
        }

        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        switch (key) {
            case GLFW.GLFW_KEY_S -> {
                if ((mods & GLFW.GLFW_MOD_CONTROL) == 0) {
                    break;
                }

                SaveRoomPopUp.promptSave(editor, this.room, mods);
            }
            case GLFW.GLFW_KEY_A -> {
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    this.room.addHistory(new SelectObjects(this.room, this.room.collectSelectedObjects(roomObject -> roomObject.selected = true), true));
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (this.room.getSelectedCount() == 0) {
                    return;
                }

                List<RoomObject> objects1 = new ArrayList<>();
                for (RoomObject object : this.room.getObjects()) {
                    if (object.selected) {
                        objects1.add(object);
                    }
                }

                this.room.removeObjects(objects1);
                this.room.addHistory(new DeleteObjects(this.room.getObjects(), objects1));
            }
            case GLFW.GLFW_KEY_Z -> {
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    this.room.historyUndo();
                }
            }
            case GLFW.GLFW_KEY_Y -> {
                if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                    this.room.historyRedo();
                }
            }
            case GLFW.GLFW_KEY_V -> editor.setPopUp(new GridSizePopUp(editor, room));
            case GLFW.GLFW_KEY_B -> editor.setPopUp(new RoomDetails(editor, room));
            case GLFW.GLFW_KEY_N -> editor.setPopUp(new LoadRoomPopUp(editor, room));
            case GLFW.GLFW_KEY_U -> editor.setPopUp(new ConsolePopUp(editor, room));
        }
    }

    @Override
    public void scroll(double x, double y) {
        if (!this.toolArea.scroll(x, y)) {
            this.roomArea.scroll(x, y);
        }
    }

    private void resizeAreas(int width, int height) {
        toolArea.width = 32;
        toolArea.height = height;
        roomArea.width = width - toolArea.width;
        roomArea.height = height;
    }

    @Override
    public void prerender() {
        toolArea.prerender();
        roomArea.prerender();
    }

    @Override
    public void render() {
        float[] mat = new float[16];

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        renderArea(mat, roomArea);
        renderArea(mat, toolArea);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

    }

    private void renderArea(float[] mat, Area area) {
        GL11.glScissor(area.x, editor.getWindow().getHeight() - (area.height + area.y), area.width, area.height);

        this.editor.getRenderer().getViewMatrix4f(mat);
        this.editor.getRenderer().resetView();
        this.editor.getRenderer().translate(area.x, area.y, 0);

        area.render();

        this.editor.getRenderer().setViewMatrix4f(mat);
    }

    public static class MouseState {
        public static final int LEFT = 0x01;
        public static final int RIGHT = 0x02;
    }
}
