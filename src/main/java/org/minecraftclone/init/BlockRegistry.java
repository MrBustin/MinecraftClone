package org.minecraftclone.init;

import org.minecraftclone.world.block.*;

import java.util.EnumMap;
import java.util.Map;

public class BlockRegistry {
    private static final int TILES_X = 16;
    private static final int TILES_Y = 16;

    private static final Map<Blocks, BlockDefinition> DEFINITIONS = new EnumMap<>(Blocks.class);

    static {
        register(Blocks.DIRT, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> tile(0, 0)
        ));

        register(Blocks.STONE, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> tile(1, 0)
        ));

        register(Blocks.GRASS, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> {
                    if (face == Face.BOTTOM) return tile(0, 0);
                    if (face == Face.TOP) return tile(3, 0);
                    return tile(2, 0);
                }
        ));

        register(Blocks.LOG, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> {
                    if (face == Face.TOP || face == Face.BOTTOM) return tile(6, 0);
                    return tile(5, 0);
                }
        ));

        register(Blocks.WATER, new BlockDefinition(
                BlockType.CUBE,
                true,
                false,
                (block, face) -> tile(0, 1)
        ));

        register(Blocks.FLOWER, new BlockDefinition(
                BlockType.CROSS,
                true,
                false,
                (block, face) -> tile(2, 1)
        ));

        register(Blocks.TALL_GRASS, new BlockDefinition(
                BlockType.CROSS,
                true,
                false,
                (block, face) -> tile(3, 1)
        ));

        register(Blocks.LEAVES, new BlockDefinition(
                BlockType.CUBE,
                true,
                true,
                (block, face) -> tile(1, 1)
        ));

        register(Blocks.SAND, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> tile(4, 0)
        ));

        register(Blocks.SNOW, new BlockDefinition(
                BlockType.CUBE,
                false,
                true,
                (block, face) -> tile(7, 0)
        ));

        register(Blocks.REEDS, new BlockDefinition(
                BlockType.CROSS,
                true,
                false,
                (block, face) -> tile(4, 1)
        ));
    }

    private static void register(Blocks block, BlockDefinition def) {
        DEFINITIONS.put(block, def);
    }

    public static BlockDefinition get(Blocks block) {
        return DEFINITIONS.get(block);
    }

    private static UVRect tile(int tileX, int tileY) {
        float tw = 1.0f / TILES_X;
        float th = 1.0f / TILES_Y;

        float u0 = tileX * tw;
        float v0 = tileY * th;
        float u1 = u0 + tw;
        float v1 = v0 + th;

        return new UVRect(u0, v0, u1, v1);
    }
}
