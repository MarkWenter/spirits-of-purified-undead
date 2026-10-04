package dev.purifiedundead.validation;

import dev.purifiedundead.foundry.*;
import dev.purifiedundead.slate.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class FoundryClientSmoke {
    private static boolean loading; private static int ticks; private static double gripY;
    private static final BlockPos POS=new BlockPos(4,100,4);
    private static void shot(Minecraft mc,String name){mc.getToasts().clear();mc.gui.getChat().clearMessages(false);net.minecraft.client.Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),m->{});}
    private static void check(boolean ok,String why){if(!ok)throw new IllegalStateException(why);}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||!Boolean.getBoolean("purified_undead.foundryClient"))return;
        if(Boolean.getBoolean("purified_undead.slateJei")){SlateJeiSmoke.tick();return;}
        var mc=Minecraft.getInstance();
        try {
            if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
            if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
            ticks++;mc.getToasts().clear();
            if(ticks==60||ticks==660)org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),1,1);
            if(ticks>=700&&ticks<810)mc.options.keyJump.setDown(true);
            if(ticks==40) {
                for(boolean lit:new boolean[]{false,true})check(mc.getBlockRenderer().getBlockModel(FoundryContent.BLOCK.get().defaultBlockState().setValue(PurificationFoundryBlock.LIT,lit))!=mc.getModelManager().getMissingModel(),"block model");
                SlateContent.creative(stack->check(mc.getItemRenderer().getModel(stack,mc.level,mc.player,0).getParticleIcon().contents().name().getPath().equals("missingno")==false,"item model "+stack));
                mc.getSingleplayerServer().execute(()->{var level=mc.getSingleplayerServer().overworld();var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(level,5,100,1,0,10);
                    level.setBlockAndUpdate(POS,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(POS,FoundryContent.BLOCK.get().defaultBlockState().setValue(PurificationFoundryBlock.FACING,Direction.NORTH));
                    level.setBlockAndUpdate(POS.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(POS.east(),net.minecraft.world.level.block.Blocks.FURNACE.defaultBlockState());level.setDayTime(1000);p.openMenu((PurificationFoundryEntity)level.getBlockEntity(POS));});
            }
            if(ticks==80){check(mc.screen instanceof dev.purifiedundead.client.PurificationFoundryScreen,"foundry screen");check(((PurificationFoundryMenu)mc.player.containerMenu).progressPixels(60)==0,"empty idle");shot(mc,"foundry-gui-idle");}
            if(ticks==90)mc.getSingleplayerServer().execute(()->{var be=(PurificationFoundryEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS);be.setItem(0,new ItemStack(Items.COAL,64));be.setItem(1,new ItemStack(ModItems.BLIGHTED_SPIRIT.get(),64));be.setItem(2,new ItemStack(ModItems.PURE_CRYSTAL.get(),64));be.setItem(3,new ItemStack(Items.COAL,64));});
            if(ticks==290){int progress=((PurificationFoundryMenu)mc.player.containerMenu).progressPixels(60);check(progress>15&&progress<50,"actual processing synchronized "+progress);shot(mc,"foundry-gui-working");}
            if(ticks==300){mc.player.closeContainer();mc.options.hideGui=true;}
            if(ticks==340){check(mc.level.getBlockState(POS).getValue(PurificationFoundryBlock.LIT),"lit while working");shot(mc,"foundry-world-lit");}
            if(ticks==550){check(!mc.level.getBlockState(POS).getValue(PurificationFoundryBlock.LIT),"idle after processing");shot(mc,"foundry-world-idle");mc.options.hideGui=false;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.openMenu((PurificationFoundryEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS));});}
            if(ticks==590){check(mc.player.containerMenu.getSlot(3).getItem().is(Items.DIAMOND)&&mc.player.containerMenu.getSlot(3).getItem().getCount()==64,"64 output cap synchronized");shot(mc,"foundry-gui-output");mc.player.closeContainer();}
            if(ticks==620)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var book=new ItemStack(ModItems.LILY_DIARY.get());var list=net.minecraft.core.NonNullList.withSize(8,ItemStack.EMPTY);for(int i=0;i<8;i++)list.set(i,new ItemStack(SlateContent.MEMORIES.get(SlateContent.WARRIORS[i]).get()));MemoryStorage.write(book,list);p.getInventory().setItem(p.getInventory().selected,book);book.getItem().use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);});
            if(ticks==680){check(mc.screen instanceof dev.purifiedundead.client.LilyMemoryScreen,"diary screen opens from real use");for(int i=0;i<8;i++)check(SlateContent.memoryKey(mc.player.containerMenu.getSlot(i).getItem())!=null,"eight memories synchronized");shot(mc,"slate-lily-memories");}
            if(ticks==700){mc.player.closeContainer();mc.getSingleplayerServer().execute(()->{var server=mc.getSingleplayerServer();var level=server.overworld();var p=server.getPlayerList().getPlayer(mc.player.getUUID());dev.purifiedundead.purification.PurificationProgress.unlock(p);top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).ifPresent(h->h.getCurios().get("wanderer_log").getStacks().setStackInSlot(0,p.getMainHandItem().copy()));p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);for(int y=90;y<106;y++)level.setBlockAndUpdate(new BlockPos(9,y,8),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().flying=false;p.onUpdateAbilities();p.teleportTo(level,8.69,100,8.5,0,0);p.setDeltaMovement(0,-.2,0);});}
            if(ticks==770)gripY=mc.player.getY();
            if(ticks==800){check(Math.abs(mc.player.getY()-gripY)<.2,"held jump stops actual client fall: "+gripY+" -> "+mc.player.getY());shot(mc,"slate-wall-grip");}
            if(ticks==810)mc.options.keyJump.setDown(false);
            if(ticks==840){check(mc.player.getY()<gripY-.5,"release resumes actual falling");mc.getSingleplayerServer().execute(()->{var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();for(int y=90;y<106;y++)server.overworld().setBlockAndUpdate(new BlockPos(9,y,8),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());});}
            if(ticks==860){mc.options.keyJump.setDown(false);System.out.println("FOUNDRY_CLIENT_OK: registered models, real batch processing, progress synchronization, 64-item output cap and idle/lit renders");System.out.println("SLATE_CLIENT_OK: all item models, main-hand diary use, eight synchronized memory slots, parchment screen, real client wall hang and release");mc.stop();}
        }catch(Throwable t){t.printStackTrace();System.out.println("FOUNDRY_CLIENT_FAILED");mc.options.hideGui=false;mc.stop();}
    }
}
