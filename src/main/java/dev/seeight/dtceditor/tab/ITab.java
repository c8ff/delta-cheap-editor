package dev.seeight.dtceditor.tab;

import dev.seeight.dtceditor.StuffListener;

public interface ITab extends StuffListener {
    default void prerender() {

    }

    default void render() {

    }

    @Override
    default void windowFocus(boolean windowFocus) {

    }

    @Override
    default void framebufferSize(int width, int height) {

    }

    @Override
    default void mouseButton(int button, int action) {

    }

    @Override
    default void cursorPosition(double x, double y) {

    }

    @Override
    default void key(int key, int action, int mods) {

    }

    @Override
    default void scroll(double x, double y) {

    }

    @Override
    default void character(int codepoint) {

    }
}
