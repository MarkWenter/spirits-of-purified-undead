package dev.purifiedundead.client;

import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.entity.ContractWispEntity;
import java.lang.reflect.Proxy;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** Optional classic dynamic-light API adapters; reflection is resolved once, never during rendering. */
public final class WispDynamicLights {
    private static final Logger LOG = LogUtils.getLogger();
    private static String backend = "none";

    public static String backend() {
        return backend;
    }

    public static void install() {
        for (String namespace :
                new String[] {
                    "org.thinkingstudio.ryoamiclights.api", "dev.lambdaurora.lambdynlights.api"
                }) {
            try {
                Class<?> handler = Class.forName(namespace + ".DynamicLightHandler");
                Class<?> registry = Class.forName(namespace + ".DynamicLightHandlers");
                Object adapter =
                        Proxy.newProxyInstance(
                                handler.getClassLoader(),
                                new Class<?>[] {handler},
                                (proxy, method, args) -> {
                                    try {
                                        return switch (method.getName()) {
                                            case "getLuminance" ->
                                                    args != null
                                                                    && args.length > 0
                                                                    && args[0]
                                                                            instanceof
                                                                            ContractWispEntity wisp
                                                            ? wisp.terrainLight()
                                                            : 0;
                                            case "isWaterSensitive" -> false;
                                            case "toString" -> "PurifiedUndeadWispLight";
                                            case "hashCode" -> System.identityHashCode(proxy);
                                            case "equals" ->
                                                    args != null
                                                            && args.length > 0
                                                            && proxy == args[0];
                                            default ->
                                                    method.isDefault()
                                                            ? java.lang.reflect.InvocationHandler
                                                                    .invokeDefault(
                                                                            proxy, method, args)
                                                            : defaultValue(method.getReturnType());
                                        };
                                    } catch (ReflectiveOperationException
                                            | RuntimeException
                                            | LinkageError failure) {
                                        dev.purifiedundead.util.FailureIsolation.report(
                                                "dynamic light callback", failure);
                                        return defaultValue(method.getReturnType());
                                    }
                                });
                registry.getMethod("registerDynamicLightHandler", EntityType.class, handler)
                        .invoke(null, ModEntities.CONTRACT_WISP.get(), adapter);
                backend = namespace;
                LOG.info("Contract visual light adapter ready: {}", namespace);
                return;
            } catch (ClassNotFoundException ignored) {
                // The light mod is optional; dedicated servers never load this class.
            } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
                LOG.warn("Optional dynamic-light API unavailable; keeping cosmetic wisp", error);
                return;
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return (char) 0;
        return null;
    }

    private WispDynamicLights() {}
}
