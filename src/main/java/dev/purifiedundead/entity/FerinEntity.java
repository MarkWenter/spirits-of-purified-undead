package dev.purifiedundead.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

/** Network-synchronized visual carrier for Ferin. Combat remains server-owned by FerinCombatEvents. */
public final class FerinEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> STAGE =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> STAGE_STARTED_AT =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.LONG);

    private static final EntityDataAccessor<org.joml.Vector3f> FLIGHT_DIRECTION =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> FLIGHT_LIFETIME =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT_AGE =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<net.minecraft.core.BlockPos> FLIGHT_CELL =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<org.joml.Vector3f> FLIGHT_OFFSET =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> FLIGHT_SPEED =
            SynchedEntityData.defineId(FerinEntity.class, EntityDataSerializers.FLOAT);
    private double lerpX, lerpY, lerpZ;
    private int lerpSteps;

    public net.minecraft.world.phys.Vec3 flightDirection() {
        return new net.minecraft.world.phys.Vec3(entityData.get(FLIGHT_DIRECTION));
    }

    public int flightLifetime() {
        return entityData.get(FLIGHT_LIFETIME);
    }

    public int flightAge() {
        return entityData.get(FLIGHT_AGE);
    }

    public void syncFlight(
            net.minecraft.world.phys.Vec3 position,
            net.minecraft.world.phys.Vec3 direction,
            int lifetime,
            int age,
            double speed) {
        setPos(position);
        var cell = net.minecraft.core.BlockPos.containing(position);
        entityData.set(FLIGHT_CELL, cell);
        entityData.set(
                FLIGHT_OFFSET,
                position.subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(cell))
                        .toVector3f());
        entityData.set(FLIGHT_SPEED, (float) speed);
        entityData.set(FLIGHT_DIRECTION, direction.toVector3f());
        entityData.set(FLIGHT_LIFETIME, lifetime);
        entityData.set(FLIGHT_AGE, age);
    }

    /** Bounded presentation prediction only. Server state corrects it; it cannot create hits. */
    public net.minecraft.world.phys.Vec3 predictedFlightPosition(float partialTick) {
        var position =
                net.minecraft.world.phys.Vec3.atLowerCornerOf(entityData.get(FLIGHT_CELL))
                        .add(new net.minecraft.world.phys.Vec3(entityData.get(FLIGHT_OFFSET)));
        double elapsed = level().getGameTime() - getStageStartedAt() + partialTick - flightAge();
        double extrapolation =
                Math.max(0, Math.min(2, Math.min(flightLifetime() - flightAge(), elapsed)));
        return position.add(flightDirection().scale(entityData.get(FLIGHT_SPEED) * extrapolation));
    }

    @Override
    public void lerpTo(
            double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (teleport
                || position().distanceToSqr(new net.minecraft.world.phys.Vec3(x, y, z)) > 4096) {
            setPos(x, y, z);
            lerpSteps = 0;
        } else {
            lerpX = x;
            lerpY = y;
            lerpZ = z;
            lerpSteps = 2;
        }
    }

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private long lastAnimatedStageStartedAt = Long.MIN_VALUE;
    private int lastAnimatedStage = -1;
    private int lastOwnerRefreshTick;

    /** Non-networked lease: a visual without an authoritative owner update must not linger. */
    public void refreshOwnerLease() {
        lastOwnerRefreshTick = tickCount;
    }

    public FerinEntity(EntityType<? extends FerinEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, Optional.empty());
        entityData.define(STAGE, 0);
        entityData.define(STAGE_STARTED_AT, 0L);
        entityData.define(FLIGHT_DIRECTION, new org.joml.Vector3f(0, 0, 1));
        entityData.define(FLIGHT_LIFETIME, 0);
        entityData.define(FLIGHT_AGE, 0);
        entityData.define(FLIGHT_CELL, net.minecraft.core.BlockPos.ZERO);
        entityData.define(FLIGHT_OFFSET, new org.joml.Vector3f());
        entityData.define(FLIGHT_SPEED, 0F);
    }

    public Optional<UUID> getOwnerUuid() {
        return entityData.get(OWNER);
    }

    public boolean belongsTo(UUID ownerUuid) {
        return getOwnerUuid().filter(ownerUuid::equals).isPresent();
    }

    public void setOwnerUuid(UUID ownerUuid) {
        entityData.set(OWNER, Optional.of(ownerUuid));
    }

    public int getStage() {
        return entityData.get(STAGE);
    }

    public long getStageStartedAt() {
        return entityData.get(STAGE_STARTED_AT);
    }

    public void syncStage(int stage, long startedAt) {
        if (stage < 1 || stage > 5) {
            throw new IllegalArgumentException("Ferin visual stage must be between one and five");
        }
        entityData.set(STAGE, stage);
        entityData.set(STAGE_STARTED_AT, startedAt);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && lerpSteps > 0) {
            setPos(
                    getX() + (lerpX - getX()) / lerpSteps,
                    getY() + (lerpY - getY()) / lerpSteps,
                    getZ() + (lerpZ - getZ()) / lerpSteps);
            lerpSteps--;
        }
        if (!level().isClientSide()
                && (tickCount - lastOwnerRefreshTick > 100
                        || getOwnerUuid()
                                .map(level()::getPlayerByUUID)
                                .filter(player -> player.isAlive())
                                .isEmpty())) {
            discard();
            return;
        }
        noPhysics = true;
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        // Player state is authoritative and recreates this visual after login/restart.
        // Keeping the visual out of chunk NBT prevents orphan or duplicate Ferin entities.
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner")) {
            setOwnerUuid(tag.getUUID("owner"));
        }
        int stage = tag.getInt("stage");
        if (stage >= 1 && stage <= 5) {
            syncStage(stage, tag.getLong("stage_started_at"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        getOwnerUuid().ifPresent(uuid -> tag.putUUID("owner", uuid));
        tag.putInt("stage", getStage());
        tag.putLong("stage_started_at", getStageStartedAt());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "main",
                        0,
                        state -> {
                            // A new attack must also restart when it repeats the same stage after a
                            // timeout.
                            if (lastAnimatedStage != getStage()
                                    || lastAnimatedStageStartedAt != getStageStartedAt()) {
                                state.getController().forceAnimationReset();
                                lastAnimatedStage = getStage();
                                lastAnimatedStageStartedAt = getStageStartedAt();
                            }
                            return state.setAndContinue(
                                    RawAnimation.begin()
                                            .thenPlayAndHold(
                                                    FerinVisualModel.animationForStage(
                                                            getStage())));
                        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
