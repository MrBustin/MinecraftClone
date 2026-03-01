package org.minecraftclone.core;

import org.joml.Vector3f;
import org.minecraftclone.world.BlockType;
import org.minecraftclone.world.ChunkManager;

public class Player {
    public final Vector3f pos = new Vector3f(8, 40, 8);
    public final Vector3f vel = new Vector3f();

    public float yaw = -90f;
    public float pitch = 0f;

    public boolean onGround = false;

    // Minecraft-ish size
    public static final float WIDTH = 0.6f;
    public static final float HEIGHT = 1.8f;
    public static final float EYE_HEIGHT = 1.62f;

    private static final float GRAVITY = 30f;      // blocks/s^2
    private static final float JUMP_VEL = 8.5f;    // blocks/s
    private static final float MOVE_SPEED = 5.0f;  // blocks/s

    public void update(ChunkManager world, float dt, boolean forward, boolean back, boolean left, boolean right, boolean jump) {
        // --- Build movement direction from yaw ---
        float rad = (float) Math.toRadians(yaw);
        float fx = (float) Math.cos(rad);
        float fz = (float) Math.sin(rad);

        // forward vector on XZ plane
        Vector3f fwd = new Vector3f(fx, 0, fz).normalize();
        Vector3f str = new Vector3f(fwd).cross(0, 1, 0).normalize(); // right

        Vector3f wish = new Vector3f();
        if (forward) wish.add(fwd);
        if (back)    wish.sub(fwd);
        if (right)   wish.add(str);
        if (left)    wish.sub(str);

        if (wish.lengthSquared() > 0) wish.normalize().mul(MOVE_SPEED);

        // set horizontal velocity from input (simple version)
        vel.x = wish.x;
        vel.z = wish.z;

        // gravity
        vel.y -= GRAVITY * dt;

        // jump
        if (jump && onGround) {
            vel.y = JUMP_VEL;
            onGround = false;
        }

        // move + collide
        moveAndCollide(world, dt);
    }

    private void moveAndCollide(ChunkManager world, float dt) {
        // axis-separated movement (stable + easy)
        onGround = false;

        pos.x += vel.x * dt;
        collideAxis(world, 0);

        pos.z += vel.z * dt;
        collideAxis(world, 2);

        pos.y += vel.y * dt;
        collideAxis(world, 1);
    }

    // axis: 0=x, 1=y, 2=z
    private void collideAxis(ChunkManager world, int axis) {
        float half = WIDTH * 0.5f;

        float minX = pos.x - half, maxX = pos.x + half;
        float minY = pos.y,        maxY = pos.y + HEIGHT;
        float minZ = pos.z - half, maxZ = pos.z + half;

        int x0 = (int) Math.floor(minX), x1 = (int) Math.floor(maxX);
        int y0 = (int) Math.floor(minY), y1 = (int) Math.floor(maxY);
        int z0 = (int) Math.floor(minZ), z1 = (int) Math.floor(maxZ);

        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    BlockType b = world.getBlock(x, y, z); // NOTE: use getBlock (loads) for collision
                    if (b == BlockType.AIR || b == BlockType.WATER) continue; // treat water non-solid for now

                    // block AABB is [x,x+1] etc
                    float bx0 = x, bx1 = x + 1;
                    float by0 = y, by1 = y + 1;
                    float bz0 = z, bz1 = z + 1;

                    if (maxX <= bx0 || minX >= bx1 || maxY <= by0 || minY >= by1 || maxZ <= bz0 || minZ >= bz1) continue;

                    // resolve penetration along axis
                    if (axis == 0) {
                        if (vel.x > 0) pos.x = bx0 - half;
                        else if (vel.x < 0) pos.x = bx1 + half;
                        vel.x = 0;
                        minX = pos.x - half; maxX = pos.x + half;
                    } else if (axis == 2) {
                        if (vel.z > 0) pos.z = bz0 - half;
                        else if (vel.z < 0) pos.z = bz1 + half;
                        vel.z = 0;
                        minZ = pos.z - half; maxZ = pos.z + half;
                    } else {
                        if (vel.y > 0) {
                            pos.y = by0 - HEIGHT;
                            vel.y = 0;
                        } else if (vel.y < 0) {
                            pos.y = by1;
                            vel.y = 0;
                            onGround = true;
                        }
                        minY = pos.y; maxY = pos.y + HEIGHT;
                    }
                }
            }
        }
    }
}
