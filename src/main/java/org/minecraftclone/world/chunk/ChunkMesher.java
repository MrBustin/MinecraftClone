package org.minecraftclone.world.chunk;

import org.minecraftclone.gfx.FloatList;
import org.minecraftclone.init.BlockRegistry;
import org.minecraftclone.world.block.*;
import org.minecraftclone.world.block.meshers.CrossBlockMesher;
import org.minecraftclone.world.block.meshers.CubeBlockMesher;

import java.util.Map;

public final class ChunkMesher {

    public record ChunkMeshData(float[] solid, float[] water) {}

    private static final Map<BlockType, BlockMesher> MESHERS = Map.of(
            BlockType.CUBE, new CubeBlockMesher(),
            BlockType.CROSS, new CrossBlockMesher()
    );

    public static ChunkMeshData buildMesh(ChunkManager world, Chunk chunk) {
        FloatList solid = new FloatList();
        FloatList water = new FloatList();

        int baseX = chunk.cx() * Chunk.SIZE;
        int baseZ = chunk.cz() * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Blocks block = chunk.get(x, y, z);
                    if (block == Blocks.AIR) continue;

                    BlockDefinition def = BlockRegistry.get(block);
                    if (def == null) continue;

                    FloatList target = (block == Blocks.WATER) ? water : solid;

                    int wx = baseX + x;
                    int wz = baseZ + z;

                    BlockMesher mesher = MESHERS.get(def.modelType());
                    if (mesher == null) continue;

                    mesher.build(new BlockMeshContext(
                            world, chunk, target, block, def,
                            x, y, z, wx, wz
                    ));
                }
            }
        }

        return new ChunkMeshData(solid.toArray(), water.toArray());
    }
}