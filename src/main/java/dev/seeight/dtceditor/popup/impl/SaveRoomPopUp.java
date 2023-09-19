package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.*;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.util.StringUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveRoomPopUp extends ComponentPopUp {
	private String selectedPath;

	private PopUp popUp;
	private final Room room;

	public SaveRoomPopUp(DeltaCheapEditor editor, Room room) {
		this(editor, null, room);
	}

	public SaveRoomPopUp(DeltaCheapEditor editor, String selectedPath, Room room) {
		super(editor);
		this.selectedPath = selectedPath;
		this.room = room;
	}

	@Override
	protected void createComponents() {
		LabelComponent e = new LabelComponent(String.valueOf(this.selectedPath));
		this.components.add(new TitleComponent("Save Room", this.titleFont));
		this.components.add(new LabelComponent("(make sure to save in the correct room directory)"));
		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("Select File...", button -> {
			String defaultPath = StringUtil.substringToLastIndexOf(selectedPath, File.separator) + File.separator;
			selectedPath = FileChooserUtil.openSaveChooser(defaultPath, "room.json", new FileFilter("Room", "json"));
			e.setString(String.valueOf(this.selectedPath));
		}));
		this.components.add(e);
		this.components.add(new CheckBoxComponent(true, "Copy background", null));
		this.components.add(new ButtonEventComponent("Save", button -> {
			if (selectedPath == null) {
				popUp = new ErrorPopUp(editor, "The file must not be null.");
				popUp.init();
				return;
			}

			String json = DeltaCheapEditor.gson.toJson(Room.serialize(room));
			try {
				room.setPath(selectedPath);
				Files.writeString(Path.of(selectedPath), json);
				this.setClosing(true);
			} catch (IOException x) {
				x.printStackTrace();
			}
		}));
	}

	@Override
	public void render() {
		super.render();

		if (popUp != null) {
			renderer.color(0, 0, 0, (100 - this.popUp.getClosingProgress()) / 200F);
			renderer.rect2d(0, 0, editor.getWindow().getWidth(), editor.getWindow().getHeight());
			renderer.color(1, 1, 1, 1);
			popUp.render();
			popUp.animateClosing();

			if (popUp.getClosingProgress() >= 100 && popUp.isClosing()) {
				popUp.close();
				popUp = null;
			}
		}
	}

	@Override
	public void mouseButton(int button, int action) {
		if (this.popUp != null) {
			if (!this.popUp.contains(editor.mouse.getX(), editor.mouse.getY())) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
					if (this.popUp.isClosing()) {
						this.popUp.setClosingProgress(100F);
					} else {
						this.popUp.setClosing(true);
					}
				}
			} else if (!this.popUp.isClosing()) {
				this.popUp.mouseButton(button, action);
			}

			return;
		}

		super.mouseButton(button, action);
	}

	@Override
	public void cursorPosition(double x, double y) {
		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.cursorPosition(x, y);
			return;
		}

		super.cursorPosition(x, y);
	}

	@Override
	public void key(int key, int action, int mods) {
		if (this.popUp != null && !this.popUp.isClosing()) {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				if (action == GLFW.GLFW_RELEASE) {
					if (this.popUp.isClosing()) {
						this.popUp.setClosingProgress(100F);
					} else {
						this.popUp.setClosing(true);
					}
				}
			} else if (!this.popUp.isClosing()) {
				this.popUp.key(key, action, mods);
			}

			return;
		}

		super.key(key, action, mods);
	}

	@Override
	public void scroll(double x, double y) {
		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.scroll(x, y);
			return;
		}

		super.scroll(x, y);
	}

	@Override
	public void character(int codepoint) {
		if (this.popUp != null && !this.popUp.isClosing()) {
			this.popUp.character(codepoint);
			return;
		}

		super.character(codepoint);
	}

	public static void promptSave(DeltaCheapEditor editor, Room room, int mods) {
		if (room.getPath() == null || ((mods & GLFW.GLFW_MOD_SHIFT) != 0)) {
			editor.setPopUp(new SaveRoomPopUp(editor, room.getPath(), room));
		} else {
			String json = DeltaCheapEditor.gson.toJson(Room.serialize(room));
			try {
				Files.writeString(Path.of(room.getPath()), json);
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}
}
