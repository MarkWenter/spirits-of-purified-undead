package dev.purifiedundead.purification;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import dev.purifiedundead.content.*;
import dev.purifiedundead.progress.PureElixirBrewing;

public final class LilySmithingRecipe extends SmithingTransformRecipe {
    private static final net.minecraftforge.registries.DeferredRegister<RecipeSerializer<?>> REG =
            net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraftforge.registries.ForgeRegistries.RECIPE_SERIALIZERS,
                    "purified_undead");
    public static final java.util.function.Supplier<RecipeSerializer<LilySmithingRecipe>>
            SERIALIZER = REG.register("lily_diary", Serializer::new);

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        REG.register(bus);
    }

    public LilySmithingRecipe(ResourceLocation id) {
        super(
                id,
                Ingredient.EMPTY,
                Ingredient.of(DiaryBridge.create()),
                potions(),
                new ItemStack(ModItems.LILY_DIARY.get()));
    }

    public static Ingredient potions() {
        var list = new java.util.ArrayList<ItemStack>();
        for (Item bottle : new Item[] {Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION})
            for (var p :
                    java.util.List.of(
                            ModPotions.PURE_ELIXIR.get(),
                            ModPotions.LONG_PURE_ELIXIR.get(),
                            ModPotions.STRONG_PURE_ELIXIR.get()))
                list.add(PureElixirBrewing.stack(bottle, p));
        return Ingredient.of(list.stream());
    }

    @Override
    public boolean isTemplateIngredient(ItemStack s) {
        return s.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack s) {
        return DiaryBridge.isDiary(s);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack s) {
        return (s.is(Items.POTION) || s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION))
                && PureElixirBrewing.isPure(s);
    }

    @Override
    public boolean matches(net.minecraft.world.Container c, net.minecraft.world.level.Level level) {
        return isTemplateIngredient(c.getItem(0))
                && isBaseIngredient(c.getItem(1))
                && isAdditionIngredient(c.getItem(2));
    }

    @Override
    public ItemStack assemble(
            net.minecraft.world.Container c, net.minecraft.core.RegistryAccess registry) {
        return new ItemStack(ModItems.LILY_DIARY.get());
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }

    public static final class Serializer implements RecipeSerializer<LilySmithingRecipe> {
        public LilySmithingRecipe fromJson(ResourceLocation id, com.google.gson.JsonObject json) {
            return new LilySmithingRecipe(id);
        }

        public LilySmithingRecipe fromNetwork(
                ResourceLocation id, net.minecraft.network.FriendlyByteBuf buf) {
            return new LilySmithingRecipe(id);
        }

        public void toNetwork(net.minecraft.network.FriendlyByteBuf buf, LilySmithingRecipe r) {}
    }
}
