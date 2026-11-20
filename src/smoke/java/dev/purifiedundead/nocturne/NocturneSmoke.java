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
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(server.overworld(),player,InteractionHand.MAIN_HAND);
            maid.setOwnerUUID(player.getUUID());
            var recipe = server.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("purified_undead","altar/knight_nocturne"));
            if (recipe.isEmpty()) throw new AssertionError("missing altar recipe");
            net.minecraft.world.inventory.Slot slot = new com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.BaubleContainer.BaubleSlot(maid,0,0,0);
            slot.setByPlayer(stack);
            if (!(NocturneEffect.damage(player,10) > 10)) throw new AssertionError("missing snapshot");
            int before = maid.getFavorability();
            for (int i=0;i<20;i++) NocturneEffect.kill(player);
            bauble.onTick(maid,stack);
            if (maid.getFavorability()!=before+1) throw new AssertionError("favor");
            bauble.onDeath(maid,stack,maid.damageSources().generic());
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
            System.out.println("NOCTURNE_MAID_OK: registration, binding, snapshot, favor, removal, revocation");
        } finally {
            NocturneItem.unbind(player);
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            maid.discard();
        }
    }
}
