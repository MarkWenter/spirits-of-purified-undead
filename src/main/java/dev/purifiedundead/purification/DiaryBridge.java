package dev.purifiedundead.purification;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
/** Patchouli book identity bridge. Patchouli is a required runtime dependency. */
public final class DiaryBridge {
 public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("purified_undead","white_witch_diary");
 public static ItemStack create(){
  if(!net.minecraftforge.fml.ModList.get().isLoaded("patchouli"))return ItemStack.EMPTY;
  try {Object api=Class.forName("vazkii.patchouli.api.PatchouliAPI").getMethod("get").invoke(null);
   return ((ItemStack)Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI").getMethod("getBookStack",ResourceLocation.class).invoke(api,ID)).copy();
  }catch(ReflectiveOperationException e){throw new IllegalStateException("Patchouli book API unavailable",e);}
 }
 public static boolean isDiary(ItemStack s){
  if(s.isEmpty()||!net.minecraftforge.fml.ModList.get().isLoaded("patchouli"))return false;
  try {Object book=Class.forName("vazkii.patchouli.common.item.ItemModBook").getMethod("getBook",ItemStack.class).invoke(null,s);
   return book!=null&&ID.equals(book.getClass().getField("id").get(book));
  }catch(ReflectiveOperationException e){return false;}
 }
}
