package org.minecraftclone.entity.core;

public final class UVs {
    private UVs() {
    }

    public static UV pixels(int x, int y, int w, int h, int texW, int texH) {
        float u0 = x / (float) texW;
        float v0 = y / (float) texH;
        float u1 = (x + w) / (float) texW;
        float v1 = (y + h) / (float) texH;
        return new UV(u0, v0, u1, v1);
    }
}
