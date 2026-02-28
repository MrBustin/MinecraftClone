package org.minecraftclone.world;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, Chunk> chunks = new HashMap<>();
    private static final int STONE_DEPTH = 1;
    private final Perlin2D noise = new Perlin2D(12345L);

    public Chunk getOrCreate(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.computeIfAbsent(key, k -> {
            Chunk c = new Chunk(cx, cz);
            generateTerrain(c);
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

    private void generateTerrain(Chunk c) {
        int baseX = c.cx() * Chunk.SIZE;
        int baseZ = c.cz() * Chunk.SIZE;

        final int seaLevel = 24;   // baseline height
        final int maxHeight = Chunk.HEIGHT - 1;
        final int dirtDepth = 4;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;

                // scale controls how “wide” hills are
                double n = noise.fbm(wx * 0.01, wz * 0.01, 4, 2.0, 0.5);

                // amplitude controls hill height
                int height = (int) Math.round(seaLevel + n * 12);

                if (height < 1) height = 1;
                if (height > maxHeight) height = maxHeight;

                // build column 0..height
                for (int y = 0; y <= height; y++) {
                    if (y == height) {
                        c.set(x, y, z, BlockType.GRASS);
                    } else if (y >= height - dirtDepth) {
                        c.set(x, y, z, BlockType.DIRT);
                    } else {
                        c.set(x, y, z, BlockType.STONE);
                    }
                }
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

    //Infinite Chunk Rendering

    public boolean unload(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.remove(key) != null;
    }

    public java.util.Collection<Chunk> loadedChunksSnapshot() {
        return new java.util.ArrayList<>(chunks.values());
    }

    public void ensureLoadedAround(int centerCx, int centerCz, int viewDistance) {
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                getOrCreate(centerCx + dx, centerCz + dz);
            }
        }
    }

    public java.util.List<Long> unloadOutsideRadius(int centerCx, int centerCz, int viewDistance) {
        int maxDist = viewDistance + 1; // a small buffer to reduce churn
        int maxDistSq = maxDist * maxDist;

        java.util.ArrayList<Long> unloadedKeys = new java.util.ArrayList<>();

        // iterate snapshot so removing is safe
        for (Chunk c : loadedChunksSnapshot()) {
            int dx = c.cx() - centerCx;
            int dz = c.cz() - centerCz;
            int distSq = dx * dx + dz * dz;

            if (distSq > maxDistSq) {
                long key = new ChunkPos(c.cx(), c.cz()).key();
                chunks.remove(key);
                unloadedKeys.add(key);
            }
        }

        return unloadedKeys;
    }
}
