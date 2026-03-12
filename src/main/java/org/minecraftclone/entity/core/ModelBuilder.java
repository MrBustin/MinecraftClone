package org.minecraftclone.entity.core;

import org.minecraftclone.gfx.FloatList;

public class ModelBuilder {
    private final FloatList vertices = new FloatList();

    public ModelBuilder box(float x, float y, float z, float w, float h, float d) {
        return box(x, y, z, w, h, d, BoxUV.all(new UV(0f, 0f, 1f, 1f)));
    }

    public ModelBuilder box(float x, float y, float z, float w, float h, float d, UV uv) {
        return box(x, y, z, w, h, d, BoxUV.all(uv));
    }

    public ModelBuilder box(float x, float y, float z, float w, float h, float d, BoxUV boxUV) {
        float x0 = x;
        float y0 = y;
        float z0 = z;
        float x1 = x + w;
        float y1 = y + h;
        float z1 = z + d;

        UV front = boxUV.front();
        UV back = boxUV.back();
        UV left = boxUV.left();
        UV right = boxUV.right();
        UV top = boxUV.top();
        UV bottom = boxUV.bottom();

        // front
        face(
                x0, y0, z1, front.u0(), front.v0(), 1f,
                x1, y0, z1, front.u1(), front.v0(), 1f,
                x1, y1, z1, front.u1(), front.v1(), 1f,
                x0, y1, z1, front.u0(), front.v1(), 1f
        );

        // back
        face(
                x1, y0, z0, back.u0(), back.v0(), 1f,
                x0, y0, z0, back.u1(), back.v0(), 1f,
                x0, y1, z0, back.u1(), back.v1(), 1f,
                x1, y1, z0, back.u0(), back.v1(), 1f
        );

        // left
        face(
                x0, y0, z0, left.u0(), left.v0(), 0.85f,
                x0, y0, z1, left.u1(), left.v0(), 0.85f,
                x0, y1, z1, left.u1(), left.v1(), 0.85f,
                x0, y1, z0, left.u0(), left.v1(), 0.85f
        );

        // right
        face(
                x1, y0, z1, right.u0(), right.v0(), 0.85f,
                x1, y0, z0, right.u1(), right.v0(), 0.85f,
                x1, y1, z0, right.u1(), right.v1(), 0.85f,
                x1, y1, z1, right.u0(), right.v1(), 0.85f
        );

        // top
        face(
                x0, y1, z1, top.u0(), top.v0(), 1.15f,
                x1, y1, z1, top.u1(), top.v0(), 1.15f,
                x1, y1, z0, top.u1(), top.v1(), 1.15f,
                x0, y1, z0, top.u0(), top.v1(), 1.15f
        );

        // bottom
        face(
                x0, y0, z0, bottom.u0(), bottom.v0(), 0.7f,
                x1, y0, z0, bottom.u1(), bottom.v0(), 0.7f,
                x1, y0, z1, bottom.u1(), bottom.v1(), 0.7f,
                x0, y0, z1, bottom.u0(), bottom.v1(), 0.7f
        );

        return this;
    }

    private void face(
            float x0, float y0, float z0, float u0, float v0, float shade0,
            float x1, float y1, float z1, float u1, float v1, float shade1,
            float x2, float y2, float z2, float u2, float v2, float shade2,
            float x3, float y3, float z3, float u3, float v3, float shade3
    ) {
        vertex(x0, y0, z0, u0, v0, shade0);
        vertex(x1, y1, z1, u1, v1, shade1);
        vertex(x2, y2, z2, u2, v2, shade2);

        vertex(x0, y0, z0, u0, v0, shade0);
        vertex(x2, y2, z2, u2, v2, shade2);
        vertex(x3, y3, z3, u3, v3, shade3);
    }

    private void vertex(float x, float y, float z, float u, float v, float shade) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(u);
        vertices.add(v);
        vertices.add(shade);
    }

    public float[] build() {
        return vertices.toArray();
    }
}