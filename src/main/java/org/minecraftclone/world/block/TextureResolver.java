package org.minecraftclone.world.block;

@FunctionalInterface
public interface TextureResolver {
    UVRect get(Blocks block, Face face);
}
