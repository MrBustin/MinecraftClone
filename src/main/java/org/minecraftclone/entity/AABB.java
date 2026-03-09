package org.minecraftclone.entity;

public class AABB {
    public double minX, minY, minZ;
    public double maxX, maxY, maxZ;

    public AABB(double minX, double minY, double minZ,
                double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
}
