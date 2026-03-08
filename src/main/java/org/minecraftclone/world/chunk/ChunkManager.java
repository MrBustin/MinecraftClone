package org.minecraftclone.world.chunk;

import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.FastNoiseLite;
import org.minecraftclone.world.Perlin2D;
import org.minecraftclone.world.placedfeatures.GroundFoliageFeature;
import org.minecraftclone.world.placedfeatures.TreeFeature;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, Chunk> chunks = new HashMap<>();

    private final FastNoiseLite continentalNoise = new FastNoiseLite(1001);
    private final FastNoiseLite erosionNoise = new FastNoiseLite(2002);
    private final FastNoiseLite pvNoise = new FastNoiseLite(3003);
    private final FastNoiseLite shoreNoise = new FastNoiseLite(4004);

    // Foliage
    private final TreeFeature treeFeature = new TreeFeature();
    private final GroundFoliageFeature groundFoliageFeature = new GroundFoliageFeature();
    private final java.util.Map<Long, java.util.ArrayList<PendingBlock>> pending = new java.util.HashMap<>();
    private record PendingBlock(int wx, int wy, int wz, Blocks type) {}

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

    public Blocks getBlockIfLoaded(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return Blocks.AIR; // missing chunk treated as air

        return c.get(lx, wy, lz);
    }

    private void generateTerrain(Chunk c) {
        //
        //Noise Maps
        //
        continentalNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        continentalNoise.SetFrequency(0.0025f);

        erosionNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        erosionNoise.SetFrequency(0.0035f);

        pvNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        pvNoise.SetFrequency(0.0045f);

        shoreNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        shoreNoise.SetFrequency(0.025f);

        int seaLevel = 40;
        int treeHeight = 70;
        int stoneHeight = 75;
        int snowHeight = stoneHeight + 45;
        //
        //Base Terrain Noise
        //
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = c.cx() * Chunk.SIZE + x;
                int worldZ = c.cz() * Chunk.SIZE + z;

                float continental = continentalNoise.GetNoise(worldX, worldZ);
                float erosion = erosionNoise.GetNoise(worldX, worldZ);
                float pv = pvNoise.GetNoise(worldX, worldZ);

                float baseHeight = sampleContinentalness(continental);

                float pvShape = samplePeaksValleys(pv);
                float ruggedness = sampleErosionFactor(erosion);

                // make mountains more likely inland than near coasts
                float inlandness = Math.max(0f, (continental + 0.15f) / 0.85f);
                inlandness = Math.min(inlandness, 1f);

                // final mountain contribution
                float mountainHeight = pvShape * ruggedness * inlandness * 85f;

                int height = (int)(baseHeight + mountainHeight);

                float erosion01 = (erosion + 1f) * 0.5f; // 0..1
                float shoreStrength = lerp(4f, 1.5f, erosion01);
                float shore = shoreNoise.GetNoise(worldX, worldZ) * shoreStrength;

                float distFromSea = Math.abs(height - seaLevel);
                float shoreWeight = 1f - Math.min(distFromSea / 8f, 1f);
                shoreWeight = shoreWeight * shoreWeight * (3f - 2f * shoreWeight);

                height += (int)(shore * shoreWeight);

                for (int y = 0; y <= height && y < Chunk.HEIGHT; y++) {
                    c.set(x, y, z, Blocks.STONE);
                }
            }
        }

        //
        // Surface Layer
        //
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                boolean foundSurface = false;
                int fillerLeft = 0;
                Blocks fillerBlock = Blocks.STONE;

                int worldX = c.cx() * Chunk.SIZE + x;
                int worldZ = c.cz() * Chunk.SIZE + z;

                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    Blocks block = c.get(x, y, z);

                    if (block == Blocks.AIR || block == Blocks.WATER) continue;

                    if (!foundSurface) {
                        float stone = rand01(worldX, worldZ, 1337);
                        float snow = rand01(worldX, worldZ, 7647);

                        Blocks topBlock;

                        if (y > snowHeight) {
                            topBlock = Blocks.SNOW;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else if (y > snowHeight - 6 && snow > 0.75f) {
                            topBlock = Blocks.SNOW;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else if (y > snowHeight - 3 && snow > 0.15f) {
                            topBlock = Blocks.SNOW;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else if (y > stoneHeight - 6 && stone > 0.75f) {
                            topBlock = Blocks.STONE;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else if (y > stoneHeight - 3 && stone > 0.15f) {
                            topBlock = Blocks.STONE;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else if (y > stoneHeight) {
                            topBlock = Blocks.STONE;
                            fillerBlock = Blocks.STONE;
                            fillerLeft = 0;

                        } else {
                            topBlock = Blocks.GRASS;
                            fillerBlock = Blocks.DIRT;
                            fillerLeft = 3;
                        }

                        c.set(x, y, z, topBlock);
                        foundSurface = true;

                    } else if (fillerLeft > 0) {
                        c.set(x, y, z, fillerBlock);
                        fillerLeft--;
                    } else {
                        c.set(x, y, z, Blocks.STONE);
                    }
                }
            }
        }

        //
        //Water Placement
        //

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                for (int y = 0; y < seaLevel; y++) {
                    if (c.get(x, y, z) == Blocks.AIR) {
                        c.set(x, y, z, Blocks.WATER);
                    }else if (c.get(x, y, z) == Blocks.GRASS){
                        c.set(x, y, z, Blocks.SAND);
                        c.set(x, y - 1, z, Blocks.SAND);
                    }
                }
            }
        }

        //
        // Placed Features
        //
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = c.cx() * Chunk.SIZE + x;
                int worldZ = c.cz() * Chunk.SIZE + z;
                float r = rand01(worldX, worldZ, 1337);

                int surfaceY = -1;
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    if (c.get(x, y, z) == Blocks.GRASS) {
                        surfaceY = y;
                        break;
                    }
                    if(c.get(x, y, z) == Blocks.SAND
                            && c.get(x, y + 1, z) == Blocks.AIR
                            && (c.get(x + 1, y - 1, z) == Blocks.WATER
                            || c.get(x, y - 1, z + 1) == Blocks.WATER)){

                        c.set(x, y + 1, z, Blocks.REEDS);
                        if (r < 0.75){
                            c.set(x, y + 2, z, Blocks.REEDS);
                            if (r < 0.5){
                                c.set(x, y + 3, z, Blocks.REEDS);
                            }

                        }

                    }
                }

                if (surfaceY == -1 || surfaceY < seaLevel) continue;


                if (r < 0.005f && surfaceY < treeHeight) {
                    treeFeature.place(this, c, worldX, surfaceY + 1, worldZ);
                }
                if (r < 0.25f && surfaceY < treeHeight + 2){
                    groundFoliageFeature.place(this, c, worldX, surfaceY + 1, worldZ);
                }
            }
        }

        c.clearDirty();
    }

    private float sampleContinentalness(float n) {
        if (n <= -0.6f) return lerp(18f, 28f, (n + 1.0f) / 0.4f);   // deep ocean -> ocean
        if (n <= -0.2f) return lerp(28f, 40f, (n + 0.6f) / 0.4f);   // ocean -> coast
        if (n <=  0.2f) return lerp(40f, 52f, (n + 0.2f) / 0.4f);   // coast -> plains
        if (n <=  0.55f) return lerp(52f, 68f, (n - 0.2f) / 0.35f); // inland hills
        return lerp(68f, 88f, (n - 0.55f) / 0.45f);                 // high inland
    }

    private float samplePeaksValleys(float pv) {
        float ridged = 1f - Math.abs(pv);
        float smooth = (pv + 1f) * 0.5f;

        float pvShape = ridged * 0.8f + smooth * 0.2f;
        pvShape = (float)Math.pow(pvShape, 1.35f);

        if (pvShape > 0.78f) {
            pvShape = 0.78f + (pvShape - 0.78f) * 0.4f;
        }
        return pvShape;
    }

    private float sampleErosionFactor(float erosion) {
        float e = (erosion + 1f) * 0.5f; // 0..1
        return 1f - e;                   // low erosion = strong mountains
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void setBlockIfLoaded(int wx, int wy, int wz, Blocks type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return; // DON'T create chunks during generation

        c.set(lx, wy, lz, type);
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

    public void setBlock(int wx, int wy, int wz, Blocks type) {
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

    public Blocks getBlockForMeshing(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk c = getIfLoaded(cx, cz);
        if (c == null) return Blocks.STONE; // anything non-air: prevents border faces

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

    public void queueBlock(int wx, int wy, int wz, Blocks type) {
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
