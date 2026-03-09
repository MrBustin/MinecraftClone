package org.minecraftclone.entity;

import org.minecraftclone.entity.model.DebugCubeModel;
import org.minecraftclone.world.chunk.ChunkManager;

public class TestEntity extends LivingEntity{

    private static final DebugCubeModel MODEL = new DebugCubeModel();

    public TestEntity(ChunkManager world, double x, double y, double z) {
        super(world, x, y, z, 0.6f, 1.8f, 20f, 0.1f, 0.42f, MODEL);
    }
}
