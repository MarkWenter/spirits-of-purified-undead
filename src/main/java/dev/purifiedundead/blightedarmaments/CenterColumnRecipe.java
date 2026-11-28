package dev.purifiedundead.blightedarmaments;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * A full 3x3 display grid keeps the guidebook and recipe transfer aligned with the required slots.
 */
public final class CenterColumnRecipe extends ShapedRecipe {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "purified_undead");
    private static final Supplier<RecipeSerializer<CenterColumnRecipe>> TYPE =
            SERIALIZERS.register("center_column", ColumnSerializer::new);
    private final ShapedRecipe source;

    static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    private static NonNullList<Ingredient> centered(ShapedRecipe source) {
        if (source.getWidth() != 1 || source.getHeight() != 3)
            throw new IllegalArgumentException(
                    "center_column requires a one-column, three-row pattern");
        var grid = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int row = 0; row < 3; row++) grid.set(1 + row * 3, source.getIngredients().get(row));
        return grid;
    }

    private CenterColumnRecipe(ShapedRecipe source) {
        super(
                source.getId(),
                source.getGroup(),
                source.category(),
                3,
                3,
                centered(source),
                source.getResultItem(RegistryAccess.EMPTY),
                source.showNotification());
        this.source = source;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TYPE.get();
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return input.getWidth() == 3 && input.getHeight() == 3 && super.matches(input, level);
    }

    private static final class ColumnSerializer implements RecipeSerializer<CenterColumnRecipe> {
        private final ShapedRecipe.Serializer vanilla = new ShapedRecipe.Serializer();

        public CenterColumnRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new CenterColumnRecipe(vanilla.fromJson(id, json));
        }

        public CenterColumnRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new CenterColumnRecipe(vanilla.fromNetwork(id, buffer));
        }

        public void toNetwork(FriendlyByteBuf buffer, CenterColumnRecipe recipe) {
            vanilla.toNetwork(buffer, recipe.source);
        }
    }
}
