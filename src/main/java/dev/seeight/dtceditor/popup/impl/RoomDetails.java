package dev.seeight.dtceditor.popup.impl;

import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.impl.*;
import dev.seeight.dtceditor.popup.ComponentPopUp;

import java.util.Objects;

public class RoomDetails extends ComponentPopUp {
	private boolean changedBackground;

	public RoomDetails(DeltaCheapEditor editor) {
		super(editor);
	}

	@Override
	protected void createComponents() {
		TextFieldComponent width;
		TextFieldComponent height;

		this.components.add(new TitleComponent("Room Details", titleFont));
		this.components.add(new LabelComponent("Room Size", font));
		this.components.add(width = new TextFieldComponent(String.valueOf(this.editor.roomWidth), "Width") {
			@Override
			protected boolean isCharacterValid(char character) {
				return (character >= '0' && character <= '9') && super.isCharacterValid(character) && this.size() <= 4;
			}
		});
		this.components.add(height = new TextFieldComponent(String.valueOf(this.editor.roomHeight), "Height") {
			@Override
			protected boolean isCharacterValid(char character) {
				return (character >= '0' && character <= '9') && super.isCharacterValid(character) && this.size() <= 4;
			}
		});
		this.components.add(new LabelComponent("Background", font));
		LabelComponent bg;
		this.components.add(bg = new LabelComponent(Objects.requireNonNullElse(this.editor.getLoadedBackgroundPath(), "null"), font));
		this.components.add(new ButtonEventComponent("Select Background...", button -> {
			String newPath = FileChooserUtil.openFileChooser(this.editor.getLoadedBackgroundPath(), new FileFilter("Image Files", "png,jpg,jpeg"));
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
					this.editor.setBackground(string.equals("null") ? null : string);
				}
				this.editor.roomWidth = Integer.parseInt(width.toString());
				this.editor.roomHeight = Integer.parseInt(height.toString());
			} catch (Exception e) {
				this.setClosingProgress(100F);
				e.printStackTrace();
			}
			this.setClosing(true);
		}));
	}
}
