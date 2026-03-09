package org.minecraftclone.world.block;

public enum Blocks {

    AIR(false),
    DIRT(true),
    STONE(true),
    GRASS(true),
    SAND(true),
    WATER(false),
    LOG(true),
    SNOW(true),
    LEAVES(true),
    FLOWER(false),
    TALL_GRASS(false),
    REEDS(false);

    private final boolean solid;

    Blocks(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}
