package dev.purifiedundead.client;
import java.util.List;
import dev.purifiedundead.foundry.FoundryRecipes;
/** Separate immutable snapshot; an optional viewer failure must not break server synchronization. */
public final class FoundryClientRecipes {
    private static List<FoundryRecipes.Recipe> recipes=List.of();
    private static boolean viewerFailed;
    public static List<FoundryRecipes.Recipe> all(){return recipes;}
    public static void clear(){recipes=List.of();viewerFailed=false;}
    public static void accept(List<FoundryRecipes.Recipe> incoming){
        FoundryRecipes.validateSync(incoming);
        if(recipes.equals(incoming))return;
        recipes=List.copyOf(incoming);
        if(!viewerFailed&&net.minecraftforge.fml.ModList.get().isLoaded("jei"))try{dev.purifiedundead.compat.jei.SlateJeiPlugin.refresh();}
        catch(LinkageError|RuntimeException error){viewerFailed=true;com.mojang.logging.LogUtils.getLogger().error("JEI foundry refresh disabled for this connection; server recipes remain intact",error);}
    }
}
