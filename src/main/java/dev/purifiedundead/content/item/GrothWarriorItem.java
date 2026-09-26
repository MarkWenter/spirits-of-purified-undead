package dev.purifiedundead.content.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/** Reverses the Elder Warrior's blight and adds 0.5 to critical damage when available. */
public final class GrothWarriorItem extends Item implements ICurioItem {
    private static final ResourceLocation CRIT_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("attributeslib", "crit_damage");

    public GrothWarriorItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!FerinWarriorItem.SLOT_ID.equals(slotContext.identifier()) || slotContext.cosmetic()) {
            return false;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .map(handler -> handler.findCurios(ModItems.GROTH_WARRIOR.get()).stream()
                        .allMatch(found -> found.slotContext().identifier().equals(slotContext.identifier())
                                && found.slotContext().index() == slotContext.index()))
                .orElse(false);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        Attribute criticalDamage = ForgeRegistries.ATTRIBUTES.getValue(CRIT_DAMAGE);
        if (criticalDamage != null && FerinWarriorItem.SLOT_ID.equals(slotContext.identifier())
                && !slotContext.cosmetic()) {
            modifiers.put(criticalDamage, new AttributeModifier(uuid, "Groth critical damage",
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothCriticalDamageBonus),
                    AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext,
                                       net.minecraft.world.damagesource.DamageSource source,
                                       int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.purified_undead.groth_warrior.summary")
                .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("item.purified_undead.groth_warrior.effects")
                .withStyle(ChatFormatting.GRAY));
    }
}
