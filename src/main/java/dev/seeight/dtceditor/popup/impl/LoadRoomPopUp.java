package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.LabelComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.tab.EditorTab;

import java.util.ArrayList;
import java.util.Objects;

public class LoadRoomPopUp extends ComponentPopUp {
	private String selectedFile;
	private LabelComponent labelComponent;
	private final Room room;

	public LoadRoomPopUp(DeltaCheapEditor editor, String roomJsonFile, Room room) {
		super(editor);
		this.selectedFile = roomJsonFile;
		this.room = room;
	}

	public LoadRoomPopUp(DeltaCheapEditor editor, Room room) {
		this(editor, null, room);
	}

	@Override
	protected void createComponents() {
		this.labelComponent = new LabelComponent("");
		this.labelComponent.setFont(this.font);
		this.setSelectedFile(this.selectedFile);

		this.components.add(new TitleComponent("Load Stage?", this.titleFont));
		this.components.add(new LabelComponent("This will load the file: "));
		this.components.add(labelComponent);
		this.components.add(new LabelComponent("You can also drag and drop any file."));
		this.components.add(new ButtonEventComponent("Select File", button -> {
			this.setSelectedFile(FileChooserUtil.openFileChooser(selectedFile, new FileFilter("Room JSON", "json")));
		}));
		this.components.add(new ButtonEventComponent("OK", button -> {
			if (selectedFile == null) {
				System.out.println("Please select a file.");
				return;
			}

			this.setClosing(true);
			Room room1 = new Room(640, 480, new ArrayList<>(), new ArrayList<>());
			Room.loadFromFile(this.editor, selectedFile, room1);
			this.editor.addTab(new EditorTab(editor, room1));
			editor.getWindow().requestWindowAttention();
		}));
		this.components.add(new ButtonEventComponent("Cancel", button -> this.setClosing(true)));
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
		this.labelComponent.setString(Objects.requireNonNullElse(this.selectedFile, "(File not set.)"));
	}
}
