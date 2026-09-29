package dev.purifiedundead.client;

import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.entity.ContractWispEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.*;

@Mod.EventBusSubscriber(modid="purified_undead", value=Dist.CLIENT)
public final class ContractWispClient {
    public static final int MAX_VISIBLE=16, MAX_LIGHTS=4;
    private static final double RANGE_SQR=32*32, LIGHT_RANGE_SQR=24*24;
    private static final TagKey<Item> WARRIOR=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("curios","undead_warrior"));
    private static final DustParticleOptions DUST=new DustParticleOptions(new org.joml.Vector3f(.52F,.26F,.29F),.45F);
    private static final Map<UUID,ContractWispEntity> WISPS=new HashMap<>();
    private static ClientLevel previousLevel;
    private static int ticks, nextId=-2000000000;
    public static int activeCount() { return WISPS.size(); }
    public static ContractWispEntity findFor(Player p) { return WISPS.get(p.getUUID()); }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.level!=previousLevel || mc.player==null) {
            WISPS.values().forEach(ContractWispEntity::discard); WISPS.clear(); previousLevel=mc.level;ticks=0;
        }
        if(mc.level==null || mc.player==null || mc.isPaused())return;
        if(ticks++%10==0) refresh(mc);
        // Tiny bounded dust trail; particle settings and distance are respected.
        if(ticks%8==0 && mc.options.particles().get()!=ParticleStatus.MINIMAL) {
            for(ContractWispEntity w:WISPS.values()) if(!w.isRemoved() && w.distanceToSqr(mc.player)<256) {
                mc.level.addParticle(DUST,w.getX(),w.getY(),w.getZ(),0,.006,0);
            }
        }
    }
    private record Candidate(Player player, int warriors, double distance) {}
    private static void refresh(Minecraft mc) {
        PriorityQueue<Candidate> nearest=new PriorityQueue<>(Comparator.comparingDouble(Candidate::distance).reversed());
        for(Player p:mc.level.players()) {
            double distance=p==mc.player ? -1 : p.distanceToSqr(mc.player);
            if(distance>RANGE_SQR || !p.isAlive() || p.isSpectator() || p.isInvisible())continue;
            int warriors=equipment(p);
            if(warriors<0)continue;
            nearest.add(new Candidate(p,warriors,distance));
            if(nearest.size()>MAX_VISIBLE)nearest.poll();
        }
        List<Candidate> selected=new ArrayList<>(nearest);selected.sort(Comparator.comparingDouble(Candidate::distance));
        Set<UUID> retained=new HashSet<>();int sources=0;
        for(Candidate c:selected) {
            Player p=c.player();retained.add(p.getUUID());
            ContractWispEntity w=WISPS.get(p.getUUID());
            if(w==null || w.isRemoved() || w.owner()!=p) {
                if(w!=null)w.discard();
                w=new ContractWispEntity(ModEntities.CONTRACT_WISP.get(),mc.level);
                w.setId(nextId++);w.attach(p);mc.level.putNonPlayerEntity(w.getId(),w);WISPS.put(p.getUUID(),w);
            }
            w.setWarriors(c.warriors());
            w.setTerrainLight(c.distance()<LIGHT_RANGE_SQR && sources++<MAX_LIGHTS);
        }
        WISPS.entrySet().removeIf(e->{if(retained.contains(e.getKey()))return false;e.getValue().discard();return true;});
    }
    private static int equipment(Player p) {
        return CuriosApi.getCuriosInventory(p).map(h->{
            if(!h.isEquipped(ModItems.ANCIENT_CONTRACT.get()))return -1;
            int count=0;
            for(var handler:h.getCurios().values()) {
                var stacks=handler.getStacks();
                for(int slot=0;slot<stacks.getSlots();slot++) {
                    if(stacks.getStackInSlot(slot).is(WARRIOR) && ++count==8)return count;
                }
            }
            return count;
        }).orElse(-1);
    }
    private ContractWispClient() {}
}
