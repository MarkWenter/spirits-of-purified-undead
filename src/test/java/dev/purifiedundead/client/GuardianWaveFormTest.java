package dev.purifiedundead.client;

import static org.junit.jupiter.api.Assertions.*;
import java.util.EnumMap;
import org.junit.jupiter.api.Test;

class GuardianWaveFormTest {
    @Test void predictionAndObserversChooseTheSameWeaponAcrossAllFiveForms() {
        var counts = new EnumMap<GuardianWaveForm, Integer>(GuardianWaveForm.class);
        for (int shot = 1; shot <= 2048; shot++) {
            var form = GuardianWaveForm.select(31, shot, -1000000000 - shot);
            assertSame(form, GuardianWaveForm.select(31, shot, 427 + shot));
            counts.merge(form, 1, Integer::sum);
        }
        assertEquals(5, counts.size());
        assertTrue(counts.values().stream().allMatch(n -> n > 300 && n < 520));
    }

    @Test void serverOnlySwingsUseTheTrackedEntityIdAndOnlyTwoWeaponsTumble() {
        var forms = java.util.EnumSet.noneOf(GuardianWaveForm.class);
        for (int id = 1; id <= 120; id++) forms.add(GuardianWaveForm.select(53, 0, id));
        assertEquals(5, forms.size());
        for (var form : forms) {
            float turn = form.angle(15, 15) - form.angle(0, 15);
            if (form == GuardianWaveForm.FERIN || form == GuardianWaveForm.SILVA)
                assertTrue(Math.abs(turn) > 360);
            else
                assertEquals(0F, turn);
        }
    }

    @Test void allFiveFormsHaveEqualFrequencyAndAllowConsecutiveRepeats() {
        var forms = GuardianWaveForm.values();
        assertEquals(5, forms.length);
        int[] counts = new int[forms.length];
        int repeats = 0;
        for (int owner : new int[]{1, 31, 319, 1001}) {
            GuardianWaveForm previous = null;
            for (int shot = 1; shot <= 250_000; shot++) {
                var form = GuardianWaveForm.select(owner, shot, -shot);
                assertSame(form, GuardianWaveForm.select(owner, 0, shot));
                if (form == previous) repeats++;
                previous = form;
                counts[form.ordinal()]++;
            }
        }
        for (int n : counts) assertTrue(Math.abs(n - 200_000) < 2_000);
        assertTrue(Math.abs(repeats - 200_000) < 2_000);
    }
}
