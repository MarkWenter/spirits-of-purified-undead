package dev.purifiedundead.slate;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.*;
import java.util.function.Supplier;

public final class SlateContent {
    public static final String[] WARRIORS={"groth","guardians","julius","ulv","eleine","hoenir","faden","ferin"};
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(Registries.ITEM,PurifiedUndead.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,PurifiedUndead.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES=DeferredRegister.create(Registries.RECIPE_SERIALIZER,PurifiedUndead.MOD_ID);
    public static final Supplier<Item> TABLET=ITEMS.register("slate_fragment",()->new Item(new Item.Properties()));
    public static final Supplier<Item> CIPHER_FRAGMENT=ITEMS.register("cipher_fragment",()->new Item(new Item.Properties()));
    public static final Supplier<Item> CIPHER_TEXT=ITEMS.register("cipher_text",()->new Item(new Item.Properties()));
    public static final Map<String,Supplier<Item>> FORGED=new LinkedHashMap<>(), MEMORIES=new LinkedHashMap<>();
    public static final Supplier<Item> GUARDIAN=ITEMS.register("shining_guardian_treasure",ShiningGuardianItem::new);
    public static final Supplier<MenuType<LilyMemoryMenu>> MENU=MENUS.register("lily_memories",()->new MenuType<>(LilyMemoryMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<RecipeSerializer<MemoryRecipe>> MEMORY_RECIPE=RECIPES.register("memory",()->new SimpleCraftingRecipeSerializer<>(MemoryRecipe::new));
    static {for(String key:WARRIORS) {
        if(!key.equals("ferin")) FORGED.put(key,ITEMS.register("forged_slate_"+key,()->new Item(new Item.Properties()) {@Override public boolean isFoil(ItemStack s){return true;}}));
        MEMORIES.put(key,ITEMS.register("blighted_memory_"+key,()->new MemoryItem(key)));
    }}
    public static void register(IEventBus bus){ITEMS.register(bus);MENUS.register(bus);RECIPES.register(bus);}
    public static void creative(java.util.function.Consumer<ItemStack> accept){for(var i:ITEMS.getEntries())accept.accept(new ItemStack(i.get()));}
    public static String memoryKey(ItemStack stack){return stack.getItem() instanceof MemoryItem memory?memory.key:null;}
    public static final class MemoryItem extends Item {
        public final String key;
        MemoryItem(String key){super(new Properties().stacksTo(1).rarity(Rarity.RARE));this.key=key;}
        @Override public void appendHoverText(ItemStack s,net.minecraft.world.level.Level level,List<net.minecraft.network.chat.Component> text,TooltipFlag flag){text.add(net.minecraft.network.chat.Component.translatable("item.purified_undead.blighted_memory_"+key+".desc").withStyle(net.minecraft.ChatFormatting.GRAY));}
    }
}
