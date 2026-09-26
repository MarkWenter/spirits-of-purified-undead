package dev.purifiedundead.content.item;

import dev.purifiedundead.content.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/** Reverses the Guardians' blight and enables their aerial movement. */
public final class GuardianWarriorsItem extends Item implements ICurioItem {
    public GuardianWarriorsItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!FerinWarriorItem.SLOT_ID.equals(slotContext.identifier()) || slotContext.cosmetic()) {
            return false;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .map(handler -> handler.findCurios(ModItems.GUARDIAN_WARRIORS.get()).stream()
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.purified_undead.guardian_warriors.summary")
                .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("item.purified_undead.guardian_warriors.effects")
                .withStyle(ChatFormatting.GRAY));
    }
}
