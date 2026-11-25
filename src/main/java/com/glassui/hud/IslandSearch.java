/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.music.MusicHub;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public final class IslandSearch {
    private static final int MAX = 80;
    private static final StringBuilder TEXT = new StringBuilder();
    private static boolean open;
    private static boolean typing;
    private static boolean hooked;
    private static long hookedWindow;
    private static GLFWKeyCallback prevKey;
    private static GLFWCharCallback prevChar;
    private static GLFWKeyCallback ownKey;
    private static GLFWCharCallback ownChar;

    private IslandSearch() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static boolean typing() {
        return open && typing;
    }

    public static String text() {
        return TEXT.toString();
    }

    public static void open() {
        open = true;
        typing = true;
        IslandSearch.hook();
        if (TEXT.length() == 0) {
            MusicHub.typing("");
        }
    }

    public static void close() {
        open = false;
        typing = false;
    }

    public static void focus() {
        typing = true;
    }

    private static void submit() {
        MusicHub.search(TEXT.toString());
    }

    private static void hook() {
        final GLFWCharCallback[] pc = new GLFWCharCallback[1];
        final GLFWKeyCallback[] pk = new GLFWKeyCallback[1];
        long win = GLFW.glfwGetCurrentContext();
        if (win == 0L) {
            return;
        }
        if (hooked) {
            if (win == hookedWindow) {
                return;
            }
            if (hookedWindow != 0L) {
                GLFWKeyCallback oldKey = GLFW.glfwSetKeyCallback(hookedWindow, prevKey);
                if (oldKey != null && oldKey == ownKey) {
                    oldKey.free();
                }
                GLFWCharCallback oldChar = GLFW.glfwSetCharCallback(hookedWindow, prevChar);
                if (oldChar != null && oldChar == ownChar) {
                    oldChar.free();
                }
            }
            hooked = false;
            hookedWindow = 0L;
            prevKey = null;
            prevChar = null;
            ownKey = null;
            ownChar = null;
        }
        hooked = true;
        hookedWindow = win;
        GLFWKeyCallback newKey = new GLFWKeyCallback() {
            public void invoke(long w, int key, int sc, int action, int mods) {
                if (IslandSearch.typing()) {
                    IslandSearch.onKey(w, key, action, mods);
                    return;
                }
                if (pk[0] != null) {
                    pk[0].invoke(w, key, sc, action, mods);
                }
            }
        };
        GLFWCharCallback newChar = new GLFWCharCallback() {
            public void invoke(long w, int cp) {
                if (IslandSearch.typing()) {
                    if (cp >= 32 && cp != 127 && TEXT.length() < 80) {
                        TEXT.appendCodePoint(cp);
                        MusicHub.typing(TEXT.toString());
                    }
                    return;
                }
                if (pc[0] != null) {
                    pc[0].invoke(w, cp);
                }
            }
        };
        pk[0] = GLFW.glfwSetKeyCallback((long)win, newKey);
        pc[0] = GLFW.glfwSetCharCallback((long)win, newChar);
        prevKey = pk[0];
        prevChar = pc[0];
        ownKey = newKey;
        ownChar = newChar;
    }

    private static void onKey(long w, int key, int action, int mods) {
        if (action != 1 && action != 2) {
            return;
        }
        switch (key) {
            case 256: {
                IslandSearch.close();
                break;
            }
            case 257: 
            case 335: {
                IslandSearch.submit();
                break;
            }
            case 259: {
                int len = TEXT.length();
                if (len <= 0) break;
                TEXT.delete(len - Character.charCount(TEXT.codePointBefore(len)), len);
                MusicHub.typing(TEXT.toString());
                break;
            }
            case 86: {
                String clip;
                if ((mods & 2) == 0 || (clip = GLFW.glfwGetClipboardString((long)w)) == null) break;
                clip = clip.replace("\r", " ").replace("\n", " ");
                TEXT.append(clip, 0, Math.min(clip.length(), Math.max(0, 80 - TEXT.length())));
                MusicHub.typing(TEXT.toString());
                break;
            }
        }
    }
}

