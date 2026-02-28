package org.minecraftclone.core;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private Window window;
    private Renderer renderer;

    public void run() {
        window = new Window(1280, 720, "LWJGL Starter");
        window.init();

        renderer = new Renderer();
        renderer.init();

        loop();

        renderer.cleanup();
        window.cleanup();
    }

    private void loop() {

        glfwSetInputMode(window.handle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        double lastTime = glfwGetTime();
        double lastMouseX = 0;
        double lastMouseY = 0;
        boolean firstMouse = true;

        boolean lastLeft = false;
        boolean lastRight = false;

        while (!window.shouldClose()) {

            // ---- Mouse Clicks (edge-triggered) ----
            boolean mouseLeft = glfwGetMouseButton(window.handle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
            boolean mouseRight = glfwGetMouseButton(window.handle(), GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

            if (mouseLeft && !lastLeft) renderer.onBreakBlock();
            if (mouseRight && !lastRight) renderer.onPlaceBlock();

            lastLeft = mouseLeft;
            lastRight = mouseRight;

            // ---- Delta Time ----
            double now = glfwGetTime();
            float dt = (float) (now - lastTime);
            lastTime = now;

            // ---- Mouse Look ----
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            glfwGetCursorPos(window.handle(), xPos, yPos);

            if (firstMouse) {
                lastMouseX = xPos[0];
                lastMouseY = yPos[0];
                firstMouse = false;
            }

            float dx = (float) (xPos[0] - lastMouseX);
            float dy = (float) (yPos[0] - lastMouseY);

            lastMouseX = xPos[0];
            lastMouseY = yPos[0];

            renderer.getCamera().processMouse(dx, dy);

            // ---- Keyboard Movement ----
            boolean forward  = glfwGetKey(window.handle(), GLFW_KEY_W) == GLFW_PRESS;
            boolean backward = glfwGetKey(window.handle(), GLFW_KEY_S) == GLFW_PRESS;
            boolean left     = glfwGetKey(window.handle(), GLFW_KEY_A) == GLFW_PRESS;
            boolean right    = glfwGetKey(window.handle(), GLFW_KEY_D) == GLFW_PRESS;

            renderer.getCamera().processKeyboard(forward, backward, left, right, dt);

            if (glfwGetKey(window.handle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                glfwSetWindowShouldClose(window.handle(), true);
            }

            renderer.beginFrame();
            renderer.endFrame(window);
        }
    }
}
