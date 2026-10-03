package dev.purifiedundead.validation;

import dev.purifiedundead.foundry.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public final class FoundryServerSmoke {
    private static void check(boolean ok, String reason) { if (!ok) throw new IllegalStateException("FOUNDRY_SERVER_FAILED: " + reason); }
    public static void run(MinecraftServer server) {
        var level=server.overworld(); var pos=new BlockPos(36,120,36);
        var fake=new net.minecraftforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("b7b01471-8762-4398-a6f7-1297a44d7102"),"FoundrySmoke"));
        fake.setPos(36.5,120,37.5);
        try {
            for(Direction front:Direction.Plane.HORIZONTAL) {
                level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos,FoundryContent.BLOCK.get().defaultBlockState().setValue(PurificationFoundryBlock.FACING,front));
                var be=(PurificationFoundryEntity)level.getBlockEntity(pos);
                Direction[] faces={Direction.UP,front.getClockWise(),front.getCounterClockWise()};
                Item[] inputs={Items.COBBLESTONE,Items.REDSTONE,Items.DIAMOND};
                for(int i=0;i<3;i++) {
                    var remain=HopperBlockEntity.addItem(null,be,new ItemStack(inputs[i],3),faces[i]);
                    check(remain.isEmpty()&&be.getItem(i).is(inputs[i])&&be.getItem(i).getCount()==3,"hopper routing "+front+" slot "+i);
                }
                for(Direction blocked:new Direction[]{front,front.getOpposite(),Direction.DOWN})check(HopperBlockEntity.addItem(null,be,new ItemStack(Items.DIRT),blocked).getCount()==1,"blocked insertion "+blocked);
                be.setItem(3,new ItemStack(Items.EMERALD,2));
                level.setBlockAndUpdate(pos.below(),Blocks.HOPPER.defaultBlockState());
                var hopper=(HopperBlockEntity)level.getBlockEntity(pos.below());
                check(HopperBlockEntity.suckInItems(level,hopper),"bottom hopper transfer");
                check(hopper.getItem(0).is(Items.EMERALD)&&be.getItem(3).getCount()==1,"only output extracted");
                check(!be.canTakeItemThroughFace(0,be.getItem(0),Direction.DOWN),"input cannot be extracted");
                var saved=be.saveWithFullMetadata();var restored=new PurificationFoundryEntity(pos,be.getBlockState());restored.load(saved);
                for(int i=0;i<4;i++)check(ItemStack.matches(be.getItem(i),restored.getItem(i)),"persistent slot "+i);
                var menu=(PurificationFoundryMenu)be.createMenu(1,fake.getInventory(),fake);
                check(!menu.getSlot(3).mayPlace(new ItemStack(Items.DIRT)),"output read only");
                be.setWorkProgress(50,100);check(menu.progressPixels(60)==30&&be.getBlockState().getValue(PurificationFoundryBlock.LIT),"half progress + lit");
                be.setWorkProgress(0,0);check(menu.progressPixels(60)==0&&!be.getBlockState().getValue(PurificationFoundryBlock.LIT),"idle presentation");
                menu.quickMoveStack(fake,0);check(be.getItem(0).isEmpty(),"shift machine to inventory");
                var box=new net.minecraft.world.phys.AABB(pos).inflate(2);level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).forEach(net.minecraft.world.entity.Entity::discard);
                int expected=0;for(int i=0;i<4;i++)expected+=be.getItem(i).getCount();
                level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                int actual=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).stream().mapToInt(e->e.getItem().getCount()).sum();
                check(expected==actual,"break inventory exactly once");
                level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).forEach(net.minecraft.world.entity.Entity::discard);
                hopper.clearContent();level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());
            }
            var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)server.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","purification_foundry")).orElseThrow();
            for(int mode=0;mode<3;mode++) {
                var stacks=new java.util.ArrayList<ItemStack>();
                for(int i=0;i<9;i++)stacks.add(new ItemStack(i==1?Items.IRON_SWORD:i==4?Items.REDSTONE:(mode==0||mode==2&&i%2==0)?Items.COBBLESTONE:Items.COBBLED_DEEPSLATE));
                check(recipe.matches(craft(stacks),level),"cobble/deepslate/mixed recipe");
                stacks.set(1,new ItemStack(Items.STONE_SWORD));check(!recipe.matches(craft(stacks),level),"wrong sword rejected");
            }
            check(recipe.getResultItem(server.registryAccess()).is(FoundryContent.ITEM.get()),"recipe output");
            System.out.println("FOUNDRY_SERVER_OK: four orientations, vanilla hopper insertion/extraction, blocked faces, persistence, menu, progress/lit, removal, 3 crafting layouts");
        } catch(Throwable t) {System.out.println("FOUNDRY_SERVER_FAILED");throw t;}
        finally {level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());}
    }
    private static net.minecraft.world.inventory.CraftingContainer craft(java.util.List<ItemStack> stacks) {
        var menu=new net.minecraft.world.inventory.AbstractContainerMenu(null,0){public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}};
        var input=new net.minecraft.world.inventory.TransientCraftingContainer(menu,3,3);for(int i=0;i<9;i++)input.setItem(i,stacks.get(i));return input;
    }
}
