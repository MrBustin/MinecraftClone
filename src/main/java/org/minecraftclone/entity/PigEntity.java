package org.minecraftclone.entity;

import org.minecraftclone.entity.core.EntityModel;
import org.minecraftclone.entity.core.LivingEntity;
import org.minecraftclone.entity.model.DebugCubeModel;
import org.minecraftclone.entity.model.PigModel;
import org.minecraftclone.world.chunk.ChunkManager;

public class PigEntity extends LivingEntity {

    private static final PigModel model = new PigModel();

    public PigEntity(ChunkManager world, double x, double y, double z) {
        super(world, x, y, z, 0.9f, 0.9f, 10f, 0.08f, 0.035f, model);
    }
}
