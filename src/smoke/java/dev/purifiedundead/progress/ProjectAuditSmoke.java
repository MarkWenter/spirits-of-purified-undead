package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Regression checks for the time spent on the death screen before respawning. */
public final class ProjectAuditSmoke {
    private static ServerPlayer player(MinecraftServer server) throws Exception {
        var p = new ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "AuditTest"));
        p.connection = new net.minecraftforge.common.util.FakePlayer(server.overworld(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NetworkStub")).connection;
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        p.setPos(0, 100, 0);
        return p;
    }
    public static void run(MinecraftServer server) throws Exception {
        int failures = 0;
        var dead = player(server);
        AccessoryDeathRetentionService.retain(dead, java.util.List.of(new ItemStack(ModItems.FERIN_WARRIOR.get())));
        dead.setHealth(0);
        AccessoryDeathRetentionService.restore(dead);
        boolean retained = dead.getInventory().isEmpty() && dead.getPersistentData().contains("purified_undead:retained_accessories");
        System.out.println("AUDIT_CASE dead_retention=" + retained);
        if (!retained) failures++;
        var replacement = player(server);
        AccessoryDeathRetentionService.copyAndRestore(dead, replacement);
        boolean restored = replacement.getInventory().countItem(ModItems.FERIN_WARRIOR.get()) == 1;
        System.out.println("AUDIT_CASE retained_after_respawn=" + restored);
        if (!restored) failures++;
        var pending = player(server);
        for (int i=0;i<36;i++) pending.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        WarriorRewardService.grantContract(pending);
        pending.getInventory().clearContent();
        pending.setHealth(0);
        WarriorRewardService.retryPending(pending);
        boolean deferred = pending.getInventory().isEmpty() && WarriorProgressStorage.load(pending).contractPending();
        System.out.println("AUDIT_CASE dead_pending_reward=" + deferred);
        if (!deferred) failures++;
        var respawn = player(server);
        WarriorRewardService.copyProgress(pending, respawn);
        WarriorRewardService.retryPending(respawn);
        boolean delivered = respawn.getInventory().countItem(ModItems.ANCIENT_CONTRACT.get()) == 1;
        System.out.println("AUDIT_CASE pending_after_respawn=" + delivered);
        if (!delivered) failures++;
        var attacker = player(server);
        var curios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(attacker).orElseThrow(()->new IllegalStateException("Curios missing"));
        curios.getCurios().get("ancient_contract").getStacks().setStackInSlot(0,new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
        var warriors=curios.getCurios().get("undead_warrior").getStacks();
        if(warriors.getSlots()<1)warriors.grow(1);
        warriors.setStackInSlot(0,new ItemStack(ModItems.FERIN_WARRIOR.get()));
        attacker.setHealth(10);
        var pig = net.minecraft.world.entity.EntityType.PIG.create(server.overworld());
        pig.setHealth(2);
        pig.hurt(attacker.damageSources().playerAttack(attacker),100);
        float expected = dev.purifiedundead.combat.FerinBlightModel.lifesteal(100,2);
        boolean capped = Math.abs(attacker.getHealth()-(10+expected))<0.001;
        System.out.println("AUDIT_CASE overkill_lifesteal="+capped+" actual="+attacker.getHealth()+" expected="+(10+expected));
        if (!capped) failures++;
        attacker.setHealth(10);
        var shielded=net.minecraft.world.entity.EntityType.PIG.create(server.overworld());
        shielded.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,100,5));
        if(shielded.getAbsorptionAmount()<20)throw new IllegalStateException("Absorption fixture missing");
        shielded.hurt(attacker.damageSources().playerAttack(attacker),5);
        boolean absorption=attacker.getHealth()==10;
        System.out.println("AUDIT_CASE absorption_no_lifesteal="+absorption+" health="+attacker.getHealth()+" target="+shielded.getHealth()+" absorption="+shielded.getAbsorptionAmount());
        if(!absorption)failures++;
        dev.purifiedundead.combat.FerinLifecycleSmoke.run(player(server),player(server));
        if (failures>0) throw new IllegalStateException("AUDIT_FAILED: " + failures + " regression cases");
        System.out.println("PROJECT_AUDIT_OK: dead-player retention, pending reward, respawn delivery, overkill cap, absorption");
    }
}
