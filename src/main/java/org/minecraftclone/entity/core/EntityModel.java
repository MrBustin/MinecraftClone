package org.minecraftclone.entity.core;

import org.minecraftclone.gfx.Mesh;
import org.minecraftclone.gfx.Texture;

public abstract class EntityModel {
    protected final Mesh mesh;
    protected final Texture texture;

    protected EntityModel(float[] vertices, Texture texture) {
        this.mesh = new Mesh(vertices);
        this.texture = texture;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Texture getTexture(){
        return texture;
    }

    public void cleanup() {
        mesh.cleanup();
        texture.cleanup();
    }
}
