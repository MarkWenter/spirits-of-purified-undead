package dev.purifiedundead.compat.jei;

import dev.purifiedundead.client.FoundryClientRecipes;
import dev.purifiedundead.client.PurificationFoundryScreen;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.foundry.*;
import dev.purifiedundead.slate.SlateContent;
import mezz.jei.api.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.List;

/** Optional JEI integration; this class is only loaded by JEI or the guarded client bridge. */
@JeiPlugin
public final class SlateJeiPlugin implements IModPlugin {
    public static final RecipeType<FoundryRecipes.Recipe> FOUNDRY =
            RecipeType.create("purified_undead", "foundry", FoundryRecipes.Recipe.class);
    public static final RecipeType<MemoryCraft> MEMORIES =
            RecipeType.create("purified_undead", "memory_crafting", MemoryCraft.class);

    public record MemoryCraft(String key) {}

    private static IJeiRuntime runtime;
    private static List<FoundryRecipes.Recipe> displayed = List.of();
    private static final java.util.Map<FoundryRecipes.Recipe, FoundryRecipes.Recipe> registered =
            new java.util.HashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return id("slate");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("purified_undead", path);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        r.addRecipeCategories(
                new FoundryCategory(r.getJeiHelpers().getGuiHelper()),
                new MemoryCategory(r.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration r) {
        r.getSmithingCategory()
                .addExtension(
                        dev.purifiedundead.purification.LilySmithingRecipe.class,
                        new LilySmithingExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        registered.clear();
        displayed = FoundryClientRecipes.all().stream().distinct().toList();
        for (var recipe : displayed) registered.put(recipe, recipe);
        r.addRecipes(FOUNDRY, displayed);
        r.addRecipes(
                MEMORIES, SlateContent.FORGED.keySet().stream().map(MemoryCraft::new).toList());
        r.addItemStackInfo(
                new ItemStack(SlateContent.CIPHER_FRAGMENT.get()),
                Component.translatable("jei.purified_undead.cipher_source"));
        r.addItemStackInfo(
                new ItemStack(SlateContent.MEMORIES.get("ferin").get()),
                Component.translatable("jei.purified_undead.ferin_source"));
        r.addItemStackInfo(
                new ItemStack(ModItems.PURE_CRYSTAL.get()),
                Component.translatable("jei.purified_undead.crystal_source"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(FoundryContent.ITEM.get()), FOUNDRY);
        r.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), MEMORIES);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration r) {
        r.addRecipeClickArea(PurificationFoundryScreen.class, 88, 43, 25, 66, FOUNDRY);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime value) {
        runtime = value;
        refresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        displayed = List.of();
        registered.clear();
    }

    /** Login sync can arrive before or after JEI registers recipes. Both orders use server values. */
    public static void refresh() {
        if (runtime == null) return;
        var incoming =
                FoundryClientRecipes.all().stream()
                        .distinct()
                        .map(r -> registered.getOrDefault(r, r))
                        .toList();
        if (displayed.equals(incoming)) return;
        var manager = runtime.getRecipeManager();
        manager.hideRecipes(FOUNDRY, displayed);
        var fresh = incoming.stream().filter(r -> !registered.containsKey(r)).toList();
        manager.addRecipes(FOUNDRY, fresh);
        for (var recipe : fresh) registered.put(recipe, recipe);
        manager.unhideRecipes(FOUNDRY, incoming);
        manager.unhideRecipeCategory(FOUNDRY);
        displayed = incoming;
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)), count);
    }

    private static void label(GuiGraphics g, String key, int x, int y) {
        g.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.purified_undead." + key),
                x,
                y,
                0xff404040,
                false);
    }

    private static void arrow(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 19, y + 3, 0xff777777);
        g.fill(x + 14, y - 3, x + 17, y + 6, 0xff777777);
        g.fill(x + 17, y - 1, x + 20, y + 4, 0xff777777);
    }

    public static final class FoundryCategory implements IRecipeCategory<FoundryRecipes.Recipe> {
        private final IDrawable background, icon;

        FoundryCategory(IGuiHelper h) {
            background = h.createBlankDrawable(176, 104);
            icon = h.createDrawableItemStack(new ItemStack(FoundryContent.ITEM.get()));
        }

        public RecipeType<FoundryRecipes.Recipe> getRecipeType() {
            return FOUNDRY;
        }

        public Component getTitle() {
            return Component.translatable("block.purified_undead.purification_foundry");
        }

        public IDrawable getBackground() {
            return background;
        }

        public IDrawable getIcon() {
            return icon;
        }

        public void setRecipe(IRecipeLayoutBuilder b, FoundryRecipes.Recipe r, IFocusGroup f) {
            b.addSlot(RecipeIngredientRole.INPUT, 80, 14)
                    .setSlotName("top")
                    .setStandardSlotBackground()
                    .addItemStack(item(r.top(), r.topCount()))
                    .addTooltipCallback(
                            (v, t) ->
                                    t.add(
                                            Component.translatable(
                                                    "jei.purified_undead."
                                                            + (r.consumeTop()
                                                                    ? "consumed"
                                                                    : "retained"))));
            b.addSlot(RecipeIngredientRole.INPUT, 17, 44)
                    .setSlotName("left_fuel")
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(ModItems.BLIGHTED_SPIRIT.get(), r.leftFuel()));
            b.addSlot(RecipeIngredientRole.INPUT, 143, 44)
                    .setSlotName("right_fuel")
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(ModItems.PURE_CRYSTAL.get(), r.rightFuel()));
            b.addSlot(RecipeIngredientRole.INPUT, 62, 74)
                    .setSlotName("bottom")
                    .setStandardSlotBackground()
                    .addItemStack(item(r.bottom(), r.bottomCount()));
            b.addSlot(RecipeIngredientRole.OUTPUT, 108, 74)
                    .setSlotName("output")
                    .setOutputSlotBackground()
                    .addItemStack(r.result());
        }

        public void draw(
                FoundryRecipes.Recipe r,
                IRecipeSlotsView slots,
                GuiGraphics g,
                double x,
                double y) {
            label(g, r.consumeTop() ? "top_consumed" : "top_retained", 60, 1);
            label(g, "left_fuel", 6, 32);
            label(g, "right_fuel", 132, 32);
            label(g, "bottom", 48, 62);
            label(g, "output", 109, 62);
            arrow(g, 84, 81);
            g.drawString(
                    Minecraft.getInstance().font,
                    Component.translatable(
                            "jei.purified_undead.seconds",
                            String.format(java.util.Locale.ROOT, "%.1f", r.ticks() / 20.0)),
                    65,
                    95,
                    0xff404040,
                    false);
        }
    }

    public static final class MemoryCategory implements IRecipeCategory<MemoryCraft> {
        private final IDrawable background, icon;

        MemoryCategory(IGuiHelper h) {
            background = h.createBlankDrawable(150, 58);
            icon = h.createDrawableItemStack(new ItemStack(Items.CRAFTING_TABLE));
        }

        public RecipeType<MemoryCraft> getRecipeType() {
            return MEMORIES;
        }

        public Component getTitle() {
            return Component.translatable("jei.purified_undead.memory_crafting");
        }

        public IDrawable getBackground() {
            return background;
        }

        public IDrawable getIcon() {
            return icon;
        }

        public void setRecipe(IRecipeLayoutBuilder b, MemoryCraft r, IFocusGroup f) {
            b.addSlot(RecipeIngredientRole.INPUT, 18, 16)
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(SlateContent.FORGED.get(r.key()).get()))
                    .addTooltipCallback(
                            (v, t) ->
                                    t.add(Component.translatable("jei.purified_undead.retained")));
            b.addSlot(RecipeIngredientRole.INPUT, 47, 16)
                    .setStandardSlotBackground()
                    .addItemStack(new ItemStack(SlateContent.CIPHER_TEXT.get()));
            b.addSlot(RecipeIngredientRole.OUTPUT, 110, 16)
                    .setOutputSlotBackground()
                    .addItemStack(new ItemStack(SlateContent.MEMORIES.get(r.key()).get()));
        }

        public void draw(MemoryCraft r, IRecipeSlotsView slots, GuiGraphics g, double x, double y) {
            arrow(g, 77, 23);
            label(g, "crafting_retains", 7, 43);
        }
    }
}
