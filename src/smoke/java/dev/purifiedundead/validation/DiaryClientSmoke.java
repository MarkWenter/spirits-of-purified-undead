package dev.purifiedundead.validation;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="purified_undead",value=Dist.CLIENT)
public final class DiaryClientSmoke {
 private static int ticks,step,delay; private static boolean loading,ready;
 private static Object book; private static final List<Object[]> spreads=new ArrayList<>();
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
  if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
  if(!Boolean.getBoolean("purified_undead.diaryClientSmoke"))return;
  var mc=Minecraft.getInstance();
  try {
   if(!loading && mc.screen instanceof TitleScreen && mc.getOverlay()==null && ++ticks>40){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
   if(mc.level==null || mc.player==null || mc.getOverlay()!=null)return;
   if(!ready) {
    if(++delay<80)return;
    var cls=Class.forName("vazkii.patchouli.common.book.BookRegistry");
    book=((Map<?,?>)cls.getField("books").get(cls.getField("INSTANCE").get(null))).get(ResourceLocation.fromNamespaceAndPath("purified_undead","white_witch_diary"));
    if(book==null)throw new IllegalStateException("missing book");
    book.getClass().getMethod("reloadContents",net.minecraft.world.level.Level.class,boolean.class).invoke(book,mc.level,false);
    Object contents=book.getClass().getMethod("getContents").invoke(book);
    if((boolean)contents.getClass().getMethod("isErrored").invoke(contents))throw new IllegalStateException("book content error: "+contents.getClass().getMethod("getException").invoke(contents));
    Map<?,?> entries=(Map<?,?>)contents.getClass().getField("entries").get(contents);
    if(entries.size()!=25)throw new IllegalStateException("entry count="+entries.size());
    var stack=(net.minecraft.world.item.ItemStack)book.getClass().getMethod("getBookItem").invoke(book);
    if(!stack.getHoverName().getString().equals("白巫女手记"))throw new IllegalStateException("book name");
    if(mc.getItemRenderer().getModel(stack,mc.level,mc.player,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("missing item model");
    mc.player.getInventory().setItem(0,stack);
    int pages=0;
    for(Object entry:entries.values()) {
     List<?> list=(List<?>)entry.getClass().getMethod("getPages").invoke(entry);pages+=list.size();
     for(int i=0;i<list.size();i+=2)spreads.add(new Object[]{entry,i/2});
    }
    System.out.println("DIARY_CONTENTS_OK: entries="+entries.size()+" pages="+pages);
    ready=true;delay=0;show(mc);return;
   }
   if(++delay<8)return;delay=0;
   Object[] pair=spreads.get(step);Object entry=pair[0];
   List<?> pages=(List<?>)entry.getClass().getMethod("getPages").invoke(entry);
   int first=(int)pair[1]*2;
   for(int i=first;i<Math.min(first+2,pages.size());i++)checkPage(pages.get(i));
   String name=entry.getClass().getMethod("getId").invoke(entry).toString().replace(':','-').replace('/','-');
   net.minecraft.client.Screenshot.grab(mc.gameDirectory,"diary-v035-"+name+"-"+pair[1]+".png",mc.getMainRenderTarget(),m->{});
   if(++step==spreads.size()){System.out.println("PURIFIED_UNDEAD_DIARY_CLIENT_OK: all spreads rendered, text bounds checked");mc.stop();return;}
   show(mc);
  }catch(Exception e){e.printStackTrace();System.out.println("DIARY_CLIENT_FAILED: "+e);mc.stop();}
 }
 private static void show(Minecraft mc)throws Exception {
  Object[] p=spreads.get(step);
  Screen screen=(Screen)Class.forName("vazkii.patchouli.client.book.gui.GuiBookEntry").getConstructor(book.getClass(),p[0].getClass(),int.class).newInstance(book,p[0],p[1]);
  mc.setScreen(screen);
 }
 private static void checkPage(Object page)throws Exception {
  Class<?> c=Class.forName("vazkii.patchouli.client.book.page.abstr.PageWithText");
  if(!c.isInstance(page))return;
  Field f=c.getDeclaredField("textRender");f.setAccessible(true);Object render=f.get(page);
  Field words=render.getClass().getDeclaredField("words");words.setAccessible(true);
  for(Object word:(List<?>)words.get(render)) {
   int y=word.getClass().getField("y").getInt(word);
   if(y+9>156)throw new IllegalStateException("text overflows page at y="+y);
  }
 }
}
