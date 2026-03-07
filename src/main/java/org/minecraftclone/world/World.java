package org.minecraftclone.world;

import org.minecraftclone.world.block.Blocks;

public class World {

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private final Blocks[][][] blocks;

    public World(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        blocks = new Blocks[sizeX][sizeY][sizeZ];

        generateFlatWorld();
    }

    private void generateFlatWorld() {
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int y = 0; y < sizeY; y++) {
                    if (y == 0) {
                        blocks[x][y][z] = Blocks.STONE;
                    } else if (y < 3) {
                        blocks[x][y][z] = Blocks.DIRT;
                    } else {
                        blocks[x][y][z] = Blocks.AIR;
                    }
                }
            }
        }
    }

    public Blocks getBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
                x >= sizeX || y >= sizeY || z >= sizeZ)
            return Blocks.AIR;

        return blocks[x][y][z];
    }

    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
}
