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

    public static ItemStack socket(ServerPlayer player, ItemStack base, String scheme, String slot, boolean melee) {
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
        check(GuardianWaveCombat.canEmit(made) == melee, "crafted eligibility " + scheme + " " + made.getTags().toList());
        check(GuardianWaveCombat.canEmit(preview) == melee, "preview eligibility " + scheme + " " + preview.getTags().toList());
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

        var single = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tetra", "modular_single"));
        var shovel = new ItemStack(single);
        IModularItem.putModuleInSlot(shovel, "single/head", "single/basic_shovel", "single/basic_shovel_material", "basic_shovel/iron");
        IModularItem.putModuleInSlot(shovel, "single/handle", "single/basic_handle", "single/basic_handle_material", "basic_handle/stick");
        IModularItem.updateIdentifier(shovel);
        socket(player, shovel, "single/socket", "single/binding", false);

        var spear = shovel.copy();
        IModularItem.putModuleInSlot(spear, "single/head", "single/spearhead", "single/spearhead_material", "spearhead/iron");
        IModularItem.updateIdentifier(spear);
        socket(player, spear, "single/socket", "single/binding", true);
        if (moreMaterials) {
            var rapier = sword.copy();
            var module = "sword/more_mod_tetra/mmt_rapier";
            IModularItem.putModuleInSlot(rapier, "sword/blade", module, module + "_material", "mmt_rapier/iron");
            IModularItem.updateIdentifier(rapier);
            var head = ((IModularItem) rapier.getItem()).getModuleFromSlot(rapier, "sword/blade");
            check(head != null && head.getUnlocalizedName().equals(module), "missing More Mod Tetra rapier");
            armed = socket(player, rapier, "sword/socket", "sword/guard", true);
        }
        System.out.println("TETRA_GUARDIAN_SOCKET_OK: actual schematic, 1 integrity, 512 durability, 2 attack, spear, shovel, broken, replacement, MMT=" + moreMaterials);
        return armed;
    }

    private TetraGuardianSmoke() {}
}
