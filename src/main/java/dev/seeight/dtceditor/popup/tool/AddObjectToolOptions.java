package dev.seeight.dtceditor.popup.tool;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.DropdownComponent;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.tools.impl.AddObjectTool;

import java.util.ArrayList;
import java.util.List;

public class AddObjectToolOptions extends ComponentPopUp {
	private final AddObjectTool tool;

	public AddObjectToolOptions(DeltaCheapEditor editor, AddObjectTool tool) {
		super(editor);
		this.tool = tool;
	}

	@Override
	protected void createComponents() {
		List<String> creators = new ArrayList<>();
		int i = 0;

		AddObjectTool.ObjectCreator[] values = AddObjectTool.ObjectCreator.values();
		for (int j = 0; j < values.length; j++) {
			AddObjectTool.ObjectCreator c = values[j];
			creators.add(c.name());
			if (c == this.tool.getCreator()) {
				i = j;
			}
		}

		DropdownComponent c = new DropdownComponent(i, creators.toArray(new String[0]));
		this.components.add(new ButtonEventComponent("Apply", button -> {
			tool.setCreator(AddObjectTool.ObjectCreator.values()[c.getSelected()]);
			this.setClosing(true);
		}));
		this.components.add(c);
	}
}
