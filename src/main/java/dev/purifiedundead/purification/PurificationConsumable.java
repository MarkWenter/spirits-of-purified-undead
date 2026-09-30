package dev.purifiedundead.purification;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
public final class PurificationConsumable extends Item {
 public enum Kind { WHITE, SCARLET, FRUIT }
 private final Kind kind;
 public PurificationConsumable(Kind kind){super(new Properties().stacksTo(16).rarity(kind==Kind.FRUIT?Rarity.EPIC:Rarity.RARE));this.kind=kind;}
 @Override public int getUseDuration(ItemStack stack) {return kind==Kind.FRUIT?32:30;}
 @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.EAT;}
 private boolean allowed(Player p,boolean message){
  if(kind!=Kind.FRUIT)return true;
  String key=PurificationProgress.used(p)?"message.purified_undead.fruit_used":!PurificationProgress.contract(p)?"message.purified_undead.fruit_denied":null;
  if(key==null)return true;
  if(message)p.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_RED),true);
  return false;
 }
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
  ItemStack s=p.getItemInHand(hand);if(!allowed(p,!level.isClientSide))return InteractionResultHolder.fail(s);
  p.startUsingItem(hand);return InteractionResultHolder.consume(s);
 }
 @Override public ItemStack finishUsingItem(ItemStack s,Level level,LivingEntity entity){
  if(!level.isClientSide && !s.isEmpty() && entity instanceof Player p && p.isAlive() && allowed(p,true)){
   if(kind==Kind.WHITE)p.heal(p.getMaxHealth()*.5F);
   else if(kind==Kind.SCARLET)p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,1000,3));
   else PurificationProgress.unlock(p);
   if(!p.getAbilities().instabuild)s.shrink(1);
   p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
  }
  return s;
 }
 @Override public void appendHoverText(ItemStack s,Level level,java.util.List<Component> lines,TooltipFlag flag){
  lines.add(Component.translatable(getDescriptionId()+".desc").withStyle(ChatFormatting.GRAY));
 }
}
