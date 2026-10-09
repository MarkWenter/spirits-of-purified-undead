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
    private Vec3 filteredMotion = Vec3.ZERO;
    private double phase;
    private int visualAge;

    public int visualAge() {
        return visualAge;
    }

    private int luminance = 3;
    private boolean terrainLight;

    public ContractWispEntity(EntityType<? extends ContractWispEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setNoGravity(true);
    }

    public void attach(Player player) {
        owner = player;
        phase = (player.getUUID().getLeastSignificantBits() & 65535) * .001;
        previousOwner = anchor();
        Vec3 p = WispMotion.idleAnchor(previousOwner, owner.getYRot(), tickCount, phase);
        setPos(p);
        xo = xOld = p.x;
        yo = yOld = p.y;
        zo = zOld = p.z;
    }

    public void restoreVisualFrom(ContractWispEntity previous) {
        if (previous.owner == owner && previous.position().distanceToSqr(anchor()) <= 16) {
            setPos(previous.position());
            xo = xOld = getX();
            yo = yOld = getY();
            zo = zOld = getZ();
            visualAge = previous.visualAge;
            phase = previous.phase;
            filteredMotion = previous.filteredMotion;
        }
    }

    private Vec3 anchor() {
        return owner.position().add(0, Math.min(1.15, owner.getBbHeight() * .7), 0);
    }

    public void setWarriors(int count) {
        luminance = WispMotion.lightLevel(count);
    }

    public int visualLight() {
        return luminance;
    }

    public void setTerrainLight(boolean value) {
        terrainLight = value;
    }

    public int terrainLight() {
        return !isRemoved() && terrainLight ? luminance : 0;
    }

    public Player owner() {
        return owner;
    }

    @Override
    public void tick() {
        // The client manager owns the clock, independently of chunk entity ticking.
    }

    @Override
    public void baseTick() {
        /* Cosmetic proxy: no fluid, portal or void processing. */
    }

    public void advanceVisual() {
        if (!level().isClientSide
                || owner == null
                || !owner.isAlive()
                || owner.isRemoved()
                || owner.isSpectator()
                || owner.level() != level()) {
            discard();
            return;
        }
        noPhysics = true;
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        Vec3 old = position(), now = anchor();
        Vec3 rawMotion = now.subtract(previousOwner);
        filteredMotion =
                rawMotion.lengthSqr() > 144 ? rawMotion : filteredMotion.lerp(rawMotion, .5);
        Vec3 next = WispMotion.step(old, now, filteredMotion, owner.getYRot(), ++visualAge, phase);
        setPos(next);
        previousOwner = now;
        // Do not interpolate an entire teleport path across the screen.
        if (old.distanceToSqr(next) > 256) old = next;
        super.tick(); // Exactly once per client tick; keeps optional light-engine hooks.
        xo = xOld = old.x;
        yo = yOld = old.y;
        zo = zOld = old.z;
    }

    @Override
    protected net.minecraft.world.phys.AABB makeBoundingBox() {
        return new net.minecraft.world.phys.AABB(position(), position());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64 * 64;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
