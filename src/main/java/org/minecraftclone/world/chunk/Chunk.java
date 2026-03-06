package org.minecraftclone.world.chunk;

import org.minecraftclone.world.BlockType;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 64; // bump to 256 later

    private final int cx, cz;
    private final BlockType[] blocks = new BlockType[SIZE * HEIGHT * SIZE];

    private boolean dirty = true; // needs mesh rebuild

    public Chunk(int cx, int cz) {
        this.cx = cx;
        this.cz = cz;
        for (int i = 0; i < blocks.length; i++) blocks[i] = BlockType.AIR;
    }

    public int cx() { return cx; }
    public int cz() { return cz; }

    private int idx(int x, int y, int z) {
        return (y * SIZE + z) * SIZE + x;
    }

    public BlockType get(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= SIZE || y >= HEIGHT || z >= SIZE) return BlockType.AIR;
        return blocks[idx(x, y, z)];
    }

    public void set(int x, int y, int z, BlockType type) {
        if (x < 0 || y < 0 || z < 0 || x >= SIZE || y >= HEIGHT || z >= SIZE) return;
        blocks[idx(x, y, z)] = type;
        dirty = true;
    }

    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    public void markDirty() { dirty = true; }
}
