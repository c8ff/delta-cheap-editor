package dev.seeight.dtceditor.popup.obj;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.TextFieldComponent;
import dev.seeight.dtceditor.history.impl.EditDoorExit;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.room.ext.DoorExit;

public class DoorExitPopUp extends ComponentPopUp {
	private final DoorExit doorExit;

	public DoorExitPopUp(DeltaCheapEditor editor, DoorExit doorExit) {
		super(editor);
		this.doorExit = doorExit;
	}

	@Override
	protected void createComponents() {
		TextFieldComponent t = new TextFieldComponent(this.doorExit.target, "Target Room");
		this.components.add(t);
		this.components.add(new ButtonEventComponent("Apply", button -> {
			editor.addHistory(new EditDoorExit(doorExit, t.toString()));
			setClosing(true);
		}));
	}
}
