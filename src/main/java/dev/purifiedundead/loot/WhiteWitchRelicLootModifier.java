package dev.purifiedundead.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.purifiedundead.content.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/** Adds relics without replacing vanilla or other mods' chest loot. Earrings have twice the chance. */
public final class WhiteWitchRelicLootModifier extends LootModifier {
    public static final Codec<WhiteWitchRelicLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).and(Codec.doubleRange(0, .5).fieldOf("statue_chance")
                    .forGetter(modifier -> modifier.statueChance)).apply(instance, WhiteWitchRelicLootModifier::new));
    private final double statueChance;
    public WhiteWitchRelicLootModifier(LootItemCondition[] conditions, double statueChance) {
        super(conditions); this.statueChance = statueChance;
    }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!context.getLevel().dimension().equals(Level.OVERWORLD)) return loot;
        if (context.getRandom().nextDouble() < statueChance) loot.add(new ItemStack(ModItems.WHITE_PRIESTESS_STATUE.get()));
        if (context.getRandom().nextDouble() < statueChance * 2) loot.add(new ItemStack(ModItems.WHITE_PRIESTESS_EARRINGS.get()));
        return loot;
    }
    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
