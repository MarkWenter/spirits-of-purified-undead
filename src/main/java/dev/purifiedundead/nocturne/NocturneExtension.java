package dev.purifiedundead.nocturne;

import com.github.tartaricacid.touhoulittlemaid.api.*;
import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

@LittleMaidExtension
public final class NocturneExtension implements ILittleMaid, IMaidBauble {
    @Override public void bindMaidBauble(BaubleManager manager) {
        manager.bind(ModItems.KNIGHT_NOCTURNE.get(), this);
    }

    private CompoundTag binding(EntityMaid maid, ItemStack stack) {
        if (maid.level().isClientSide || maid.getOwnerUUID() == null) return null;
        var charm = stack.getOrCreateTag().getCompound(NocturneItem.KEY);
        var record = NocturneLedger.get(maid.getServer()).record(maid.getOwnerUUID());
        return charm.hasUUID("owner") && charm.getUUID("owner").equals(maid.getOwnerUUID())
                && record.hasUUID("epoch") && charm.hasUUID("epoch")
                && charm.getUUID("epoch").equals(record.getUUID("epoch"))
                && charm.hasUUID("id") && record.hasUUID("id")
                && charm.getUUID("id").equals(record.getUUID("id")) ? record : null;
    }

    @Override public void onPutOn(EntityMaid maid, ItemStack stack) { onTick(maid, stack); }

    @Override public void onTick(EntityMaid maid, ItemStack stack) {
        var r = binding(maid, stack);
        if (!maid.isAlive() || r == null || (r.hasUUID("maid") && !maid.getUUID().equals(r.getUUID("maid")))) return;
        var ledger = NocturneLedger.get(maid.getServer());
        if (!r.hasUUID("maid") || r.getBoolean("dead")) {
            r.putUUID("maid", maid.getUUID());
            r.remove("dead");
            ledger.setDirty();
        }
        int pending = r.getInt("favor");
        if (pending > 0) {
            maid.setFavorability((int)Math.min(Integer.MAX_VALUE, (long)maid.getFavorability() + pending));
            r.remove("favor");
            ledger.setDirty();
        }
        long now = maid.level().getGameTime();
        if (r.getBoolean("paused") || now < r.getLong("next")) return;
        r.putLong("next", now + PurifiedUndeadConfig.NOCTURNE_INTERVAL.get());
        try {
            var attack = maid.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack == null) throw new IllegalStateException("missing maid attack attribute");
            var s = new CompoundTag();
            double base = attack.getBaseValue(), add = 0, mul = 0, prod = 1;
            for (var mod : attack.getModifiers()) {
                double value = mod.getAmount();
                switch (mod.getOperation()) {
                    case ADDITION -> add += value;
                    case MULTIPLY_BASE -> mul += value;
                    case MULTIPLY_TOTAL -> prod *= 1 + value;
                }
            }
            if (!Double.isFinite(base + add + mul + prod)) throw new IllegalStateException("invalid maid attributes");
            s.putDouble("base",base); s.putDouble("add",add); s.putDouble("mul",mul); s.putDouble("prod",prod);
            s.putInt("favorability", maid.getFavorability());
            putText(s, "backpack", maid.getMaidBackpackType().getId().toString());
            putText(s, "backpackItem", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(maid.getBackpackShowItem().getItem()).toString());
            s.putBoolean("showBackpack", maid.getConfigManager().isShowBackpack());
            s.putBoolean("showBackItem", maid.getConfigManager().isShowBackItem());
            s.putBoolean("leftHanded", maid.isLeftHanded());
            putText(s, "backpackFluid", maid.getBackpackFluid());
            if (maid.isYsmModel()) {
                var variables = new CompoundTag();
                if (maid.roamingVars != null) {
                    for (var entry : maid.roamingVars.object2FloatEntrySet()) {
                        if (variables.size() == EchoAppearance.MAX_VARIABLES) break;
                        if (EchoAppearance.validVariable(entry.getKey(), entry.getFloatValue()))
                            variables.putFloat(entry.getKey(), entry.getFloatValue());
                    }
                }
                if (!variables.isEmpty()) s.put("variables", variables);
                putText(s, "ysmModel", maid.getYsmModelId());
                putText(s, "ysmTexture", maid.getYsmModelTexture());
            }
            String model = maid.getModelId();
            if (model != null && model.length() <= 256) s.putString("model",model);
            if (!s.equals(r.getCompound("current"))) {
                if (r.contains("current")) r.put("previous",r.getCompound("current"));
                r.put("current",s);
                ledger.setDirty();
            }
        } catch (RuntimeException ex) {
            r.putBoolean("paused",true);
            ledger.setDirty();
            com.mojang.logging.LogUtils.getLogger().warn("Knight Nocturne snapshot paused for {}",maid.getUUID(),ex);
        }
    }

    private static void putText(CompoundTag s, String key, String value) {
        if (value != null && value.length() <= 256) s.putString(key,value);
    }

    @Override public void onTakeOff(EntityMaid maid, ItemStack stack) {
        var r = binding(maid, stack);
        if (r != null && !r.getBoolean("dead") && r.hasUUID("maid") && maid.getUUID().equals(r.getUUID("maid"))) {
            NocturneLedger.get(maid.getServer()).setDirty();
            for (String key : new String[]{"maid","current","previous","next","paused","dead","kills","favor"}) r.remove(key);
        }
    }
    @Override public boolean onDeath(EntityMaid maid, ItemStack stack, net.minecraft.world.damagesource.DamageSource source) {
        var r = binding(maid, stack);
        if (r != null && r.hasUUID("maid") && maid.getUUID().equals(r.getUUID("maid"))) {
            r.putBoolean("dead",true);
            NocturneLedger.get(maid.getServer()).setDirty();
        }
        return false;
    }
}
