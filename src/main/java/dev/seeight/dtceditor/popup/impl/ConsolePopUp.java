package dev.seeight.dtceditor.popup.impl;

import com.google.gson.JsonObject;
import dev.seeight.astrakit.components.impl.TextFieldComponent;
import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.dtceditor.Room;
import dev.seeight.dtceditor.Server;
import dev.seeight.dtceditor.popup.PopUp;
import dev.seeight.util.StringUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsolePopUp extends PopUp {
	private static final List<String> lines = new ArrayList<>();
	private final TextFieldComponent textFieldComponent;
	private float typeTimer;

	public ConsolePopUp(DeltaCheapEditor editor) {
		super(editor);
		this.textFieldComponent = new TextFieldComponent("", "");
	}

	@Override
	public void render() {
		float a = (100 - this.getClosingProgress()) / 100F;
		width = editor.getWindow().getWidth();
		height = editor.getWindow().getHeight();

		renderer.color(1, 1, 1, a);
		FontRenderer font = editor.font;
		font.drawString(" (click ESC to exit.)", editor.fontBold.drawString("Console", 10, 10), 10 + (editor.fontBold.FONT_HEIGHT_FLOAT - editor.font.FONT_HEIGHT_FLOAT));


		// draw lines (inverted, down to up, last to first)

		final float fieldY = height - font.FONT_HEIGHT_FLOAT - 10;
		final float fieldX = (float) font.drawString("> ", 10, fieldY);

		float y = fieldY - font.FONT_HEIGHT_FLOAT;
		for (int i = lines.size() - 1; i >= 0; i--) {
			font.drawString(lines.get(i), 10, y);
			y -= font.FONT_HEIGHT_FLOAT;
		}

		//region draw text field

		// update type timer
		typeTimer += editor.getDeltaTime();

		TextFieldComponent field = textFieldComponent;
		final String fieldText = field.toString();
		final TextFieldComponent.Selection sel = field.getSelection();
		final float headX = field.getHeadIndex() == 0 ? 0 : font.getWidthFloat(fieldText, field.getHeadIndex());
		final float headY2 = fieldY + font.FONT_HEIGHT_FLOAT;

		// Main Text
		renderer.color(1, 1, 1, a);
		font.drawString(fieldText, fieldX, fieldY);

		// Cursor Head
		if (typeTimer < 0.5F) {
			// (The ' + 2' fixes an alignment issue)
			float x = fieldX + 2 + headX;
			renderer.color(1, 1, 1, a);
			renderer.rect2d(x, fieldY, x + 1, headY2);
		} else if (typeTimer > 1) {
			typeTimer = 0;
		}

		// Selection
		if (sel != null) {
			float startX = font.getWidthFloat(fieldText, sel.start);
			float endX = font.getWidthFloat(fieldText, sel.end);

			renderer.color(0, 0, 0, a * 0.5F);
			// (The ' + 2' fixes an alignment issue)
			renderer.rect2d(fieldX + 2 + startX, fieldY, fieldX + 2 + endX, headY2);
		}
		//endregion
	}

	@Override
	public void key(int key, int action, int mods) {
		if (key == GLFW.GLFW_KEY_ENTER) {
			if (action == GLFW.GLFW_PRESS) {
				String input = textFieldComponent.toString().trim();

				if (input.equals("clear")) {
					lines.clear();
				} else if (input.equals("close")) {
					editor.getWindow().setShouldClose(true);
				} else if (input.startsWith("echo")) {
					if (input.length() >= 5) {
						String s = input.substring(5);
						lines.add(s);
					} else {
						lines.add("");
					}
				} else if (input.startsWith("startServer")) {
					if (Server.serverThread != null && Server.serverThread.isAlive()) {
						lines.add("Server is already on.");
					} else {
						Server.startServerThread(this.editor);
					}
				} else if (input.startsWith("stopServer")) {
					if (Server.serverThread == null || !Server.serverThread.isAlive()) {
						lines.add("Server is already off.");
					} else {
						try {
							Server.shutdownServerThread();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else if (input.startsWith("sendMessage ")) {
					if (Server.serverThread == null || !Server.serverThread.isAlive()) {
						lines.add("Server is not on. Turn it on.");
						return;
					}

					String msg = input.substring(12);
					Server.serverThread.server.sendMessage(msg, "OK");
				} else if (input.startsWith("resend")) {
					if (Server.serverThread == null || !Server.serverThread.isAlive()) {
						lines.add("Server is not on. Turn it on.");
						return;
					}

					JsonObject object = Room.serialize(editor.room);
					object.addProperty("tempFilePath", editor.room.getPath() == null ? null : StringUtil.substringToLastIndexOf(editor.room.getPath(), File.separator));
					Server.serverThread.server.sendMessage("setRoom " + DeltaCheapEditor.gsonRaw.toJson(object), "OK");
				} else {
					lines.add("Unknown command: '" + input + "'. Type close to close the editor.");
				}

				textFieldComponent.set("");
			}
		} else {
			textFieldComponent.keyEvent(key, action, mods);
			typeTimer = 0;
		}
	}

	@Override
	public void character(int codepoint) {
		textFieldComponent.charEvent((char) codepoint);
		typeTimer = 0;
	}
}
