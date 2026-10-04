package dev.purifiedundead.slate;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

/** Only this recipe returns the forged tablet; the seven-tablet relic consumes them. */
public final class MemoryRecipe extends CustomRecipe {
    public MemoryRecipe(ResourceLocation id,CraftingBookCategory category){super(id,category);}
    private String key(CraftingContainer c){
        String found=null;int texts=0,tablets=0;
        for(int i=0;i<c.getContainerSize();i++){
            var s=c.getItem(i);if(s.isEmpty())continue;
            if(s.is(SlateContent.CIPHER_TEXT.get())){texts++;continue;}
            String key=null;for(var e:SlateContent.FORGED.entrySet())if(s.is(e.getValue().get())){key=e.getKey();break;}
            if(key==null)return null;found=key;tablets++;
        }
        return texts==1&&tablets==1?found:null;
    }
    @Override public boolean matches(CraftingContainer c,Level l){return key(c)!=null;}
    @Override public ItemStack assemble(CraftingContainer c,RegistryAccess access){String k=key(c);return k==null?ItemStack.EMPTY:new ItemStack(SlateContent.MEMORIES.get(k).get());}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer c){
        var out=NonNullList.withSize(c.getContainerSize(),ItemStack.EMPTY);
        if(key(c)!=null)for(int i=0;i<c.getContainerSize();i++)if(!c.getItem(i).is(SlateContent.CIPHER_TEXT.get())&&!c.getItem(i).isEmpty()){var s=c.getItem(i).copy();s.setCount(1);out.set(i,s);}
        return out;
    }
    @Override public boolean canCraftInDimensions(int w,int h){return w*h>=2;}
    @Override public RecipeSerializer<?> getSerializer(){return SlateContent.MEMORY_RECIPE.get();}
}
