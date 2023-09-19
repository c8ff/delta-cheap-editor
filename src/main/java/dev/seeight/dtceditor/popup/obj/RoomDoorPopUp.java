package dev.seeight.dtceditor.popup.obj;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.CheckBoxComponent;
import dev.seeight.astrakit.components.impl.TextFieldComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.history.impl.EditRoomDoorTarget;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.room.ext.RoomDoor;

public class RoomDoorPopUp extends ComponentPopUp {
	private final RoomDoor door;
	private final Room room;

	public RoomDoorPopUp(DeltaCheapEditor editor, RoomDoor door, Room room) {
		super(editor);
		this.door = door;
		this.room = room;
	}

	@Override
	protected void createComponents() {
		this.components.add(new TitleComponent("Edit Room Door", font));
		TextFieldComponent f = new TextFieldComponent(door.targetRoom, "Target Room");
		CheckBoxComponent s = new CheckBoxComponent(true, "External Room (Game's running directory)", null);
		this.components.add(f);
		this.components.add(s);
		this.components.add(new ButtonEventComponent("Apply", button -> {
			room.addHistory(new EditRoomDoorTarget(door, f.toString(), s.getValue()));
			this.setClosing(true);
		}));
	}
}
