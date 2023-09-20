package dev.seeight.dtceditor.popup.impl;

import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.LabelComponent;
import dev.seeight.astrakit.components.impl.SkipNewLineComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.tab.ITab;

public class AskCloseTabPopUp extends ComponentPopUp {
    private final ITab tab;

    public AskCloseTabPopUp(DeltaCheapEditor editor, ITab tab) {
        super(editor, 400, 136);
        this.tab = tab;
    }

    @Override
    protected void createComponents() {
        this.components.clear();
        this.components.add(new TitleComponent("Close tab?", this.titleFont));
        this.components.add(new LabelComponent("Any unsaved progress will be lost.", this.font));
        this.components.add(SkipNewLineComponent.INSTANCE);
        this.components.add(new ButtonEventComponent("Close", buttonEventComponent -> {
            this.setClosing(true);
            this.editor.removeTab(tab);
        }));
        this.components.add(SkipNewLineComponent.INSTANCE);
        this.components.add(new ButtonEventComponent("Cancel", buttonEventComponent -> this.setClosing(true)));
    }
}
