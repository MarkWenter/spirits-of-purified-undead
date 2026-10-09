package dev.purifiedundead.validation;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.purification.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import top.theillusivec4.curios.api.CuriosApi;

public final class PurificationPartTwoSmoke {
    private static void check(boolean pass, String message) {
        if (!pass) throw new IllegalStateException("PART2_FAILED: " + message);
    }

    public static void run(net.minecraft.server.MinecraftServer server) {
        var level = server.overworld();
        var p =
                new net.minecraftforge.common.util.FakePlayer(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "PartTwoSmoke"));
        var h =
                CuriosApi.getCuriosInventory(p)
                        .orElseThrow(() -> new IllegalStateException("Curios"));
        var contract = h.getCurios().get("ancient_contract").getStacks();
        p.setHealth(2);
        var flower = new ItemStack(ModItems.WHITE_LOTUS.get(), 2);
        flower.getItem().finishUsingItem(flower, level, p);
        check(p.getHealth() == 12 && flower.getCount() == 1, "lotus heals 50% max");
        var red = new ItemStack(ModItems.SCARLET_LOTUS.get(), 2);
        red.getItem().finishUsingItem(red, level, p);
        check(
                p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() == 3
                        && p.getEffect(MobEffects.DAMAGE_BOOST).getDuration() == 1000
                        && red.getCount() == 1,
                "scarlet strength IV 50s");
        var fruit = new ItemStack(ModItems.PURE_FORBIDDEN_FRUIT.get(), 3);
        fruit.getItem().finishUsingItem(fruit, level, p);
        check(!PurificationProgress.used(p) && fruit.getCount() == 3, "contract required");
        contract.setStackInSlot(0, new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
        fruit.getItem().finishUsingItem(fruit, level, p);
        check(PurificationProgress.used(p) && fruit.getCount() == 2, "fruit consumed once");
        int modifiers = h.getModifiers().size();
        fruit.getItem().finishUsingItem(fruit, level, p);
        check(fruit.getCount() == 2 && modifiers == h.getModifiers().size(), "no repeat reward");
        contract.setStackInSlot(0, ItemStack.EMPTY);
        PurificationProgress.ensure(p);
        check(
                h.getCurios().get("white_witch_relic").getModifiers().size() > 0
                        && h.getCurios().get("wanderer_log").getModifiers().size() > 0,
                "independent permanent slots");
        var clone =
                new net.minecraftforge.common.util.FakePlayer(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "PartTwoClone"));
        PurificationProgress.clone(
                new net.minecraftforge.event.entity.player.PlayerEvent.Clone(clone, p, true));
        PurificationProgress.ensure(clone);
        check(PurificationProgress.used(clone), "death clone flag");
        Item[] armors = {
            ModItems.IMMACULATE_HELMET.get(),
            ModItems.IMMACULATE_CHESTPLATE.get(),
            ModItems.IMMACULATE_LEGGINGS.get(),
            ModItems.IMMACULATE_BOOTS.get()
        };
        String[] types = {"helmet", "chestplate", "leggings", "boots"};
        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        int[] durability = {667, 898, 847, 738};
        int[] defense = {3, 8, 6, 3};
        for (int i = 0; i < 4; i++) {
            var item = new ItemStack(armors[i]);
            check(item.getMaxDamage() == durability[i], "armor durability " + i);
            check(((ArmorItem) armors[i]).getDefense() == defense[i], "armor defense");
            p.setItemSlot(slots[i], item);
            var base =
                    new ItemStack(
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                            "minecraft", "netherite_" + types[i])));
            base.setDamageValue(10);
            base.setHoverName(Component.literal("Preserved"));
            var recipe =
                    (net.minecraft.world.item.crafting.SmithingRecipe)
                            server.getRecipeManager()
                                    .byKey(
                                            net.minecraft.resources.ResourceLocation
                                                    .fromNamespaceAndPath(
                                                            "purified_undead",
                                                            "immaculate_" + types[i]))
                                    .orElseThrow();
            var input =
                    new net.minecraft.world.SimpleContainer(
                            new ItemStack(
                                    ModItems.PURIFIED_ARCSTEEL_UPGRADE_SMITHING_TEMPLATE.get()),
                            base,
                            new ItemStack(ModItems.PURIFIED_ARCSTEEL.get()));
            check(recipe.matches(input, level), "smithing matches");
            var result = recipe.assemble(input, server.registryAccess());
            check(
                    result.is(armors[i])
                            && result.getDamageValue() == 10
                            && result.getHoverName().getString().equals("Preserved"),
                    "smith preserves components");
        }
        try {
            var checkArmor =
                    Class.forName("com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler")
                            .getMethod(
                                    "hasAnyArmor", net.minecraft.world.entity.LivingEntity.class);
            check(
                    !(Boolean) checkArmor.invoke(null, p),
                    "golem heart treats immaculate set as no armor");
            p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            check(
                    (Boolean) checkArmor.invoke(null, p),
                    "ordinary armor still blocks no-armor bonus");
            p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.IMMACULATE_HELMET.get()));
            System.out.println(
                    "GOLEM_COMPAT_OK: immaculate excluded, ordinary armor still counted");
        } catch (ClassNotFoundException absent) {
            System.out.println("GOLEM_COMPAT_ABSENT_OK");
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        var sources =
                new net.minecraft.world.damagesource.DamageSource[] {
                    p.damageSources()
                            .arrow(
                                    new net.minecraft.world.entity.projectile.Arrow(
                                            net.minecraft.world.entity.EntityType.ARROW, level),
                                    p),
                    p.damageSources().magic(),
                    p.damageSources().onFire(),
                    p.damageSources().freeze()
                };
        for (int i = 0; i < 4; i++) {
            var event =
                    new net.minecraftforge.event.entity.living.LivingHurtEvent(p, sources[i], 10);
            PurificationEquipmentEvents.damage(event);
            check(
                    Math.abs(event.getAmount() - (i == 1 ? 3 : 6.5)) < .001,
                    "typed armor reduction " + i);
        }
        var sword = new ItemStack(ModItems.BLIGHTED_GUARDIAN.get());
        check(sword.getMaxDamage() == 7989, "sword durability");
        BlightedGuardianItem.ensureMending(sword, level);
        check(
                sword.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.MENDING)
                        == 1,
                "sword mending");
        var attrs = sword.getAttributeModifiers(EquipmentSlot.MAINHAND);
        double damage =
                1
                        + attrs.get(Attributes.ATTACK_DAMAGE).stream()
                                .mapToDouble(a -> a.getAmount())
                                .sum();
        double speed =
                4
                        + attrs.get(Attributes.ATTACK_SPEED).stream()
                                .mapToDouble(a -> a.getAmount())
                                .sum();
        check(
                Math.abs(damage - 23) < .001 && Math.abs(speed - 2.2) < .001,
                "sword 23 damage / 2.2 speed");
        check(
                sword.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP),
                "sword sweep");
        check(
                !sword.canApplyAtEnchantingTable(
                        net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE),
                "curse rejected");
        sword.enchant(net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE, 1);
        BlightedGuardianItem.ensureMending(sword, level);
        check(
                sword.getEnchantmentLevel(
                                net.minecraft.world.item.enchantment.Enchantments.VANISHING_CURSE)
                        == 0,
                "external curse sanitized");
        for (String id :
                new String[] {
                    "white_lotus",
                    "scarlet_lotus",
                    "pure_forbidden_fruit",
                    "pure_touch",
                    "blighted_guardian"
                })
            check(
                    server.getRecipeManager()
                            .byKey(
                                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                            "purified_undead", id))
                            .isPresent(),
                    "craft recipe " + id);
        var recipeSword =
                server.getRecipeManager()
                        .byKey(
                                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                        "purified_undead", "blighted_guardian"))
                        .orElseThrow();
        var crafted = recipeSword.getResultItem(server.registryAccess());
        check(
                crafted.getEnchantmentLevel(
                                net.minecraft.world.item.enchantment.Enchantments.MENDING)
                        == 1,
                "craft output mending");
        System.out.println(
                "PURIFICATION_PART2_OK: lotus effects, one-time fruit, independent slots, clone persistence, armor durability/defense/resistances, four smithing recipes preserve metadata, sword durability/mending, crafting loaded");
    }
}
