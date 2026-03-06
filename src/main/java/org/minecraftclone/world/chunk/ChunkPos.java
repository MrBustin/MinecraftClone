package org.minecraftclone.world.chunk;

public record ChunkPos(int x, int z) {
    public long key() {
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }
}
