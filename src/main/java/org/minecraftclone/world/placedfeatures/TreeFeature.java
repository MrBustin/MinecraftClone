package org.minecraftclone.world.placedfeatures;

import org.minecraftclone.world.BlockType;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

import java.util.Random;

public class TreeFeature extends PlacedFeature {

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

        // 1) Place trunk first
        for (int ty = 0; ty < trunkH; ty++) {
            setSafe(cm, current, wx, wy + ty, wz, BlockType.LOG);
        }

        int ox = wx - w / 2;
        int oz = wz - d / 2;


        // 3) Start the canopy AFTER the trunk is generated
        int canopyBaseY = wy + trunkH;

        for (int y = 0; y < h; y++) {
            for (int z = 0; z < d; z++) {
                String row = LAYERS[y][z];
                for (int x = 0; x < w; x++) {
                    char ch = row.charAt(x);
                    if (ch == ' ') continue;

                    int px = ox + x;
                    int py = canopyBaseY + (y);
                    int pz = oz + z;

                    // keep your old leaf logic exactly
                    if (ch == 'F') setSafe(cm, current, px, py, pz, BlockType.LEAVES);

                    int num = getRandomNumber(1, 2);
                    if (num == 1) {
                        if (ch == 'X') setSafe(cm, current, px, py, pz, BlockType.LEAVES);
                    } else {
                        if (ch == 'X') setSafe(cm, current, px, py, pz, BlockType.AIR);
                    }
                }
            }
        }
    }

    public static int getRandomNumber(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }
}