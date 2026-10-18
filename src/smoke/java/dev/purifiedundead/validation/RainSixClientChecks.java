package dev.purifiedundead.validation;

public final class RainSixClientChecks {
    private static Object call(Object o, String n, Object... args) throws Exception {
        for (var m : o.getClass().getMethods())
            if (m.getName().equals(n) && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                return m.invoke(o, args);
            }
        throw new NoSuchMethodException(n);
    }

    public static void check(net.minecraft.client.Minecraft mc) throws Exception {
        var keys = java.util.Arrays.asList(mc.options.keyMappings);
        if (!keys.contains(dev.purifiedundead.client.AbilityKeys.FERIN)
                || !keys.contains(dev.purifiedundead.client.AbilityKeys.MOVEMENT)
                || !dev.purifiedundead.client.AbilityKeys.FERIN.isUnbound()
                || !dev.purifiedundead.client.AbilityKeys.MOVEMENT.isUnbound())
            throw new IllegalStateException("Keys not registered unbound");
        var optional =
                (java.util.Optional<?>)
                        Class.forName("mezz.jei.common.Internal")
                                .getMethod("getOptionalJeiRuntime")
                                .invoke(null);
        if (optional.isEmpty()) throw new IllegalStateException("JEI not ready");
        var manager = call(optional.get(), "getRecipeManager");
        Object type =
                Class.forName("dev.purifiedundead.compat.jei.SlateJeiPlugin")
                        .getField("FOUNDRY")
                        .get(null);
        var foundry =
                ((java.util.stream.Stream<?>)
                                call(call(manager, "createRecipeLookup", type), "get"))
                        .toList();
        boolean beacon = false;
        for (var recipe : foundry)
            if (call(recipe, "output").equals("purified_undead:still_rain_beacon")) {
                if ((boolean) call(recipe, "consumeTop"))
                    throw new IllegalStateException("beacon treasure consumed");
                beacon = true;
            }
        if (!beacon) throw new IllegalStateException("JEI beacon recipe missing");
        var outputs = new java.util.HashSet<String>();
        for (String category :
                new String[] {"CRAFTING", "SMELTING", "SMOKING", "CAMPFIRE_COOKING"}) {
            type = Class.forName("mezz.jei.api.constants.RecipeTypes").getField(category).get(null);
            for (var holder :
                    ((java.util.stream.Stream<?>)
                                    call(call(manager, "createRecipeLookup", type), "get"))
                            .toList()) {
                Object recipe = holder;
                try {
                    recipe = call(holder, "value");
                } catch (NoSuchMethodException ignored) {
                }
                var output =
                        (net.minecraft.world.item.ItemStack)
                                call(recipe, "getResultItem", mc.level.registryAccess());
                outputs.add(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .getKey(output.getItem())
                                .toString());
            }
        }
        for (String name :
                new String[] {"cliff_village_youth", "headless_knight", "cooked_blighted_meat"})
            if (!outputs.contains("purified_undead:" + name))
                throw new IllegalStateException("JEI missing " + name);
        System.out.println(
                "RAIN6_CLIENT_KEYS_JEI_OK default-unbound keys, beacon preserving treasure, youth/shield/cooked meat recipes");
    }
}
