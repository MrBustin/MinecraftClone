package org.minecraftclone.core;


import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

public class Window {
    private final int width;
    private final int height;
    private final String title;
    private long handle;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure hints BEFORE creating the window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // --- Fullscreen setup ---
        long monitor = glfwGetPrimaryMonitor();
        if (monitor == 0L) {
            throw new IllegalStateException("No primary monitor found (GLFW not initialized?)");
        }

        GLFWVidMode vid = glfwGetVideoMode(monitor);
        if (vid == null) {
            throw new IllegalStateException("Could not get video mode for monitor");
        }

        // TRUE fullscreen
        handle = glfwCreateWindow(vid.width(), vid.height(), title, monitor, 0L);

        if (handle == 0L) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // vsync
        glfwShowWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public long handle() {
        return handle;
    }

    public void cleanup() {
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}
