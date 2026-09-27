package dev.purifiedundead.combat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FinalDamageRulesTest {
 @Test void finalPenaltyDoesNotHealOrAmplifyAbsorption() {
  assertEquals(3.0F, FinalDamageRules.subtractFromHealth(4,0,1));
  assertEquals(0.0F, FinalDamageRules.subtractFromHealth(0.5F,0,1));
  assertEquals(2.0F, FinalDamageRules.subtractFromHealth(2,5,1));
  assertEquals(4.0F, FinalDamageRules.subtractFromHealth(5,2,1));
  assertEquals(2.0F, FinalDamageRules.subtractFromHealth(2.5F,2,1));
 }
}
