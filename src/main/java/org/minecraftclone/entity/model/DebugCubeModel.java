package org.minecraftclone.entity.model;

import org.minecraftclone.entity.core.EntityModel;
import org.minecraftclone.entity.core.ModelBuilder;
import org.minecraftclone.entity.core.UV;
import org.minecraftclone.gfx.Texture;

public class DebugCubeModel extends EntityModel {
    private static final Texture texture = new Texture("/textures/entity/debug_cube.png");

    public DebugCubeModel() {
        super(
                new ModelBuilder()
                        .box(-0.3f, 0.0f, -0.3f, 0.6f, 0.6f, 0.6f,  new UV(0f, 0f, 1f, 1f))
                        .build(), texture

        );
    }
}