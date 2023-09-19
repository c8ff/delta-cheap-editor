package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.LabelComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.ComponentPopUp;

public class ErrorPopUp extends ComponentPopUp {
	private final String str;

	public ErrorPopUp(DeltaCheapEditor editor, String str) {
		super(editor, 300, 200);
		this.str = str;
	}

	@Override
	protected void createComponents() {
		this.components.add(new TitleComponent("Error", font));
		this.components.add(new LabelComponent(str));
		this.components.add(new ButtonEventComponent("Ok", button -> setClosing(true)));
	}
}
