package org.minecraftclone.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    public Vector3f position = new Vector3f(0, 80, 3);

    private float yaw = -90f;   // looking toward -Z
    private float pitch = 0f;

    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up1 = new Vector3f(0, 1, 0);

    public Matrix4f getViewMatrix() {
        updateVectors();
        return new Matrix4f().lookAt(
                position,
                new Vector3f(position).add(front),
                up1
        );
    }

    private void updateVectors() {
        float cosPitch = (float) Math.cos(Math.toRadians(pitch));
        float sinPitch = (float) Math.sin(Math.toRadians(pitch));
        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));

        front.x = cosYaw * cosPitch;
        front.y = sinPitch;
        front.z = sinYaw * cosPitch;

        front.normalize();
    }

    public void processMouse(float dx, float dy) {
        float sensitivity = 0.1f;
        yaw += dx * sensitivity;
        pitch -= dy * sensitivity;

        if (pitch > 89f) pitch = 89f;
        if (pitch < -89f) pitch = -89f;
    }

    public void processKeyboard(boolean forward, boolean backward, boolean up, boolean down,
                                boolean left, boolean right, float dt) {

        float speed = 20.5f * dt;

        Vector3f rightVec = new Vector3f(front).cross(up1).normalize();

        if (forward) position.add(new Vector3f(front).mul(speed));
        if (backward) position.sub(new Vector3f(front).mul(speed));
        if (up) position.add(new Vector3f(up1).mul(speed));
        if (down) position.sub(new Vector3f(up1).mul(speed));
        if (left) position.sub(new Vector3f(rightVec).mul(speed));
        if (right) position.add(new Vector3f(rightVec).mul(speed));
    }

    public org.joml.Vector3f getPosition() {
        return new org.joml.Vector3f(position);
    }

    public org.joml.Vector3f getForward() {
        return new org.joml.Vector3f(front);
    }
}
