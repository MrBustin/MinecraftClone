package org.minecraftclone.world;

import org.joml.Vector3f;
import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.chunk.ChunkManager;

public final class VoxelRaycast {

    public record Hit(int x, int y, int z, int nx, int ny, int nz) {}

    // DDA voxel traversal. Returns first non-air block hit up to maxDist.
    public static Hit raycast(ChunkManager world, Vector3f origin, Vector3f dir, float maxDist) {
        Vector3f d = new Vector3f(dir).normalize();

        int x = fastFloor(origin.x);
        int y = fastFloor(origin.y);
        int z = fastFloor(origin.z);

        int stepX = d.x > 0 ? 1 : (d.x < 0 ? -1 : 0);
        int stepY = d.y > 0 ? 1 : (d.y < 0 ? -1 : 0);
        int stepZ = d.z > 0 ? 1 : (d.z < 0 ? -1 : 0);

        float tMaxX = intBound(origin.x, d.x);
        float tMaxY = intBound(origin.y, d.y);
        float tMaxZ = intBound(origin.z, d.z);

        float tDeltaX = stepX == 0 ? Float.POSITIVE_INFINITY : Math.abs(1f / d.x);
        float tDeltaY = stepY == 0 ? Float.POSITIVE_INFINITY : Math.abs(1f / d.y);
        float tDeltaZ = stepZ == 0 ? Float.POSITIVE_INFINITY : Math.abs(1f / d.z);

        int nx = 0, ny = 0, nz = 0;

        float dist = 0f;
        while (dist <= maxDist) {
            Blocks bt = world.getBlockIfLoaded(x, y, z);
            if (bt != Blocks.AIR) {
                return new Hit(x, y, z, nx, ny, nz);
            }

            // advance to next voxel boundary
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    dist = tMaxX;
                    tMaxX += tDeltaX;
                    nx = -stepX; ny = 0; nz = 0;
                } else {
                    z += stepZ;
                    dist = tMaxZ;
                    tMaxZ += tDeltaZ;
                    nx = 0; ny = 0; nz = -stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    dist = tMaxY;
                    tMaxY += tDeltaY;
                    nx = 0; ny = -stepY; nz = 0;
                } else {
                    z += stepZ;
                    dist = tMaxZ;
                    tMaxZ += tDeltaZ;
                    nx = 0; ny = 0; nz = -stepZ;
                }
            }
        }

        return null;
    }

    private static int fastFloor(float x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    // Distance until next integer boundary on that axis
    private static float intBound(float s, float ds) {
        if (ds == 0f) return Float.POSITIVE_INFINITY;

        float sFrac = s - (float)Math.floor(s);
        if (ds > 0) {
            return (1 - sFrac) / ds;
        } else {
            return sFrac / -ds;
        }
    }

    private VoxelRaycast() {}
}
