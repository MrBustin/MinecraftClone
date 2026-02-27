package org.minecraftclone.world;

public record ChunkPos(int x, int z) {
    public long key() {
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }
}
