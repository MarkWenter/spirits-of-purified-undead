package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

final class FerinEquipment {
    private FerinEquipment() {}

    static State read(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(
                        handler ->
                                new State(
                                        handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                                        handler.isEquipped(ModItems.FERIN_WARRIOR.get())))
                .orElse(State.NONE);
    }

    record State(boolean contract, boolean ferinWarrior) {
        private static final State NONE = new State(false, false);

        boolean reversed() {
            return contract && ferinWarrior;
        }
    }
}
