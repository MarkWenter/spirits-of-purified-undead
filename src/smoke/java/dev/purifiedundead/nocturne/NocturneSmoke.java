package dev.purifiedundead.nocturne;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import dev.purifiedundead.content.ModItems;

public final class NocturneSmoke {
    public static void run(MinecraftServer server, ServerPlayer player) {
        var stack = new ItemStack(ModItems.KNIGHT_NOCTURNE.get());
        var hand = player.getMainHandItem();
        var maid = new EntityMaid(server.overworld());
        try {
            var bauble = BaubleManager.getBauble(stack);
            if (!(bauble instanceof NocturneExtension)) throw new AssertionError("missing maid bauble");
            // Migrated or externally edited charm data must never initialize world-owned state.
            var incidental = new net.minecraft.nbt.CompoundTag();
            incidental.putInt("favor", 1);
            incidental.putBoolean("dead", true);
            stack.getOrCreateTag().put(NocturneItem.KEY, incidental);
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(server.overworld(),player,InteractionHand.MAIN_HAND);
            var initial = NocturneEffect.record(player);
            if (initial.size() != 3 || !initial.hasUUID("owner") || !initial.hasUUID("id") || !initial.hasUUID("epoch"))
                throw new AssertionError("unexpected ledger fields before maid equip");
            System.out.println("NOCTURNE_BINDING_BOUNDARY_OK");
            maid.setOwnerUUID(player.getUUID());
            var recipe = server.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("purified_undead","altar/knight_nocturne"));
            if (recipe.isEmpty()) throw new AssertionError("missing altar recipe");
            net.minecraft.world.inventory.Slot slot = new com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.BaubleContainer.BaubleSlot(maid,0,0,0);
            maid.setModelId("geckolib:winefox_blue");
            maid.setBackpackShowItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND));
            maid.getConfigManager().setShowBackpack(false);
            maid.setYsmModel("wine_fox/01_taisho_maid", "skin_white", net.minecraft.network.chat.Component.empty());
            maid.setIsYsmModel(true);
            var helmet = new ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET);
            helmet.getOrCreateTag().putInt("CustomModelData", 81);
            helmet.getOrCreateTag().putString("discardMe", "private");
            maid.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
            slot.setByPlayer(stack);
            var appearance = NocturneEffect.appearance(player);
            if (!"geckolib:winefox_blue".equals(appearance.getString("model"))
                    || !"minecraft:diamond".equals(appearance.getString("backpackItem"))
                    || appearance.getBoolean("showBackpack") || appearance.contains("base"))
                throw new AssertionError("incorrect appearance projection " + appearance);
            if (!appearance.getString("geckoEcho").equals("geckolib:winefox_blue")
                    || !appearance.getString("ysmTexture").equals("skin_white")
                    || appearance.getCompound("headStack").getInt("model") != 81
                    || appearance.getCompound("headStack").contains("discardMe")
                    || !appearance.getCompound("displayStack").getString("id").equals("minecraft:diamond"))
                throw new AssertionError("mapped skin or display equipment missing: " + appearance);
            var restored = EchoItemAppearance.restore(appearance.getCompound("headStack"));
            if (restored.getCount() != 1 || !restored.is(helmet.getItem()) || restored.getOrCreateTag().getInt("CustomModelData") != 81)
                throw new AssertionError("head render stack round trip");
            var banner = new ItemStack(net.minecraft.world.item.Items.WHITE_BANNER, 12);
            var patterns = new net.minecraft.nbt.ListTag();
            var layer = new net.minecraft.nbt.CompoundTag();
            layer.putString("Pattern", "cr");
            layer.putInt("Color", net.minecraft.world.item.DyeColor.RED.getId());
            patterns.add(layer);
            banner.getOrCreateTagElement("BlockEntityTag").put("Patterns", patterns);
            var bannerView = EchoItemAppearance.capture(banner);
            var renderedBanner = EchoItemAppearance.restore(bannerView);
            if (renderedBanner.getCount() != 1 || !EchoItemAppearance.capture(renderedBanner).equals(bannerView)
                    || bannerView.getList("patterns", 10).size() != 1)
                throw new AssertionError("banner appearance round trip: " + bannerView);
            var copy = appearance.copy();
            copy.remove("geckoEcho");
            copy.putString("ysmTexture", "custom");
            WineFoxSkins.capture(copy, appearance);
            if (!copy.getString("geckoEcho").isEmpty()) throw new AssertionError("unknown color borrowed old skin");
            copy.putString("ysmTexture", "skin");
            var vars = new net.minecraft.nbt.CompoundTag();
            vars.putFloat("roaming.a", 1);
            copy.put("variables", vars);
            WineFoxSkins.capture(copy, appearance);
            if (!copy.getString("geckoEcho").isEmpty()) throw new AssertionError("lost custom body form");
            System.out.println("NOCTURNE_YSM_SNAPSHOT_OK: variant, head, display item and native fallback");
            appearance.remove("model");
            if (!NocturneEffect.appearance(player).contains("model")) throw new AssertionError("aliased stored snapshot");
            if (!(NocturneEffect.damage(player,10) > 10)) throw new AssertionError("missing snapshot");
            int before = maid.getFavorability();
            for (int i=0;i<20;i++) NocturneEffect.kill(player);
            bauble.onTick(maid,stack);
            if (maid.getFavorability()!=before+1) throw new AssertionError("favor");
            bauble.onDeath(maid,stack,maid.damageSources().generic());
            if (!NocturneEffect.appearance(player).isEmpty()) throw new AssertionError("echo after death");
            if (!NocturneEffect.sealed(player)) throw new AssertionError("missing death lock");
            bauble.onTick(maid,stack);
            if (NocturneEffect.sealed(player)) throw new AssertionError("missing recovery");
            slot.onTake(player,stack);
            slot.set(ItemStack.EMPTY);
            if (NocturneEffect.damage(player,10)!=10) throw new AssertionError("stale snapshot");
            bauble.onPutOn(maid,stack);
            NocturneItem.unbind(player);
            bauble.onTick(maid,stack);
            if (NocturneEffect.damage(player,10)!=10) throw new AssertionError("stale epoch");
            if (!NocturneEffect.appearance(player).isEmpty()) throw new AssertionError("echo after revocation");
            System.out.println("NOCTURNE_ECHO_SNAPSHOT_OK: appearance projection, independent copy, death and revocation");
            System.out.println("NOCTURNE_MAID_OK: registration, binding, snapshot, favor, removal, revocation");
        } finally {
            NocturneItem.unbind(player);
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            maid.discard();
        }
    }
}
