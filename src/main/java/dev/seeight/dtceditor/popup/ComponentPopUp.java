package dev.seeight.dtceditor.popup;

import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.Component;
import dev.seeight.astrakit.IComponentRenderer;
import dev.seeight.astrakit.components.impl.*;
import dev.seeight.util.MathUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public abstract class ComponentPopUp extends PopUp implements IComponentRenderer {
	protected final List<Component> components;
	protected FontRenderer font;
	protected FontRenderer titleFont;
	protected Component selected;

	public ComponentPopUp(DeltaCheapEditor editor) {
		this(editor, 400, 480);
	}

	public ComponentPopUp(DeltaCheapEditor editor, int width, int height) {
		super(editor, width, height);
		components = new ArrayList<>();
	}

	protected abstract void createComponents();

	@Override
	public void init() {
		this.font = this.editor.font;
		this.titleFont = this.editor.fontBold;

		this.components.clear();
		this.createComponents();
		this.calculateComponents();
	}

	@Override
	public void render() {
		float alpha = (100 - this.getClosingProgress()) / 100F;
		int x = this.getX();
		int y = this.getY();

		float[] matrix = new float[16];
		this.renderer.getViewMatrix4f(matrix);
        /* this.renderer.translate(x + this.getWidth() * 0.5F * (1F - alpha), y + this.getHeight() * 0.5F * (1F - alpha), 0);
        this.renderer.scale(alpha, alpha, 1F); */
		this.renderer.translate(x, y, 0F);

		this.backgroundColor(alpha);
		this.renderer.rect2d(0, 0, this.getWidth(), this.getHeight());

		for (Component component : this.components) {
			component.parentX = x;
			component.parentY = y;
			component.render(this, alpha);

			if (component.isFocused()) {
				this.accentColor(alpha);
				this.renderer.hollowRect2d(component.x, component.y, component.x + component.width, component.y + component.height, 2F);
			}
		}
		this.renderer.setViewMatrix4f(matrix);
	}

	@Override
	public void mouseButton(int button, int action) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_1) return;

		if (action == GLFW.GLFW_PRESS) {
			boolean set = false;
			for (Component component : this.components) {
				if (component.shouldFocus(this.editor.mouse.getXi(), this.editor.mouse.getYi())) {
					for (Component c : this.components) {
						if (c != component) {
							c.unfocus();
						}
					}

					set = true;
					if (this.selected != component) {
						component.focus();
						this.selected = component;
						break;
					}
				}
			}

			if (!set) {
				if (this.selected != null) {
					this.selected.unfocus();
				}
				this.selected = null;
			}
		}

		if (this.selected != null) {
			this.selected.mouseButtonEvent(this.editor.mouse.getXi(), this.editor.mouse.getYi(), button, action);
		}
	}

	@Override
	public void cursorPosition(double x, double y) {
		if (this.selected != null) {
			this.selected.cursorPositionEvent(this.editor.mouse.getXi(), this.editor.mouse.getYi());
		}
	}

	@Override
	public void key(int key, int action, int mods) {
		if (key == GLFW.GLFW_KEY_TAB) {
			if (action == GLFW.GLFW_PRESS) {
				if (this.selected == null) {
					this.selected = components.get(0);
					this.selected.focus();
				} else {
					this.selected.unfocus();
					int wrap = MathUtil.wrap(components.indexOf(this.selected) + 1, 0, components.size());

					if (wrap == components.size()) {
						this.selected = null;
						return;
					}

					this.selected = components.get(wrap);
					this.selected.focus();
				}
			}

			return;
		}

		if (this.selected != null) {
			this.selected.keyEvent(key, action, mods);
		}
	}

	@Override
	public void scroll(double x, double y) {
		if (this.selected != null) {
			this.selected.scrollEvent(x, y);
		}
	}

	@Override
	public void character(int codepoint) {
		if (this.selected != null) {
			this.selected.charEvent((char) codepoint);
		}
	}

	@Override
	public void renderSlider(SliderComponent slider, float a) {
		float prg = ((slider.value - slider.min) / (slider.max - slider.min));

		this.renderer.color(0.5F, 0.5F, 0.5F, a);
		this.renderer.rect2d(slider.x, slider.y, slider.x + slider.width, slider.y + slider.height);
		this.accentColor(a);
		this.renderer.rect2d(slider.x, slider.y, slider.x + slider.width * prg, slider.y + slider.height);
		this.textColor(a);
		this.font.drawString(String.valueOf(slider.value), slider.x, slider.y);
	}

	@Override
	public void renderTextField(TextFieldComponent field, float a) {
		final String fieldText = field.toString();
		final TextFieldComponent.Selection sel = field.getSelection();
		final float headX = field.getHeadIndex() == 0 ? 0 : this.font.getWidthFloat(fieldText, field.getHeadIndex());
		final float marginX = 5;
		final float headY = field.y + (field.height - this.font.FONT_HEIGHT_FLOAT) / 2F;
		final float headY2 = headY + this.font.FONT_HEIGHT_FLOAT;

		final float textX = field.x + marginX;
		final float textY = field.y + (field.height - this.font.FONT_HEIGHT_FLOAT) / 2F;

		// Background
		this.backgroundForegroundColor(a);
		this.renderer.rect2f(field.x, field.y, field.x + field.width, field.y + field.height);
		// Bar at the bottom
		if (field.isFocused()) this.accentColor(a);
		else this.renderer.color(0.85F, 0.85F, 0.85F, a);
		this.renderer.rect2f(field.x, field.y + field.height - 2, field.x + field.width, field.y + field.height);

		// Main Text
		if (!fieldText.isEmpty()) {
			this.renderer.color(0, 0, 0, a);
			this.font.drawString(fieldText, textX, textY);
		} else if (!field.isFocused() && field.getEmptyText() != null) {
			this.renderer.color(0.55F, 0.55F, 0.55F, a);
			this.font.drawString(field.getEmptyText(), textX, textY);
		}

		// Selection
		if (sel != null) {
			float startX = this.font.getWidthFloat(fieldText, sel.start);
			float endX = this.font.getWidthFloat(fieldText, sel.end);

			this.renderer.color(0, 0, 0, a * 0.5F);
			// (The ' + 2' fixes an alignment issue)
			this.renderer.rect2d(field.x + marginX + 2 + startX, headY, field.x + marginX + 2 + endX, headY2);
		}

		// Cursor Head
		if (field.isFocused()) {
			// (The ' + 2' fixes an alignment issue)
			float x = field.x + marginX + 2 + headX;
			this.renderer.color(0, 0, 0, a);
			this.renderer.rect2d(x, headY, x + 1, headY2);
		}
	}

	@Override
	public void renderLabel(LabelComponent label, float alpha) {
		this.textColor(alpha);
		this.font.drawString(label.getString(), label.x, label.y + (label.height - font.FONT_HEIGHT_FLOAT) / 2F);
	}

	@Override
	public void renderButton(ButtonComponent button, float alpha) {
		final String string = button.getString();

		this.renderer.color(0.90F, 0.90F, 0.90F, alpha);
		this.renderer.rect2f(button.x, button.y, button.x + button.width, button.y + button.height);
		this.textColor(alpha);
		this.font.drawString(string, button.x + (button.width - this.font.getWidthFloat(string)) / 2F, button.y + (button.height - font.FONT_HEIGHT_FLOAT) / 2F);
	}

	@Override
	public void renderCheckBox(CheckBoxComponent component, float alpha) {
		final float marginX = 4;
		final float boxSize = 18;
		final float asd = (component.height - boxSize) / 2F;

		if (component.getValue()) this.accentColor(alpha);
		else this.renderer.color(0.90F, 0.90F, 0.90F, alpha);
		this.renderer.rect2f(component.x + marginX, component.y + asd, component.x + marginX + boxSize, component.y + asd + boxSize);
		this.textColor(alpha);
		this.font.drawString(component.getValueName(), component.x + marginX * 2 + boxSize, component.y + (component.height - font.FONT_HEIGHT_FLOAT) / 2F);
	}

	@Override
	public void renderDropdown(DropdownComponent component, float alpha) {
		this.textColor(alpha);
		this.font.drawString(component.getElmSelected(), component.x, component.y);

		if (component.opened) {
			float yf = component.getElementHeight();
			this.backgroundForegroundColor(1F);
			this.renderer.rect2d(component.x, component.y + yf, component.x + component.width, component.y + yf + component.getListHeight());
			this.textColor(alpha);
			for (String option : component.options) {
				this.font.drawString(option, component.x, component.y + yf);
				yf += this.font.FONT_HEIGHT_FLOAT;
			}
		}
	}

	@Override
	public void renderTitle(TitleComponent component, float alpha) {
		this.textColor(alpha);
		this.editor.fontBold.drawString(component.text, component.x, component.y);
	}

	@Override
	public void renderDefault(Component component, float alpha) {
		this.textColor(alpha);
		this.font.drawString(component.getClass().getName(), component.x, component.y);
	}

	protected void calculateComponents() {
		float marginX = 12;
		float marginY = 6;

		float componentWidth = this.width - 16 * 2;
		float componentHeight = 36;

		float x = 16;
		float startX = x;
		float y = 16;
		float maxHeight = 0;
		boolean skip = false;
		for (ListIterator<Component> iterator = this.components.listIterator(); iterator.hasNext(); ) {
			Component component = iterator.next();

			if (component instanceof SkipNewLineComponent) {
				iterator.remove();
				skip = true;
				continue;
			}

			component.x = x;
			component.y = y;

			if (component instanceof ButtonComponent d) {
				d.calcSize(font, marginX, marginY);
			} else if (component instanceof LabelComponent d) {
				d.calcSize(font);
			} else if (component instanceof TitleComponent d) {
				d.setSize(titleFont.getWidthFloat(d.text), titleFont.FONT_HEIGHT_FLOAT);
			} else if (component instanceof TextFieldComponent d) {
				d.setSize(componentWidth, 36);
			} else if (component instanceof DropdownComponent d) {
				component.setSize(componentWidth, componentHeight);
				d.calcSize(font);
			} else {
				component.setSize(componentWidth, componentHeight);
			}

			maxHeight = Math.max(maxHeight, component.height);

			if (skip) {
				x += component.width + 4;
				skip = false;
				continue;
			}

			y += maxHeight + 4;
			maxHeight = 0;
			x = startX;
		}
	}

	public void backgroundColor(float alpha) {
		renderer.color(0.95F, 0.95F, 0.95F, alpha);
	}

	public void backgroundForegroundColor(float alpha) {
		renderer.color(0.90F, 0.90F, 0.90F, alpha);
	}

	public void textColor(float alpha) {
		renderer.color(0.25F, 0.25F, 0.25F, alpha);
	}

	public void accentColor(float alpha) {
		// #00C8FF
		renderer.color(0, 0.78431374f, 1.0f, alpha);
	}
}
