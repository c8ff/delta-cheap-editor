package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.*;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.ComponentPopUp;

import java.util.Objects;

public class RoomDetails extends ComponentPopUp {
	private boolean changedBackground;
	private final Room room;

	public RoomDetails(DeltaCheapEditor editor, Room room) {
		super(editor);
		this.room = room;
	}

	@Override
	protected void createComponents() {
		TextFieldComponent width;
		TextFieldComponent height;

		this.components.add(new TitleComponent("Room Details", titleFont));
		this.components.add(new LabelComponent("Room Size", font));
		this.components.add(width = new TextFieldComponent(String.valueOf(this.room.getWidth()), "Width") {
			@Override
			protected boolean isCharacterValid(char character) {
				return (character >= '0' && character <= '9') && super.isCharacterValid(character) && this.size() <= 4;
			}
		});
		this.components.add(height = new TextFieldComponent(String.valueOf(this.room.getWidth()), "Height") {
			@Override
			protected boolean isCharacterValid(char character) {
				return (character >= '0' && character <= '9') && super.isCharacterValid(character) && this.size() <= 4;
			}
		});
		this.components.add(new LabelComponent("Background", font));
		LabelComponent bg;
		this.components.add(bg = new LabelComponent(Objects.requireNonNullElse(this.room.getPath(), "null"), font));
		this.components.add(new ButtonEventComponent("Select Background...", button -> {
			String newPath = FileChooserUtil.openFileChooser(this.room.getPath(), new FileFilter("Image Files", "png,jpg,jpeg"));
			bg.setString(Objects.requireNonNullElse(newPath, "null"));
			changedBackground = true;
		}));
		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("Cancel", button -> this.setClosing(true)));
		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("Apply", button -> {
			try {
				if (changedBackground) {
					String string = bg.getString();
					this.editor.queueLoadBackground(this.room, string.equals("null") ? null : string);
				}
				this.room.setWidth(Integer.parseInt(width.toString()));
				this.room.setHeight(Integer.parseInt(height.toString()));
			} catch (Exception e) {
				this.setClosingProgress(100F);
				e.printStackTrace();
			}
			this.setClosing(true);
		}));
	}
}
