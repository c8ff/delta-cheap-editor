package dev.seeight.dtceditor;

import com.google.gson.*;
import dev.seeight.dtceditor.history.IHistoryEntry;
import dev.seeight.dtceditor.room.RoomObject;
import dev.seeight.dtceditor.room.ext.*;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import dev.seeight.util.StringUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Room {
    /**
     * The width of the room.
     */
    private int width;

    /**
     * The height of the room.
     */
    private int height;


    /**
     * The objects of the room.
     */
    private final List<RoomObject> objects;

    /**
     * An unmodifiable wrapper of {@link #objects}.
     */
    private final List<RoomObject> refObjects;

    /**
     * The layers of the room.
     */
    private final List<Layer> layers;

    /**
     * An unmodifiable wrapper of {@link #objects}.
     */
    private final List<Layer> refLayers;

    private Texture backgroundTexture;
    private String backgroundPath;

    private String path;

    private final List<IHistoryEntry> historyEntries;
    private int historyIndex;

    private int gridSize = 10;

    /**
     * @param width   The width of the room.
     * @param height  The height of the room.
     * @param objects The objects of the room.
     * @param layers
     */
    public Room(int width, int height, List<RoomObject> objects, List<Layer> layers) {
        this.width = width;
        this.height = height;
        this.objects = objects;
        this.path = null;
        this.refObjects = Collections.unmodifiableList(objects);
        this.layers = layers;
        this.refLayers = Collections.unmodifiableList(layers);
        this.historyEntries = new ArrayList<>();
        this.historyIndex = 0;
    }

    public void addHistory(IHistoryEntry historyEntry) {
        if (this.historyIndex < this.historyEntries.size() - 1) {
            this.historyEntries.subList(Math.max(this.historyIndex, 0), this.historyEntries.size()).clear();
        }

        if (this.historyIndex == 0 && !this.historyEntries.isEmpty()) {
            this.historyEntries.clear();
        }

        this.historyEntries.add(historyEntry);
        this.historyIndex = this.historyEntries.size();
    }

    public void clearHistory() {
        this.historyEntries.clear();
        this.historyIndex = 0;
    }

    public void historyUndo() {
        if (this.historyIndex > 0 && this.historyIndex <= this.historyEntries.size()) {
            this.historyEntries.get(--this.historyIndex).undo();
        }
    }

    public void historyRedo() {
        if (this.historyIndex < this.historyEntries.size() && this.historyIndex >= -1) {
            this.historyEntries.get(this.historyIndex++).redo();
        }
    }

    protected void setBackgroundTexture(String backgroundPath, Texture backgroundTexture) {
        if (this.backgroundTexture != null && backgroundTexture == null) {
            this.backgroundTexture.delete();
        }

        if (backgroundTexture == null) {
            backgroundPath = null;
        }

        this.backgroundPath = backgroundPath;
        this.backgroundTexture = backgroundTexture;
    }

    public Texture getBackgroundTexture() {
        if (backgroundTexture == null) {
            return null;
        }

        if (backgroundTexture.isDeleted()) {

            return null;
        }

        return backgroundTexture;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public int snapToGrid(float input) {
        double mod = input % gridSize;
        int a = (int) (input / gridSize) * gridSize;
        if (mod > gridSize / 2d) {
            return a + gridSize;
        }

        return a;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return The width of the room.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height of the room.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return An unmodifiable version of {@link #objects}
     */
    public List<RoomObject> getObjects() {
        return refObjects;
    }

    /**
     * @return An unmodifiable version of {@link #layers}
     */
    public List<Layer> getLayers() {
        return refLayers;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void addObjects(List<RoomObject> objects) {
        this.objects.addAll(objects);
    }

    public void addObject(RoomObject object) {
        if (object instanceof Player) {
            if (this.objects.contains(object)) {
                System.out.println("tried to add a double player.");
                return;
            }
        }

        this.objects.add(object);
    }

    public void removeObjects(List<RoomObject> objects) {
        this.objects.removeAll(objects);
    }

    public void removeObject(RoomObject object) {
        this.objects.remove(object);
    }

    public boolean hasObject(RoomObject object) {
        return this.objects.contains(object);
    }

    public void clearObjects() {
        this.objects.clear();
    }

    public void addLayers(List<Layer> layer) {
        this.layers.addAll(layer);
    }

    public void addLayer(Layer layer) {
        this.layers.add(layer);
    }

    public void removeLayers(List<Layer> layer) {
        this.layers.removeAll(layer);
    }

    public void removeLayer(Layer layer) {
        this.layers.remove(layer);
    }

    public boolean hasLayer(Layer layer) {
        return this.layers.contains(layer);
    }

    public void clearLayers() {
        this.layers.clear();
    }

    public void loadBackground(String backgroundPath) {
        try {
            System.out.println("Loading background '" + backgroundPath + "'.");
            this.setBackgroundTexture(backgroundPath, GLTexture.fromInputStream(new FileInputStream(backgroundPath)));
        } catch (IOException e) {
            System.err.print("Couldn't load background texture '" + backgroundPath + "'. ");
            e.printStackTrace();
        }
    }

    public void loadLayerTextures() {
        for (Layer layer : layers) {
            if (layer.texture != null && layer.texture.isDeleted()) continue;

            try {
                System.out.println("loading layer texture " + layer.filename);
                layer.texture = GLTexture.fromInputStream(new FileInputStream(layer.folder + File.separator + layer.filename));
            } catch (IOException e) {
                System.err.println("Couldn't load layer '" + layer.filename + "', at folder '" + layer.folder + "', order " + layer.order + ". ");
                e.printStackTrace();
            }
        }
    }

    public int getSelectedCount() {
        int c = 0;

        for (RoomObject object : this.objects) {
            if (object.selected) {
                c++;
            }
        }

        return c;
    }

    public List<RoomObject> collectSelectedObjects(Consumer<RoomObject> consumer) {
        List<RoomObject> list = new ArrayList<>();

        for (RoomObject object : this.objects) {
            if (object.selected) {
                try {
                    list.add(object);
                    consumer.accept(object);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        return list;
    }

    public static void loadFromFile(DeltaCheapEditor editor, String selectedFile, Room room) {
        if (selectedFile == null) {
            System.err.println("file not selected!");
            return;
        }

        try {
            JsonObject object = JsonParser.parseReader(new FileReader(selectedFile)).getAsJsonObject();
            loadFromJson(editor, selectedFile, object, room);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromJson(DeltaCheapEditor editor, String filePath, JsonObject object, Room room) {
        room.clearLayers();
        room.clearObjects();
        room.setPath(filePath);

        room.setWidth(640);
        room.setHeight(480);

        room.setWidth(object.get("width").getAsInt());
        room.setHeight(object.get("height").getAsInt());

        String parent = StringUtil.substringToLastIndexOf(filePath, File.separator);

        JsonElement background = object.get("background");
        if (background instanceof JsonPrimitive f && f.isString()) {
            File backgroundToLoad = new File(parent, f.getAsString().substring(1));
            if (!backgroundToLoad.exists()) {
                throw new RuntimeException("Background file '" + backgroundToLoad.getAbsolutePath() + "' doesn't exist.");
            }
            editor.queueLoadBackground(room, backgroundToLoad.getAbsolutePath());
        }

        JsonElement layers = object.get("layers");
        if (layers instanceof JsonArray a && !a.isEmpty()) {
            for (JsonElement j : a) {
                if (!(j instanceof JsonObject obj)) {
                    continue;
                }

                room.addLayer(new Layer(obj.get("filename").getAsString(), obj.get("order").getAsString(), parent + "/layers/"));
            }
        }

        editor.queueLoadLayerTextures(room);

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
                room.addObject(exit);
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

                    // TODO: BAD BAD
                    room.addObject(Player.player);
                }
                case "InvisibleWall" -> {
                    InvisibleWall object1 = new InvisibleWall();
                    object1.x = Integer.parseInt(split[1]);
                    object1.y = Integer.parseInt(split[2]);
                    object1.setSize(Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                    room.addObject(object1);
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
                    room.addObject(object1);
                }
                case "RoomDoor" -> {
                    RoomDoor object1 = new RoomDoor("0unknown");
                    object1.x = Integer.parseInt(split[1]);
                    object1.y = Integer.parseInt(split[2]);
                    object1.setSize(Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                    object1.externalRoom = split[5].charAt(0) == '1';
                    object1.targetRoom = split[5].substring(1);
                    room.addObject(object1);
                }
            }
        }

        JsonElement editorElm = object.get("editor");
        if (editorElm instanceof JsonObject o) {
            JsonElement gridSize1 = o.get("gridSize");
            if (gridSize1 != null) {
                room.setGridSize(gridSize1.getAsInt());
            }
        }
    }

    public static JsonObject serialize(Room room) {
        JsonObject root = new JsonObject();

        JsonArray objects = new JsonArray();
        boolean player = false;
        for (RoomObject rm : room.getObjects()) {
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
        List<Layer> l = room.getLayers();
        if (l != null) {
            for (Layer layer : l) {
                JsonObject obj = new JsonObject();
                obj.addProperty("filename", layer.filename);
                obj.addProperty("order", layer.order);
                layers.add(obj);
            }
        }

        JsonArray exits = new JsonArray();
        for (RoomObject roomObject : room.getObjects()) {
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
        root.addProperty("background", "1" + (room.getPath() == null ? "null" : StringUtil.substringFromLastIndexOf(room.getPath(), File.separator)));
        root.addProperty("width", room.getWidth());
        root.addProperty("height", room.getHeight());
        root.add("layers", layers);
        root.add("exits", exits);

        JsonObject editorObj = new JsonObject();
        editorObj.addProperty("gridSize", room.gridSize);
        root.add("editor", editorObj);

        return root;
    }

    public void unselectAllObjects() {
        for (RoomObject object : this.objects) {
            object.selected = false;
        }
    }
}
