package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.LabelComponent;
import dev.seeight.astrakit.components.impl.SkipNewLineComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.tab.EditorTab;
import dev.seeight.dtceditor.tab.ITab;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class LoadRoomPopUp extends ComponentPopUp {
	private String selectedFile;
	private LabelComponent labelComponent;

	public LoadRoomPopUp(DeltaCheapEditor editor, String roomJsonFile) {
		super(editor);
		this.selectedFile = roomJsonFile;
	}

	public LoadRoomPopUp(DeltaCheapEditor editor) {
		this(editor, null);
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
		this.components.add(SkipNewLineComponent.INSTANCE);

		ITab tab = this.editor.getTab();
		if (tab instanceof EditorTab t) {
			Room target = t.room;
			this.components.add(new ButtonEventComponent("Load", button -> {
				if (selectedFile == null) {
					System.out.println("Please select a file.");
					return;
				}

				this.setClosing(true);
				// TODO: Ask to save before loading
				Room.loadFromFile(this.editor, selectedFile, target);
				editor.getWindow().requestWindowAttention();
			}));
		}
		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("Load In New Tab", button -> {
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
		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("Cancel", button -> this.setClosing(true)));
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
		String string = Objects.requireNonNullElse(this.selectedFile, "(File not set.)");

		int maxLength = 36;
		if (string.length() >= maxLength) {
			int n = -1;

			char[] charArray = string.toCharArray();
			for (int i = charArray.length - 1, l = 0; i >= 0 && l <= maxLength; i--, l++) {
				char c = charArray[i];

				if (c == File.separatorChar) {
					n = i;
				}
			}

			if (n != -1) {
				string = "..." + string.substring(n);
			} else {
				string = "..." + string.substring(string.length() - 33);
			}
		}

		this.labelComponent.setString(string);
	}
}
