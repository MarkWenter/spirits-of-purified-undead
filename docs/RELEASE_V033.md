# v0.33.0 — movement response and combat balance

Minecraft 1.20.1 / Forge. Install the new mod JAR on both client and server; the network protocol changed. Existing dependency versions are unchanged. This release also includes the previously local brewing-recipe viewer fix.

## Changes

- Guardian double jump and air dash now apply locally on input, using movement settings sent by the server. The server still validates equipment and airborne usage and corrects rejected actions. Successful actions no longer send a second impulse back to the owner after a network round trip. Other players still receive movement updates. This removes the initial round-trip wait, but does not eliminate server corrections or all effects of poor connectivity.
- Blight fragments default to a 25% drop chance, up from 12.5%. Successful eligible player kills drop a random 1–8 fragments, plus a random 0–Looting-level bonus. Looting III therefore gives 1–11. Existing player-kill attribution and entity whitelist remain in force. Excess stacks from unusually high enchantment levels are split.
- Mad Knight curse damage is added to the original health-damage calculation. It no longer schedules another hurt call, so it does not create a second hit/knockback. Fully absorbed or blocked hits do not trigger the extra health loss. Ulv's purified follow-up attack remains separate.
- Knight Captain's unpurified curse no longer reduces reach. It instead subtracts 1 from final direct player melee health damage, clamped at zero. Purified reach bonuses and sprint effects remain unchanged.
- Hoenir acquisition now defaults to three distinct harmful effects when killing a zombie.
- The two requested contract descriptions remain unchanged, even though their requirements/effects changed.
- Blight elixir brewing uses standard discoverable recipe descriptors while retaining the ordinary awkward-potion restriction.

## Existing configurations

On first load, a configuration without `balanceRevision = 1` migrates the previous exact defaults: fragment probability `0.125` becomes `0.25`, and Hoenir threshold `5` becomes `3`. Other values remain unchanged. Values exactly equal to old defaults cannot be distinguished from deliberate custom choices; after migration they can be edited again. The migration runs once.

New key: `[julius] unreversedFinalMeleeDamagePenalty = 1.0`. The old `unreversedReachPenalty` key remains readable for compatibility but no longer controls the curse.

## Validation

Forge: 109 automated tests passed, including final-damage/absorption boundaries, direction-independent dash speed, jump momentum and queued-input expiry. The parallel NeoForge build passed 111 tests and retains NeoForge 21.1.248 as its minimum. Runtime smoke checks and old-default config migration are recorded locally. Real multiplayer testing under artificial latency and with the user's complete modpack remains outstanding.
