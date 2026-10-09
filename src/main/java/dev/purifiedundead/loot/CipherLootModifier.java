package dev.purifiedundead.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.loot.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import dev.purifiedundead.slate.*;

public final class CipherLootModifier extends LootModifier {
    public static final Codec<CipherLootModifier> CODEC =
            RecordCodecBuilder.create(i -> codecStart(i).apply(i, CipherLootModifier::new));

    public CipherLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext c) {
        var dimension = c.getLevel().dimension();
        var table = c.getQueriedLootTableId();
        // Standard chest paths cover vanilla and structure mods; origin/block/entity guards exclude
        // mob/block loot.
        if ((dimension.equals(Level.OVERWORLD) || dimension.equals(Level.NETHER))
                && table != null
                && table.getPath().contains("chests/")
                && !c.hasParam(LootContextParams.BLOCK_STATE)
                && (!c.hasParam(LootContextParams.THIS_ENTITY)
                        || c.getParamOrNull(LootContextParams.THIS_ENTITY)
                                instanceof net.minecraft.world.entity.player.Player)
                && c.getRandom().nextDouble() < SlateConfig.get(SlateConfig.chestChance))
            loot.add(
                    new ItemStack(
                            SlateContent.CIPHER_FRAGMENT.get(), 1 + c.getRandom().nextInt(5)));
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
