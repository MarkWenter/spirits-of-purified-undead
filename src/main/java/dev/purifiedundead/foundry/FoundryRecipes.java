package dev.purifiedundead.foundry;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Validated server configuration, reloaded at server start. Invalid files never overwrite user edits. */
public final class FoundryRecipes {
    public record Recipe(String top,int topCount,String bottom,int bottomCount,String output,int outputCount,boolean consumeTop,int leftFuel,int rightFuel,int ticks){
        public boolean matches(ItemStack a,ItemStack b){return id(a).equals(top)&&a.getCount()>=topCount&&id(b).equals(bottom)&&b.getCount()>=bottomCount;}
        public ItemStack result(){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(output)),outputCount);}
        public int batch(ItemStack a,ItemStack b,ItemStack left,ItemStack right){
            if(!matches(a,b))return 0;
            int n=Math.min(b.getCount()/bottomCount,Math.min(left.getCount()/leftFuel,right.getCount()/rightFuel));
            if(consumeTop)n=Math.min(n,a.getCount()/topCount);
            return Math.min(n,result().getMaxStackSize()/outputCount);
        }
    }
    private static List<Recipe> recipes=List.of();
    public static String id(ItemStack s){return s.isEmpty()?"":BuiltInRegistries.ITEM.getKey(s.getItem()).toString();}
    public static Recipe find(ItemStack a,ItemStack b){for(var r:recipes)if(r.matches(a,b))return r;return null;}
    public static boolean bottom(ItemStack s){return recipes.stream().anyMatch(r->r.bottom.equals(id(s)));}
    public static boolean top(ItemStack s){return recipes.stream().anyMatch(r->r.top.equals(id(s)));}
    public static List<Recipe> all(){return recipes;}
    public static List<Recipe> defaults(){
        var list=new ArrayList<Recipe>();
        list.add(new Recipe("minecraft:coal",1,"minecraft:coal",1,"minecraft:diamond",2,true,1,1,400));
        list.add(new Recipe("minecraft:iron_ingot",4,"minecraft:gold_ingot",4,"minecraft:netherite_scrap",1,true,1,1,400));
        list.add(new Recipe("minecraft:amethyst_shard",1,"minecraft:ink_sac",1,"minecraft:echo_shard",1,true,1,1,400));
        list.add(new Recipe("minecraft:gold_ingot",2,"minecraft:stick",1,"minecraft:clock",1,true,1,1,400));
        list.add(new Recipe("minecraft:diamond",16,"minecraft:wither_skeleton_skull",1,"minecraft:nether_star",1,true,1,1,400));
        for(String k:dev.purifiedundead.slate.SlateContent.WARRIORS)if(!k.equals("ferin"))list.add(new Recipe("purified_undead:"+(k.equals("guardians")?"guardian_warriors":k+"_warrior"),1,"purified_undead:slate_fragment",1,"purified_undead:forged_slate_"+k,1,false,1,1,400));
        return list;
    }
    public static void load(){
        Path path=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("purified_undead-foundry.json");
        var gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            if(!Files.exists(path)){var root=new JsonObject();root.addProperty("_help","A=top / 上槽; B=bottom / 下槽. consumeTop=false preserves A. Counts and fuels are per result batch unit; ticks=400 is 20 seconds. Restart server after changes. IDs may come from any mod; invalid entries are skipped with a warning.");root.add("recipes",gson.toJsonTree(defaults()));Files.writeString(path,gson.toJson(root),StandardCharsets.UTF_8);}
            var root=JsonParser.parseString(Files.readString(path,StandardCharsets.UTF_8)).getAsJsonObject();var loaded=new ArrayList<Recipe>();
            for(var element:root.getAsJsonArray("recipes"))try{var object=element.getAsJsonObject();if(!object.has("consumeTop")||!object.get("consumeTop").isJsonPrimitive()||!object.get("consumeTop").getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("consumeTop must be an explicit boolean");var recipe=gson.fromJson(element,Recipe.class);validate(recipe);loaded.add(recipe);}catch(RuntimeException bad){com.mojang.logging.LogUtils.getLogger().warn("Invalid foundry recipe skipped: {}",bad.getMessage());}
            recipes=List.copyOf(loaded);
        }catch(Exception e){recipes=List.of();com.mojang.logging.LogUtils.getLogger().error("Foundry configuration could not load; processing disabled, inventory untouched: {}",path,e);}
    }
    public static void validate(Recipe r){
        for(String s:new String[]{r.top,r.bottom,r.output}){var id=ResourceLocation.tryParse(s==null?"":s);if(id==null||!BuiltInRegistries.ITEM.containsKey(id)||BuiltInRegistries.ITEM.get(id)==Items.AIR)throw new IllegalArgumentException("Unknown item: "+s);}
        if(r.topCount<1||r.bottomCount<1||r.outputCount<1||r.topCount>64||r.bottomCount>64||r.outputCount>r.result().getMaxStackSize()||r.leftFuel<1||r.leftFuel>64||r.rightFuel<1||r.rightFuel>64||r.ticks<1||r.ticks>32767)throw new IllegalArgumentException("Invalid count/fuel/ticks: "+r);
    }
    private FoundryRecipes(){}
}
