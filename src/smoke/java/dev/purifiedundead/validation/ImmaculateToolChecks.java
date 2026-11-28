package dev.purifiedundead.validation;

import dev.purifiedundead.api.PurifiedUndeadScripts;
import dev.purifiedundead.blightedarmaments.ImmaculateTools;
import dev.purifiedundead.content.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

final class ImmaculateToolChecks {
    private static void check(boolean result, String label) {
        if (!result) throw new IllegalStateException("IMMACULATE_TOOLS_FAILED: " + label);
    }

    static void verify(ServerLevel level, ServerPlayer player) throws Exception {
        var sword = new ItemStack(ImmaculateTools.SWORD.get());
        var target = EntityType.COW.create(level);
        var control = EntityType.COW.create(level);
        var previousHand = player.getMainHandItem();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        target.invulnerableTime = 0;
        target.hurt(level.damageSources().playerAttack(player), 0.5F);
        check(target.hasEffect(MobEffects.POISON), "harmful effect retained");
        var benefits =
                java.util.List.of(
                        MobEffects.MOVEMENT_SPEED,
                        MobEffects.JUMP,
                        MobEffects.DAMAGE_BOOST,
                        MobEffects.DAMAGE_RESISTANCE,
                        MobEffects.REGENERATION,
                        MobEffects.NIGHT_VISION,
                        MobEffects.WATER_BREATHING,
                        MobEffects.FIRE_RESISTANCE);
        for (var effect : benefits) target.addEffect(new MobEffectInstance(effect, 200));
        for (int i = 0; i < benefits.size(); i++) {
            int size = target.getActiveEffects().size();
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), 0.5F);
            check(target.getActiveEffects().size() == size - 1, "exactly one seal per hit");
        }
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, previousHand);
        var saved = new net.minecraft.nbt.CompoundTag();
        target.saveWithoutId(saved);
        var reloaded = EntityType.COW.create(level);
        reloaded.load(saved);
        for (int n = 0; n < 1024; n++) {
            var effect = benefits.get(n % benefits.size());
            check(!reloaded.addEffect(new MobEffectInstance(effect, 200)), "reapplication");
        }
        check(
                PurifiedUndeadScripts.immaculateEffectSealed(reloaded, "minecraft:speed"),
                "persistent script query");
        check(
                control.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200)),
                "entity isolation");
        if (Boolean.getBoolean("purified_undead.testL2Force")) {
            var force =
                    Class.forName("dev.xkmc.l2library.base.effects.EffectUtil")
                            .getDeclaredMethod(
                                    "forceAddEffect",
                                    LivingEntity.class,
                                    MobEffectInstance.class,
                                    Entity.class);
            force.setAccessible(true);
            for (int n = 0; n < 128; n++)
                force.invoke(
                        null,
                        reloaded,
                        new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200),
                        player);
            check(!reloaded.hasEffect(MobEffects.MOVEMENT_SPEED), "L2 force veto");
            control.removeEffect(MobEffects.MOVEMENT_SPEED);
            force.invoke(
                    null, control, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200), player);
            check(control.hasEffect(MobEffects.MOVEMENT_SPEED), "unsealed L2 force");
            com.mojang.logging.LogUtils.getLogger().info("IMMACULATE_L2_FORCE_OK retries=128");
        }

        if (Boolean.getBoolean("purified_undead.testSealBurst")) {
            var targets = new LivingEntity[32];
            for (int i = 0; i < targets.length; i++) {
                targets[i] = EntityType.COW.create(level);
                targets[i].load(saved);
            }
            int accepted = 0;
            long start = System.nanoTime();
            for (int n = 0; n < 32768; n++) {
                var instance = new MobEffectInstance(benefits.get((n / targets.length) % benefits.size()), 200);
                if (targets[n % targets.length].addEffect(instance)) accepted++;
            }
            long elapsed = System.nanoTime() - start;
            check(accepted == 0, "batch reapplication");
            com.mojang.logging.LogUtils.getLogger().info(
                    "IMMACULATE_SEAL_BURST_OK targets=32 retries=32768 elapsedMicros={}", elapsed / 1000);
        }
        if (Boolean.getBoolean("purified_undead.testL2Trait")) {
            var type = Class.forName("dev.xkmc.l2hostility.content.traits.base.SelfEffectTrait");
            var tick = type.getMethod("tick", LivingEntity.class, int.class);
            var trait =
                    type.getConstructor(java.util.function.Supplier.class)
                            .newInstance(
                                    (java.util.function.Supplier<MobEffect>)
                                            () -> MobEffects.MOVEMENT_SPEED);
            control.removeEffect(MobEffects.MOVEMENT_SPEED);
            for (int n = 0; n < 128; n++) {
                tick.invoke(trait, control, 1);
                tick.invoke(trait, reloaded, 1);
            }
            check(
                    control.hasEffect(MobEffects.MOVEMENT_SPEED)
                            && !reloaded.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "L2 Hostility self-effect trait");
            com.mojang.logging.LogUtils.getLogger().info("IMMACULATE_L2_TRAIT_OK retries=128");
        }

        for (String kind : new String[] {"sword", "shovel", "axe", "hoe", "pickaxe"}) {
            var id = ResourceLocation.parse("purified_undead:immaculate_" + kind);
            var item = BuiltInRegistries.ITEM.get(id);
            var base =
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.parse("purified_undead:breathgold_" + kind));
            var recipe = (SmithingRecipe) level.getRecipeManager().byKey(id).orElseThrow();
            var input =
                    new net.minecraft.world.SimpleContainer(
                            new ItemStack(
                                    ModItems.PURIFIED_ARCSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                            new ItemStack(base),
                            new ItemStack(ModItems.PURIFIED_ARCSTEEL.get()));
            check(
                    recipe.matches(input, level)
                            && recipe.assemble(input, level.registryAccess()).is(item),
                    "smith " + kind);
            check(
                    new ItemStack(item).getMaxDamage()
                            == Integer.getInteger("purified_undead.testImmaculateDurability", 2031),
                    "durability " + kind);
        }
        var axe = new ItemStack(ImmaculateTools.AXE.get());
        axe.setDamageValue(2);
        axe.getItem()
                .mineBlock(axe, level, Blocks.OAK_LOG.defaultBlockState(), BlockPos.ZERO, player);
        check(axe.getDamageValue() == 1, "log repaired");
        axe.getItem()
                .mineBlock(
                        axe, level, Blocks.OAK_PLANKS.defaultBlockState(), BlockPos.ZERO, player);
        check(axe.getDamageValue() == 2, "planks use durability");
        var pick = new ItemStack(ImmaculateTools.PICKAXE.get());
        check(pick.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "diamond tier");
        var hoe = new ItemStack(ImmaculateTools.HOE.get());
        var shovel = new ItemStack(ImmaculateTools.SHOVEL.get());
        var wheat = (CropBlock) Blocks.WHEAT;
        pick.enchant(Enchantments.BLOCK_FORTUNE, 3);
        hoe.enchant(Enchantments.BLOCK_FORTUNE, 3);
        int fragmentRolls = 0, maxDiamonds = 0, maxSeeds = 0;
        for (int i = 0; i < 300; i++) {
            int fragments =
                    count(
                            level,
                            player,
                            shovel,
                            Blocks.DIRT.defaultBlockState(),
                            ModItems.BLIGHT_FRAGMENT.get());
            check(fragments >= 0 && fragments <= 4, "soil bounds");
            if (fragments > 0) fragmentRolls++;
            maxDiamonds =
                    Math.max(
                            maxDiamonds,
                            count(
                                    level,
                                    player,
                                    pick,
                                    Blocks.DIAMOND_ORE.defaultBlockState(),
                                    Items.DIAMOND));
            maxSeeds =
                    Math.max(
                            maxSeeds,
                            count(
                                    level,
                                    player,
                                    hoe,
                                    wheat.getStateForAge(wheat.getMaxAge()),
                                    Items.WHEAT_SEEDS));
            check(
                    count(level, player, hoe, wheat.getStateForAge(0), Items.WHEAT_SEEDS) == 1,
                    "immature crop");
        }
        check(fragmentRolls > 0 && fragmentRolls < 300, "soil probability");
        check(maxDiamonds > 4 && maxDiamonds <= 8, "independent fortune ore rolls");
        check(maxSeeds > 7, "fortune applies to mature crop rolls");
        var silk = new ItemStack(ImmaculateTools.PICKAXE.get());
        silk.enchant(Enchantments.SILK_TOUCH, 1);
        for (int n = 0; n < 40; n++)
            check(
                    count(
                                    level,
                                    player,
                                    silk,
                                    Blocks.DIAMOND_ORE.defaultBlockState(),
                                    Items.DIAMOND_ORE)
                            == 1,
                    "silk ore");
        com.mojang.logging.LogUtils.getLogger()
                .info(
                        "IMMACULATE_TOOLS_OK recipes=5 sealed=8 retries=1024 fragments={}"
                                + " maxDiamonds={} maxSeeds={}",
                        fragmentRolls,
                        maxDiamonds,
                        maxSeeds);
    }

    private static int count(
            ServerLevel level, ServerPlayer player, ItemStack tool, BlockState state, Item item) {
        return Block.getDrops(state, level, BlockPos.ZERO, null, player, tool).stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
