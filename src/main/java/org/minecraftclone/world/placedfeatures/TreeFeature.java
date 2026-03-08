package org.minecraftclone.world.placedfeatures;

import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

import java.util.Random;

public class TreeFeature extends PlacedFeature {

    private static final Random RAND = new Random();

    private static final String[][] LAYERS = new String[][]{
            {
                    "XFFFX",
                    "FFFFF",
                    "FFFFF",
                    "FFFFF",
                    "XFFFX"
            },
            {
                    "XFFFX",
                    "FFFFF",
                    "FFFFF",
                    "FFFFF",
                    "XFFFX"
            },
            {
                    "     ",
                    " FFF ",
                    " FFF ",
                    " FFF ",
                    "     "
            },
            {
                    "     ",
                    "  F  ",
                    " FFF ",
                    "  F  ",
                    "     "
            }
    };

    @Override
    public void place(ChunkManager cm, Chunk current, int wx, int wy, int wz) {
        int h = LAYERS.length;
        int d = LAYERS[0].length;
        int w = LAYERS[0][0].length();

        int trunkH = getRandomNumber(1, 4);

        for (int ty = 0; ty < trunkH; ty++) {
            setSafe(cm, current, wx, wy + ty, wz, Blocks.LOG);
        }

        int ox = wx - w / 2;
        int oz = wz - d / 2;
        int canopyBaseY = wy + trunkH;

        for (int y = 0; y < h; y++) {
            for (int z = 0; z < d; z++) {
                String row = LAYERS[y][z];
                for (int x = 0; x < w; x++) {
                    char ch = row.charAt(x);
                    if (ch == ' ') continue;

                    int px = ox + x;
                    int py = canopyBaseY + y;
                    int pz = oz + z;

                    if (ch == 'F') {
                        setSafe(cm, current, px, py, pz, Blocks.LEAVES);
                    } else if (ch == 'X') {
                        if (getRandomNumber(1, 2) == 1) {
                            setSafe(cm, current, px, py, pz, Blocks.LEAVES);
                        }
                    }
                }
            }
        }
    }

    public static int getRandomNumber(int min, int max) {
        return RAND.nextInt(max - min + 1) + min;
    }
}