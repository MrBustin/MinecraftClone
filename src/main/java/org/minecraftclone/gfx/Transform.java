package org.minecraftclone.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Transform {
    public final Vector3f position = new Vector3f();
    public final Vector3f rotation = new Vector3f(); // radians
    public final Vector3f scale    = new Vector3f(1, 1, 1);

    public Matrix4f toMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);
    }
}
