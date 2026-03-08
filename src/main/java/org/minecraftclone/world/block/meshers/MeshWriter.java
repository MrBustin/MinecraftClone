package org.minecraftclone.world.block.meshers;

import org.minecraftclone.gfx.FloatList;
import org.minecraftclone.world.block.UVRect;

public final class MeshWriter {

    private MeshWriter() {}

    private static void v(FloatList o, float x, float y, float z, float u, float v, float shade) {
        o.add(x); o.add(y); o.add(z); o.add(u); o.add(v); o.add(shade);
    }


    public static final float SHADE_TOP = 1.0f;
    public static final float SHADE_FRONT_BACK = 0.9f;
    public static final float SHADE_LEFT_RIGHT = 0.8f;
    public static final float SHADE_BOTTOM = 0.6f;


    public static void addFront(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x, x1 = x + 1, y0 = y, y1 = y + 1, z1 = z + 1;
        v(o, x0, y0, z1, uv.u0(), uv.v0(), shade);
        v(o, x1, y0, z1, uv.u1(), uv.v0(), shade);
        v(o, x1, y1, z1, uv.u1(), uv.v1(), shade);

        v(o, x1, y1, z1, uv.u1(), uv.v1(), shade);
        v(o, x0, y1, z1, uv.u0(), uv.v1(), shade);
        v(o, x0, y0, z1, uv.u0(), uv.v0(), shade);
    }

    public static void addBack(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x;
        float x1 = x + 1;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;

        v(o, x1, y0, z0, uv.u0(), uv.v0(), shade);
        v(o, x0, y0, z0, uv.u1(), uv.v0(), shade);
        v(o, x0, y1, z0, uv.u1(), uv.v1(), shade);

        v(o, x0, y1, z0, uv.u1(), uv.v1(), shade);
        v(o, x1, y1, z0, uv.u0(), uv.v1(), shade);
        v(o, x1, y0, z0, uv.u0(), uv.v0(), shade);
    }

    public static void addLeft(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;

        v(o, x0, y0, z0, uv.u0(), uv.v0(), shade);
        v(o, x0, y0, z1, uv.u1(), uv.v0(), shade);
        v(o, x0, y1, z1, uv.u1(), uv.v1(), shade);

        v(o, x0, y1, z1, uv.u1(), uv.v1(), shade);
        v(o, x0, y1, z0, uv.u0(), uv.v1(), shade);
        v(o, x0, y0, z0, uv.u0(), uv.v0(), shade);
    }

    public static void addRight(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x1 = x + 1;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;

        v(o, x1, y0, z1, uv.u0(), uv.v0(), shade);
        v(o, x1, y0, z0, uv.u1(), uv.v0(), shade);
        v(o, x1, y1, z0, uv.u1(), uv.v1(), shade);

        v(o, x1, y1, z0, uv.u1(), uv.v1(), shade);
        v(o, x1, y1, z1, uv.u0(), uv.v1(), shade);
        v(o, x1, y0, z1, uv.u0(), uv.v0(), shade);
    }

    public static void addTop(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x;
        float x1 = x + 1;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;

        v(o, x0, y1, z1, uv.u0(), uv.v0(), shade);
        v(o, x1, y1, z1, uv.u1(), uv.v0(), shade);
        v(o, x1, y1, z0, uv.u1(), uv.v1(), shade);

        v(o, x1, y1, z0, uv.u1(), uv.v1(), shade);
        v(o, x0, y1, z0, uv.u0(), uv.v1(), shade);
        v(o, x0, y1, z1, uv.u0(), uv.v0(), shade);
    }

    public static void addBottom(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x;
        float x1 = x + 1;
        float y0 = y;
        float z0 = z;
        float z1 = z + 1;

        v(o, x0, y0, z0, uv.u0(), uv.v0(), shade);
        v(o, x1, y0, z0, uv.u1(), uv.v0(), shade);
        v(o, x1, y0, z1, uv.u1(), uv.v1(), shade);

        v(o, x1, y0, z1, uv.u1(), uv.v1(), shade);
        v(o, x0, y0, z1, uv.u0(), uv.v1(), shade);
        v(o, x0, y0, z0, uv.u0(), uv.v0(), shade);
    }

    public static void addCrossPlant(FloatList o, int x, int y, int z, UVRect uv, float shade) {
        float x0 = x;
        float x1 = x + 1;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;

        addDoubleSidedQuad(o, x0, y0, z0, x1, y0, z1, x1, y1, z1, x0, y1, z0, uv, shade);

        addDoubleSidedQuad(o, x1, y0, z0, x0, y0, z1, x0, y1, z1, x1, y1, z0, uv, shade);
    }

    public static void addDoubleSidedQuad(FloatList o,
                                          float ax, float ay, float az,
                                          float bx, float by, float bz,
                                          float cx, float cy, float cz,
                                          float dx, float dy, float dz,
                                          UVRect uv, float shade) {
        v(o, ax, ay, az, uv.u0(), uv.v0(), shade);
        v(o, bx, by, bz, uv.u1(), uv.v0(), shade);
        v(o, cx, cy, cz, uv.u1(), uv.v1(), shade);

        v(o, cx, cy, cz, uv.u1(), uv.v1(), shade);
        v(o, dx, dy, dz, uv.u0(), uv.v1(), shade);
        v(o, ax, ay, az, uv.u0(), uv.v0(), shade);

        v(o, ax, ay, az, uv.u0(), uv.v0(), shade);
        v(o, dx, dy, dz, uv.u0(), uv.v1(), shade);
        v(o, cx, cy, cz, uv.u1(), uv.v1(), shade);

        v( o, cx, cy, cz, uv.u1(), uv.v1(), shade);
        v( o, bx, by, bz, uv.u1(), uv.v0(), shade);
        v(o, ax, ay, az, uv.u0(), uv.v0(), shade);
    }
}
