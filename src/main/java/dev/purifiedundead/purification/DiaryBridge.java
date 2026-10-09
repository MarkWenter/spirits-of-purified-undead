package dev.purifiedundead.purification;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Lazy, cached Patchouli bridge; no dependency implementation is bundled. */
public final class DiaryBridge {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("purified_undead", "white_witch_diary");
    private static volatile boolean failed;

    private DiaryBridge() {}

    private record Access(Object api, Method create, Method resolve, Field id) {}

    private static final class LazyAccess {
        private static final Access VALUE = resolve();

        private static Access resolve() {
            if (!net.minecraftforge.fml.ModList.get().isLoaded("patchouli")) return null;
            try {
                Object api =
                        Class.forName("vazkii.patchouli.api.PatchouliAPI")
                                .getMethod("get")
                                .invoke(null);
                Method create =
                        Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI")
                                .getMethod("getBookStack", ResourceLocation.class);
                Method resolve =
                        Class.forName("vazkii.patchouli.common.item.ItemModBook")
                                .getMethod("getBook", ItemStack.class);
                return new Access(api, create, resolve, resolve.getReturnType().getField("id"));
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
                fail(ex);
                return null;
            }
        }
    }

    private static synchronized void fail(Throwable ex) {
        if (failed) return;
        failed = true;
        com.mojang.logging.LogUtils.getLogger()
                .error(
                        "Patchouli book bridge unavailable; diary lookup disabled for this session",
                        ex);
    }

    public static ItemStack create() {
        var access = LazyAccess.VALUE;
        if (access == null || failed) return ItemStack.EMPTY;
        try {
            return ((ItemStack) access.create.invoke(access.api, ID)).copy();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
            fail(ex);
            return ItemStack.EMPTY;
        }
    }

    public static boolean isDiary(ItemStack stack) {
        if (stack.isEmpty() || failed) return false;
        var access = LazyAccess.VALUE;
        if (access == null) return false;
        try {
            Object book = access.resolve.invoke(null, stack);
            return book != null && ID.equals(access.id.get(book));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
            fail(ex);
            return false;
        }
    }
}
