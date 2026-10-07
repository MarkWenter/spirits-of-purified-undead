package dev.purifiedundead.compat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
/** Probe normal reference weapons, preserving each enchantment's own category and exclusivity rules. */
public final class GuardianEnchantments {
 public static boolean accepts(Enchantment e){
  if(e.isCurse())return false;
  var id=BuiltInRegistries.ENCHANTMENT.getKey(e);if(id==null)return false;
  String ns=id.getNamespace();
  if((ns.equals("apotheosis")||ns.equals("celestial_enchantments"))&&e.canEnchant(new ItemStack(Items.NETHERITE_SWORD)))return true;
  if(ns.equals("malum")){
   String name=id.getPath();
   if(name.equals("spirit_plunder")||name.equals("haunted"))return true;
   for(String scythe:new String[]{"soul_stained_steel_scythe","malignant_stronghold_scythe","edge_of_deliverance"}){
    var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("malum",scythe));if(item==Items.AIR)continue;
    var probe=new ItemStack(item);if(probe.canApplyAtEnchantingTable(e))return true;
   }
  }
  if(ns.equals("goety"))for(String name:new String[]{"dark_scythe","dark_metal_scythe","death_scythe","scythe"}){
   var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("goety",name));
   if(item!=Items.AIR&&new ItemStack(item).canApplyAtEnchantingTable(e))return true;
  }
  return false;
 }
 private GuardianEnchantments(){}
}
