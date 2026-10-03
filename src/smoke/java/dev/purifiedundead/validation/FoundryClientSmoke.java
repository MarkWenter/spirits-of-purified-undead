package dev.purifiedundead.validation;

import dev.purifiedundead.foundry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class FoundryClientSmoke {
    private static boolean loading; private static int ticks;
    private static final BlockPos POS=new BlockPos(4,100,4);
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||!Boolean.getBoolean("purified_undead.foundryClient"))return;
        var mc=Minecraft.getInstance();
        try {
            if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
            if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
            ticks++;
            if(ticks==40) {
                for(boolean lit:new boolean[]{false,true}) {
                    var model=mc.getBlockRenderer().getBlockModel(FoundryContent.BLOCK.get().defaultBlockState().setValue(PurificationFoundryBlock.LIT,lit));
                    if(model==mc.getModelManager().getMissingModel()||model.getParticleIcon().contents().name().getPath().equals("missingno"))throw new IllegalStateException("missing block model");
                }
                mc.getSingleplayerServer().execute(()->{
                    var server=mc.getSingleplayerServer();var level=server.overworld();var p=server.getPlayerList().getPlayer(mc.player.getUUID());
                    p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(level,4.5,100,1.0,0,10);
                    level.setBlockAndUpdate(POS,FoundryContent.BLOCK.get().defaultBlockState().setValue(PurificationFoundryBlock.FACING,Direction.NORTH));
                    level.setBlockAndUpdate(POS.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    level.setDayTime(1000);p.openMenu((PurificationFoundryEntity)level.getBlockEntity(POS));
                });
            }
            if(ticks==80||ticks==120||ticks==160) {
                if(!(mc.screen instanceof dev.purifiedundead.client.PurificationFoundryScreen)||!(mc.player.containerMenu instanceof PurificationFoundryMenu menu))throw new IllegalStateException("server menu did not open");
                int expected=ticks==80?0:ticks==120?30:60;
                if(menu.progressPixels(60)!=expected)throw new IllegalStateException("progress sync "+menu.progressPixels(60));
                mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
                net.minecraft.client.Screenshot.grab(mc.gameDirectory,"foundry-gui-"+expected+".png",mc.getMainRenderTarget(),m->{});
            }
            if(ticks==90||ticks==130) {int progress=ticks==90?50:100;mc.getSingleplayerServer().execute(()->((PurificationFoundryEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS)).setWorkProgress(progress,100));}
            if(ticks==170) {mc.player.closeContainer();mc.options.hideGui=true;}
            if(ticks==200)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"foundry-world-lit.png",mc.getMainRenderTarget(),m->{});
            if(ticks==210)mc.getSingleplayerServer().execute(()->((PurificationFoundryEntity)mc.getSingleplayerServer().overworld().getBlockEntity(POS)).setWorkProgress(0,0));
            if(ticks==240){net.minecraft.client.Screenshot.grab(mc.gameDirectory,"foundry-world-idle.png",mc.getMainRenderTarget(),m->{});mc.options.hideGui=false;System.out.println("FOUNDRY_CLIENT_OK: registered models, real server menu, 0/50/100 percent synchronization and idle/lit world renders");mc.stop();}
        } catch(Throwable t){t.printStackTrace();System.out.println("FOUNDRY_CLIENT_FAILED");mc.options.hideGui=false;mc.stop();}
    }
}
