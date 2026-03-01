package org.minecraftclone.world;

import org.minecraftclone.world.placedfeatures.TreeFeature;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, Chunk> chunks = new HashMap<>();
    private final Perlin2D noise = new Perlin2D(12345L);
    private final Perlin2D foliageNoise = new Perlin2D(54321L);
    private final Noise3D caveNoise = new Noise3D(12345L);


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
        int baseX = c.cx() * Chunk.SIZE;
        int baseZ = c.cz() * Chunk.SIZE;

        final int seaLevel = 24;   // baseline height
        final int maxHeight = Chunk.HEIGHT - 1;
        final int dirtDepth = 2;
        boolean cavesDone = false;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;

                // scale controls how “wide” hills are
                double n = noise.fbm(wx * 0.008, wz * 0.008, 5, 2.0, 0.5);

                // amplitude controls hill height
                int height = (int) Math.round(seaLevel + n * 26);

                if (height < 1) height = 1;
                if (height > maxHeight) height = maxHeight;

                // build column 0..height
                for (int y = 0; y <= height; y++) {
                    if (y == height) {
                        c.set(x, y, z, (y <= seaLevel + 1) ? BlockType.SAND : BlockType.GRASS);
                    } else if (y >= height - dirtDepth) {
                        c.set(x, y, z, BlockType.DIRT);
                    } else {
                        c.set(x, y, z, BlockType.STONE);
                    }
                }

                // fill water ABOVE ground up to seaLevel
                if (height < seaLevel) {
                    int waterTop = Math.min(seaLevel, maxHeight);
                    for (int y = height + 1; y <= waterTop; y++) {
                        c.set(x, y, z, BlockType.WATER);
                    }
                }

                // ---- Simple foliage template (LOG only) ----

                if (height >= seaLevel) { // don't place underwater

                    double f = foliageNoise.noise(wx * 0.03, wz * 0.03);
                    final int cell = 6; // spacing: 8 = fewer clumps, try 10/12 for even fewer

                    int cellX = Math.floorDiv(wx, cell);
                    int cellZ = Math.floorDiv(wz, cell);

                    int h = hash2D(cellX, cellZ, 9001);
                    int ox = Math.floorMod(h, cell);
                    int oz = Math.floorMod(h >>> 8, cell);

                    int candX = cellX * cell + ox;
                    int candZ = cellZ * cell + oz;

                    if (wx != candX || wz != candZ) continue; // only 1 spot per cell can place a log
                    double density = (f + 1.0) * 0.5; // convert [-1,1] -> [0,1]

                    if (c.get(x, height, z) == BlockType.GRASS) {

                        // simple threshold
                        if (density > 0.45) {

                            // deterministic pseudo-random thinning
                            float r = rand01(wx, wz, 1337);

                            if (r < 0.15f) {
                                treeFeature.place(this, c, wx, height + 1, wz);
                            }
                        }
                    }
                }
            }
        }

        CaveCarver.carveChunk(this, c, 12345L); // use your world seed
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
