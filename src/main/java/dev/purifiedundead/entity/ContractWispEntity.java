package dev.purifiedundead.entity;

import dev.purifiedundead.client.WispMotion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A client-created visual proxy only: no server spawn, persistence, collision, AI or packets. */
public final class ContractWispEntity extends Entity {
    private Player owner;
    private Vec3 previousOwner;
    private double phase;
    private int luminance = 3;
    private boolean terrainLight;
    public ContractWispEntity(EntityType<? extends ContractWispEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    public void attach(Player player) {
        owner = player;
        phase = (player.getUUID().getLeastSignificantBits() & 65535) * .001;
        previousOwner = anchor();
        Vec3 p = WispMotion.idleAnchor(previousOwner, owner.getYRot(), tickCount, phase);
        setPos(p); xo = xOld = p.x; yo = yOld = p.y; zo = zOld = p.z;
    }
    private Vec3 anchor() { return owner.position().add(0, Math.min(1.15, owner.getBbHeight() * .7), 0); }
    public void setWarriors(int count) { luminance = WispMotion.lightLevel(count); }
    public int visualLight() { return luminance; }
    public void setTerrainLight(boolean value) { terrainLight = value; }
    public int terrainLight() { return !isRemoved() && terrainLight ? luminance : 0; }
    public Player owner() { return owner; }
    @Override public void tick() {
        super.tick(); // Dynamic light engines observe the normal Entity tick hook.
        if (!level().isClientSide || owner == null || !owner.isAlive() || owner.isRemoved()
                || owner.isSpectator() || owner.isInvisible() || owner.level() != level()) { discard(); return; }
        Vec3 now = anchor();
        Vec3 next = WispMotion.step(position(), now, now.subtract(previousOwner), owner.getYRot(), tickCount, phase);
        setPos(next); previousOwner = now;
    }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
