package dev.purifiedundead.validation;

import dev.purifiedundead.slate.*;
import dev.purifiedundead.foundry.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.*;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;

/** Bounded stress and malformed-input checks, development servers only. */
public final class CompatibilitySmoke {
    public static int chainCalls;

    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("COMPATIBILITY_FAILED: " + why);
    }

    public static void run(net.minecraft.server.MinecraftServer server) {
        try {
            runChecked(server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("COMPATIBILITY_FAILED", e);
        }
    }

    private static void runChecked(net.minecraft.server.MinecraftServer server)
            throws ReflectiveOperationException {
        var book = new ItemStack(ModItems.LILY_DIARY.get());
        var memories = NonNullList.withSize(8, ItemStack.EMPTY);
        for (int i = 0; i < 8; i++)
            memories.set(
                    i, new ItemStack(SlateContent.MEMORIES.get(SlateContent.WARRIORS[i]).get()));
        MemoryStorage.write(book, memories);
        check(MemoryStorage.mask(book) == 255, "all eight identities");
        var malformed = book.copy();
        var entries =
                malformed
                        .getOrCreateTag()
                        .getCompound("SlateMemories")
                        .getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        var duplicate = entries.getCompound(0).copy();
        duplicate.putString("id", "minecraft:stone");
        entries.add(duplicate);
        check(
                MemoryStorage.mask(malformed) == 254,
                "duplicate invalid identity overrides old slot");
        duplicate.putString("id", "purified_undead:blighted_memory_groth");
        duplicate.putByte("Count", (byte) 0);
        check(MemoryStorage.mask(malformed) == 254, "zero-count duplicate clears old identity");
        duplicate.putByte("Slot", (byte) 255);
        check(MemoryStorage.mask(malformed) == 255, "invalid slot ignored");
        for (int i = entries.size(); i < 257; i++) entries.add(duplicate.copy());
        check(MemoryStorage.mask(malformed) == 0, "oversized external list cannot monopolize tick");
        var bean =
                (com.sun.management.ThreadMXBean)
                        java.lang.management.ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        for (int i = 0; i < 2000; i++) MemoryStorage.mask(book);
        long before = bean.getThreadAllocatedBytes(thread);
        int value = 0;
        for (int i = 0; i < 2000; i++) value |= MemoryStorage.mask(book);
        long fast = bean.getThreadAllocatedBytes(thread) - before;
        before = bean.getThreadAllocatedBytes(thread);
        for (int i = 0; i < 2000; i++)
            for (var item : MemoryStorage.read(book))
                value |= MemoryStorage.bit(SlateContent.memoryKey(item));
        long legacy = bean.getThreadAllocatedBytes(thread) - before;
        check(
                value == 255 && fast < legacy,
                "effect lookup allocates less than full deserialization: "
                        + fast
                        + " vs "
                        + legacy);
        memories.set(0, ItemStack.EMPTY);
        MemoryStorage.write(book, memories);
        check(MemoryStorage.mask(book) == 254, "same-tick content mutation visible");
        var player =
                new net.minecraftforge.common.util.FakePlayer(
                        server.overworld(),
                        new com.mojang.authlib.GameProfile(UUID.randomUUID(), "CompatSmoke"));
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, book);
        var menu = new LilyMemoryMenu(1, player.getInventory());
        player.getInventory().setItem(0, new ItemStack(Items.STONE));
        check(menu.quickMoveStack(player, 1).isEmpty(), "stale book menu cannot extract memories");
        check(MemoryStorage.mask(book) == 254, "detached book unchanged");
        var bytes = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            bytes.writeVarInt(1);
            bytes.writeUtf("minecraft:coal");
            bytes.writeVarInt(0);
            bytes.writeUtf("minecraft:coal");
            bytes.writeVarInt(1);
            bytes.writeUtf("minecraft:diamond");
            bytes.writeVarInt(2);
            bytes.writeBoolean(true);
            bytes.writeVarInt(1);
            bytes.writeVarInt(1);
            bytes.writeVarInt(400);
            boolean rejected = false;
            try {
                FoundryRecipes.read(bytes);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
            check(rejected, "zero-count network recipe rejected before processing");
        } finally {
            bytes.release();
        }
        var field = FoundryRecipes.class.getDeclaredField("recipes");
        field.setAccessible(true);
        var original = FoundryRecipes.all();
        try {
            var large = new ArrayList<FoundryRecipes.Recipe>();
            for (int i = 0; i < 4095; i++)
                large.add(
                        new FoundryRecipes.Recipe(
                                "minecraft:stone",
                                64,
                                "minecraft:dirt",
                                64,
                                "minecraft:diamond",
                                1,
                                true,
                                1,
                                1,
                                400));
            var coal = FoundryRecipes.defaults().get(0);
            large.add(coal);
            FoundryRecipes.validateSync(large);
            field.set(null, List.copyOf(large));
            check(
                    FoundryRecipes.find(
                                    new ItemStack(Items.COAL, 64), new ItemStack(Items.COAL, 64))
                            .equals(coal),
                    "4096 recipe index preserves matching");
            check(
                    !FoundryRecipes.top(ItemStack.EMPTY)
                            && !FoundryRecipes.bottom(new ItemStack(Items.APPLE)),
                    "indexed input exclusions");
            var idle =
                    new PurificationFoundryEntity(
                            new BlockPos(48, 120, 48),
                            FoundryContent.BLOCK.get().defaultBlockState());
            idle.setLevel(server.overworld());
            before = System.nanoTime();
            for (int i = 0; i < 100000; i++) idle.process();
            long elapsed = System.nanoTime() - before;
            check(idle.isEmpty(), "100000 idle ticks preserve inventory");
            System.out.println(
                    "COMPATIBILITY_PERF: mask bytes="
                            + fast
                            + " legacy bytes="
                            + legacy
                            + " idle 100000 ticks ns="
                            + elapsed);
        } finally {
            field.set(null, original);
            FoundryRecipes.find(new ItemStack(Items.COAL), new ItemStack(Items.COAL));
            player.discard();
        }
        var diary = dev.purifiedundead.purification.DiaryBridge.create();
        check(
                !diary.isEmpty() && dev.purifiedundead.purification.DiaryBridge.isDiary(diary),
                "cached Patchouli create/resolve");
        check(
                !dev.purifiedundead.purification.DiaryBridge.isDiary(new ItemStack(Items.BOOK)),
                "vanilla book is not diary");
        for (int i = 0; i < 1000; i++)
            check(
                    dev.purifiedundead.purification.DiaryBridge.isDiary(diary),
                    "repeated cached diary lookup");
        var actor =
                new net.minecraftforge.common.util.FakePlayer(
                        server.overworld(),
                        new com.mojang.authlib.GameProfile(UUID.randomUUID(), "ScopeSmoke"));
        var equipped = new ItemStack(ModItems.LILY_DIARY.get());
        var memoryList = NonNullList.withSize(8, ItemStack.EMPTY);
        memoryList.set(0, new ItemStack(SlateContent.MEMORIES.get("hoenir").get()));
        MemoryStorage.write(equipped, memoryList);
        dev.purifiedundead.purification.PurificationProgress.unlock(actor);
        var diarySlots =
                top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(actor)
                        .orElseThrow(IllegalStateException::new)
                        .getCurios()
                        .get("wanderer_log")
                        .getStacks();
        diarySlots.setStackInSlot(0, equipped);
        var source = actor.damageSources().playerAttack(actor);
        var nested = actor.damageSources().magic();
        int[] calls = {0};
        try {
            MemoryDamageCriteria.evaluate(
                    source,
                    () -> {
                        calls[0]++;
                        check(MemoryDamageCriteria.active(source), "criteria scope active");
                        MemoryDamageCriteria.evaluate(
                                nested,
                                () -> {
                                    check(
                                            MemoryDamageCriteria.active(source),
                                            "unrelated source preserves outer scope");
                                    return true;
                                });
                        throw new IllegalArgumentException("expected scope failure");
                    });
            throw new IllegalStateException("expected exception was swallowed");
        } catch (IllegalArgumentException expected) {
            check(
                    expected.getMessage().equals("expected scope failure"),
                    "exact exception preserved");
        }
        check(
                calls[0] == 1 && !MemoryDamageCriteria.active(source),
                "wrapped call runs once, exception restores scope");
        actor.discard();
        System.out.println(
                "AUDIT_BOUNDARIES_OK: cached book lookup, wrapped criterion once, exceptional scope cleanup");
        System.out.println(
                "COMPATIBILITY_OK: low-allocation identities, immediate mutation, stale menu, malformed packet, 4096 recipes and 100000 idle ticks");
    }
}
