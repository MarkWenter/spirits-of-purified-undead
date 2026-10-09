package dev.purifiedundead.mixin;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Scale the single random draw, preserving the original threshold including enchantment bonuses. */
@Mixin({LootItemRandomChanceCondition.class,LootItemRandomChanceWithLootingCondition.class})
public abstract class MemoryDropChanceMixin {
    @Redirect(method="test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
        at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float purifiedUndead$drop(RandomSource random,LootContext context){
        float roll=random.nextFloat();
        return dev.purifiedundead.slate.MemoryDropChance.applies(context)?roll*.5F:roll;
    }
}
