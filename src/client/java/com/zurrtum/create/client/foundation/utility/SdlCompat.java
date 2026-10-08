package com.zurrtum.create.client.foundation.utility;

import org.lwjgl.sdl.SDLMouse;

import java.nio.FloatBuffer;

/**
 * Remplace les appels GLFW directs (GLFW n'est plus fourni en 26.3, Minecraft passe par SDL).
 * Les numeros de boutons de souris sont ceux de {@code InputConstants.MOUSE_BUTTON_*} (base 1, SDL).
 */
public final class SdlCompat {
    private SdlCompat() {
    }

    public static void setCursorPos(long windowHandle, double x, double y) {
        SDLMouse.SDL_WarpMouseInWindow(windowHandle, (float) x, (float) y);
    }

    public static void hideCursor() {
        SDLMouse.SDL_HideCursor();
    }

    public static void showCursor() {
        SDLMouse.SDL_ShowCursor();
    }

    public static boolean isMouseButtonDown(int button) {
        int state = SDLMouse.SDL_GetMouseState((FloatBuffer) null, (FloatBuffer) null);
        return (state & (1 << (button - 1))) != 0;
    }
}
