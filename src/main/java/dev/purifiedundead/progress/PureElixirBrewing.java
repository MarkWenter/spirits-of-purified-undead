package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModPotions;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import java.util.function.Consumer;

/** Explicit potion-aware descriptors are discoverable by recipe viewers. */
public final class PureElixirBrewing extends BrewingRecipe {
    private final Item bottle, reagent;
    private final Potion from, to;

    private PureElixirBrewing(Item bottle, Potion from, Item reagent, Potion to) {
        super(Ingredient.of(stack(bottle, from)), Ingredient.of(reagent), stack(bottle, to));
        this.bottle = bottle;
        this.from = from;
        this.reagent = reagent;
        this.to = to;
    }

    public static void register(Consumer<PureElixirBrewing> registry) {
        for (Item bottle : new Item[] {Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION}) {
            registry.accept(
                    new PureElixirBrewing(
                            bottle,
                            Potions.AWKWARD,
                            ModItems.PURE_CRYSTAL.get(),
                            ModPotions.PURE_ELIXIR.get()));
            registry.accept(
                    new PureElixirBrewing(
                            bottle,
                            ModPotions.PURE_ELIXIR.get(),
                            Items.REDSTONE,
                            ModPotions.LONG_PURE_ELIXIR.get()));
            registry.accept(
                    new PureElixirBrewing(
                            bottle,
                            ModPotions.PURE_ELIXIR.get(),
                            Items.GLOWSTONE_DUST,
                            ModPotions.STRONG_PURE_ELIXIR.get()));
        }
    }

    public static boolean isPure(ItemStack stack) {
        Potion p = PotionUtils.getPotion(stack);
        return p == ModPotions.PURE_ELIXIR.get()
                || p == ModPotions.LONG_PURE_ELIXIR.get()
                || p == ModPotions.STRONG_PURE_ELIXIR.get();
    }

    public static void color(ItemStack stack) {
        // Canonical stacks have no cosmetic NBT; rendering derives the tint from potion identity.
        if (isPure(stack) && stack.hasTag()) stack.getTag().remove("CustomPotionColor");
    }

    public static ItemStack stack(Item bottle, Potion potion) {
        ItemStack stack = PotionUtils.setPotion(new ItemStack(bottle), potion);
        color(stack);
        return stack;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getCount() == 1 && stack.is(bottle) && PotionUtils.getPotion(stack) == from;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(reagent);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        return isInput(input) && isIngredient(ingredient) ? stack(bottle, to) : ItemStack.EMPTY;
    }
}
