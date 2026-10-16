package dev.purifiedundead.blightedrain.content;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

public final class RainEquipment {
    private static final java.util.UUID ID =
            java.util.UUID.fromString("610de06d-3c11-44de-ac21-4c83e1c7810d");

    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        var armor = entity.getAttribute(Attributes.ARMOR);
        if (armor == null) return;
        boolean held =
                entity.getMainHandItem().is(RainItems.SHIELD.get())
                        || entity.getOffhandItem().is(RainItems.SHIELD.get());
        if (held && armor.getModifier(ID) == null)
            armor.addTransientModifier(
                    new AttributeModifier(
                            ID,
                            "Headless knight held armor",
                            5,
                            AttributeModifier.Operation.ADDITION));
        else if (!held && armor.getModifier(ID) != null) armor.removeModifier(ID);
    }

    private RainEquipment() {}
}
