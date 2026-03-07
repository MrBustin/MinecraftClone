package org.minecraftclone.world.block;

public record BlockDefinition(
        BlockType modelType,
        boolean transparent,
        boolean occludesFaces,
        TextureResolver textures
) { }
