package org.minecraftclone.world.chunk;

import org.minecraftclone.entity.Entity;
import org.minecraftclone.world.FastNoiseLite;
import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.placedfeatures.GroundFoliageFeature;
import org.minecraftclone.world.placedfeatures.TreeFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkManager {

    // =========================================================
    // World storage
    // =========================================================

    private final Map<Long, Chunk> chunks = new HashMap<>();
    private final Map<Long, ArrayList<PendingBlock>> pending = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();

    private record PendingBlock(int wx, int wy, int wz, Blocks type) {}

    // =========================================================
    // Terrain constants
    // =========================================================

    private static final int SEA_LEVEL = 40;
    private static final int TREE_HEIGHT_LIMIT = 80;
    private static final int STONE_HEIGHT = 85;
    private static final int SNOW_HEIGHT = STONE_HEIGHT + 35;

    private static final float CONTINENTAL_FREQ = 0.0025f;
    private static final float EROSION_FREQ = 0.0035f;
    private static final float PV_FREQ = 0.0045f;
    private static final float SHORE_FREQ = 0.018f;

    private static final int CONTINENTAL_SEED = 1001;
    private static final int EROSION_SEED = 2002;
    private static final int PV_SEED = 3003;
    private static final int SHORE_SEED = 4004;

    private static final int SURFACE_STONE_SEED = 1337;
    private static final int SURFACE_SNOW_SEED = 7647;
    private static final int FEATURE_SEED = 1337;

    // =========================================================
    // Noise
    // =========================================================

    private final FastNoiseLite continentalNoise = createNoise(CONTINENTAL_SEED, CONTINENTAL_FREQ);
    private final FastNoiseLite erosionNoise = createNoise(EROSION_SEED, EROSION_FREQ);
    private final FastNoiseLite pvNoise = createNoise(PV_SEED, PV_FREQ);
    private final FastNoiseLite shoreNoise = createNoise(SHORE_SEED, SHORE_FREQ);

    // =========================================================
    // Features
    // =========================================================

    private final TreeFeature treeFeature = new TreeFeature();
    private final GroundFoliageFeature groundFoliageFeature = new GroundFoliageFeature();

    // =========================================================
    // Chunk access
    // =========================================================

    public Chunk getOrCreate(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();

        return chunks.computeIfAbsent(key, k -> {
            Chunk chunk = new Chunk(cx, cz);
            generateTerrain(chunk);
            applyPending(chunk);
            markNeighborChunksDirty(cx, cz);
            return chunk;
        });
    }

    public Chunk getIfLoaded(int cx, int cz) {
        long key = new ChunkPos(cx, cz).key();
        return chunks.get(key);
    }

    public Blocks getBlockIfLoaded(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk chunk = getIfLoaded(cx, cz);
        if (chunk == null) return Blocks.AIR;

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);
        return chunk.get(lx, wy, lz);
    }

    public Blocks getBlockForMeshing(int wx, int wy, int wz) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk chunk = getIfLoaded(cx, cz);
        if (chunk == null) return Blocks.STONE; // prevents border faces

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);
        return chunk.get(lx, wy, lz);
    }

    // =========================================================
    // Block placement
    // =========================================================

    public void setBlockIfLoaded(int wx, int wy, int wz, Blocks type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk chunk = getIfLoaded(cx, cz);
        if (chunk == null) return;

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);
        chunk.set(lx, wy, lz, type);
    }

    public void setBlock(int wx, int wy, int wz, Blocks type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        Chunk chunk = getOrCreate(cx, cz);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);
        chunk.set(lx, wy, lz, type);

        // If the edited block is on a chunk edge, neighbor mesh may also need rebuilding.
        if (lx == 0) markDirtyIfLoaded(cx - 1, cz);
        if (lx == Chunk.SIZE - 1) markDirtyIfLoaded(cx + 1, cz);
        if (lz == 0) markDirtyIfLoaded(cx, cz - 1);
        if (lz == Chunk.SIZE - 1) markDirtyIfLoaded(cx, cz + 1);
    }

    public void queueBlock(int wx, int wy, int wz, Blocks type) {
        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);
        long key = new ChunkPos(cx, cz).key();

        pending.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new PendingBlock(wx, wy, wz, type));
    }

    private void applyPending(Chunk chunk) {
        long key = new ChunkPos(chunk.cx(), chunk.cz()).key();
        List<PendingBlock> list = pending.remove(key);
        if (list == null) return;

        for (PendingBlock pb : list) {
            if (pb.wy() < 0 || pb.wy() >= Chunk.HEIGHT) continue;

            int lx = mod(pb.wx(), Chunk.SIZE);
            int lz = mod(pb.wz(), Chunk.SIZE);
            chunk.set(lx, pb.wy(), lz, pb.type());
        }

        chunk.markDirty();
    }

    // =========================================================
    // Terrain generation
    // =========================================================

    private void generateTerrain(Chunk chunk) {
        generateBaseStone(chunk);
        applySurfaceLayers(chunk);
        placeWaterAndBeaches(chunk);
        placeFeatures(chunk);
        chunk.clearDirty();
    }

    private void generateBaseStone(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = chunk.cx() * Chunk.SIZE + x;
                int worldZ = chunk.cz() * Chunk.SIZE + z;

                int height = computeTerrainHeight(worldX, worldZ);

                for (int y = 0; y <= height && y < Chunk.HEIGHT; y++) {
                    chunk.set(x, y, z, Blocks.STONE);
                }
            }
        }
    }

    private void applySurfaceLayers(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = chunk.cx() * Chunk.SIZE + x;
                int worldZ = chunk.cz() * Chunk.SIZE + z;

                boolean foundSurface = false;
                int fillerLeft = 0;
                Blocks fillerBlock = Blocks.STONE;

                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    Blocks block = chunk.get(x, y, z);
                    if (block == Blocks.AIR || block == Blocks.WATER) continue;

                    if (!foundSurface) {
                        float stone = rand01(worldX, worldZ, SURFACE_STONE_SEED);
                        float snow = rand01(worldX, worldZ, SURFACE_SNOW_SEED);

                        SurfaceChoice surface = getSurfaceChoice(y, stone, snow);

                        chunk.set(x, y, z, surface.topBlock());
                        fillerBlock = surface.fillerBlock();
                        fillerLeft = surface.fillerDepth();
                        foundSurface = true;
                    } else if (fillerLeft > 0) {
                        chunk.set(x, y, z, fillerBlock);
                        fillerLeft--;
                    } else {
                        chunk.set(x, y, z, Blocks.STONE);
                    }
                }
            }
        }
    }

    private void placeWaterAndBeaches(Chunk chunk) {
        final int beachMinY = SEA_LEVEL - 2;
        final int beachMaxY = SEA_LEVEL + 3;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = chunk.cx() * Chunk.SIZE + x;
                int worldZ = chunk.cz() * Chunk.SIZE + z;

                int surfaceY = -1;

                // Find top solid block in this column
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    Blocks block = chunk.get(x, y, z);
                    if (block != Blocks.AIR && block != Blocks.WATER) {
                        surfaceY = y;
                        break;
                    }
                }

                // Fill water up to sea level
                for (int y = 0; y < SEA_LEVEL; y++) {
                    if (chunk.get(x, y, z) == Blocks.AIR) {
                        chunk.set(x, y, z, Blocks.WATER);
                    }
                }

                if (surfaceY == -1) continue;

                float shore = shoreNoise.GetNoise(worldX, worldZ);

                // Vary sand depth with shore noise
                int sandDepth = 3 + (int) ((shore + 1f) * 0.5f * 3f); // 3..6

                // Underwater columns: make full seafloor sand
                if (surfaceY < SEA_LEVEL) {
                    Blocks top = chunk.get(x, surfaceY, z);
                    if (top != Blocks.AIR && top != Blocks.WATER) {
                        chunk.set(x, surfaceY, z, Blocks.SAND);
                    }

                    for (int d = 1; d <= sandDepth; d++) {
                        int y = surfaceY - d;
                        if (y < 0) break;

                        Blocks below = chunk.get(x, y, z);
                        if (below == Blocks.AIR || below == Blocks.WATER) break;

                        chunk.set(x, y, z, Blocks.SAND);
                    }

                    continue;
                }

                // Shoreline beaches slightly above/below sea level
                if (surfaceY >= beachMinY && surfaceY <= beachMaxY) {
                    Blocks top = chunk.get(x, surfaceY, z);
                    if (top == Blocks.GRASS || top == Blocks.DIRT || top == Blocks.STONE) {
                        chunk.set(x, surfaceY, z, Blocks.SAND);
                    }

                    for (int d = 1; d <= sandDepth; d++) {
                        int y = surfaceY - d;
                        if (y < 0) break;

                        Blocks below = chunk.get(x, y, z);
                        if (below == Blocks.AIR || below == Blocks.WATER) break;

                        if (below == Blocks.DIRT || below == Blocks.GRASS || below == Blocks.STONE) {
                            chunk.set(x, y, z, Blocks.SAND);
                        }
                    }
                }
            }
        }
    }

    private void placeFeatures(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = chunk.cx() * Chunk.SIZE + x;
                int worldZ = chunk.cz() * Chunk.SIZE + z;
                float r = rand01(worldX, worldZ, FEATURE_SEED);

                int surfaceY = findGrassSurfaceY(chunk, x, z);
                tryPlaceReeds(chunk, x, z, r);

                if (surfaceY == -1 || surfaceY < SEA_LEVEL) continue;

                if (r < 0.005f && surfaceY < TREE_HEIGHT_LIMIT) {
                    treeFeature.place(this, chunk, worldX, surfaceY + 1, worldZ);
                }

                if (r < 0.25f && surfaceY < TREE_HEIGHT_LIMIT + 2) {
                    groundFoliageFeature.place(this, chunk, worldX, surfaceY + 1, worldZ);
                }
            }
        }
    }

    private int computeTerrainHeight(int worldX, int worldZ) {
        float continental = continentalNoise.GetNoise(worldX, worldZ);
        float erosion = erosionNoise.GetNoise(worldX, worldZ);
        float pv = pvNoise.GetNoise(worldX, worldZ);

        float baseHeight = sampleContinentalness(continental);
        float pvShape = samplePeaksValleys(pv);
        float ruggedness = sampleErosionFactor(erosion);

        float inlandness = Math.max(0f, (continental + 0.15f) / 0.65f);
        inlandness = Math.min(inlandness, 1f);

        float mountainHeight = pvShape * ruggedness * inlandness * 85f;
        int height = (int) (baseHeight + mountainHeight);

        if (height < SEA_LEVEL) {
            float seaFloorNoise = shoreNoise.GetNoise(worldX, worldZ);
            float depthBelowSea = SEA_LEVEL - height;
            float seaFloorWeight = Math.min(depthBelowSea / 12f, 1f);
            height += (int) (seaFloorNoise * 3f * seaFloorWeight);
        }

        float erosion01 = (erosion + 1f) * 0.25f;
        float shoreStrength = lerp(3f, 1.5f, erosion01);
        float shore = shoreNoise.GetNoise(worldX, worldZ) * shoreStrength;

        float distFromSea = Math.abs(height - SEA_LEVEL);
        float shoreWeight = 1f - Math.min(distFromSea / 8f, 1f);
        shoreWeight = shoreWeight * shoreWeight * (3f - 2f * shoreWeight);

        height += (int) (shore * shoreWeight);
        return height;
    }

    private int findGrassSurfaceY(Chunk chunk, int x, int z) {
        for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
            if (chunk.get(x, y, z) == Blocks.GRASS) {
                return y;
            }
        }
        return -1;
    }

    private void tryPlaceReeds(Chunk chunk, int x, int z, float r) {
        for (int y = Chunk.HEIGHT - 1; y >= 1; y--) {
            if (chunk.get(x, y, z) != Blocks.SAND) continue;
            if (y + 1 >= Chunk.HEIGHT || chunk.get(x, y + 1, z) != Blocks.AIR) continue;

            boolean waterNearby =
                    getLocalBlockSafe(chunk, x + 1, y - 1, z) == Blocks.WATER ||
                            getLocalBlockSafe(chunk, x, y - 1, z + 1) == Blocks.WATER;

            if (!waterNearby) continue;

            chunk.set(x, y + 1, z, Blocks.REEDS);

            if (r < 0.75f && y + 2 < Chunk.HEIGHT) {
                chunk.set(x, y + 2, z, Blocks.REEDS);
            }
            if (r < 0.5f && y + 3 < Chunk.HEIGHT) {
                chunk.set(x, y + 3, z, Blocks.REEDS);
            }
            return;
        }
    }

    private Blocks getLocalBlockSafe(Chunk chunk, int x, int y, int z) {
        if (x < 0 || x >= Chunk.SIZE) return Blocks.AIR;
        if (z < 0 || z >= Chunk.SIZE) return Blocks.AIR;
        if (y < 0 || y >= Chunk.HEIGHT) return Blocks.AIR;
        return chunk.get(x, y, z);
    }

    // =========================================================
    // Surface helpers
    // =========================================================

    private record SurfaceChoice(Blocks topBlock, Blocks fillerBlock, int fillerDepth) {}

    private SurfaceChoice getSurfaceChoice(int y, float stone, float snow) {
        if (y > SNOW_HEIGHT) {
            return new SurfaceChoice(Blocks.SNOW, Blocks.STONE, 0);
        }

        if (y > SNOW_HEIGHT - 6 && snow > 0.75f) {
            return new SurfaceChoice(Blocks.SNOW, Blocks.STONE, 0);
        }

        if (y > SNOW_HEIGHT - 3 && snow > 0.15f) {
            return new SurfaceChoice(Blocks.SNOW, Blocks.STONE, 0);
        }

        if (y > STONE_HEIGHT - 6 && stone > 0.75f) {
            return new SurfaceChoice(Blocks.STONE, Blocks.STONE, 0);
        }

        if (y > STONE_HEIGHT - 3 && stone > 0.15f) {
            return new SurfaceChoice(Blocks.STONE, Blocks.STONE, 0);
        }

        if (y > STONE_HEIGHT) {
            return new SurfaceChoice(Blocks.STONE, Blocks.STONE, 0);
        }

        return new SurfaceChoice(Blocks.GRASS, Blocks.DIRT, 3);
    }

    // =========================================================
    // Chunk loading / unloading
    // =========================================================

    public Collection<Chunk> loadedChunksSnapshot() {
        return new ArrayList<>(chunks.values());
    }

    public void ensureLoadedAround(int centerCx, int centerCz, int viewDistance) {
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                getOrCreate(centerCx + dx, centerCz + dz);
            }
        }
    }

    public List<Long> unloadOutsideRadius(int centerCx, int centerCz, int viewDistance) {
        int maxDist = viewDistance + 1;
        int maxDistSq = maxDist * maxDist;

        ArrayList<Long> unloadedKeys = new ArrayList<>();

        for (Chunk chunk : loadedChunksSnapshot()) {
            int dx = chunk.cx() - centerCx;
            int dz = chunk.cz() - centerCz;
            int distSq = dx * dx + dz * dz;

            if (distSq > maxDistSq) {
                long key = new ChunkPos(chunk.cx(), chunk.cz()).key();
                chunks.remove(key);
                unloadedKeys.add(key);
            }
        }

        return unloadedKeys;
    }

    // =========================================================
    // Entities
    // =========================================================

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void tickEntities() {
        for (Entity entity : entities) {
            entity.tick();
        }

        entities.removeIf(Entity::isRemoved);
    }

    public List<Entity> getEntities() {
        return entities;
    }


    // =========================================================
    // Dirty marking
    // =========================================================

    private void markNeighborChunksDirty(int cx, int cz) {
        markDirtyIfLoaded(cx - 1, cz);
        markDirtyIfLoaded(cx + 1, cz);
        markDirtyIfLoaded(cx, cz - 1);
        markDirtyIfLoaded(cx, cz + 1);
    }

    private void markDirtyIfLoaded(int cx, int cz) {
        Chunk chunk = getIfLoaded(cx, cz);
        if (chunk != null) {
            chunk.markDirty();
        }
    }

    // =========================================================
    // Noise helpers
    // =========================================================

    private FastNoiseLite createNoise(int seed, float frequency) {
        FastNoiseLite noise = new FastNoiseLite(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFrequency(frequency);
        return noise;
    }

    private float sampleContinentalness(float n) {
        if (n <= -0.6f) return lerp(18f, 28f, (n + 1.0f) / 0.4f);
        if (n <= -0.2f) return lerp(28f, 40f, (n + 0.6f) / 0.4f);
        if (n <= 0.2f) return lerp(40f, 52f, (n + 0.2f) / 0.4f);
        if (n <= 0.55f) return lerp(52f, 68f, (n - 0.2f) / 0.35f);
        return lerp(68f, 74f, (n - 0.55f) / 0.45f);
    }

    private float samplePeaksValleys(float pv) {
        float ridged = 1f - Math.abs(pv);   // 0..1, highest near center
        ridged = 1f - ridged;               // flip so high values are rarer
        ridged = Math.max(0f, ridged - 0.35f) / 0.65f; // threshold out small bumps
        return ridged * ridged;             // strongly bias toward low values
    }

    private float sampleErosionFactor(float erosion) {
        float e = (erosion + 1f) * 0.5f;
        float rugged = 1f - e;
        return rugged * rugged;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // =========================================================
    // Math / random helpers
    // =========================================================

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

    private static int hash2D(int x, int z, int seed) {
        int h = x * 374761393 + z * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    private static float rand01(int x, int z, int seed) {
        return (hash2D(x, z, seed) & 0xFFFFFF) / (float) 0x1000000;
    }
}