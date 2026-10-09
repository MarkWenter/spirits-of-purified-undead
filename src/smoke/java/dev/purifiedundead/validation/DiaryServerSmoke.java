package dev.purifiedundead.validation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.purifiedundead.content.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

@Mod.EventBusSubscriber(modid = "purified_undead")
public final class DiaryServerSmoke {
    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("purified_undead.diarySmoke")) return;
        try {
            var server = event.getServer();
            var id = ResourceLocation.fromNamespaceAndPath("purified_undead", "white_witch_diary");
            boolean loaded = net.minecraftforge.fml.ModList.get().isLoaded("patchouli");
            var optional = server.getRecipeManager().byKey(id);
            if (optional.isPresent() != loaded)
                throw new IllegalStateException("conditional recipe availability");
            if (loaded) {
                var recipe = (net.minecraft.world.item.crafting.CraftingRecipe) optional.get();
                if (recipe.getIngredients().size() != 2)
                    throw new IllegalStateException("two ingredients required");
                var result = recipe.getResultItem(server.registryAccess());
                Object book =
                        Class.forName("vazkii.patchouli.common.item.ItemModBook")
                                .getMethod("getBook", ItemStack.class)
                                .invoke(null, result);
                if (book == null || !id.equals(book.getClass().getField("id").get(book)))
                    throw new IllegalStateException("wrong book output");
                for (boolean reversed : new boolean[] {false, true}) {
                    var stacks =
                            new java.util.ArrayList<ItemStack>(
                                    java.util.List.of(
                                            ItemStack.EMPTY,
                                            ItemStack.EMPTY,
                                            ItemStack.EMPTY,
                                            ItemStack.EMPTY));
                    stacks.set(reversed ? 3 : 0, new ItemStack(Items.BOOK));
                    stacks.set(reversed ? 0 : 3, new ItemStack(ModItems.BLIGHT_FRAGMENT.get()));
                    var menu =
                            new net.minecraft.world.inventory.AbstractContainerMenu(null, 0) {
                                public ItemStack quickMoveStack(
                                        net.minecraft.world.entity.player.Player p, int i) {
                                    return ItemStack.EMPTY;
                                }

                                public boolean stillValid(
                                        net.minecraft.world.entity.player.Player p) {
                                    return true;
                                }
                            };
                    var input =
                            new net.minecraft.world.inventory.TransientCraftingContainer(
                                    menu, 2, 2);
                    for (int i = 0; i < 4; i++) input.setItem(i, stacks.get(i));
                    if (!recipe.matches(input, server.overworld()))
                        throw new IllegalStateException("shapeless matching");
                }
            }
            System.out.println(
                    "PURIFIED_UNDEAD_DIARY_SERVER_OK: patchouli="
                            + loaded
                            + " conditional recipe and book identity");
            if (Boolean.getBoolean("purified_undead.serverSmokeStop"))
                server.execute(() -> server.halt(false));
        } catch (Exception e) {
            throw new IllegalStateException("DIARY_SMOKE_FAILED", e);
        }
    }
}
