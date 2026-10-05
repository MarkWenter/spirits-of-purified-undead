package dev.purifiedundead.compat.jei;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.purification.DiaryBridge;
import dev.purifiedundead.purification.LilySmithingRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.ItemStack;

/** Older JEI versions enumerate templates to derive outputs, skipping template-free recipes. */
final class LilySmithingExtension implements ISmithingCategoryExtension<LilySmithingRecipe> {
    @Override public <T extends IIngredientAcceptor<T>> void setTemplate(LilySmithingRecipe recipe, T slot) {
        // Deliberately empty: the real smithing recipe requires no template.
    }
    @Override public <T extends IIngredientAcceptor<T>> void setBase(LilySmithingRecipe recipe, T slot) {
        slot.addItemStack(DiaryBridge.create());
    }
    @Override public <T extends IIngredientAcceptor<T>> void setAddition(LilySmithingRecipe recipe, T slot) {
        slot.addIngredients(LilySmithingRecipe.potions());
    }
    @Override public <T extends IIngredientAcceptor<T>> void setOutput(LilySmithingRecipe recipe, T slot) {
        slot.addItemStack(new ItemStack(ModItems.LILY_DIARY.get()));
    }
}
