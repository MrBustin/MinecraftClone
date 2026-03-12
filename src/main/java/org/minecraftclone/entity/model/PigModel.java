package org.minecraftclone.entity.model;

import org.minecraftclone.entity.core.BoxUV;
import org.minecraftclone.entity.core.EntityModel;
import org.minecraftclone.entity.core.ModelBuilder;
import org.minecraftclone.entity.core.UVs;
import org.minecraftclone.gfx.Texture;

public class PigModel extends EntityModel {

    private static final Texture texture = new Texture("/textures/entity/pig.png");

    public PigModel() {
        super(
                new ModelBuilder()
                        .box(-0.22f, 0.6f,  0.55f, 0.55f, 0.5f, 0.5f, headUV) // head
                        .box(-0.33f, 0.45f,  -0.45f, 0.75f, 0.5f, 1.1f)   // body
                        .box(-0.28f, 0.0f, -0.5f, 0.25f, 0.45f, 0.3f) // back left leg
                        .box( 0.13f, 0.0f, -0.5f, 0.25f, 0.45f, 0.3f) // back right leg
                        .box(-0.28f, 0.0f,  0.30f, 0.25f, 0.45f, 0.25f) // front left leg
                        .box( 0.13f, 0.0f,  0.30f, 0.25f, 0.45f, 0.25f) // front right leg
                        .build(), texture
        );
    }

    private static final BoxUV headUV = new BoxUV(
            UVs.pixels(8, 8, 8, 8, 64, 32),   // front
            UVs.pixels(8, 0, 8, 8, 64, 32),   // back
            UVs.pixels(0, 8, 8, 8, 64, 32),  // left
            UVs.pixels(16, 8, 8, 8, 64, 32),  // right
            UVs.pixels(0, 8, 8, 8, 64, 32),   // top
            UVs.pixels(8, 8, 8, 8, 64, 32)    // bottom
    );
}
