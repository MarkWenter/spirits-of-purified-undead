package dev.purifiedundead.purification;
import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.*;import dev.purifiedundead.content.ModItems;
import net.minecraft.world.entity.Entity;import net.minecraft.world.level.Level;
public final class BlightedGuardianItem extends SwordItem {
 private final com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> heldAttributes;
 @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot){return slot==net.minecraft.world.entity.EquipmentSlot.MAINHAND?heldAttributes:super.getDefaultAttributeModifiers(slot);}
 private static final Tier TIER=new Tier(){
  public int getUses(){return 7989;}public float getSpeed(){return 9;}public float getAttackDamageBonus(){return 4;}
  public int getEnchantmentValue(){return 22;}public Ingredient getRepairIngredient(){return Ingredient.of(ModItems.PURIFIED_ARCSTEEL.get());}
  public int getLevel(){return 4;}
 };
 public BlightedGuardianItem(){super(TIER,18,-1.8F,new Item.Properties().fireResistant().rarity(Rarity.EPIC));
 var builder=com.google.common.collect.ImmutableMultimap.<net.minecraft.world.entity.ai.attributes.Attribute,net.minecraft.world.entity.ai.attributes.AttributeModifier>builder();
 builder.putAll(super.getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
 builder.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.fromString("413f3982-3d99-4fd7-97ad-dd105b810a4c"),"Blighted guardian held armor",10,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));heldAttributes=builder.build();}
 @Override public ItemStack getDefaultInstance(){ItemStack s=super.getDefaultInstance();s.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING,1);return s;}
 @Override public void inventoryTick(ItemStack s,Level level,Entity e,int slot,boolean selected){if(!level.isClientSide)ensureMending(s,level);super.inventoryTick(s,level,e,slot,selected);}
 public static void ensureMending(ItemStack s,Level level){var map=net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(s);
 boolean changed=map.keySet().removeIf(net.minecraft.world.item.enchantment.Enchantment::isCurse);
 if(!map.containsKey(net.minecraft.world.item.enchantment.Enchantments.MENDING)){map.put(net.minecraft.world.item.enchantment.Enchantments.MENDING,1);changed=true;}
 if(changed)net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(map,s);}
 @Override public void appendHoverText(ItemStack s,Level level,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){lines.add(net.minecraft.network.chat.Component.translatable("tooltip.purified_undead.guardian_wave").withStyle(net.minecraft.ChatFormatting.GRAY));lines.add(net.minecraft.network.chat.Component.literal("Thank you for finding your way to me.").withStyle(net.minecraft.ChatFormatting.GRAY));}
@Override public boolean canApplyAtEnchantingTable(ItemStack s,net.minecraft.world.item.enchantment.Enchantment e){return !e.isCurse()&&(super.canApplyAtEnchantingTable(s,e)||dev.purifiedundead.compat.GuardianEnchantments.accepts(e));}}
