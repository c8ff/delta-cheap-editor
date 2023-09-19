package dev.seeight.dtceditor.mgr;

import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class TextureManager {
    /**
     * A hashmap that holds every texture by their path location
     */
    private final HashMap<String, Texture> textures = new HashMap<>();

    public @NotNull Texture get(@NotNull String path) {
        return this.get(path, true, GL11.GL_NEAREST);
    }

    public @NotNull Texture get(@NotNull String path, boolean fromJar) {
        return this.get(path, fromJar, GL11.GL_NEAREST);
    }

    /**
     * @param path    The path of the texture. If {@code fromJar} is true, the path must be inside '/assets/textures/'.
     * @param fromJar Specifies if the texture is located inside the program or outside.
     * @param filter  The filter for the texture.
     * @return The specified texture located on {@code path}
     */
    public @NotNull Texture get(@NotNull String path, boolean fromJar, int filter) {
        Texture texture = textures.get(path);

        if (texture == null) {
            GLTexture.DEFAULT_TEXTURE_MAG_FILTER = filter;
            GLTexture.DEFAULT_TEXTURE_MIN_FILTER = filter;
            texture = new GLTexture((fromJar ? "" : "") + path, fromJar);
            textures.put(path, texture);
            GLTexture.DEFAULT_TEXTURE_MAG_FILTER = GL11.GL_NEAREST;
            GLTexture.DEFAULT_TEXTURE_MIN_FILTER = GL11.GL_NEAREST;
        }

        return texture;
    }

    public @NotNull Texture get(@NotNull String path, @NotNull Callable<@NotNull InputStream> streamSupplier) throws Exception {
        Texture texture = this.textures.get(path);

        if (texture == null) {
            texture = GLTexture.fromInputStream(streamSupplier.call());
            this.textures.put(path, texture);
        }

        return texture;
    }

    public void delete(@NotNull String path) {
        Texture remove = textures.remove(path);
        if (remove instanceof GLTexture glTexture) {
            glTexture.delete();
        }
    }
}
