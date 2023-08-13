package dev.seeight.dtceditor.popup.obj;

import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.Component;
import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.SkipNewLineComponent;
import dev.seeight.astrakit.components.impl.TextFieldComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.history.impl.EditInvisibleWallText;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.room.ext.TextInvisibleWall;

import java.util.ConcurrentModificationException;

public class TextInvisibleWallPopUp extends ComponentPopUp {
	private final TextInvisibleWall obj;

	public TextInvisibleWallPopUp(DeltaCheapEditor editor, TextInvisibleWall obj) {
		super(editor);
		this.obj = obj;
	}

	@Override
	protected void createComponents() {
		this.components.add(new TitleComponent("Edit Invisible Wall Text", font));

		for (String s : this.obj.text) {
			this.components.add(new TextFieldComponent(s, "Text"));
		}

		this.components.add(SkipNewLineComponent.INSTANCE);
		this.components.add(new ButtonEventComponent("+", button -> {
			this.components.add(this.components.size() - 3, new TextFieldComponent("", "Text"));
			this.calculateComponents();
		}));
		this.components.add(new ButtonEventComponent("-", button -> {
			for (int i = this.components.size() - 1; i >= 0; i--) {
				var comp = this.components.get(i);
				if (comp instanceof TextFieldComponent) {
					this.components.remove(i);
					break;
				}
			}
			this.calculateComponents();
		}));
		this.components.add(new ButtonEventComponent("Apply", button -> {
			int count = 0;
			for (Component component : this.components) {
				if (component instanceof TextFieldComponent) {
					count++;
				}
			}

			String[] strings = new String[count];

			int i = 0;
			for (Component component : this.components) {
				if (component instanceof TextFieldComponent f) {
					strings[i++] = f.toString();

					if (i > strings.length) {
						throw new ConcurrentModificationException();
					}
				}
			}

			editor.addHistory(new EditInvisibleWallText(obj, strings));
			this.setClosing(true);
		}));
	}

}
