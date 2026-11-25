package dev.purifiedundead.nocturne;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public final class NocturneEffect {
    static CompoundTag record(ServerPlayer owner) {
        return NocturneLedger.get(owner.server).record(owner.getUUID());
    }
    public static CompoundTag appearance(ServerPlayer owner) {
        if (ModItems.KNIGHT_NOCTURNE == null) return new CompoundTag();
        var binding = record(owner);
        if (!binding.hasUUID("maid") || binding.getBoolean("dead")) return new CompoundTag();
        return EchoAppearance.fromSnapshot(binding.getCompound("current"));
    }

    public static boolean sealed(ServerPlayer owner) {
        return ModItems.KNIGHT_NOCTURNE != null && record(owner).getBoolean("dead");
    }
    public static float damage(ServerPlayer owner, float original) {
        if (ModItems.KNIGHT_NOCTURNE == null) return original;
        var r = record(owner);
        if (!r.hasUUID("maid") || !r.contains("current") || r.getBoolean("dead")) return original;
        var s = r.getCompound("current");
        double combined = ((original + s.getDouble("base") + s.getDouble("add"))
                * (1 + s.getDouble("mul")) * s.getDouble("prod"));
        return Double.isFinite(combined) && combined >= 0 && combined <= Float.MAX_VALUE ? (float)combined : original;
    }
    public static DamageSource source(ServerPlayer owner, DamageSource ordinary) {
        if (ModItems.KNIGHT_NOCTURNE != null && PurifiedUndeadConfig.NOCTURNE_MAID_DAMAGE.get()) {
            var r = record(owner);
            if (r.hasUUID("maid") && owner.serverLevel().getEntity(r.getUUID("maid")) instanceof net.minecraft.world.entity.LivingEntity maid && maid.isAlive())
                return new DamageSource(ordinary.typeHolder(), maid, owner);
        }
        return ordinary;
    }
    public static void kill(ServerPlayer owner) {
        if (ModItems.KNIGHT_NOCTURNE == null) return;
        var r = record(owner);
        if (!r.hasUUID("maid") || !r.contains("current") || r.getBoolean("dead")) return;
        NocturneLedger.get(owner.server).setDirty();
        int kills = r.getInt("kills") + 1;
        r.putInt("kills",kills % 20);
        if (kills >= 20 && r.getInt("favor") < Integer.MAX_VALUE) r.putInt("favor", r.getInt("favor")+1);
    }
}
