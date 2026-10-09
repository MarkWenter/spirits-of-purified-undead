package dev.purifiedundead.purification;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public final class LilyDiaryItem extends Item implements ICurioItem {
 public LilyDiaryItem(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));}
 @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,Player player,net.minecraft.world.InteractionHand hand){
  if(hand!=net.minecraft.world.InteractionHand.MAIN_HAND)return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
  if(!level.isClientSide)player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,p)->new dev.purifiedundead.slate.LilyMemoryMenu(id,inv),Component.translatable("container.purified_undead.lily_memories")));
  return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
 }
 @Override public boolean canEquip(SlotContext c,ItemStack s){return c.identifier().equals("wanderer_log")&&!c.cosmetic();}
 @Override public void curioTick(SlotContext c,ItemStack s){if(!c.cosmetic()&&c.identifier().equals("wanderer_log")&&c.entity() instanceof Player p&&!p.level().isClientSide)LilyDiaryProgress.apply(p,LilyDiaryProgress.count(p));}
 @Override public void onUnequip(SlotContext c,ItemStack next,ItemStack old){if(c.entity() instanceof Player p&&!p.level().isClientSide)LilyDiaryProgress.apply(p,0);}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.level.Level ctx,java.util.List<Component> text,TooltipFlag flag){
  text.add(Component.translatable("item.purified_undead.lily_diary.memories").withStyle(net.minecraft.ChatFormatting.GRAY));
  text.add(Component.translatable("item.purified_undead.lily_diary.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
  text.add(Component.translatable("item.purified_undead.lily_diary.lore").withStyle(net.minecraft.ChatFormatting.AQUA));
 }
}
