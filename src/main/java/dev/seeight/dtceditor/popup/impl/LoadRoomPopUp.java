package dev.seeight.dtceditor.popup.impl;

import com.google.gson.*;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.common.lwjgl.nfd.FileFilter;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.astrakit.components.impl.ButtonEventComponent;
import dev.seeight.astrakit.components.impl.LabelComponent;
import dev.seeight.astrakit.components.impl.TitleComponent;
import dev.seeight.dtceditor.popup.ComponentPopUp;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.*;
import dev.seeight.util.StringUtil;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
		this.components.add(new ButtonEventComponent("OK", button -> {
			if (selectedFile == null) {
				System.out.println("Please select a file.");
				return;
			}

			this.setClosing(true);
			LoadRoomPopUp.loadFromFile(this.editor, selectedFile);
			editor.getWindow().requestWindowAttention();
		}));
		this.components.add(new ButtonEventComponent("Cancel", button -> this.setClosing(true)));
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
		this.labelComponent.setString(Objects.requireNonNullElse(this.selectedFile, "(File not set.)"));
	}

	public static void loadFromFile(DeltaCheapEditor editor, String selectedFile) {
		if (selectedFile == null) {
			System.err.println("file not selected!");
			return;
		}

		try {
			JsonObject object = JsonParser.parseReader(new FileReader(selectedFile)).getAsJsonObject();
			loadFromJson(editor, selectedFile, object);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void loadFromJson(DeltaCheapEditor editor, String filePath, JsonObject object) {
		editor.clearObjects();
		editor.clearBackground();
		editor.clearHistory();
		editor.roomWidth = 640;
		editor.roomHeight = 480;

		editor.roomWidth = object.get("width").getAsInt();
		editor.roomHeight = object.get("height").getAsInt();
		editor.setLoadedRoomPath(filePath);

		String parent = StringUtil.substringToLastIndexOf(filePath, File.separator);

		JsonElement background = object.get("background");
		if (background instanceof JsonPrimitive f && f.isString()) {
			File backgroundToLoad = new File(parent, f.getAsString().substring(1));
			if (!backgroundToLoad.exists()) {
				throw new RuntimeException("Background file '" + backgroundToLoad.getAbsolutePath() + "' doesn't exist.");
			}
			editor.setBackground(backgroundToLoad.getAbsolutePath());
		}

		JsonElement layers = object.get("layers");
		if (layers instanceof JsonArray a && !a.isEmpty()) {
			List<DeltaCheapEditor.Layer> layersList = new ArrayList<>();
			for (JsonElement j : a) {
				if (!(j instanceof JsonObject obj)) {
					continue;
				}

				layersList.add(new DeltaCheapEditor.Layer(obj.get("filename").getAsString(), obj.get("order").getAsString(), parent + "/layers/"));
			}
			editor.setLayers(layersList);
		}

		JsonElement exits = object.get("exits");
		if (exits instanceof JsonArray b) {
			for (JsonElement jsonElement : b) {
				if (!(jsonElement instanceof JsonObject o)) {
					continue;
				}

				DoorExit exit = new DoorExit(o.get("target").getAsString());
				exit.x = o.get("x").getAsInt();
				exit.y = o.get("y").getAsInt();
				exit.setWidth(Player.player.getWidth());
				exit.setHeight(Player.player.getHeight());
				editor.addObject(exit);
			}
		}

		for (JsonElement objs : object.get("objects").getAsJsonArray()) {
			String asString = objs.getAsString();
			String[] split = asString.split("\\|");
			String name = split[0];

			switch (name) {
				case "Player" -> {
					if (split.length > 3) {
						Player.player.x = Integer.parseInt(split[1]);
						Player.player.y = Integer.parseInt(split[2]);
					}

					editor.addObject(Player.player);
				}
				case "InvisibleWall" -> {
					InvisibleWall object1 = new InvisibleWall();
					object1.x = Integer.parseInt(split[1]);
					object1.y = Integer.parseInt(split[2]);
					object1.setSize(Integer.parseInt(split[3]), Integer.parseInt(split[4]));
					editor.addObject(object1);
				}
				case "TextInvisibleWall" -> {
					TextInvisibleWall object1 = new TextInvisibleWall();
					object1.x = Integer.parseInt(split[1]);
					object1.y = Integer.parseInt(split[2]);
					object1.setSize(Integer.parseInt(split[3]), Integer.parseInt(split[4]));
					object1.text = Arrays.copyOfRange(split, 5, split.length);
					for (int i = 0; i < object1.text.length; i++) {
						object1.text[i] = StringEscapeUtils.escapeJava(object1.text[i]);
					}
					editor.addObject(object1);
				}
				case "RoomDoor" -> {
					RoomDoor object1 = new RoomDoor("0unknown");
					object1.x = Integer.parseInt(split[1]);
					object1.y = Integer.parseInt(split[2]);
					object1.setSize(Integer.parseInt(split[3]), Integer.parseInt(split[4]));
					object1.externalRoom = split[5].charAt(0) == '1';
					object1.targetRoom = split[5].substring(1);
					editor.addObject(object1);
				}
			}
		}
	}

	public static JsonObject serialize(DeltaCheapEditor editor) {
		JsonObject root = new JsonObject();

		JsonArray objects = new JsonArray();
		boolean player = false;
		for (RoomObject rm : editor.objectsUnmodifiable) {
			if (rm instanceof TextInvisibleWall i) {
				StringBuilder b = new StringBuilder();

				for (String s : i.text) {
					b.append('|').append(StringEscapeUtils.unescapeJava(s));
				}

				objects.add("TextInvisibleWall|" + rm.x + "|" + rm.y + "|" + rm.getWidth() + "|" + rm.getHeight() + b);
			} else if (rm instanceof RoomDoor i) {
				objects.add("RoomDoor|" + rm.x + "|" + rm.y + "|" + rm.getWidth() + "|" + rm.getHeight() + "|" + (i.externalRoom ? "1" : 0) + i.targetRoom);
			} else if (rm instanceof Player) {
				player = true;
			} else if (rm instanceof DoorExit) {

			} else {
				objects.add("InvisibleWall|" + rm.x + "|" + rm.y + "|" + rm.getWidth() + "|" + rm.getHeight());
			}
		}
		if (player) {
			objects.add("Player|" + Player.player.x + "|" + Player.player.y);
		}

		JsonArray layers = new JsonArray();
		List<DeltaCheapEditor.Layer> l = editor.getLayers();
		if (l != null) {
			for (DeltaCheapEditor.Layer layer : l) {
				JsonObject obj = new JsonObject();
				obj.addProperty("filename", layer.filename);
				obj.addProperty("order", layer.order);
				layers.add(obj);
			}
		}

		JsonArray exits = new JsonArray();
		for (RoomObject roomObject : editor.objectsUnmodifiable) {
			if (!(roomObject instanceof DoorExit d)) {
				continue;
			}

			JsonObject object = new JsonObject();
			object.addProperty("target", d.target);
			object.addProperty("x", d.x);
			object.addProperty("y", d.y);
			exits.add(object);
		}

		root.add("objects", objects);
		root.add("music", null);
		root.addProperty("background", "1" + StringUtil.substringFromLastIndexOf(editor.getLoadedBackgroundPath(), File.separator));
		root.addProperty("width", editor.roomWidth);
		root.addProperty("height", editor.roomHeight);
		root.add("layers", layers);
		root.add("exits", exits);

		return root;
	}
}
