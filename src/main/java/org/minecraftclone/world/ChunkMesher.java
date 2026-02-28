package org.minecraftclone.world;

import org.minecraftclone.gfx.FloatList;

public final class ChunkMesher {

    public record ChunkMeshData(float[] solid, float[] water) {}

    public static ChunkMeshData buildMesh(ChunkManager world, Chunk chunk) {
        FloatList solid = new FloatList();
        FloatList water = new FloatList();

        int baseX = chunk.cx() * Chunk.SIZE;
        int baseZ = chunk.cz() * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {

                    BlockType t = chunk.get(x, y, z);
                    if (t == BlockType.AIR) continue;

                    int wx = baseX + x;
                    int wz = baseZ + z;

                    // choose which mesh we append to
                    FloatList target = (t == BlockType.WATER) ? water : solid;

                    // For solids: face visible if neighbor is AIR
                    // For water: face visible if neighbor is AIR (not water), so water merges into one volume
                    // (Later: you can also render water faces against solids for shoreline – this already does.)
                    BlockType nFront  = world.getBlockForMeshing(wx, y, wz + 1);
                    BlockType nBack   = world.getBlockForMeshing(wx, y, wz - 1);
                    BlockType nLeft   = world.getBlockForMeshing(wx - 1, y, wz);
                    BlockType nRight  = world.getBlockForMeshing(wx + 1, y, wz);
                    BlockType nTop    = world.getBlockForMeshing(wx, y + 1, wz);
                    BlockType nBottom = world.getBlockForMeshing(wx, y - 1, wz);

                    if (isFaceVisible(t, nFront)) {
                        float[] uv = uvFor(t, Face.FRONT);
                        addFront(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                    if (isFaceVisible(t, nBack)) {
                        float[] uv = uvFor(t, Face.BACK);
                        addBack(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                    if (isFaceVisible(t, nLeft)) {
                        float[] uv = uvFor(t, Face.LEFT);
                        addLeft(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                    if (isFaceVisible(t, nRight)) {
                        float[] uv = uvFor(t, Face.RIGHT);
                        addRight(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                    if (isFaceVisible(t, nTop)) {
                        float[] uv = uvFor(t, Face.TOP);
                        addTop(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                    if (isFaceVisible(t, nBottom)) {
                        float[] uv = uvFor(t, Face.BOTTOM);
                        addBottom(target, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                }
            }
        }

        return new ChunkMeshData(solid.toArray(), water.toArray());
    }

    private static boolean isTransparent(BlockType t) {
        return t == BlockType.AIR || t == BlockType.WATER;
    }

    private static boolean isFaceVisible(BlockType self, BlockType neighbor) {
        if (self == BlockType.WATER) {
            // water faces render against anything except water (merges water volumes)
            return neighbor == BlockType.AIR;
        }
        // solid faces render against transparent neighbors (air OR water)
        return isTransparent(neighbor);
    }

    private static float[] uvFor(BlockType t, Face face) {
        int tilesX = 16;
        int tilesY = 16;

        int tileX, tileY;

        switch (t) {
            case DIRT  -> { tileX = 0; tileY = 0; }
            case STONE -> { tileX = 1; tileY = 0; }
            case SAND  -> { tileX = 4; tileY = 0; }
            case WATER -> { tileX = 0; tileY = 1; }

            case GRASS -> {
                if (face == Face.BOTTOM) { tileX = 0; tileY = 0; }      // dirt bottom
                else if (face == Face.TOP) { tileX = 3; tileY = 0; }    // grass top
                else { tileX = 2; tileY = 0; }                          // grass side
            }

            default -> { tileX = 0; tileY = 0; }
        }

        float tw = 1.0f / tilesX;
        float th = 1.0f / tilesY;

        float u0 = tileX * tw;
        float v0 = tileY * th;
        float u1 = u0 + tw;
        float v1 = v0 + th;

        return new float[]{u0, v0, u1, v1};
    }

    private static void v(FloatList o, float x, float y, float z, float u, float v) {
        o.add(x); o.add(y); o.add(z); o.add(u); o.add(v);
    }

    private static void addFront(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x0=x, x1=x+1, y0=y, y1=y+1, z1=z+1;
        v(o,x0,y0,z1, u0,v0); v(o,x1,y0,z1, u1,v0); v(o,x1,y1,z1, u1,v1);
        v(o,x1,y1,z1, u1,v1); v(o,x0,y1,z1, u0,v1); v(o,x0,y0,z1, u0,v0);
    }
    private static void addBack(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x0=x, x1=x+1, y0=y, y1=y+1, z0=z;
        v(o,x1,y0,z0, u0,v0); v(o,x0,y0,z0, u1,v0); v(o,x0,y1,z0, u1,v1);
        v(o,x0,y1,z0, u1,v1); v(o,x1,y1,z0, u0,v1); v(o,x1,y0,z0, u0,v0);
    }
    private static void addLeft(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x0=x, y0=y, y1=y+1, z0=z, z1=z+1;
        v(o,x0,y0,z0, u0,v0); v(o,x0,y0,z1, u1,v0); v(o,x0,y1,z1, u1,v1);
        v(o,x0,y1,z1, u1,v1); v(o,x0,y1,z0, u0,v1); v(o,x0,y0,z0, u0,v0);
    }
    private static void addRight(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x1=x+1, y0=y, y1=y+1, z0=z, z1=z+1;
        v(o,x1,y0,z1, u0,v0); v(o,x1,y0,z0, u1,v0); v(o,x1,y1,z0, u1,v1);
        v(o,x1,y1,z0, u1,v1); v(o,x1,y1,z1, u0,v1); v(o,x1,y0,z1, u0,v0);
    }
    private static void addTop(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x0=x, x1=x+1, y1=y+1, z0=z, z1=z+1;
        v(o,x0,y1,z1, u0,v0); v(o,x1,y1,z1, u1,v0); v(o,x1,y1,z0, u1,v1);
        v(o,x1,y1,z0, u1,v1); v(o,x0,y1,z0, u0,v1); v(o,x0,y1,z1, u0,v0);
    }
    private static void addBottom(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) {
        float x0=x, x1=x+1, y0=y, z0=z, z1=z+1;
        v(o,x0,y0,z0, u0,v0); v(o,x1,y0,z0, u1,v0); v(o,x1,y0,z1, u1,v1);
        v(o,x1,y0,z1, u1,v1); v(o,x0,y0,z1, u0,v1); v(o,x0,y0,z0, u0,v0);
    }
}