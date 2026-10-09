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
                                (proxy, method, args) ->
                                        switch (method.getName()) {
                                            case "getLuminance" ->
                                                    args[0] instanceof ContractWispEntity wisp
                                                            ? wisp.terrainLight()
                                                            : 0;
                                            case "isWaterSensitive" -> false;
                                            case "toString" -> "PurifiedUndeadWispLight";
                                            case "hashCode" -> System.identityHashCode(proxy);
                                            case "equals" -> proxy == args[0];
                                            default ->
                                                    method.isDefault()
                                                            ? java.lang.reflect.InvocationHandler
                                                                    .invokeDefault(
                                                                            proxy, method, args)
                                                            : null;
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

    private WispDynamicLights() {}
}
