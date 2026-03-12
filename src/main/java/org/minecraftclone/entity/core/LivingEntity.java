package org.minecraftclone.entity.core;

import org.minecraftclone.world.chunk.ChunkManager;

public abstract class LivingEntity extends Entity{
    protected float currentHealth;
    protected float maxHealth;

    protected float moveSpeed;
    protected float jumpStrength;

    protected boolean dead;
    protected int hurtTime;

    protected final EntityModel model;

    public LivingEntity(ChunkManager world, double x, double y, double z,
                        float width, float height,
                        float maxHealth, float moveSpeed, float jumpStrength, EntityModel model) {
        super(world, x, y, z, width, height);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.moveSpeed = moveSpeed;
        this.jumpStrength = jumpStrength;
        this.model = model;
    }


    @Override
    public void tick() {
        if (dead) return;

        if (hurtTime >0){
            hurtTime --;
        }
        super.tick();
    }

    public void damage(float amount){
        if (dead) return;
        if (hurtTime > 0) return;

        currentHealth -= amount;
        hurtTime = 10;

        if (currentHealth <= 0){
            currentHealth = 0;
            die();
        }
    }

    public void heal(float amount) {
        if (dead) return;
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    public void jump() {
        if (onGround) {
            vy = jumpStrength;
            onGround = false;
        }
    }

    protected void die() {
        dead = true;
        removed = true;
    }

    public boolean isDead() {
        return dead;
    }

    public float getHealth() {
        return currentHealth;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public EntityModel getModel() {
        return model;
    }
}
