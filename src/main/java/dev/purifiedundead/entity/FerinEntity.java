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

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private long lastAnimatedStageStartedAt = Long.MIN_VALUE;
    private int lastAnimatedStage = -1;
    private int lastOwnerRefreshTick;

    /** Non-networked lease: a visual without an authoritative owner update must not linger. */
    public void refreshOwnerLease() { lastOwnerRefreshTick = tickCount; }

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
        if (!level().isClientSide() && (tickCount - lastOwnerRefreshTick > 100
                || getOwnerUuid().map(level()::getPlayerByUUID)
                .filter(player -> player.isAlive()).isEmpty())) {
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
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            // A new attack must also restart when it repeats the same stage after a timeout.
            if (lastAnimatedStage != getStage() || lastAnimatedStageStartedAt != getStageStartedAt()) {
                state.getController().forceAnimationReset();
                lastAnimatedStage = getStage();
                lastAnimatedStageStartedAt = getStageStartedAt();
            }
            return state.setAndContinue(RawAnimation.begin()
                    .thenPlayAndHold(FerinVisualModel.animationForStage(getStage())));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
