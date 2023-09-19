package dev.seeight.dtceditor;

import dev.seeight.renderer.renderer.gl.components.GLTexture;

public class Layer {
    public transient GLTexture texture;

    public String filename;
    public String order;
    public transient String folder;

    public Layer(String filename, String order, String folder) {
        this.filename = filename;
        this.order = order;
        this.folder = folder;
    }
}
