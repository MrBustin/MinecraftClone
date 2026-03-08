package org.minecraftclone.world.placedfeatures;

import org.minecraftclone.world.FastNoiseLite;
import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

import java.util.Random;


public class GroundFoliageFeature extends PlacedFeature {
    private static final Random RAND = new Random();

    public GroundFoliageFeature() {}

    @Override
    public void place(ChunkManager cm, Chunk current, int wx, int wy, int wz) {
        if (wy <= 0 || wy >= Chunk.HEIGHT) return;

        int cx = floorDiv(wx, Chunk.SIZE);
        int cz = floorDiv(wz, Chunk.SIZE);
        int lx = mod(wx, Chunk.SIZE);
        int lz = mod(wz, Chunk.SIZE);

        Blocks here;
        Blocks below;

        if (current != null && current.cx() == cx && current.cz() == cz) {
            here = current.get(lx, wy, lz);
            below = current.get(lx, wy - 1, lz);
        } else {
            here = cm.getBlockIfLoaded(wx, wy, wz);
            below = cm.getBlockIfLoaded(wx, wy - 1, wz);
        }

        if (here != Blocks.AIR) return;
        if (below != Blocks.GRASS) return;

        if (getRandomNumber(1, 10) == 1) {
            setSafe(cm, current, wx, wy, wz, Blocks.FLOWER);
        }else{
            setSafe(cm, current, wx, wy, wz, Blocks.TALL_GRASS);
        }


    }

    public static int getRandomNumber(int min, int max) {
        return RAND.nextInt(max - min + 1) + min;
    }

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
