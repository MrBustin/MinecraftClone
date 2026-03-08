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

        float shadeTop = MeshWriter.SHADE_TOP;
        float shadeFrontBack = MeshWriter.SHADE_FRONT_BACK;
        float shadeLeftRight = MeshWriter.SHADE_LEFT_RIGHT;
        float shadeBottom = MeshWriter.SHADE_BOTTOM;

        if (self == Blocks.WATER) {
            shadeTop = 0.8f;
            shadeFrontBack = 0.95f;
            shadeLeftRight = 0.9f;
            shadeBottom = 0.8f;
        }

        int wx = ctx.worldX();
        int wy = ctx.y();
        int wz = ctx.worldZ();

        // FRONT (+Z)
        if (isFaceVisible(self, nFront)) {
            UVRect uv = ctx.definition().textures().get(self, Face.FRONT);

            float s00 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz + 1)
            ); // bottom-left

            float s10 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz + 1)
            ); // bottom-right

            float s11 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz + 1)
            ); // top-right

            float s01 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz + 1)
            ); // top-left

            MeshWriter.addFront(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }

        // BACK (-Z)
        if (isFaceVisible(self, nBack)) {
            UVRect uv = ctx.definition().textures().get(self, Face.BACK);

            float s00 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz - 1)
            ); // bottom-right

            float s10 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz - 1)
            ); // bottom-left

            float s11 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz - 1)
            ); // top-left

            float s01 = shadeFrontBack * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz - 1)
            ); // top-right

            MeshWriter.addBack(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }

        // LEFT (-X)
        if (isFaceVisible(self, nLeft)) {
            UVRect uv = ctx.definition().textures().get(self, Face.LEFT);

            float s00 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz - 1)
            ); // bottom-back

            float s10 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz + 1)
            ); // bottom-front

            float s11 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz + 1)
            ); // top-front

            float s01 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz - 1)
            ); // top-back

            MeshWriter.addLeft(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }

        // RIGHT (+X)
        if (isFaceVisible(self, nRight)) {
            UVRect uv = ctx.definition().textures().get(self, Face.RIGHT);

            float s00 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz + 1)
            ); // bottom-front

            float s10 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz - 1)
            ); // bottom-back

            float s11 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz - 1)
            ); // top-back

            float s01 = shadeLeftRight * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz + 1)
            ); // top-front

            MeshWriter.addRight(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }

        // TOP (+Y)
        if (isFaceVisible(self, nTop)) {
            UVRect uv = ctx.definition().textures().get(self, Face.TOP);

            float s00 = shadeTop * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz + 1)
            ); // front-left

            float s10 = shadeTop * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz + 1)
            ); // front-right

            float s11 = shadeTop * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy + 1, wz - 1)
            ); // back-right

            float s01 = shadeTop * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy + 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy + 1, wz - 1)
            ); // back-left

            MeshWriter.addTop(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }

        // BOTTOM (-Y)
        if (isFaceVisible(self, nBottom)) {
            UVRect uv = ctx.definition().textures().get(self, Face.BOTTOM);

            float s00 = shadeBottom * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz - 1)
            ); // back-left

            float s10 = shadeBottom * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz - 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz - 1)
            ); // back-right

            float s11 = shadeBottom * ao(
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx + 1, wy - 1, wz + 1)
            ); // front-right

            float s01 = shadeBottom * ao(
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz),
                    ctx.world().getBlockForMeshing(wx, wy - 1, wz + 1),
                    ctx.world().getBlockForMeshing(wx - 1, wy - 1, wz + 1)
            ); // front-left

            MeshWriter.addBottom(ctx.target(), ctx.x(), ctx.y(), ctx.z(), uv, s00, s10, s11, s01);
        }
    }

    private float ao(Blocks side1, Blocks side2, Blocks corner) {
        boolean s1 = occludes(side1);
        boolean s2 = occludes(side2);
        boolean c = occludes(corner);

        int blockers;
        if (s1 && s2) {
            blockers = 3;
        } else {
            blockers = (s1 ? 1 : 0) + (s2 ? 1 : 0) + (c ? 1 : 0);
        }

        return switch (blockers) {
            case 0 -> 1.0f;
            case 1 -> 0.85f;
            case 2 -> 0.7f;
            default -> 0.55f;
        };
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
