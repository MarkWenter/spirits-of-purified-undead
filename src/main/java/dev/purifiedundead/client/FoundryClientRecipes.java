package dev.purifiedundead.client;
import java.util.List;
import dev.purifiedundead.foundry.FoundryRecipes;
/** A separate client snapshot: never mutate an integrated server's recipe list. */
public final class FoundryClientRecipes {
    private static List<FoundryRecipes.Recipe> recipes=List.of();
    public static List<FoundryRecipes.Recipe> all(){return recipes;}
    public static void accept(List<FoundryRecipes.Recipe> incoming){recipes=List.copyOf(incoming);if(net.minecraftforge.fml.ModList.get().isLoaded("jei"))dev.purifiedundead.compat.jei.SlateJeiPlugin.refresh();}
}
