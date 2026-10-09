package dev.purifiedundead.validation;

import dev.purifiedundead.api.*;
import dev.purifiedundead.command.PurifiedUndeadCommands;
import dev.purifiedundead.config.ConfigCatalog;
import dev.purifiedundead.progress.*;
import dev.purifiedundead.foundry.FoundryRecipes;
import net.minecraft.world.item.*;

public final class AdministrationSmoke {
    private static void check(boolean value,String why){if(!value)throw new IllegalStateException("ADMIN_API_FAILED: "+why);}
    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        var player=new net.minecraftforge.common.util.FakePlayer(server.overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"AdminApiSmoke"));
        var dispatcher=server.getCommands().getDispatcher();
        check(dispatcher.getRoot().getChild("purifiedundead")!=null,"real command registration");
        var normal=player.createCommandSourceStack().withPermission(0);
        var admin=normal.withPermission(2);
        check(dispatcher.execute("purifiedundead status",normal)==1,"ordinary player self query");
        for(String command:new String[]{"warrior grant @s groth","warrior reset @s groth","warrior collect @s groth","talisman set @s 0","config common","status @s","contract grant @s","rewards retry @s","config reload foundry"}) {
            boolean denied=false;try{dispatcher.execute("purifiedundead "+command,normal);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){denied=true;}
            check(denied,"permissions: "+command);
        }
        int[] notices={0},blocked={0};
        java.util.function.Consumer<ProgressChangedEvent> listener=e->{if(e.player()!=player)return;notices[0]++;try{ProgressApi.setTalismanLevel(player,0);}catch(IllegalStateException expected){blocked[0]++;}};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            check(dispatcher.execute("purifiedundead warrior grant @s groth",admin)==1,"admin grant");
            check(ProgressApi.snapshot(player).warriors().get(WarriorId.GROTH).obtained(),"saved state");
            check(ProgressApi.grantWarrior(player,WarriorId.GROTH)==ProgressApi.GrantResult.ALREADY_OBTAINED,"no duplicate");
            check(notices[0]==1&&blocked[0]==1,"single notification and reentrant protection");
            boolean immutable=false;try{ProgressApi.snapshot(player).warriors().clear();}catch(UnsupportedOperationException expected){immutable=true;}check(immutable,"immutable snapshot");
            for(int i=0;i<player.getInventory().items.size();i++)player.getInventory().items.set(i,new ItemStack(Items.STONE,64));
            check(ProgressApi.grantWarrior(player,WarriorId.ELEINE)==ProgressApi.GrantResult.PENDING,"full inventory");
            check(ProgressApi.snapshot(player).warriors().get(WarriorId.ELEINE).pending(),"pending persisted");
            player.getInventory().items.set(0,ItemStack.EMPTY);WarriorRewardService.retryPending(player);
            check(!ProgressApi.snapshot(player).warriors().get(WarriorId.ELEINE).pending(),"retry delivery");
            check(player.getInventory().getItem(0).is(WarriorId.ELEINE.item()),"correct item");
            check(dispatcher.execute("purifiedundead warrior reset @s eleine",admin)==1,"reset command");
            check(!ProgressApi.snapshot(player).warriors().get(WarriorId.ELEINE).obtained()&&ProgressApi.snapshot(player).eleineDrownedKills()==0,"reset counter");
            check(player.getInventory().getItem(0).is(WarriorId.ELEINE.item()),"reset retains item");
            check(dispatcher.execute("purifiedundead warrior collect @s eleine",admin)==1,"collect command");
            check(player.getInventory().getItem(0).isEmpty()&&ProgressApi.snapshot(player).warriors().get(WarriorId.ELEINE).obtained(),"collection no auto regrant");
            check(dispatcher.execute("purifiedundead talisman set @s 1",admin)==1&&ProgressApi.snapshot(player).talismanLevel()==1,"set level");
            check(dispatcher.execute("purifiedundead talisman set @s 2147483647",admin)==0&&ProgressApi.snapshot(player).talismanLevel()==1,"range rejected");
            check(ProgressApi.grantContract(player)==ProgressApi.GrantResult.DELIVERED,"contract grant");
            boolean offThread=java.util.concurrent.CompletableFuture.supplyAsync(()->{try{ProgressApi.snapshot(player);return false;}catch(IllegalStateException expected){return true;}}).join();
            check(offThread,"off-thread access rejected");
            check(ConfigCatalog.values("common").containsKey("acquisition.ferinEnabled"),"existing config key catalog");
            check(dispatcher.execute("purifiedundead config common acquisition.ferinEnabled",admin)==1,"config query");
            check(dispatcher.execute("purifiedundead config common bad.key",admin)==0,"unknown config key");
        } finally {net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);player.getInventory().clearContent();}
        var path=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("purified_undead-foundry.json");
        byte[] original=java.nio.file.Files.readAllBytes(path);var before=FoundryRecipes.all();
        try {
            java.nio.file.Files.writeString(path,"{broken json");
            check(dispatcher.execute("purifiedundead config reload foundry",admin)==0&&FoundryRecipes.all().equals(before),"bad JSON retains recipes");
            java.nio.file.Files.writeString(path,"{\"recipes\":[{}]}");
            check(dispatcher.execute("purifiedundead config reload foundry",admin)==0&&FoundryRecipes.all().equals(before),"invalid recipe retains all recipes");
            java.nio.file.Files.write(path,original);
            check(dispatcher.execute("purifiedundead config reload foundry",admin)==1,"valid reload");
        } finally {java.nio.file.Files.write(path,original);FoundryRecipes.reloadStrict(server);}
        System.out.println("ADMIN_API_OK: real commands, permissions, grants, duplicate/pending recovery, reset/collect, level bounds, immutable notifications, thread/reentry protection, config catalog and atomic reload");
    }
}
