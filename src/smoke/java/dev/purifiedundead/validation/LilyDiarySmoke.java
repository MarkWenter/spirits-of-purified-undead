package dev.purifiedundead.validation;
import dev.purifiedundead.content.*;
import dev.purifiedundead.purification.*;
import dev.purifiedundead.progress.PureElixirBrewing;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.theillusivec4.curios.api.CuriosApi;
public final class LilyDiarySmoke {
 private static void check(boolean v,String m){if(!v)throw new IllegalStateException("LILY_FAILED: "+m);}
 public static void run(net.minecraft.server.MinecraftServer server){
  var level=server.overworld();var p=new net.minecraftforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"LilySmoke"));
  LilyDiaryProgress.grant(p);LilyDiaryProgress.grant(p);int books=0;for(var s:p.getInventory().items)if(DiaryBridge.isDiary(s))books+=s.getCount();check(books==1,"one-time starter book");
  check(!LilyDiaryProgress.record(p,EntityType.ZOMBIE.create(level)),"not worn does not record");
  PurificationProgress.unlock(p);var h=CuriosApi.getCuriosInventory(p).orElseThrow(()->new IllegalStateException("Curios"));var slot=h.getCurios().get("wanderer_log").getStacks();slot.setStackInSlot(0,new ItemStack(ModItems.LILY_DIARY.get()));
  var crit=net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("attributeslib","crit_chance"));
  double initial=p.getAttributeValue(crit),luck=p.getAttributeValue(Attributes.LUCK);
  check(LilyDiaryProgress.record(p,EntityType.ZOMBIE.create(level)),"zombie recorded");check(!LilyDiaryProgress.record(p,EntityType.ZOMBIE.create(level)),"duplicate rejected");
  check(LilyDiaryProgress.record(p,EntityType.DROWNED.create(level)),"drowned separate");check(!LilyDiaryProgress.record(p,EntityType.COW.create(level)),"living rejected");
  check(LilyDiaryProgress.count(p)==2&&Math.abs(p.getAttributeValue(crit)-initial-.06)<1e-6&&Math.abs(p.getAttributeValue(Attributes.LUCK)-luck-1)<1e-6,"two types bonuses");
  slot.setStackInSlot(0,ItemStack.EMPTY);LilyDiaryProgress.apply(p,0);check(Math.abs(p.getAttributeValue(crit)-initial)<1e-6&&p.getAttributeValue(Attributes.LUCK)==luck,"unequip removes bonuses");
  slot.setStackInSlot(0,new ItemStack(ModItems.LILY_DIARY.get()));LilyDiaryProgress.apply(p,LilyDiaryProgress.count(p));check(Math.abs(p.getAttributeValue(crit)-initial-.06)<1e-6,"replacement diary retains history");
  var clone=new net.minecraftforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"LilyClone"));LilyDiaryProgress.clone(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(clone,p,true));check(LilyDiaryProgress.count(clone)==2&&clone.getPersistentData().getBoolean(LilyDiaryProgress.GIFT),"clone retains history and gift flag");
  var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","lily_diary");var recipe=(LilySmithingRecipe)server.getRecipeManager().byKey(id).orElseThrow();
  var menu=new net.minecraft.world.inventory.SmithingMenu(9,p.getInventory());
  for(Item bottle:new Item[]{Items.POTION,Items.SPLASH_POTION,Items.LINGERING_POTION})for(var potion:java.util.List.of(ModPotions.PURE_ELIXIR.get(),ModPotions.LONG_PURE_ELIXIR.get(),ModPotions.STRONG_PURE_ELIXIR.get())){
   menu.getSlot(0).set(ItemStack.EMPTY);menu.getSlot(1).set(DiaryBridge.create());menu.getSlot(2).set(PureElixirBrewing.stack(bottle,potion));menu.createResult();
   check(menu.getSlot(3).getItem().is(ModItems.LILY_DIARY.get()),"actual smithing menu accepts all 9 elixirs");check(!DiaryBridge.isDiary(menu.getSlot(3).getItem()),"output not a readable book");
  }
  menu.getSlot(2).set(new ItemStack(Items.POTION));menu.createResult();check(menu.getSlot(3).getItem().isEmpty(),"ordinary potion rejected");
  menu.getSlot(2).set(PureElixirBrewing.stack(Items.POTION,ModPotions.PURE_ELIXIR.get()));menu.getSlot(1).set(new ItemStack(Items.BOOK));menu.createResult();check(menu.getSlot(3).getItem().isEmpty(),"ordinary book rejected");
  menu.getSlot(1).set(DiaryBridge.create());menu.getSlot(0).set(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));menu.createResult();check(menu.getSlot(3).getItem().isEmpty(),"template rejected");
  UndeadCompatSmoke.run(server);
  System.out.println("LILY_DIARY_OK: first gift once, distinct undead only while worn, bonuses/remove/re-equip, clone persistence, real smithing menu all 9 elixirs and invalid inputs");
 }
}
