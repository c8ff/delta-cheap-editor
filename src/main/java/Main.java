import dev.seeight.common.lwjgl.Window;
import dev.seeight.common.lwjgl.nfd.FileChooserUtil;
import dev.seeight.dtceditor.DeltaCheapEditor;
import dev.seeight.renderer.renderer.gl.OpenGLRenderer2;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Main {
	public static void main(String[] args) {
		if (!GLFW.glfwInit()) {
			throw new RuntimeException("GLFW failed to initialized.");
		}

		Window window = new Window("Editor", 1280, 720);
		window.setVSync(1);
		window.createWindow();

		FileChooserUtil.initialize();

		GL.createCapabilities();

		OpenGLRenderer2 renderer = new OpenGLRenderer2();
		renderer.INVERT_V_COORDINATES = true;

		GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
		renderer.ortho(0F, window.getWidth(), window.getHeight(), 0F, 0F, 10F);

		DeltaCheapEditor editor = new DeltaCheapEditor(window, renderer);

		window.setFramebufferSizeCallback(new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long __, int width, int height) {
				GL11.glViewport(0, 0, width, height);
				renderer.ortho(0F, width, height, 0F, 0F, 10F);
				window.setSizeVariables(width, height);
				editor.framebufferSize(width, height);
			}
		});
		window.setWindowFocusCallback(new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				editor.windowFocus(focused);
			}
		});
		window.setMouseButtonCallback(new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				editor.mouseButton(button, action);
			}
		});
		window.setCursorPosCallback(new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				editor.cursorPosition(xpos, ypos);
			}
		});
		window.setKeyCallback(new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				editor.key(key, action, mods);
			}
		});
		window.setScrollCallback(new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				editor.scroll(xoffset, yoffset);
			}
		});
		window.setCharCallback(new GLFWCharCallback() {
			@Override
			public void invoke(long window, int codepoint) {
				editor.character(codepoint);
			}
		});

		while (!window.shouldClose()) {
			editor.render();
			GLFW.glfwPollEvents();
		}

		editor.shutdown();

		window.glfwFreeCallbacks();
		GL.destroy();
		FileChooserUtil.quit();
		window.destroy();
		GLFW.glfwTerminate();
	}
}
