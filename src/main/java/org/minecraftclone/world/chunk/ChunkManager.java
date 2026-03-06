package org.minecraftclone.world.chunk;

import org.minecraftclone.world.BlockType;
import org.minecraftclone.world.FastNoiseLite;
import org.minecraftclone.world.Perlin2D;
import org.minecraftclone.world.placedfeatures.TreeFeature;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, Chunk> chunks = new HashMap<>();
    private final FastNoiseLite noise = new FastNoiseLite();
    private final Perlin2D foliageNoise = new Perlin2D(54321L);


    // Foliage
    private final TreeFeature treeFeature = new TreeFeature();
    private final java.util.Map<Long, java.util.ArrayList<PendingBlock>> pending = new java.util.HashMap<>();
    private record PendingBlock(int wx, int wy, int wz, BlockType type) {}

    public Chunk getOrCreate(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.computeIfAbsent(key, k -> {
            Chunk c = new Chunk(cx, cz);
            generateTerrain(c);
            applyPending(c);
            Chunk left  = getIfLoaded(cx - 1, cz);
            Chunk right = getIfLoaded(cx + 1, cz);
            Chunk back  = getIfLoaded(cx, cz - 1);
            Chunk front = getIfLoaded(cx, cz + 1);

            if (left  != null) left.markDirty();
            if (right != null) right.markDirty();
            if (back  != null) back.markDirty();
            if (front != null) front.markDirty();
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
        FastNoiseLite baseNoise = new FastNoiseLite();
        baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        baseNoise.SetFrequency(0.005f);

        FastNoiseLite mountainNoise = new FastNoiseLite();
        mountainNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        mountainNoise.SetFrequency(0.005f);

        FastNoiseLite mountainMaskNoise = new FastNoiseLite();
        mountainMaskNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        mountainMaskNoise.SetFrequency(0.002f);

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = c.cx() * Chunk.SIZE + x;
                int worldZ = c.cz() * Chunk.SIZE + z;

                float base = baseNoise.GetNoise(worldX, worldZ);
                float mountain = mountainNoise.GetNoise(worldX, worldZ);
                float mask = mountainMaskNoise.GetNoise(worldX, worldZ);

                float baseHeight = base * 12f + 45f;

                mountain = Math.abs(mountain);
                mountain = (float)Math.pow(mountain, 1.8f);

                mask = (mask + 1f) * 0.5f;
                mask = Math.max(0f, mask - 0.45f) / 0.55f;

                float uplift = mask * 18f;
                float peakiness = mountain * mask * 22f;

                int height = (int)(baseHeight + uplift + peakiness);

                for (int y = 0; y <= height && y < Chunk.HEIGHT; y++) {
                    c.set(x, y, z, BlockType.STONE);
                }
            }
        }

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                boolean foundSurface = false;
                int dirtLeft = 3;

                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    BlockType block = c.get(x, y, z);

                    if (block == BlockType.AIR || block == BlockType.WATER) continue;

                    if (!foundSurface) {
                        c.set(x, y, z, BlockType.GRASS);
                        foundSurface = true;
                    } else if (dirtLeft > 0) {
                        c.set(x, y, z, BlockType.DIRT);
                        dirtLeft--;
                    } else {
                        c.set(x, y, z, BlockType.STONE);
                    }
                }
            }
        }

        int seaLevel = 40;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                for (int y = 0; y < seaLevel && y < Chunk.HEIGHT; y++) {
                    if (c.get(x, y, z) == BlockType.AIR) {
                        c.set(x, y, z, BlockType.WATER);
                    }
                }
            }
        }

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = c.cx() * Chunk.SIZE + x;
                int worldZ = c.cz() * Chunk.SIZE + z;

                int surfaceY = -1;
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    if (c.get(x, y, z) == BlockType.GRASS) {
                        surfaceY = y;
                        break;
                    }
                }

                if (surfaceY == -1 || surfaceY < seaLevel) continue;

                float r = rand01(worldX, worldZ, 1337);
                if (r < 0.005f) {
                    treeFeature.place(this, c, worldX, surfaceY + 1, worldZ);
                }
            }
        }

        c.clearDirty();
    }
    public void setBlockIfLoaded(int wx, int wy, int wz, BlockType type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return; // DON'T create chunks during generation

        c.set(lx, wy, lz, type);
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

    public void setBlock(int wx, int wy, int wz, BlockType type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getOrCreate(cx, cz);
        c.set(lx, wy, lz, type);

        // if we changed a block on a chunk edge, neighbor chunk mesh may need rebuild too
        if (lx == 0) markDirtyIfLoaded(cx - 1, cz);
        if (lx == Chunk.SIZE - 1) markDirtyIfLoaded(cx + 1, cz);
        if (lz == 0) markDirtyIfLoaded(cx, cz - 1);
        if (lz == Chunk.SIZE - 1) markDirtyIfLoaded(cx, cz + 1);
    }

    private void markDirtyIfLoaded(int cx, int cz) {
        Chunk n = getIfLoaded(cx, cz);
        if (n != null) {
            n.markDirty();
        }
    }

    public BlockType getBlockForMeshing(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return BlockType.STONE; // anything non-air: prevents border faces

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);
        return c.get(lx, wy, lz);
    }

    private static int hash2D(int x, int z, int seed) {
        int h = x * 374761393 + z * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    private static float rand01(int x, int z, int seed) {
        return (hash2D(x, z, seed) & 0xFFFFFF) / (float) 0x1000000;
    }

    public void queueBlock(int wx, int wy, int wz, BlockType type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);
        long key = new ChunkPos(cx, cz).key();
        pending.computeIfAbsent(key, k -> new java.util.ArrayList<>())
                .add(new PendingBlock(wx, wy, wz, type));
    }

    private void applyPending(Chunk c) {
        long key = new ChunkPos(c.cx(), c.cz()).key();
        var list = pending.remove(key);
        if (list == null) return;

        for (var pb : list) {
            int lx = mod(pb.wx(), Chunk.SIZE);
            int lz = mod(pb.wz(), Chunk.SIZE);
            if (pb.wy() < 0 || pb.wy() >= Chunk.HEIGHT) continue;
            c.set(lx, pb.wy(), lz, pb.type());
        }
        c.markDirty();
    }
}
