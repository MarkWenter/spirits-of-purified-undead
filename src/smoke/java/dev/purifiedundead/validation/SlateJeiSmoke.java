package dev.purifiedundead.validation;
import dev.purifiedundead.compat.jei.SlateJeiPlugin;
import dev.purifiedundead.client.FoundryClientRecipes;
import dev.purifiedundead.foundry.*;
import dev.purifiedundead.slate.SlateContent;
import dev.purifiedundead.content.ModItems;
import mezz.jei.api.constants.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.*;
import java.util.*;
/** Loaded only by the opt-in JEI client test, never on a normal or dedicated-server path. */
public final class SlateJeiSmoke {
 private static boolean loading,checked;private static int ticks,after;private static IJeiRuntime runtime;private static List<FoundryRecipes.Recipe> recipes;
 private static void check(boolean ok,String why){if(!ok)throw new IllegalStateException("SLATE_JEI_FAILED: "+why);}
 public static void tick(){var mc=Minecraft.getInstance();try{
  if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
  if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
  if(++ticks>2400)throw new IllegalStateException("JEI timeout");
  if(!checked){
   var optional=(Optional<?>)Class.forName("mezz.jei.common.Internal").getMethod("getOptionalJeiRuntime").invoke(null);if(optional.isEmpty()||FoundryClientRecipes.all().isEmpty())return;
   runtime=(IJeiRuntime)optional.get();var manager=runtime.getRecipeManager();recipes=manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().toList();
   check(recipes.size()==13,"12 defaults plus server custom recipe: "+recipes.size());check(recipes.equals(FoundryClientRecipes.all()),"JEI uses server synchronized list");
   check(recipes.stream().anyMatch(r->r.top().equals("minecraft:copper_ingot")&&r.leftFuel()==2&&r.rightFuel()==3&&r.ticks()==200),"custom counts/fuels/time");
   var items=runtime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK);
   SlateContent.creative(stack->check(items.stream().anyMatch(s->s.is(stack.getItem())),"item indexed "+stack));check(items.stream().anyMatch(s->s.is(FoundryContent.ITEM.get())),"foundry indexed");
   var focus=runtime.getJeiHelpers().getFocusFactory();
   for(var fuel:List.of(ModItems.BLIGHTED_SPIRIT.get(),ModItems.PURE_CRYSTAL.get()))check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.INPUT,VanillaTypes.ITEM_STACK,new ItemStack(fuel)))).get().count()==13,"fuel usages find all recipes");
   check(manager.createRecipeCatalystLookup(SlateJeiPlugin.FOUNDRY).getItemStack().anyMatch(s->s.is(FoundryContent.ITEM.get())),"foundry category catalyst");
   check(manager.createRecipeLookup(SlateJeiPlugin.MEMORIES).get().count()==7,"seven visible memory crafting recipes");
   var crafting=manager.createRecipeLookup(RecipeTypes.CRAFTING).get().toList();
   for(var item:List.of(SlateContent.TABLET.get(),SlateContent.CIPHER_TEXT.get(),SlateContent.GUARDIAN.get(),FoundryContent.ITEM.get()))check(crafting.stream().anyMatch(r->r.getResultItem(mc.level.registryAccess()).is(item)),"ordinary crafting visible "+item);
   var category=manager.getRecipeCategory(SlateJeiPlugin.FOUNDRY);for(var r:recipes){var layout=manager.createRecipeLayoutDrawable(category,r,focus.getEmptyFocusGroup()).orElseThrow();check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size()==4,"four inputs including fuels");check(layout.getRecipeSlotsView().findSlotByName("output").isPresent(),"output visible");}
   FoundryClientRecipes.accept(List.of(recipes.get(12)));check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().count()==1,"late sync hides stale recipes");FoundryClientRecipes.accept(recipes);check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().count()==13,"refresh restores exact set");
   checked=true;mc.setScreen(null);runtime.getRecipesGui().showRecipes(category,List.of(recipes.get(0),recipes.get(5),recipes.get(12)),List.of());
  }else{
   after++;if(after==40)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"slate-jei-foundry.png",mc.getMainRenderTarget(),m->{});
   if(after==60)runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(SlateJeiPlugin.MEMORIES),runtime.getRecipeManager().createRecipeLookup(SlateJeiPlugin.MEMORIES).get().toList(),List.of());
   if(after==100){net.minecraft.client.Screenshot.grab(mc.gameDirectory,"slate-jei-memories.png",mc.getMainRenderTarget(),m->{});System.out.println("SLATE_JEI_OK: 20 indexed items, normal crafting, 12 defaults + custom server recipe, both fuel usages, retained templates, 7 memory recipes, live refresh and displayed layouts");mc.stop();}
  }
 }catch(Throwable e){e.printStackTrace();System.out.println("SLATE_JEI_FAILED");mc.stop();}}
}
