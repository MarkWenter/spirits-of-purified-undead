package dev.purifiedundead.validation;

import dev.purifiedundead.combat.GuardianWaveCombat;
import dev.purifiedundead.compat.TetraGuardianSocket;
import dev.purifiedundead.content.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.module.SchematicRegistry;

/** Run in a Tetra-equipped test world with an empty main-hand socket and a real workbench schematic. */
public final class TetraGuardianSmoke {
    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("TETRA_GUARDIAN_FAILED: " + message);
    }

    public static ItemStack socket(ServerPlayer player, ItemStack base, String scheme, String slot, boolean expectedActive) {
        var schematic = SchematicRegistry.getSchematic(scheme);
        var material = new ItemStack(ModItems.PURE_TOUCH.get(), 2);
        ItemStack[] inputs = {material};
        check(schematic != null && schematic.isApplicableForSlot(slot, base), "missing socket " + scheme);
        check(schematic.acceptsMaterial(base, slot, 0, material)
                && schematic.isMaterialsValid(base, slot, inputs), "pure touch rejected by " + scheme);
        ItemStack preview = schematic.applyUpgrade(base, inputs, false, slot, player);
        check(material.getCount() == 2, "preview consumed material");
        ItemStack made = schematic.applyUpgrade(base, inputs, true, slot, player);
        var modular = (IModularItem) made.getItem();
        modular.assemble(made, player.level(), 0);
        modular.assemble(preview, player.level(), 0);
        var gem = modular.getModuleFromSlot(made, slot);
        check(material.getCount() == 1 && gem != null, "assembly must consume one gem");
        check(gem.getDurability(made) == 512 && gem.getIntegrityCost(made) == 1, "socket durability or integrity");
        double damage = gem.getAttributeModifiers(made).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(m -> m.getAmount()).sum();
        check(damage == 2 && modular.getEffectLevel(made, TetraGuardianSocket.EFFECT) > 0, "native socket attributes " + damage);
        check(!GuardianWaveCombat.canEmit(base), "unsocketed weapon " + scheme);
        check(GuardianWaveCombat.canEmit(made) == expectedActive, "crafted eligibility " + scheme + " " + made.getTags().toList());
        check(GuardianWaveCombat.canEmit(preview) == expectedActive, "preview eligibility " + scheme + " " + preview.getTags().toList());
        return made;
    }

    public static ItemStack run(ServerPlayer player, boolean moreMaterials) {
        ItemStack sword = ModularBladedItem.createItemStack("basic_blade", "iron", "stick");
        ((IModularItem) sword.getItem()).assemble(sword, player.level(), 0);
        int originalDurability = sword.getMaxDamage();
        int originalIntegrityCost = IModularItem.getIntegrityCost(sword);
        var scheme = SchematicRegistry.getSchematic("sword/socket");
        check(!scheme.isIntegrityViolation(player, sword, new ItemStack[]{new ItemStack(ModItems.PURE_TOUCH.get())},
                "sword/guard"), "workbench rejected socket integrity");
        ItemStack armed = socket(player, sword, "sword/socket", "sword/guard", true);
        check(armed.getMaxDamage() - originalDurability == 512, "durability " + originalDurability + " -> " + armed.getMaxDamage());
        check(IModularItem.getIntegrityCost(armed) - originalIntegrityCost == 1, "integrity on the actual sword");
        var broken = armed.copy();
        broken.setDamageValue(broken.getMaxDamage());
        check(!GuardianWaveCombat.canEmit(broken), "broken weapon");
        var diamond = scheme.applyUpgrade(armed, new ItemStack[]{new ItemStack(Items.DIAMOND)}, false, "sword/guard", player);
        ((IModularItem) diamond.getItem()).assemble(diamond, player.level(), 0);
        check(!GuardianWaveCombat.canEmit(diamond), "old effect after replacing the socket");

        var shovel = shovel();
        socket(player, shovel, "single/socket", "single/binding", true);

        var spear = shovel.copy();
        IModularItem.putModuleInSlot(spear, "single/head", "single/spearhead", "single/spearhead_material", "spearhead/iron");
        IModularItem.updateIdentifier(spear);
        socket(player, spear, "single/socket", "single/binding", true);
        var cutter = sword.copy();
        part(cutter, "sword/blade", "sword/stonecutter", "stonecutter/stonecutter");
        socket(player, cutter, "sword/socket", "sword/guard", true);

        var twoHeads = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tetra", "modular_double")));
        part(twoHeads, "double/handle", "double/basic_handle", "basic_handle/stick");
        part(twoHeads, "double/head_left", "double/basic_pickaxe_left", "basic_pickaxe/iron");
        part(twoHeads, "double/head_right", "double/basic_pickaxe_right", "basic_pickaxe/iron");
        socket(player, twoHeads, "double/socket", "double/binding", true);
        part(twoHeads, "double/head_right", "double/basic_hammer_right", "basic_hammer/iron");
        socket(player, twoHeads, "double/socket", "double/binding", true);
        System.out.println("TETRA121_MODULAR_TOOLS_OK: shovel, stonecutter, double pickaxe, pickaxe and hammer");
        if (moreMaterials) {
            var rapier = sword.copy();
            var module = "sword/more_mod_tetra/mmt_rapier";
            IModularItem.putModuleInSlot(rapier, "sword/blade", module, module + "_material", "mmt_rapier/iron");
            IModularItem.updateIdentifier(rapier);
            var head = ((IModularItem) rapier.getItem()).getModuleFromSlot(rapier, "sword/blade");
            check(head != null && head.getUnlocalizedName().equals(module), "missing More Mod Tetra rapier");
            armed = socket(player, rapier, "sword/socket", "sword/guard", true);
        }
        if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("tetra", "sword_renew"))) {
            var saved = journeySword();
            armed = socket(player, saved, "sword/socket", "sword/guard", true);
            part(saved, "sword/guard", "sword/socket", "sword_socket/purified_undead_pure_touch");
            saved.setDamageValue(1);
            check(GuardianWaveCombat.canEmit(saved), "previously socketed Journey sword without reassembly");
            System.out.println("TETRA121_EXISTING_SOCKET_OK");
        }
        if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("tetra", "single_gai"))) {
            journeyHeads(player);
        }
        System.out.println("TETRA_GUARDIAN_SOCKET_OK: actual schematic, 1 integrity, 512 durability, 2 attack, spear, shovel, broken, replacement, MMT=" + moreMaterials);
        return armed;
    }

    public static ItemStack shovel() {
        var shovel = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tetra", "modular_single")));
        part(shovel, "single/head", "single/basic_shovel", "basic_shovel/iron");
        part(shovel, "single/handle", "single/basic_handle", "basic_handle/stick");
        return shovel;
    }

    /** Reconstruct the registered layout of the Journey of Forging sword without using a player's save. */
    public static ItemStack journeySword() {
        var sword = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tetra", "sword_renew")));
        part(sword, "sword/blade", "sword/bastard_blade", "bastard_blade/diamond");
        part(sword, "sword/hilt", "sword/basic_hilt", "basic_hilt/bone_handle");
        part(sword, "sword/pommel", "sword/decorative_pommel", "decorative_pommel/diamond");
        part(sword, "sword/fuller", "sword/howling", "howling/iron");
        return sword;
    }

    private static void journeyHeads(ServerPlayer player) {
        var single = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tetra", "single_gai")));
        part(single, "single/handle", "single/basic_handle", "basic_handle/iron");
        part(single, "single/head", "single/basic_shovel", "basic_shovel/iron");
        socket(player, single, "single/socket_1", "single/binding_1", true);
        part(single, "single/head", "single/halberd_head", "halberd_head/iron");
        socket(player, single, "single/socket_2", "single/binding_2", true);
        System.out.println("TETRA121_JOURNEY_HEADS_OK: shovel active in first socket, halberd active in second socket");
    }

    private static void part(ItemStack item, String slot, String module, String variant) {
        IModularItem.putModuleInSlot(item, slot, module, module + "_material", variant);
        IModularItem.updateIdentifier(item);
    }

    private TetraGuardianSmoke() {}
}
