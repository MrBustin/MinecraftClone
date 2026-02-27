package org.minecraftclone.gfx;


public final class FloatList {
    private float[] data = new float[1024];
    private int size = 0;

    public void add(float v) {
        if (size >= data.length) {
            float[] n = new float[data.length * 2];
            System.arraycopy(data, 0, n, 0, data.length);
            data = n;
        }
        data[size++] = v;
    }

    public float[] toArray() {
        float[] out = new float[size];
        System.arraycopy(data, 0, out, 0, size);
        return out;
    }

    public int size() { return size; }
}
