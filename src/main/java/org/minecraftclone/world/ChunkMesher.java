package org.minecraftclone.world;


import org.minecraftclone.gfx.FloatList;

public final class ChunkMesher {

    // Each face is 2 triangles = 6 vertices. Each vertex: x y z u v
    // UVs are 0..1 for now (later you’ll map into an atlas)

    public static float[] buildMesh(ChunkManager world, Chunk chunk) {
        FloatList out = new FloatList();

        int baseX = chunk.cx() * Chunk.SIZE;
        int baseZ = chunk.cz() * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {

                    BlockType t = chunk.get(x, y, z);
                    if (t == BlockType.AIR) continue;

                    int wx = baseX + x;
                    int wz = baseZ + z;


                    // For each face: if neighbor is AIR, add that face
                    if (world.getBlockIfLoaded(wx, y, wz + 1) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.FRONT);
                        addFront(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }

                    if (world.getBlockIfLoaded(wx, y, wz - 1) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.BACK);
                        addBack(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }

                    if (world.getBlockIfLoaded(wx - 1, y, wz) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.LEFT);
                        addLeft(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }

                    if (world.getBlockIfLoaded(wx + 1, y, wz) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.RIGHT);
                        addRight(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }

                    if (world.getBlockIfLoaded(wx, y + 1, wz) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.TOP);
                        addTop(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }

                    if (world.getBlockIfLoaded(wx, y - 1, wz) == BlockType.AIR) {
                        float[] uv = uvFor(t, Face.BOTTOM);
                        addBottom(out, x, y, z, uv[0], uv[1], uv[2], uv[3]);
                    }
                }
            }
        }

        return out.toArray();
    }

    private static float[] uvFor(BlockType t, Face face) {
        int tilesX = 16;
        int tilesY = 16;

        int tileX, tileY;

        // Choose atlas tile based on block + face
        switch (t) {
            case DIRT -> { tileX = 0; tileY = 0; }
            case STONE -> { tileX = 1; tileY = 0; }

            case GRASS -> {
                // bottom uses dirt
                if (face == Face.BOTTOM) { tileX = 0; tileY = 0; }
                // top uses grass top
                else if (face == Face.TOP) { tileX = 3; tileY = 0; }
                // sides use grass side
                else { tileX = 2; tileY = 0; }
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

// These faces are for a cube from (x,y,z) to (x+1,y+1,z+1)

    private static void addFront(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // +Z
        float x0=x, x1=x+1, y0=y, y1=y+1, z1=z+1;
        v(o,x0,y0,z1, u0,v0); v(o,x1,y0,z1, u1,v0); v(o,x1,y1,z1, u1,v1);
        v(o,x1,y1,z1, u1,v1); v(o,x0,y1,z1, u0,v1); v(o,x0,y0,z1, u0,v0);
    }

    private static void addBack(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // -Z
        float x0=x, x1=x+1, y0=y, y1=y+1, z0=z;
        v(o,x1,y0,z0, u0,v0); v(o,x0,y0,z0, u1,v0); v(o,x0,y1,z0, u1,v1);
        v(o,x0,y1,z0, u1,v1); v(o,x1,y1,z0, u0,v1); v(o,x1,y0,z0, u0,v0);
    }

    private static void addLeft(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // -X
        float x0=x, y0=y, y1=y+1, z0=z, z1=z+1;
        v(o,x0,y0,z0, u0,v0); v(o,x0,y0,z1, u1,v0); v(o,x0,y1,z1, u1,v1);
        v(o,x0,y1,z1, u1,v1); v(o,x0,y1,z0, u0,v1); v(o,x0,y0,z0, u0,v0);
    }

    private static void addRight(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // +X
        float x1=x+1, y0=y, y1=y+1, z0=z, z1=z+1;
        v(o,x1,y0,z1, u0,v0); v(o,x1,y0,z0, u1,v0); v(o,x1,y1,z0, u1,v1);
        v(o,x1,y1,z0, u1,v1); v(o,x1,y1,z1, u0,v1); v(o,x1,y0,z1, u0,v0);
    }

    private static void addTop(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // +Y
        float x0=x, x1=x+1, y1=y+1, z0=z, z1=z+1;
        v(o,x0,y1,z1, u0,v0); v(o,x1,y1,z1, u1,v0); v(o,x1,y1,z0, u1,v1);
        v(o,x1,y1,z0, u1,v1); v(o,x0,y1,z0, u0,v1); v(o,x0,y1,z1, u0,v0);
    }

    private static void addBottom(FloatList o, int x, int y, int z, float u0, float v0, float u1, float v1) { // -Y
        float x0=x, x1=x+1, y0=y, z0=z, z1=z+1;
        v(o,x0,y0,z0, u0,v0); v(o,x1,y0,z0, u1,v0); v(o,x1,y0,z1, u1,v1);
        v(o,x1,y0,z1, u1,v1); v(o,x0,y0,z1, u0,v1); v(o,x0,y0,z0, u0,v0);
    }
}
