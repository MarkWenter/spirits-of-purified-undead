package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModPotions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;

/** Awkward potion plus one Blighted Spirit, restricted to a drinkable potion bottle. */
public final class BlightElixirBrewingRecipe implements IBrewingRecipe {
    @Override
    public boolean isInput(@NotNull ItemStack input) {
        return input.is(Items.POTION) && PotionUtils.getPotion(input) == Potions.AWKWARD;
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack ingredient) {
        return ingredient.is(ModItems.BLIGHTED_SPIRIT.get());
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = new ItemStack(Items.POTION);
        PotionUtils.setPotion(output, ModPotions.BLIGHT_ELIXIR.get());
        return output;
    }
}
