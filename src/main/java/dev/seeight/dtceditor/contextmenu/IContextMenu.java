package dev.seeight.dtceditor.contextmenu;

import dev.seeight.dtceditor.DeltaCheapEditor;

public interface IContextMenu {
    void onOpen();

    void render(DeltaCheapEditor editor);

    void onClose();

    /**
     * @return True if the context menu should close.
     */
    boolean mouseButton(int button, int action, int x, int y);

    int getX();

    int getY();

    int getWidth();

    int getHeight();
}
