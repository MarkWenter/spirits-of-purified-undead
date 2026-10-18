package dev.purifiedundead.mixin;

import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Fox.class)
public interface RainFoxAccessor {
    @Invoker("getTrustedUUIDs")
    java.util.List<java.util.UUID> purifiedUndead$trusted();
}
