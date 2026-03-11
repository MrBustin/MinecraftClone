package org.minecraftclone.entity;

import org.minecraftclone.world.block.Blocks;
import org.minecraftclone.world.chunk.Chunk;
import org.minecraftclone.world.chunk.ChunkManager;

public abstract class Entity {
    protected final ChunkManager world;

    protected double x, y, z;
    protected double prevX, prevY, prevZ;
    protected double vx, vy, vz;

    protected float width;
    protected float height;

    protected boolean onGround;
    protected boolean removed;

    public Entity(ChunkManager world, double x, double y, double z, float width, float height) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.width = width;
        this.height = height;
    }

    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;

        applyGravity();
        moveAndCollide(vx, vy, vz);
        checkGroundSupport();
        applyDrag();
    }

    public int getChunkX() {
        return Math.floorDiv((int)Math.floor(x), Chunk.SIZE);
    }

    public int getChunkZ() {
        return Math.floorDiv((int)Math.floor(z), Chunk.SIZE);
    }

    public void remove() {
        removed = true;
    }

    protected void applyGravity() {
        if (!onGround) {
            vy -= 0.08;
        }
    }

    protected void applyDrag() {
        vx *= 0.91;
        vz *= 0.91;

        if (!onGround) {
            vy *= 0.98;
        }
    }

    protected void moveAndCollide(double dx, double dy, double dz) {
        if (!intersectsSolidBlocks(getBoundingBoxAt(x + dx, y, z))) {
            x += dx;
        } else {
            vx = 0;
        }

        if (!intersectsSolidBlocks(getBoundingBoxAt(x, y + dy, z))) {
            y += dy;
        } else {
            if (dy < 0) {
                vy = 0;
            }
        }

        if (!intersectsSolidBlocks(getBoundingBoxAt(x, y, z + dz))) {
            z += dz;
        } else {
            vz = 0;
        }
    }

    protected boolean isSolidForCollision(int bx, int by, int bz) {
        int cx = Math.floorDiv(bx, Chunk.SIZE);
        int cz = Math.floorDiv(bz, Chunk.SIZE);

        Chunk chunk = world.getIfLoaded(cx, cz);
        if (chunk == null) return true;

        Blocks block = world.getBlockIfLoaded(bx, by, bz);
        return block.isSolid();
    }

    protected AABB getBoundingBoxAt(double x, double y, double z) {
        double half = width / 2.0;
        return new AABB(
                x - half, y, z - half,
                x + half, y + height, z + half
        );
    }

    protected boolean intersectsSolidBlocks(AABB box) {
        int minX = (int)Math.floor(box.minX);
        int maxX = (int)Math.floor(box.maxX);
        int minY = (int)Math.floor(box.minY);
        int maxY = (int)Math.floor(box.maxY);
        int minZ = (int)Math.floor(box.minZ);
        int maxZ = (int)Math.floor(box.maxZ);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (isSolidForCollision(bx, by, bz)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void checkGroundSupport() {
        double half = width / 2.0;

        AABB supportBox = new AABB(
                x - half,
                y - 0.05,
                z - half,
                x + half,
                y - 0.001,
                z + half
        );

        onGround = intersectsSolidBlocks(supportBox);
    }

    public boolean isRemoved() {
        return removed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}