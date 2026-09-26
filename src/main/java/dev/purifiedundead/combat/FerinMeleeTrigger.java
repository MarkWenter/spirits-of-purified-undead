package dev.purifiedundead.combat;

/** Pure decision boundary for the runtime Forge damage-event adapter. */
public final class FerinMeleeTrigger {
    private FerinMeleeTrigger() { }

    public static boolean qualifies(boolean serverSide, boolean contractEquipped,
                                    boolean playerIsCausingAndDirectEntity,
                                    boolean playerAttackDamageType, float finalHealthDamage) {
        return serverSide && contractEquipped && playerIsCausingAndDirectEntity
                && playerAttackDamageType && Float.isFinite(finalHealthDamage) && finalHealthDamage > 0.0F;
    }

    /** Collapses the primary hit and all sweep victims produced by one player attack tick. */
    public static final class AttackTickGate {
        private long lastAcceptedTick = Long.MIN_VALUE;

        public AttackTickGate() { }

        public AttackTickGate(long lastAcceptedTick) {
            this.lastAcceptedTick = lastAcceptedTick;
        }

        public boolean accept(long gameTick) {
            if (lastAcceptedTick == gameTick) {
                return false;
            }
            lastAcceptedTick = gameTick;
            return true;
        }

        public long lastAcceptedTick() {
            return lastAcceptedTick;
        }
    }
}
