package org.minecraftclone.world;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, Chunk> chunks = new HashMap<>();

    public Chunk getOrCreate(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.computeIfAbsent(key, k -> {
            Chunk c = new Chunk(cx, cz);
            generateFlat(c);
            return c;
        });
    }

    public Chunk getIfLoaded(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.get(key);
    }

    public BlockType getBlockIfLoaded(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return BlockType.AIR; // missing chunk treated as air

        return c.get(lx, wy, lz);
    }

    // TEMP generator: flat terrain
    private void generateFlat(Chunk c) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                c.set(x, 0, z, BlockType.STONE);
                c.set(x, 1, z, BlockType.DIRT);
                c.set(x, 2, z, BlockType.DIRT);
            }
        }
        c.clearDirty();
    }

    // Global block lookup (handles chunk boundaries)
    public BlockType getBlock(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getOrCreate(cx, cz);
        return c.get(lx, wy, lz);
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    private static int mod(int a, int b) {
        int m = a % b;
        if (m < 0) m += b;
        return m;
    }

    public Iterable<Chunk> loadedChunks() {
        return chunks.values();
    }
}
