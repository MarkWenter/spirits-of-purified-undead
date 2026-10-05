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
   var field=SlateJeiPlugin.class.getDeclaredField("runtime");field.setAccessible(true);var optional=Optional.ofNullable((IJeiRuntime)field.get(null));if(optional.isEmpty()||FoundryClientRecipes.all().isEmpty())return;
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
   var smithing=manager.createRecipeLookup(RecipeTypes.SMITHING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,new ItemStack(ModItems.LILY_DIARY.get())))).get().toList();
   check(smithing.size()==1,"Lily Diary output lookup must find one smithing source, got "+smithing.size());
   var lilyLayout=manager.createRecipeLayoutDrawable(manager.getRecipeCategory(RecipeTypes.SMITHING),smithing.get(0),focus.getEmptyFocusGroup()).orElseThrow();
   var lilySlots=lilyLayout.getRecipeSlotsView();
   check(lilySlots.getSlotViews(RecipeIngredientRole.OUTPUT).stream().flatMap(v->v.getItemStacks()).anyMatch(s->s.is(ModItems.LILY_DIARY.get())),"Lily output visible");
   var lilyInputs=lilySlots.getSlotViews(RecipeIngredientRole.INPUT);
   check(lilyInputs.get(0).getItemStacks().allMatch(ItemStack::isEmpty),"smithing template remains empty");
   check(lilyInputs.get(1).getItemStacks().allMatch(dev.purifiedundead.purification.DiaryBridge::isDiary),"base has correct Patchouli book identity");
   check(lilyInputs.get(2).getItemStacks().count()==9,"nine pure potion variants");
   for(var potion:lilyInputs.get(2).getItemStacks().toList())check(manager.createRecipeLookup(RecipeTypes.SMITHING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.INPUT,VanillaTypes.ITEM_STACK,potion))).get().count()>=1,"potion finds smithing use");
   for(var recipe:crafting){var result=recipe.getResultItem(mc.level.registryAccess());if(!result.isEmpty()&&net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).getNamespace().equals("purified_undead"))check(manager.createRecipeLookup(RecipeTypes.CRAFTING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,result))).get().findAny().isPresent(),"crafting output lookup "+result);}
   var filledBook=new ItemStack(ModItems.LILY_DIARY.get());var memories=net.minecraft.core.NonNullList.withSize(8,ItemStack.EMPTY);memories.set(0,new ItemStack(SlateContent.MEMORIES.get("groth").get()));dev.purifiedundead.slate.MemoryStorage.write(filledBook,memories);
   check(manager.createRecipeLookup(RecipeTypes.SMITHING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,filledBook))).get().count()==1,"filled diary still finds source");
   for(var armor:List.of(ModItems.IMMACULATE_HELMET.get(),ModItems.IMMACULATE_CHESTPLATE.get(),ModItems.IMMACULATE_LEGGINGS.get(),ModItems.IMMACULATE_BOOTS.get()))check(manager.createRecipeLookup(RecipeTypes.SMITHING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,new ItemStack(armor)))).get().anyMatch(r->r.getId().equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(armor))),"armor smithing source "+armor);
   for(var key:SlateContent.FORGED.keySet())check(manager.createRecipeLookup(SlateJeiPlugin.MEMORIES).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,new ItemStack(SlateContent.MEMORIES.get(key).get())))).get().count()==1,"memory output source "+key);
   for(var potion:dev.purifiedundead.purification.LilySmithingRecipe.potions().getItems())check(manager.createRecipeLookup(RecipeTypes.BREWING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,potion))).get().findAny().isPresent(),"pure potion brewing source "+potion);
   check(manager.createRecipeLookup(RecipeTypes.CRAFTING).limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,dev.purifiedundead.purification.DiaryBridge.create()))).get().findAny().isPresent(),"White Witch Diary book crafting source");
   int audited=0;
   for(var source:mc.level.getRecipeManager().getRecipes())if(source.getId().getNamespace().equals("purified_undead")){
    var result=source.getResultItem(mc.level.registryAccess());if(result.isEmpty())continue;
    var outputFocus=List.of(focus.createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,result));
    if(source instanceof net.minecraft.world.item.crafting.CraftingRecipe)check(manager.createRecipeLookup(RecipeTypes.CRAFTING).limitFocus(outputFocus).get().findAny().isPresent(),"loaded crafting has JEI source "+source.getId());
    else if(source instanceof net.minecraft.world.item.crafting.SmithingRecipe)check(manager.createRecipeLookup(RecipeTypes.SMITHING).limitFocus(outputFocus).get().findAny().isPresent(),"loaded smithing has JEI source "+source.getId());
    audited++;
   }
   System.out.println("JEI_SOURCE_AUDIT_OK: "+audited+" nonempty mod recipe outputs; four armor upgrades, filled diary, seven memory crafts, nine pure potions");
   System.out.println("LILY_JEI_OK: output lookup, visible result, correct book identity, empty template, all nine potion usages and ordinary crafting output audit");
   var category=manager.getRecipeCategory(SlateJeiPlugin.FOUNDRY);for(var r:recipes){var layout=manager.createRecipeLayoutDrawable(category,r,focus.getEmptyFocusGroup()).orElseThrow();check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size()==4,"four inputs including fuels");check(layout.getRecipeSlotsView().findSlotByName("output").isPresent(),"output visible");}
   FoundryClientRecipes.accept(List.of(recipes.get(12)));check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().count()==1,"late sync hides stale recipes");FoundryClientRecipes.accept(recipes);check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().count()==13,"refresh restores exact set");
   for(int repeat=0;repeat<40;repeat++){FoundryClientRecipes.accept(List.of(recipes.get(12)));FoundryClientRecipes.accept(recipes);}
   var registered=SlateJeiPlugin.class.getDeclaredField("registered");registered.setAccessible(true);check(((java.util.Map<?,?>)registered.get(null)).size()==13,"repeated sync retains only 13 distinct JEI recipe identities");
   check(manager.createRecipeLookup(SlateJeiPlugin.FOUNDRY).get().count()==13,"repeated sync does not multiply visible recipes");
   System.out.println("JEI_COMPATIBILITY_OK: 80 refreshes, 13 registered identities and 13 visible recipes");
   checked=true;mc.setScreen(null);runtime.getRecipesGui().showRecipes(category,List.of(recipes.get(0),recipes.get(5),recipes.get(12)),List.of());
  }else{
   after++;if(after==40)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"slate-jei-foundry.png",mc.getMainRenderTarget(),m->{});
   if(after==60)runtime.getRecipesGui().showRecipes(runtime.getRecipeManager().getRecipeCategory(SlateJeiPlugin.MEMORIES),runtime.getRecipeManager().createRecipeLookup(SlateJeiPlugin.MEMORIES).get().toList(),List.of());
   if(after==110)runtime.getRecipesGui().show(List.of(runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,new ItemStack(ModItems.LILY_DIARY.get()))));
   if(after==150){net.minecraft.client.Screenshot.grab(mc.gameDirectory,"lily-jei-smithing.png",mc.getMainRenderTarget(),m->{});}
   if(after==160){net.minecraft.client.Screenshot.grab(mc.gameDirectory,"slate-jei-memories.png",mc.getMainRenderTarget(),m->{});System.out.println("SLATE_JEI_OK: 20 indexed items, normal crafting, 12 defaults + custom server recipe, both fuel usages, retained templates, 7 memory recipes, live refresh and displayed layouts");mc.stop();}
  }
 }catch(Throwable e){e.printStackTrace();System.out.println("SLATE_JEI_FAILED");mc.stop();}}
}
