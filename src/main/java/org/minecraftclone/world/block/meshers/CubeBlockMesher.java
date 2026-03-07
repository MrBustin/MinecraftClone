package org.minecraftclone.world.block.meshers;

import org.minecraftclone.init.BlockRegistry;
import org.minecraftclone.world.block.*;

public final class CubeBlockMesher implements BlockMesher {

    @Override
    public void build(BlockMeshContext ctx) {
        Blocks self = ctx.block();

        Blocks nFront  = ctx.world().getBlockForMeshing(ctx.worldX(), ctx.y(), ctx.worldZ() + 1);
        Blocks nBack   = ctx.world().getBlockForMeshing(ctx.worldX(), ctx.y(), ctx.worldZ() - 1);
        Blocks nLeft   = ctx.world().getBlockForMeshing(ctx.worldX() - 1, ctx.y(), ctx.worldZ());
        Blocks nRight  = ctx.world().getBlockForMeshing(ctx.worldX() + 1, ctx.y(), ctx.worldZ());
        Blocks nTop    = ctx.world().getBlockForMeshing(ctx.worldX(), ctx.y() + 1, ctx.worldZ());
        Blocks nBottom = ctx.world().getBlockForMeshing(ctx.worldX(), ctx.y() - 1, ctx.worldZ());

        if (isFaceVisible(self, nFront)) {
            UVRect uv = ctx.definition().textures().get(self, Face.FRONT);
            MeshWriter.addFront(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
        if (isFaceVisible(self, nBack)) {
            UVRect uv = ctx.definition().textures().get(self, Face.BACK);
            MeshWriter.addBack(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
        if (isFaceVisible(self, nLeft)) {
            UVRect uv = ctx.definition().textures().get(self, Face.LEFT);
            MeshWriter.addLeft(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
        if (isFaceVisible(self, nRight)) {
            UVRect uv = ctx.definition().textures().get(self, Face.RIGHT);
            MeshWriter.addRight(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
        if (isFaceVisible(self, nTop)) {
            UVRect uv = ctx.definition().textures().get(self, Face.TOP);
            MeshWriter.addTop(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
        if (isFaceVisible(self, nBottom)) {
            UVRect uv = ctx.definition().textures().get(self, Face.BOTTOM);
            MeshWriter.addBottom(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv);
        }
    }

    private boolean isFaceVisible(Blocks self, Blocks neighbor) {
        if (neighbor == Blocks.AIR) return true;

        if (self == Blocks.WATER) {
            // only render water faces against non-water
            return false;
        }

        return !occludes(neighbor);
    }

    private boolean occludes(Blocks t) {
        if (t == Blocks.AIR) return false;

        BlockDefinition def = BlockRegistry.get(t);
        return def != null && def.occludesFaces();
    }
}
