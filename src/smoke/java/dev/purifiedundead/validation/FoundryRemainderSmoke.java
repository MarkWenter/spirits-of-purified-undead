package dev.purifiedundead.validation;

import dev.purifiedundead.foundry.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.*;

public final class FoundryRemainderSmoke {
    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("FOUNDRY_REMAINDER_FAILED: " + why);
    }

    private static void fill(
            PurificationFoundryEntity be, Item a, int ac, Item b, int bc, int fuel) {
        be.clearContent();
        be.process();
        be.setItem(0, new ItemStack(a, ac));
        be.setItem(3, new ItemStack(b, bc));
        be.setItem(1, new ItemStack(ModItems.BLIGHTED_SPIRIT.get(), fuel));
        be.setItem(2, new ItemStack(ModItems.PURE_CRYSTAL.get(), fuel));
    }

    public static void run(PurificationFoundryEntity be) {
        fill(be, Items.IRON_INGOT, 7, Items.GOLD_INGOT, 7, 64);
        for (int i = 0; i < 420; i++) be.process();
        check(
                be.getItem(0).getCount() == 7
                        && be.getItem(3).getCount() == 7
                        && be.getItem(1).getCount() == 64
                        && !be.getBlockState().getValue(PurificationFoundryBlock.LIT),
                "7 iron + 7 gold does not start");
        fill(be, Items.IRON_INGOT, 16, Items.GOLD_INGOT, 18, 64);
        for (int i = 0; i < 400; i++) be.process();
        check(
                be.getItem(0).is(Items.GOLD_INGOT)
                        && be.getItem(0).getCount() == 2
                        && be.getItem(3).is(Items.NETHERITE_SCRAP)
                        && be.getItem(3).getCount() == 4
                        && be.getItem(4).isEmpty(),
                "16 iron +18 gold moves 2 gold upward");
        fill(be, Items.IRON_INGOT, 18, Items.GOLD_INGOT, 16, 64);
        for (int i = 0; i < 400; i++) be.process();
        check(
                be.getItem(0).is(Items.IRON_INGOT)
                        && be.getItem(0).getCount() == 2
                        && be.getItem(3).getCount() == 4,
                "lower input exactly consumed");
        fill(be, Items.IRON_INGOT, 16, Items.GOLD_INGOT, 18, 3);
        for (int i = 0; i < 400; i++) be.process();
        check(
                be.getItem(0).getCount() == 16
                        && be.getItem(3).getCount() == 18
                        && be.getItem(1).getCount() == 3,
                "insufficient fuel cannot strand two different remainders");
        fill(be, Items.COAL, 64, Items.COAL, 64, 16);
        for (int i = 0; i < 400; i++) be.process();
        check(
                be.getItem(0).getCount() == 64
                        && be.getItem(3).is(Items.COAL)
                        && be.getItem(1).getCount() == 16,
                "96 leftover coal cannot fit a single slot");
        fill(be, Items.COAL, 64, Items.COAL, 64, 64);
        be.getItem(0).setHoverName(net.minecraft.network.chat.Component.literal("distinct"));
        for (int i = 0; i < 400; i++) be.process();
        check(
                be.getItem(3).is(Items.COAL) && be.getItem(1).getCount() == 64,
                "different item metadata cannot be merged");
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            FoundryRecipes.write(buffer, FoundryRecipes.all());
            check(
                    FoundryRecipes.read(buffer).equals(FoundryRecipes.all()),
                    "recipe sync retains custom counts and template flag");
        } finally {
            buffer.release();
        }
        System.out.println(
                "FOUNDRY_REMAINDER_OK: reject stranded inputs, move lower remainder, fuel/capacity/metadata guards, recipe packet roundtrip");
    }
}
