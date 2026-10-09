package dev.purifiedundead.content.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.purifiedundead.progress.WarriorRewardService;
import dev.purifiedundead.progress.ContractProgressService;
import dev.purifiedundead.progress.WhiteWitchTalisman;
import dev.purifiedundead.content.ContractRoster;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/** Irremovable contract that applies the active ancient blights and unlocks warrior slots. */
public final class AncientContractItem extends Item implements ICurioItem {
    public static final String SLOT_ID = "ancient_contract";
    public static final String WHITE_WITCH_RELIC_SLOT_ID = "white_witch_relic";
    private static final String TALISMAN_LEVEL_KEY = "purified_undead:talisman_level";
    private static final String RESOLVED_KEY = "purified_undead:resolved_blights";
    // Tooltip blight order differs from the general warrior roster (Guardians before Julius).
    private static final List<String> BLIGHT_WARRIORS =
            List.of(
                    "ferin_warrior",
                    "groth_warrior",
                    "guardian_warriors",
                    "julius_warrior",
                    "ulv_warrior",
                    "eleine_warrior",
                    "hoenir_warrior",
                    "faden_warrior");

    public AncientContractItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return SLOT_ID.equals(slotContext.identifier()) && !slotContext.cosmetic();
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        // Creative mode serves as the development/admin recovery path.
        return slotContext.entity() instanceof Player player && player.isCreative();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        ModEnchantments.applyAllBlightCurses(stack);
        if (!slotContext.entity().level().isClientSide()
                && slotContext.entity() instanceof ServerPlayer player) {
            updateTooltipProgress(stack, player);
            WarriorRewardService.onContractEquipped(player);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide()) {
            ModEnchantments.applyAllBlightCurses(stack);
            if (slotContext.entity() instanceof ServerPlayer player && player.tickCount % 5 == 0) {
                updateTooltipProgress(stack, player);
            }
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (SLOT_ID.equals(slotContext.identifier()) && !slotContext.cosmetic()) {
            CuriosApi.addSlotModifier(
                    modifiers,
                    FerinWarriorItem.SLOT_ID,
                    uuid,
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.warriorSlots),
                    AttributeModifier.Operation.ADDITION);
            CuriosApi.addSlotModifier(
                    modifiers,
                    WHITE_WITCH_RELIC_SLOT_ID,
                    uuid,
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicSlots),
                    AttributeModifier.Operation.ADDITION);
        }
        return modifiers;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ModEnchantments.applyAllBlightCurses(stack);
        return stack;
    }

    @Override
    public void inventoryTick(
            ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide()) {
            ModEnchantments.applyAllBlightCurses(stack);
            if (entity instanceof ServerPlayer player && player.tickCount % 5 == 0) {
                updateTooltipProgress(stack, player);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public ICurio.DropRule getDropRule(
            SlotContext slotContext,
            net.minecraft.world.damagesource.DamageSource source,
            int lootingLevel,
            boolean recentlyHit,
            ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(
                Component.translatable("item.purified_undead.ancient_contract.summary")
                        .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(
                Component.translatable("item.purified_undead.ancient_contract.curse")
                        .withStyle(ChatFormatting.DARK_RED));
        for (int index = 1; index <= 8; index++) {
            boolean resolved =
                    (stack.getOrCreateTag().getInt(RESOLVED_KEY) & (1 << (index - 1))) != 0;
            lines.add(
                    Component.translatable(
                                    "item.purified_undead.ancient_contract."
                                            + (resolved ? "resolved." : "blight.")
                                            + index)
                            .withStyle(resolved ? ChatFormatting.AQUA : ChatFormatting.RED));
            lines.add(
                    Component.translatable("item.purified_undead.ancient_contract.answer." + index)
                            .withStyle(ChatFormatting.GRAY));
        }
        int talismanLevel = stack.getOrCreateTag().getInt(TALISMAN_LEVEL_KEY);
        lines.add(
                Component.translatable(
                                "item.purified_undead.ancient_contract.talisman",
                                talismanLevel,
                                WhiteWitchTalisman.maxLevel())
                        .withStyle(ChatFormatting.WHITE));
    }

    private static void updateTooltipProgress(ItemStack stack, ServerPlayer player) {
        int mask =
                CuriosApi.getCuriosInventory(player)
                        .map(
                                handler -> {
                                    int equipped = 0;
                                    for (int i = 0; i < BLIGHT_WARRIORS.size(); i++) {
                                        var item =
                                                net.minecraftforge.registries.ForgeRegistries.ITEMS
                                                        .getValue(
                                                                net.minecraft.resources
                                                                        .ResourceLocation
                                                                        .fromNamespaceAndPath(
                                                                                "purified_undead",
                                                                                BLIGHT_WARRIORS.get(
                                                                                        i)));
                                        if (item != null && handler.isEquipped(item))
                                            equipped |= 1 << i;
                                    }
                                    return equipped;
                                })
                        .orElse(0);
        if (stack.getOrCreateTag().getInt(RESOLVED_KEY) != mask) {
            stack.getOrCreateTag().putInt(RESOLVED_KEY, mask);
        }
        int level = ContractProgressService.talismanLevel(player);
        if (stack.getOrCreateTag().getInt(TALISMAN_LEVEL_KEY) != level) {
            stack.getOrCreateTag().putInt(TALISMAN_LEVEL_KEY, level);
        }
    }
}
