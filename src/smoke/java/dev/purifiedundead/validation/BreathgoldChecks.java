package dev.purifiedundead.validation;

import com.mojang.logging.LogUtils;

import dev.purifiedundead.blightedarmaments.*;
import dev.purifiedundead.blightedrain.*;
import dev.purifiedundead.blightedrain.BlightedRainData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;

public final class BreathgoldChecks {
    private static int checks;

    private static void check(boolean result, String name) {
        if (!result) throw new IllegalStateException(name);
        checks++;
    }

    public static void verify(ServerLevel level) throws Exception {
        var pos = new BlockPos(8, 100, 8);
        var natural = pos.east(2);
        level.getChunkAt(pos);
        level.setBlockAndUpdate(pos, Blocks.GOLD_ORE.defaultBlockState());
        level.setBlockAndUpdate(natural, Blocks.GOLD_ORE.defaultBlockState());
        level.removeBlock(pos.above(), false);
        level.setWeatherParameters(0, 20000, true, false);
        level.setRainLevel(1F);
        var field = BlightedRainData.class.getDeclaredField("model");
        field.setAccessible(true);
        var model = (BlightedRainModel) field.get(BlightedRainData.get(level));
        model.reset();
        var data = PlacedGoldData.get(level);
        var placingPlayer = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var snapshot =
                net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(), level, pos);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent(
                        snapshot, Blocks.STONE.defaultBlockState(), placingPlayer));
        for (int i = 0; i < 220; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "ordinary rain must not convert");
        model.start(level.getGameTime(), 6000);
        for (int i = 0; i < 220; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "warning must not convert");
        model.advance(level.getGameTime() + 300);
        check(BlightedRainData.get(level).isActive(), "real base ACTIVE bridge");
        check(RainExposure.exposedAt(level, pos.above()), "test ore is exposed");
        for (int i = 0; i < 199; i++)
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                    new net.minecraftforge.event.TickEvent.LevelTickEvent(
                            net.minecraftforge.fml.LogicalSide.SERVER,
                            net.minecraftforge.event.TickEvent.Phase.END,
                            level,
                            () -> true));
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "199 ticks must not convert");
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.TickEvent.LevelTickEvent(
                        net.minecraftforge.fml.LogicalSide.SERVER,
                        net.minecraftforge.event.TickEvent.Phase.END,
                        level,
                        () -> true));
        check(
                level.getBlockState(pos).is(BreathgoldContent.ORE.get()),
                "200 ticks converts via real place/tick subscribers");
        check(
                level.getBlockState(natural).is(Blocks.GOLD_ORE),
                "unplaced natural gold is untouched");
        level.setBlockAndUpdate(pos, Blocks.GOLD_ORE.defaultBlockState());
        data.placed(pos);
        for (int i = 0; i < 100; i++) data.tick(level);
        level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        data.tick(level);
        for (int i = 0; i < 210; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "roof blocks conversion");
        level.removeBlock(pos.above(), false);
        for (int i = 0; i < 100; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "shelter resets exposure");
        model.stop(level.getGameTime());
        data.tick(level);
        model.reset();
        model.start(level.getGameTime(), 6000);
        model.advance(level.getGameTime() + 300);
        for (int i = 0; i < 100; i++) data.tick(level);
        check(
                level.getBlockState(pos).is(Blocks.GOLD_ORE),
                "rain phase interruption resets exposure");
        CompoundTag saved = data.save(new CompoundTag());
        var reloaded = PlacedGoldData.load(saved);
        for (int i = 0; i < 199; i++) reloaded.tick(level);
        check(
                level.getBlockState(pos).is(Blocks.GOLD_ORE),
                "saved placement retained but partial progress reset");
        reloaded.tick(level);
        check(
                level.getBlockState(pos).is(BreathgoldContent.ORE.get()),
                "saved placement converts after full exposure");
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var ore = BreathgoldContent.ORE.get().defaultBlockState();
        for (Item tool :
                new Item[] {
                    Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_AXE
                })
            check(
                    !new ItemStack(tool).isCorrectToolForDrops(ore),
                    "invalid tool rejected: " + tool);
        for (Item tool :
                new Item[] {
                    Items.IRON_PICKAXE,
                    Items.DIAMOND_PICKAXE,
                    Items.NETHERITE_PICKAXE,
                    BreathgoldContent.PICKAXE.get()
                })
            check(
                    new ItemStack(tool).isCorrectToolForDrops(ore),
                    "iron or higher pickaxe accepted: " + tool);
        var pick = new ItemStack(Items.IRON_PICKAXE);
        var drops = Block.getDrops(ore, level, pos, null, player, pick);
        check(
                drops.size() == 1
                        && drops.get(0).is(BreathgoldContent.MATERIAL.get())
                        && drops.get(0).getCount() == 1,
                "normal ore material drop");
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
        check(
                Block.getDrops(ore, level, pos, null, player, pick).isEmpty(),
                "mining fatigue prevents material");
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        var silk = pick.copy();
        silk.enchant(Enchantments.SILK_TOUCH, 1);
        check(
                Block.getDrops(ore, level, pos, null, player, silk).stream()
                        .anyMatch(i -> i.is(BreathgoldContent.ORE_ITEM.get())),
                "silk touch preserves ore");
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
        check(
                Block.getDrops(ore, level, pos, null, player, silk).isEmpty(),
                "mining fatigue also prevents silk harvest");
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        var fortune = pick.copy();
        fortune.enchant(Enchantments.BLOCK_FORTUNE, 3);
        int sum = 0;
        for (int i = 0; i < 80; i++) {
            var d = Block.getDrops(ore, level, pos, null, player, fortune);
            int n = d.stream().mapToInt(ItemStack::getCount).sum();
            check(n >= 1 && n <= 4, "fortune bounds");
            sum += n;
        }
        check(sum > 80, "fortune increases yield");
        for (Item item :
                new Item[] {
                    BreathgoldContent.SWORD.get(),
                    BreathgoldContent.AXE.get(),
                    BreathgoldContent.HOE.get(),
                    BreathgoldContent.PICKAXE.get(),
                    BreathgoldContent.SHOVEL.get()
                }) {
            check(new ItemStack(item).getMaxDamage() == 375, "durability 375: " + item);
            check(
                    item.isValidRepairItem(
                            new ItemStack(item), new ItemStack(BreathgoldContent.MATERIAL.get())),
                    "material repairs tool");
        }
        check(BreathgoldContent.TIER.getSpeed() == 6F, "iron mining speed");
        check(
                ((SwordItem) BreathgoldContent.SWORD.get()).getDamage() == 6F,
                "sword damage is iron + 1");
        String[] kinds = {"sword", "axe", "hoe", "pickaxe", "shovel"};
        String[][] patterns = {
            {"M  ", "M  ", "S  "},
            {"MM ", "MS ", " S "},
            {"MM ", " S ", " S "},
            {"MMM", " S ", " S "},
            {"M  ", "S  ", "S  "}
        };
        for (int k = 0; k < kinds.length; k++) {
            String kind = kinds[k];
            var recipe =
                    level.getRecipeManager()
                            .byKey(new ResourceLocation("purified_undead:breathgold_" + kind))
                            .orElseThrow();
            var craft = (CraftingRecipe) recipe;
            var menu =
                    new AbstractContainerMenu(null, 0) {
                        public ItemStack quickMoveStack(Player p, int n) {
                            return ItemStack.EMPTY;
                        }

                        public boolean stillValid(Player p) {
                            return true;
                        }
                    };
            var input = new TransientCraftingContainer(menu, 3, 3);
            int slot = 0;
            for (String row : patterns[k])
                for (char c : row.toCharArray())
                    input.setItem(
                            slot++,
                            c == 'M'
                                    ? new ItemStack(BreathgoldContent.MATERIAL.get())
                                    : c == 'S' ? new ItemStack(Items.STICK) : ItemStack.EMPTY);
            check(craft.matches(input, level), "recipe matches vanilla layout: " + kind);
            check(
                    craft.getResultItem(level.registryAccess())
                            .is(
                                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                            new ResourceLocation(
                                                    "purified_undead:breathgold_" + kind))),
                    "recipe output: " + kind);
        }

        // Cancellation and breaking must not leave a new conversion candidate.
        level.setBlockAndUpdate(pos, Blocks.GOLD_ORE.defaultBlockState());
        data.removed(pos);
        var canceled =
                new net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent(
                        net.minecraftforge.common.util.BlockSnapshot.create(
                                level.dimension(), level, pos),
                        Blocks.STONE.defaultBlockState(),
                        player);
        canceled.setCanceled(true);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(canceled);
        for (int i = 0; i < 210; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "canceled placement not tracked");
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent(
                        net.minecraftforge.common.util.BlockSnapshot.create(
                                level.dimension(), level, pos),
                        Blocks.STONE.defaultBlockState(),
                        player));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.level.BlockEvent.BreakEvent(
                        level, pos, level.getBlockState(pos), player));
        for (int i = 0; i < 210; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "break removes placement record");
        // Removed/replaced ore cannot transfer exposure to an unrelated later block.
        data.placed(pos);
        for (int i = 0; i < 190; i++) data.tick(level);
        level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        data.tick(level);
        level.setBlockAndUpdate(pos, Blocks.GOLD_ORE.defaultBlockState());
        for (int i = 0; i < 210; i++) data.tick(level);
        check(level.getBlockState(pos).is(Blocks.GOLD_ORE), "replacement clears tracking");
        var distant = new BlockPos(1000000, 100, 1000000);
        check(!level.hasChunkAt(distant), "distant chunk absent before tracking");
        data.placed(distant);
        data.tick(level);
        check(!level.hasChunkAt(distant), "tracking does not force-load chunk");
        data.removed(distant);
        data.removed(pos);
        model.reset();
        level.removeBlock(pos, false);
        level.removeBlock(natural, false);
        LogUtils.getLogger()
                .info(
                        "BREATHGOLD_SERVER_OK checks={} conversion=200 fatigue=true fortune=true"
                            + " silk=true recipes=5 durability=375",
                        checks);
    }
}
