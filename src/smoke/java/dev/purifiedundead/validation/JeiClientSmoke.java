package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.item.ItemStack;
import dev.purifiedundead.progress.PureElixirBrewing;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class JeiClientSmoke {
 private static boolean loading,checked; private static int ticks,after;
 private static Object call(Object obj,String name,Object...args) throws Exception {
  for(var m:obj.getClass().getMethods())if(m.getName().equals(name)&&m.getParameterCount()==args.length){m.setAccessible(true);return m.invoke(obj,args);}
  throw new NoSuchMethodException(name);
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
  if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
  if(!Boolean.getBoolean("purified_undead.jeiSmoke"))return;
  var mc=Minecraft.getInstance();
  try {
   if(!loading && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"wisp-validation");return;}
   if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
   if(++ticks>2400)throw new IllegalStateException("JEI startup timeout");
   if(!checked) {
    var optional=(java.util.Optional<?>)Class.forName("mezz.jei.common.Internal").getMethod("getOptionalJeiRuntime").invoke(null);
    if(optional.isEmpty())return;
    Object runtime=optional.get(),manager=call(runtime,"getRecipeManager"),type=Class.forName("mezz.jei.api.constants.RecipeTypes").getField("BREWING").get(null);
    var all=((java.util.stream.Stream<?>)call(call(manager,"createRecipeLookup",type),"get")).toList();
    var ours=new java.util.ArrayList<Object>();var keys=new java.util.HashSet<String>();
    for(Object recipe:all) {
     var output=(ItemStack)call(recipe,"getPotionOutput");if(!PureElixirBrewing.isPure(output))continue;
     var inputs=(java.util.List<ItemStack>)call(recipe,"getPotionInputs");var reagents=(java.util.List<ItemStack>)call(recipe,"getIngredients");
     String key=key(inputs.get(0))+"/"+reagents.get(0).getItem()+"/"+key(output);
     if(!keys.add(key))throw new IllegalStateException("duplicate JEI recipe "+key);
     for(var input:inputs)for(var reagent:reagents) {
      ItemStack actual=net.minecraftforge.common.brewing.BrewingRecipeRegistry.getOutput(input,reagent);
      if(!key(actual).equals(key(output)))throw new IllegalStateException("ghost JEI recipe "+key);
     }
     int color=mc.getItemColors().getColor(output,0)&0xffffff;
     if(color!=dev.purifiedundead.content.ModPotions.PURE_ELIXIR_COLOR)throw new IllegalStateException("wrong JEI tint "+color);
     ours.add(recipe);
    }
    if(ours.size()!=15)throw new IllegalStateException("expected 15 unique recipes, got "+ours.size());
    checked=true;mc.setScreen(null);
    call(call(runtime,"getRecipesGui"),"showRecipes",call(manager,"getRecipeCategory",type),ours,java.util.List.of());
    System.out.println("JEI_PURE_ELIXIR_OK: 15 unique recipes, all inputs match actual brewing, all outputs sky-blue");
   } else if(++after==60) {
    net.minecraft.client.Screenshot.grab(mc.gameDirectory,"jei-pure-elixir-038.png",mc.getMainRenderTarget(),m->{});mc.stop();
   }
  } catch(Throwable e){e.printStackTrace();System.out.println("JEI_SMOKE_FAILED");mc.stop();}
 }
 private static String key(ItemStack s){return s.isEmpty()?"empty":s.getItem()+"/"+net.minecraft.world.item.alchemy.PotionUtils.getPotion(s);}
}
