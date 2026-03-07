package org.minecraftclone.world.block.meshers;

import org.minecraftclone.world.block.BlockMeshContext;
import org.minecraftclone.world.block.BlockMesher;
import org.minecraftclone.world.block.Face;
import org.minecraftclone.world.block.UVRect;

public final class CrossBlockMesher implements BlockMesher {

    @Override
    public void build(BlockMeshContext ctx) {
        UVRect uv = ctx.definition().textures().get(ctx.block(), Face.FRONT);
        MeshWriter.addCrossPlant(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
    }
}
