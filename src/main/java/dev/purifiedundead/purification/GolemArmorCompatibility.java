package dev.purifiedundead.purification;
/** Optional integration through Enigmatic Legacy's public armor exclusion list. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class GolemArmorCompatibility {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void setup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e){e.enqueueWork(GolemArmorCompatibility::install);}
 public static void install(){
  if(!net.minecraftforge.fml.ModList.get().isLoaded("enigmaticlegacy"))return;
  try {
   var list=(java.util.List<Object>)Class.forName("com.aizistral.enigmaticlegacy.items.GolemHeart").getField("EXCLUDED_ARMOR").get(null);
   for(var item:java.util.List.of(dev.purifiedundead.content.ModItems.IMMACULATE_HELMET.get(),dev.purifiedundead.content.ModItems.IMMACULATE_CHESTPLATE.get(),dev.purifiedundead.content.ModItems.IMMACULATE_LEGGINGS.get(),dev.purifiedundead.content.ModItems.IMMACULATE_BOOTS.get()))if(!list.contains(item))list.add(item);
  }catch(ReflectiveOperationException|LinkageError e){com.mojang.logging.LogUtils.getLogger().warn("Enigmatic Legacy armor exclusion API unavailable",e);}
 }
}
