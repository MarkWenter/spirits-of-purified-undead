package dev.purifiedundead.validation;
import dev.purifiedundead.content.*;
import dev.purifiedundead.progress.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import top.theillusivec4.curios.api.CuriosApi;
@Mod.EventBusSubscriber(modid="purified_undead")
public final class PurificationServerSmoke {
 private static void check(boolean v,String why) { if(!v)throw new IllegalStateException("PURIFICATION_FAILED: "+why); }
 private static ServerStartedEvent pending;private static int warmup;
 @SubscribeEvent public static void queue(ServerStartedEvent e){if(!Boolean.getBoolean("purified_undead.purificationSmoke"))return;pending=e;warmup=0;for(int x=2;x<=3;x++)for(int z=2;z<=3;z++)e.getServer().overworld().setChunkForced(x,z,true);}
 @SubscribeEvent public static void afterWorldReady(net.minecraftforge.event.TickEvent.ServerTickEvent t){if(t.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;if(pending==null||++warmup<30)return;var e=pending;pending=null;try{started(e);}finally{for(int x=2;x<=3;x++)for(int z=2;z<=3;z++)e.getServer().overworld().setChunkForced(x,z,false);}}
 public static void started(ServerStartedEvent e) {
  if(!Boolean.getBoolean("purified_undead.purificationSmoke"))return;
  var server=e.getServer();var level=server.overworld();
  try {
   for(var bottle:new Item[]{Items.POTION,Items.SPLASH_POTION,Items.LINGERING_POTION}) {
    var awkward=PureElixirBrewing.stack(bottle,Potions.AWKWARD);
    var base=mix(server,awkward,ModItems.PURE_CRYSTAL.get());check(!base.isEmpty(),"base recipe");
    check(PotionUtils.getPotion(base)==ModPotions.PURE_ELIXIR.get(),"base identity");
    var extended=mix(server,base,Items.REDSTONE);var strong=mix(server,base,Items.GLOWSTONE_DUST);
    check(!extended.isEmpty()&&!strong.isEmpty(),"upgrades");
    int index=0;
    for(var potion:new ItemStack[]{base,extended,strong}) {
     var effects=PotionUtils.getMobEffects(potion);check(effects.size()==3,"3 effects");
     for(var effect:effects)check(effect.getDuration()==new int[]{1800,4800,900}[index]&&effect.getAmplifier()==(index==2?2:1),"duration/amplifier");
     check(!potion.getOrCreateTag().contains("CustomPotionColor"),"canonical potion NBT");
     if(bottle==Items.POTION) {
      var splash=mix(server,potion,Items.GUNPOWDER);check(splash.is(Items.SPLASH_POTION),"splash conversion");
      var lingering=mix(server,splash,Items.DRAGON_BREATH);check(lingering.is(Items.LINGERING_POTION),"lingering conversion");
      check(PotionUtils.getMobEffects(lingering).get(0).getDuration()==effects.get(0).getDuration(),"conversion preserves base duration; vanilla impact/cloud applies scaling");
      var stacks=net.minecraft.core.NonNullList.withSize(5,ItemStack.EMPTY);stacks.set(0,splash);stacks.set(1,lingering);
      PureElixirColorEvents.brewed(new net.minecraftforge.event.brewing.PotionBrewEvent.Post(stacks));
      check(PureElixirBrewing.isPure(stacks.get(0))&&PureElixirBrewing.isPure(stacks.get(1)),"converted potion identity");
     }
     index++;
    }
    check(mix(server,extended,Items.GLOWSTONE_DUST).isEmpty(),"no long+strong stacking");
    check(mix(server,strong,Items.REDSTONE).isEmpty(),"no strong+long stacking");
    check(mix(server,PureElixirBrewing.stack(bottle,Potions.WATER),ModItems.PURE_CRYSTAL.get()).isEmpty(),"water rejected");
   }
   for(String id:new String[]{"purified_arcsteel","purified_arcsteel_upgrade_smithing_template"}) {
    var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)server.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("purified_undead",id)).orElseThrow();
    var result=recipe.getResultItem(server.registryAccess());boolean steel=id.equals("purified_arcsteel");
    check(result.is(steel?ModItems.PURIFIED_ARCSTEEL.get():ModItems.PURIFIED_ARCSTEEL_UPGRADE_SMITHING_TEMPLATE.get())&&result.getCount()==(steel?2:1),"crafted result");
    Item C=ModItems.PURE_CRYSTAL.get(),D=Items.DIAMOND,I=Items.IRON_INGOT,T=Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
    Item[] layout=steel?new Item[]{Items.AIR,D,Items.AIR,I,C,I,Items.AIR,D,Items.AIR}:new Item[]{C,T,C,C,D,C,C,C,C};
    var stacks=new java.util.ArrayList<ItemStack>();for(var item:layout)stacks.add(item==Items.AIR?ItemStack.EMPTY:new ItemStack(item));
    var input=craft(stacks);check(recipe.matches(input,level),"shaped recipe layout "+id);
    stacks.set(4,new ItemStack(Items.DIRT));check(!recipe.matches(craft(stacks),level),"wrong center rejected");
   }
   var player=new net.minecraftforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("9c174499-4529-4d8f-8a27-9789fc8c4a14"),"PurificationSmoke"));
   player.getAbilities().instabuild=false;
   var h=CuriosApi.getCuriosInventory(player).orElseThrow(()->new IllegalStateException("Curios missing"));
   var slot=h.getCurios().get("ancient_contract").getStacks();slot.setStackInSlot(0,new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
   var progress=new WarriorProgress();player.getPersistentData().put("purified_undead:warrior_progress",progress.save());
   var spirit=(dev.purifiedundead.content.item.BlightedSpiritItem)ModItems.BLIGHTED_SPIRIT.get();
   var insufficient=new ItemStack(spirit,3);spirit.finishUsingItem(insufficient,level,player);check(insufficient.getCount()==3&&ContractProgressService.talismanLevel(player)==0&&crystals(player)==0,"insufficient rejected");
   var four=new ItemStack(spirit,4);spirit.finishUsingItem(four,level,player);check(four.isEmpty()&&ContractProgressService.talismanLevel(player)==1&&crystals(player)==4,"upgrade grants exactly 4");
   progress=WarriorProgress.load(player.getPersistentData().getCompound("purified_undead:warrior_progress"));while(progress.upgradeTalisman()){}player.getPersistentData().put("purified_undead:warrior_progress",progress.save());
   four=new ItemStack(spirit,4);player.getInventory().selected=8;player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,four);
   check(spirit.use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND).getResult().consumesAction(),"max allows hold use");
   spirit.finishUsingItem(four,level,player);check(four.isEmpty()&&ContractProgressService.talismanLevel(player)==WhiteWitchTalisman.maxLevel()&&crystals(player)==8,"max consumes without overlevel");
   spirit.finishUsingItem(four,level,player);check(crystals(player)==8,"empty stack cannot duplicate");
   slot.setStackInSlot(0,ItemStack.EMPTY);four=new ItemStack(spirit,4);spirit.finishUsingItem(four,level,player);check(four.getCount()==4&&crystals(player)==8,"unequipped at finish rejected");
   var costConfig=dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.spiritsPerTalismanLevel;
   int oldCost=costConfig.get();
   try {
    slot.setStackInSlot(0,new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
    for(int cost:new int[]{1,7,64}) {
     costConfig.set(cost);int before=crystals(player);
     var shortStack=new ItemStack(spirit,Math.max(0,cost-1));spirit.finishUsingItem(shortStack,level,player);
     check(crystals(player)==before,"custom cost rejects shortage "+cost);
     var batch=new ItemStack(spirit,cost);spirit.finishUsingItem(batch,level,player);
     check(batch.isEmpty()&&crystals(player)==before+cost,"custom cost yields same crystals "+cost);
     spirit.finishUsingItem(batch,level,player);check(crystals(player)==before+cost,"spent batch cannot repeat");
    }
    costConfig.set(7);player.getPersistentData().put("purified_undead:warrior_progress",new WarriorProgress().save());
    int before=crystals(player);var seven=new ItemStack(spirit,7);spirit.finishUsingItem(seven,level,player);
    check(seven.isEmpty()&&crystals(player)==before+7&&ContractProgressService.talismanLevel(player)==1,"custom cost also upgrades");
    // Inventory full: only the uninserted reward drops, still exactly the configured quantity.
    for(int i=0;i<player.getInventory().items.size();i++)player.getInventory().items.set(i,new ItemStack(Items.STONE,64));
    var prior=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(4)).stream().map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
    seven=new ItemStack(spirit,7);spirit.finishUsingItem(seven,level,player);
    int dropped=0;for(var item:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(4)))if(!prior.contains(item.getUUID())&&item.getItem().is(ModItems.PURE_CRYSTAL.get())){dropped+=item.getItem().getCount();item.discard();}
    check(seven.isEmpty()&&dropped==7,"full inventory preserves exact reward");
    System.out.println("PURIFICATION_RATIO_OK: 1/7/64 costs, upgrade and max, shortage, no duplicate, full inventory");
   } finally {costConfig.set(oldCost);player.getInventory().clearContent();}
   FoundryServerSmoke.run(server);
   FerinLargeMobSmoke.run(server);
   PurificationPartTwoSmoke.run(server);
   LilyDiarySmoke.run(server);
   AdvancementSmoke.run(server);
   try { HardcoreSmoke.run(server); dev.purifiedundead.progress.ProjectAuditSmoke.run(server); } catch (Exception failure) { throw new RuntimeException(failure); }
   System.out.println("PURIFICATION_SERVER_OK: 9 brewing recipes, 6 bottle conversions, colors, effects, 2 shaped recipes, upgrade/max/insufficient/no-contract/duplicate guards");
  } finally {server.execute(()->server.halt(false));}
 }
 private static int crystals(net.minecraft.world.entity.player.Player player) {int n=0;for(var stack:player.getInventory().items)if(stack.is(ModItems.PURE_CRYSTAL.get()))n+=stack.getCount();return n;}
 private static ItemStack mix(net.minecraft.server.MinecraftServer server,ItemStack input,Item ingredient) {return net.minecraftforge.common.brewing.BrewingRecipeRegistry.getOutput(input,new ItemStack(ingredient));}
 private static net.minecraft.world.inventory.CraftingContainer craft(java.util.List<ItemStack> stacks) {
  var menu=new net.minecraft.world.inventory.AbstractContainerMenu(null,0){public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}};
  var input=new net.minecraft.world.inventory.TransientCraftingContainer(menu,3,3);for(int i=0;i<9;i++)input.setItem(i,stacks.get(i));return input;
 }
}
