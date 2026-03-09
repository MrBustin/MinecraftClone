package org.minecraftclone.entity.model;

import org.joml.Matrix4f;
import org.minecraftclone.core.Renderer;
import org.minecraftclone.gfx.Mesh;

public abstract class EntityModel {
    protected final Mesh mesh;

    protected EntityModel(float[] vertices) {
        this.mesh = new Mesh(vertices);
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void cleanup() {
        mesh.cleanup();
    }
}
