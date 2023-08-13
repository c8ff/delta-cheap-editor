package dev.seeight.dtceditor.utils;

import com.google.gson.Gson;
import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.common.lwjgl.font.json.FontData;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.gl.components.GLTexture;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtil {
	public static GLTexture textureFromPath(Object instance, String path) throws IOException {
		return GLTexture.fromInputStream(resource(instance, path));
	}

	public static FontRenderer fontFromPath(Renderer renderer, Gson gson, Object instance, String path) throws IOException {
		String checkPath = StringUtil.checkPath(path);
		return new FontRenderer(
				renderer,
				gson.fromJson(readerFromResource(instance, checkPath + "/data.json"), FontData.class),
				textureFromPath(instance, checkPath + "/font.png")
		);
	}

	public static InputStream resource(Object instance, String resourceName) throws NullPointerException {
		InputStream resourceAsStream = instance.getClass().getResourceAsStream(resourceName);

		if (resourceAsStream == null) {
			throw new NullPointerException("Resource not found: " + resourceName);
		}

		return resourceAsStream;
	}

	public static InputStreamReader readerFromResource(Object instance, String resourceName) throws NullPointerException {
		return new InputStreamReader(resource(instance, resourceName));
	}

}
