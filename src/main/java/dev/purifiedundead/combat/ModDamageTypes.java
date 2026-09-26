package dev.purifiedundead.combat;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> FERIN_ASSIST = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "ferin_assist"));
    public static final ResourceKey<DamageType> ULV_BLIGHT = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "ulv_blight"));
    public static final ResourceKey<DamageType> ULV_FOLLOW_UP = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "ulv_follow_up"));
    public static final ResourceKey<DamageType> ELEINE_MAGIC_ORB = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "eleine_magic_orb"));

    private ModDamageTypes() { }

    public static DamageSource ferinAssist(Level level, Player owner) {
        var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(FERIN_ASSIST);
        // The player remains the causing entity for attribution. The direct entity
        // is deliberately absent until Ferin has an entity: this prevents attribute
        // libraries from automatically rolling another per-target direct-hit crit.
        return new DamageSource(holder, null, owner);
    }

    public static DamageSource ulvBlight(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ULV_BLIGHT));
    }

    public static DamageSource ulvFollowUp(Level level, Player owner) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ULV_FOLLOW_UP), null, owner);
    }

    public static DamageSource eleineMagicOrb(Level level, Entity directEntity, Player owner) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ELEINE_MAGIC_ORB), directEntity, owner);
    }
}
