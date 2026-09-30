package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import dev.purifiedundead.content.ModItems;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class PartTwoClientSmoke {
 private static volatile boolean equipped;private static int title,ticks;private static boolean loading;
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){
 if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
 if(!Boolean.getBoolean("purified_undead.partTwoSmoke"))return;
 var mc=Minecraft.getInstance();try{
 if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null&&++title>40){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"wisp-validation");return;}
 if(mc.player==null||mc.level==null||mc.getOverlay()!=null)return;
 if(ticks==30&&!equipped)return;
 ticks++;
 if(ticks==30){mc.options.pauseOnLostFocus=false;mc.setScreen(null);mc.getSingleplayerServer().execute(()->{
 var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
 p.setHealth(p.getMaxHealth());p.setAirSupply(300);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.serverLevel().setDayTime(6000);p.serverLevel().setWeatherParameters(6000,0,false,false);p.teleportTo(p.serverLevel(),64.5,80,64.5,180,0);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,600,0));
 p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();
 p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.IMMACULATE_HELMET.get()));
 p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.IMMACULATE_CHESTPLATE.get()));
 p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.IMMACULATE_LEGGINGS.get()));
 p.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.IMMACULATE_BOOTS.get()));
 Item[] items={ModItems.BLIGHTED_GUARDIAN.get(),ModItems.WHITE_LOTUS.get(),ModItems.SCARLET_LOTUS.get(),ModItems.PURE_FORBIDDEN_FRUIT.get(),ModItems.PURE_TOUCH.get(),ModItems.PURIFIED_ARCSTEEL.get(),ModItems.IMMACULATE_HELMET.get(),ModItems.IMMACULATE_CHESTPLATE.get(),ModItems.IMMACULATE_LEGGINGS.get(),ModItems.IMMACULATE_BOOTS.get()};
 for(int i=0;i<items.length;i++)p.getInventory().setItem(i,new ItemStack(items[i]));p.containerMenu.broadcastChanges();equipped=true;
 });}
 if(ticks==120){
 for(int i=0;i<10;i++){var stack=mc.player.getInventory().getItem(i);if(stack.isEmpty()||mc.getItemRenderer().getModel(stack,mc.level,mc.player,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("missing item "+i+" stack="+stack);}
 var model=mc.getItemRenderer().getModel(mc.player.getInventory().getItem(0),mc.level,mc.player,0);float min=99,max=-99;
 for(var q:model.getQuads(null,null,net.minecraft.util.RandomSource.create(1))){int[] v=q.getVertices();for(int k=2;k<v.length;k+=v.length/4){float z=Float.intBitsToFloat(v[k]);min=Math.min(min,z);max=Math.max(max,z);}}
 if(max-min<.01)throw new IllegalStateException("sword lacks thickness");
 System.out.println("PART2_MODELS_OK sword depth="+(max-min));mc.setScreen(new InventoryScreen(mc.player));
 }
 if(ticks==150)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"part2-inventory.png",mc.getMainRenderTarget(),m->{});
 if(ticks==170){mc.setScreen(null);mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);}
 if(ticks==200)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"part2-armor.png",mc.getMainRenderTarget(),m->{});
 if(ticks==220){System.out.println("PART2_CLIENT_OK");mc.stop();}
 }catch(Throwable ex){ex.printStackTrace();System.out.println("PART2_CLIENT_FAILED "+ex);mc.stop();}
 }
}
