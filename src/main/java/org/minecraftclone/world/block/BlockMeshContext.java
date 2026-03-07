package org.minecraftclone.world.block;

import org.minecraftclone.gfx.FloatList;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

public record BlockMeshContext(
        ChunkManager world,
        Chunk chunk,
        FloatList target,
        Blocks block,
        BlockDefinition definition,
        int x, int y, int z,
        int worldX, int worldZ
) {}
