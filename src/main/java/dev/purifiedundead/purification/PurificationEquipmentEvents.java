package dev.purifiedundead.purification;
import dev.purifiedundead.content.ModItems;
import net.minecraft.tags.DamageTypeTags;import net.minecraft.world.entity.EquipmentSlot;import net.minecraft.world.item.ItemStack;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead")
public final class PurificationEquipmentEvents {
 public static final net.minecraft.tags.TagKey<net.minecraft.world.damagesource.DamageType> MAGIC=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","immaculate_magic"));
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOW)
 public static void damage(net.minecraftforge.event.entity.living.LivingHurtEvent e){
  if(e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
  var p=e.getEntity();var source=e.getSource();float scale=1;
  if(p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.IMMACULATE_HELMET.get())&&source.is(DamageTypeTags.IS_PROJECTILE))scale*=.65F;
  if(p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.IMMACULATE_CHESTPLATE.get())&&source.is(MAGIC))scale*=.3F;
  if(p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.IMMACULATE_LEGGINGS.get())&&source.is(DamageTypeTags.IS_FIRE))scale*=.65F;
  if(p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.IMMACULATE_BOOTS.get())&&source.is(DamageTypeTags.IS_FREEZING))scale*=.65F;
  e.setAmount(e.getAmount()*scale);
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void anvil(net.minecraftforge.event.AnvilUpdateEvent e){
  if(e.getLeft().is(ModItems.BLIGHTED_GUARDIAN.get())&&cursed(e.getRight()))e.setCanceled(true);
 }
 private static boolean cursed(ItemStack s){return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(s).keySet().stream().anyMatch(net.minecraft.world.item.enchantment.Enchantment::isCurse);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void crafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent e){
  if(e.getCrafting().is(ModItems.BLIGHTED_GUARDIAN.get()))BlightedGuardianItem.ensureMending(e.getCrafting(),e.getEntity().level());
 }
}
