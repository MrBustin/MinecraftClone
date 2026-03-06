package org.minecraftclone.world.placedfeatures;

import org.minecraftclone.world.BlockType;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

public abstract class PlacedFeature {

    /** Place feature at WORLD coords (wx, wy, wz), with a known current chunk being generated. */
    public abstract void place(ChunkManager cm, Chunk current, int wx, int wy, int wz);

    /** Safe worldgen setter: write into current chunk if it matches, otherwise only into already-loaded neighbors. */
    protected static void setSafe(ChunkManager cm, Chunk current, int wx, int wy, int wz, BlockType type) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return;

        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);

        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        // If target block is inside the chunk we're currently generating, write directly.
        if (current != null && current.cx() == cx && current.cz() == cz) {
            current.set(lx, wy, lz, type);
            return;
        }

        // Otherwise, ONLY set if neighbor chunk is already loaded (no recursion).
        // Otherwise, set if neighbor chunk is loaded; if not, queue it for when it loads.
        if (cm.getIfLoaded(cx, cz) != null) {
            cm.setBlockIfLoaded(wx, wy, wz, type);
        } else {
            cm.queueBlock(wx, wy, wz, type);
        }
    }

    // copy of ChunkManager's helpers (keep private to avoid recursion / dependency)
    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    private static int mod(int a, int b) {
        int m = a % b;
        if (m < 0) m += b;
        return m;
    }
}
