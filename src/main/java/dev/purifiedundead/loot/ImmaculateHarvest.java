package dev.purifiedundead.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.purifiedundead.blightedarmaments.ImmaculateTools;
import dev.purifiedundead.content.ModItems;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.*;

public final class ImmaculateHarvest extends LootModifier {
    public static final Codec<ImmaculateHarvest> CODEC =
            RecordCodecBuilder.create(i -> codecStart(i).apply(i, ImmaculateHarvest::new));

    public ImmaculateHarvest(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext c) {
        var tool = c.getParamOrNull(LootContextParams.TOOL);
        var state = c.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (tool == null
                || state == null
                || !(c.getParamOrNull(LootContextParams.THIS_ENTITY)
                        instanceof net.minecraft.world.entity.player.Player player)
                || player.isCreative()) return loot;
        if (tool.is(ImmaculateTools.SHOVEL.get())) {
            if (state.is(ImmaculateTools.SOILS)
                    && c.getRandom().nextFloat() < ImmaculateTools.SOIL_CHANCE)
                loot.add(
                        new ItemStack(
                                ModItems.BLIGHT_FRAGMENT.get(), 1 + c.getRandom().nextInt(4)));
            return loot;
        }

        boolean mining = tool.is(ImmaculateTools.PICKAXE.get());
        if (mining) {
            if (!state.is(ImmaculateTools.ORES) || !tool.isCorrectToolForDrops(state)) return loot;
        } else if (!tool.is(ImmaculateTools.HOE.get())
                || !state.is(ImmaculateTools.CROPS)
                || !ripe(state)) return loot;

        if (loot.isEmpty()
                || c.getRandom().nextFloat()
                        >= (mining ? ImmaculateTools.ORE_CHANCE : ImmaculateTools.CROP_CHANCE))
            return loot;
        var key = state.getBlock().getLootTable();
        if (!key.equals(c.getQueriedLootTableId())) return loot;
        var table = c.getLevel().getServer().getLootData().getLootTable(key);
        // Roll the block table again with the original Fortune tool. Never re-enter global
        // modifiers.
        table.getRandomItemsRaw(
                c,
                LootTable.createStackSplitter(
                        c.getLevel(),
                        stack -> {
                            if (mining && isOreBlock(stack, state)) return;
                            loot.add(stack);
                        }));
        return loot;
    }

    private static boolean isOreBlock(ItemStack stack, BlockState source) {
        if (stack.is(source.getBlock().asItem())) return true;
        return stack.getItem() instanceof BlockItem block
                && block.getBlock().defaultBlockState().is(ImmaculateTools.ORES);
    }

    private static boolean ripe(BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop)
            return crop.isMaxAge(state);
        var age = state.getBlock().getStateDefinition().getProperty("age");
        return !(age instanceof IntegerProperty property)
                || state.getValue(property).intValue()
                        == java.util.Collections.max(property.getPossibleValues());
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
