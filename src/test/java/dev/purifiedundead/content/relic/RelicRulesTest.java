package dev.purifiedundead.content.relic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelicRulesTest {
    @Test
    void smallExperienceOrbsKeepTheirFractionalBonus() {
        double remainder = 0;
        int total = 0;
        for (int i = 0; i < 100; i++) {
            var gain = RelicRules.experience(1, remainder, 1, 1);
            total += gain.amount();
            remainder = gain.remainder();
        }
        assertEquals(125, total);
        assertEquals(0, remainder);
    }

    @Test
    void negativeXpAndDisabledEffectsDoNotGrantExperience() {
        assertEquals(-5, RelicRules.experience(-5, .75, 1, 1).amount());
        assertEquals(4, RelicRules.experience(4, .75, 1, 0).amount());
        assertEquals(Integer.MAX_VALUE, RelicRules.experience(Integer.MAX_VALUE, 0, 1, 1).amount());
    }

    @Test
    void clawIsAnIncreaseNotAReplacementAndDoesNotAffectMelee() {
        assertEquals(2.35, RelicRules.damageMultiplier(0, 1, true, 1), 1e-8);
        assertEquals(1, RelicRules.damageMultiplier(0, 1, false, 1), 1e-8);
        assertEquals(2.9375, RelicRules.damageMultiplier(1, 1, true, 1), 1e-8);
        assertEquals(1.25, RelicRules.damageMultiplier(1, 1, false, 1), 1e-8);
    }

    @Test
    void necklaceHealsMissingHealthRatherThanMaximumHealth() {
        assertEquals(3.5F, RelicRules.killHealing(10, 20, 1, 1));
        assertEquals(0, RelicRules.killHealing(20, 20, 1, 1));
        assertEquals(10, RelicRules.killHealing(10, 20, 3, 1));
    }

    @Test
    void healthBonusesAreAdditiveAndCooldownBoundaryIsExact() {
        assertEquals(.30, RelicRules.healthBonus(1, 1, 1), 1e-8);
        assertFalse(RelicRules.ready(2399, 2400));
        assertTrue(RelicRules.ready(2400, 2400));
        assertFalse(RelicRules.ready(11999, 12000));
        assertTrue(RelicRules.ready(12000, 12000));
    }
}
