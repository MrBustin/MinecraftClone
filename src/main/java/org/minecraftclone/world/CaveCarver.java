package org.minecraftclone.world;

import java.util.Random;

public final class CaveCarver {
    private static final int REGION = 4; // 4x4 chunks share a cave seed
    private static final int MAX_WORMS_PER_REGION = 6; // tune 3-8

    // Fix B: only check 5 regions (cross), not 9
    private static final int[][] REGION_OFFS = {
            {0, 0},
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
    };

    private CaveCarver() {}

    public static void carveChunk(ChunkManager world, Chunk c, long worldSeed) {
        int cx = c.cx();
        int cz = c.cz();

        int baseX = cx * Chunk.SIZE;
        int baseZ = cz * Chunk.SIZE;

        int cminX = baseX;
        int cmaxX = baseX + Chunk.SIZE - 1;
        int cminZ = baseZ;
        int cmaxZ = baseZ + Chunk.SIZE - 1;

        int rx0 = floorDiv(cx, REGION);
        int rz0 = floorDiv(cz, REGION);

        // Evaluate neighboring regions (cross only)
        for (int[] off : REGION_OFFS) {
            int rx = rx0 + off[0];
            int rz = rz0 + off[1];

            long regionSeed = mix(worldSeed ^ pack(rx, rz) * 0x9E3779B97F4A7C15L);
            Random rng = new Random(regionSeed);

            int regionBaseX = rx * REGION * Chunk.SIZE;
            int regionBaseZ = rz * REGION * Chunk.SIZE;

            int worms = 3 + rng.nextInt(MAX_WORMS_PER_REGION - 2);

            for (int w = 0; w < worms; w++) {
                double x = regionBaseX + rng.nextInt(REGION * Chunk.SIZE) + 0.5;
                double z = regionBaseZ + rng.nextInt(REGION * Chunk.SIZE) + 0.5;
                double y = 12 + rng.nextInt(48);

                double yaw = rng.nextDouble() * Math.PI * 2.0;
                double pitch = (rng.nextDouble() - 0.5) * 0.12;

                int steps = 120 + rng.nextInt(120);
                double radius = 2.2 + rng.nextDouble() * 1.0;

                double phase = rng.nextDouble() * 10.0;

                // Fix A: early-reject whole worm if it can never reach this chunk.
                // Step length in XZ is ~0.55 per step (from stepWorm).
                double maxTravel = steps * 0.60;              // safe upper bound
                double reachR = maxTravel + radius + 2.0;     // include sphere radius + padding
                if (!sphereIntersectsChunk(x, z, reachR, cminX, cmaxX, cminZ, cmaxZ)) {
                    continue;
                }

                for (int i = 0; i < steps; i++) {
                    // If current sphere is far, we still move, but we skip carving
                    if (sphereIntersectsChunk(x, z, radius + 1.0, cminX, cmaxX, cminZ, cmaxZ)) {
                        carveSphereInsideChunk(c, x, y, z, radius, baseX, baseZ);
                    }

                    StepResult sr = stepWorm(rng, yaw, pitch, i, phase);
                    yaw = sr.yaw; pitch = sr.pitch;
                    x += sr.dx; y += sr.dy; z += sr.dz;
                    y = clamp(y, 6, Chunk.HEIGHT - 6);
                }
            }
        }

        c.markDirty();
    }

    private static StepResult stepWorm(Random rng, double yaw, double pitch, int i, double phase) {
        // Smooth drift (stronger than before)
        yaw   += (rng.nextDouble() - 0.5) * 0.10;
        pitch += (rng.nextDouble() - 0.5) * 0.05;

        // Slow vertical wave so tunnels naturally go up/down
        pitch += Math.sin(i * 0.10 + phase) * 0.03;

        // Rare stronger turns (adds "interesting" bends)
        if (rng.nextFloat() < 0.03f) {
            yaw += (rng.nextBoolean() ? 1 : -1) * (0.5 + rng.nextDouble() * 0.7);
        }
        if (rng.nextFloat() < 0.02f) {
            pitch += (rng.nextBoolean() ? 1 : -1) * (0.15 + rng.nextDouble() * 0.20);
        }

        // Gentle damping toward level (prevents permanent steep diagonals)
        pitch *= 0.98;
        pitch = clamp(pitch, -0.60, 0.60);

        // Movement: make vertical change noticeable
        double stepXZ = 0.55;
        double stepY  = 0.45;

        double dx = Math.cos(yaw) * Math.cos(pitch) * stepXZ;
        double dy = Math.sin(pitch) * stepY;
        double dz = Math.sin(yaw) * Math.cos(pitch) * stepXZ;

        return new StepResult(yaw, pitch, dx, dy, dz);
    }

    private record StepResult(double yaw, double pitch, double dx, double dy, double dz) {}

    private static void carveSphereInsideChunk(Chunk c, double cx, double cy, double cz, double r, int baseX, int baseZ) {
        int minX = (int) Math.floor(cx - r);
        int maxX = (int) Math.floor(cx + r);
        int minZ = (int) Math.floor(cz - r);
        int maxZ = (int) Math.floor(cz + r);

        // clamp to chunk XZ bounds
        minX = Math.max(minX, baseX);
        maxX = Math.min(maxX, baseX + Chunk.SIZE - 1);
        minZ = Math.max(minZ, baseZ);
        maxZ = Math.min(maxZ, baseZ + Chunk.SIZE - 1);

        double r2 = r * r;

        for (int wx = minX; wx <= maxX; wx++) {
            double dx = (wx + 0.5) - cx;
            double dx2 = dx * dx;
            int lx = wx - baseX;

            for (int wz = minZ; wz <= maxZ; wz++) {
                double dz = (wz + 0.5) - cz;
                double dXZ2 = dx2 + dz * dz;
                if (dXZ2 > r2) continue;

                int lz = wz - baseZ;

                // Compute y span for this (x,z)
                double rem = Math.sqrt(r2 - dXZ2);
                int minY = (int) Math.floor(cy - rem);
                int maxY = (int) Math.floor(cy + rem);

                minY = Math.max(minY, 2);
                maxY = Math.min(maxY, Chunk.HEIGHT - 2);

                for (int y = minY; y <= maxY; y++) {
                    BlockType cur = c.get(lx, y, lz);
                    if (cur == BlockType.STONE || cur == BlockType.DIRT) {
                        c.set(lx, y, lz, BlockType.AIR);
                    }
                }
            }
        }
    }

    private static boolean sphereIntersectsChunk(double x, double z, double r, int minX, int maxX, int minZ, int maxZ) {
        double cx = clamp(x, minX, maxX);
        double cz = clamp(z, minZ, maxZ);
        double dx = x - cx;
        double dz = z - cz;
        return (dx * dx + dz * dz) <= (r * r);
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    private static long pack(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static long mix(long x) {
        x ^= (x >>> 33);
        x *= 0xff51afd7ed558ccdL;
        x ^= (x >>> 33);
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= (x >>> 33);
        return x;
    }

    private static double clamp(double v, double a, double b) {
        return v < a ? a : (v > b ? b : v);
    }
}