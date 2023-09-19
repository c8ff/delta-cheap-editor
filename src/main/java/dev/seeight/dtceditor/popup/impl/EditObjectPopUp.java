package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.room.RoomObject;

public class EditObjectPopUp extends ComponentPopUp {
	private final RoomObject object;

	public EditObjectPopUp(DeltaCheapEditor editor, RoomObject object) {
		super(editor);
		this.object = object;
	}

	@Override
	protected void createComponents() {
		this.components.add(new TitleComponent("Edit Object", font));
		this.components.add(new ButtonEventComponent("Apply", button -> {
			this.setClosing(true);
		}));
	}
}
