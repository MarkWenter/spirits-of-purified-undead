package dev.purifiedundead.content.relic;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.item.AncientContractItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Shared slot restrictions and death retention for White Witch Relics. */
public abstract class WhiteWitchRelicItem extends Item implements ICurioItem, WhiteWitchRelicEffect {
    protected WhiteWitchRelicItem(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicsEnabled)
                || !AncientContractItem.WHITE_WITCH_RELIC_SLOT_ID.equals(slotContext.identifier())
                || slotContext.cosmetic()) {
            return false;
        }
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.allowDuplicateWhiteWitchRelics)) {
            return true;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .map(handler -> handler.findCurios(stack.getItem()).stream()
                        .allMatch(found -> found.slotContext().identifier().equals(slotContext.identifier())
                                && found.slotContext().index() == slotContext.index()))
                .orElse(false);
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext,
                                       net.minecraft.world.damagesource.DamageSource source,
                                       int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }
}
