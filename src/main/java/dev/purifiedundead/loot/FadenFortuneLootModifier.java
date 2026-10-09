package dev.purifiedundead.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.purifiedundead.combat.FadenCombatEvents;
import dev.purifiedundead.combat.FadenModel;
import dev.purifiedundead.content.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/** Re-evaluates player-broken block loot with one extra effective Fortune level. */
public final class FadenFortuneLootModifier extends LootModifier {
    public static final Codec<FadenFortuneLootModifier> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            codecStart(instance).apply(instance, FadenFortuneLootModifier::new));
    private static final ThreadLocal<Boolean> REEVALUATING = ThreadLocal.withInitial(() -> false);

    public FadenFortuneLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (REEVALUATING.get()
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player)
                || !FadenCombatEvents.isReversed(player)) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        net.minecraft.world.phys.Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        ItemStack originalTool = context.getParamOrNull(LootContextParams.TOOL);
        if (state == null || origin == null || originalTool == null) {
            return generatedLoot;
        }

        ItemStack boostedTool =
                originalTool.isEmpty() ? new ItemStack(Items.STICK) : originalTool.copy();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(boostedTool);
        int fortune =
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, boostedTool);
        enchantments.put(
                Enchantments.BLOCK_FORTUNE, FadenModel.effectiveEnchantmentLevel(fortune, true));
        EnchantmentHelper.setEnchantments(enchantments, boostedTool);

        BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        try {
            REEVALUATING.set(true);
            return new ObjectArrayList<>(
                    Block.getDrops(
                            state,
                            context.getLevel(),
                            BlockPos.containing(origin),
                            blockEntity,
                            player,
                            boostedTool));
        } finally {
            REEVALUATING.remove();
        }
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.FADEN_FORTUNE.get();
    }
}
