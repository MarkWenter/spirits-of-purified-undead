package dev.purifiedundead.validation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.purifiedundead.client.FoundryClientRecipes;
import dev.purifiedundead.foundry.FoundryRecipes;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AdministrationClientSmoke {
    private static boolean loading;private static int ticks,step;private static byte[] original;private static java.nio.file.Path path;
    private static int count;private static java.util.concurrent.CompletableFuture<?> task;
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||!Boolean.getBoolean("purified_undead.adminClient"))return;
        var mc=Minecraft.getInstance();
        try {
            if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
            if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;
            if(++ticks>1800)throw new IllegalStateException("client sync timeout");
            var server=mc.getSingleplayerServer();
            if(step==0) {
                if(FoundryClientRecipes.all().size()<2||!viewerReady())return;
                count=FoundryClientRecipes.all().size();path=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("purified_undead-foundry.json");original=java.nio.file.Files.readAllBytes(path);
                task=server.submit(()->{try {
                    var root=new com.google.gson.JsonObject();root.add("recipes",new com.google.gson.Gson().toJsonTree(java.util.List.of(FoundryRecipes.all().get(0))));
                    java.nio.file.Files.writeString(path,root.toString());
                    int result=server.getCommands().getDispatcher().execute("purifiedundead config reload foundry",server.createCommandSourceStack());
                    if(result!=1)throw new IllegalStateException("reload command failed");
                }catch(Exception ex){throw new RuntimeException(ex);}});step=1;
            } else if(step==1&&task.isDone()) {
                task.join();if(FoundryClientRecipes.all().size()!=1)return;
                checkViewer(1);
                task=server.submit(()->{try{java.nio.file.Files.write(path,original);FoundryRecipes.reloadStrict(server);}catch(Exception ex){throw new RuntimeException(ex);}});step=2;
            } else if(step==2&&task.isDone()) {
                task.join();if(FoundryClientRecipes.all().size()!=count)return;
                checkViewer(count);System.out.println("ADMIN_CLIENT_SYNC_OK: real command reload packets replace client/JEI recipes and restore original configuration");original=null;step=3;mc.stop();
            }
        } catch(Exception ex) {
            ex.printStackTrace();System.out.println("ADMIN_CLIENT_FAILED: "+ex);
            if(original!=null)try{java.nio.file.Files.write(path,original);}catch(Exception restore){restore.printStackTrace();}
            mc.stop();
        }
    }
    private static mezz.jei.api.runtime.IJeiRuntime viewer() throws Exception {
        var f=dev.purifiedundead.compat.jei.SlateJeiPlugin.class.getDeclaredField("runtime");f.setAccessible(true);return (mezz.jei.api.runtime.IJeiRuntime)f.get(null);
    }
    private static boolean viewerReady() throws Exception{return viewer()!=null;}
    private static void checkViewer(int expected) throws Exception {
        long count=viewer().getRecipeManager().createRecipeLookup(dev.purifiedundead.compat.jei.SlateJeiPlugin.FOUNDRY).get().count();
        if(count!=expected)throw new IllegalStateException("JEI recipes "+count+" != "+expected);
    }
}
