package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import top.theillusivec4.curios.api.CuriosApi;

/** Event-driven awards, plus a one-time migration for pre-advancement saves. */
public final class ModAdvancements {
    public static final String MIGRATION = "purified_undead:advancements_043";
    public static final String[] IDS = {"hidden_power", "anger", "lament", "determination", "falling_petals",
            "blighted_throne", "separation", "purification", "witch_relic", "dawn_prayer", "frontier_witch", "rebirth"};
    public static void award(ServerPlayer player, String id) {
        var advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("purified_undead", "journey/" + id));
        if (advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone())
            player.getAdvancements().award(advancement, "history");
    }
    public static void progression(ServerPlayer p, WarriorProgress s) {
        if (s.contractObtained() && !s.contractPending()) award(p, "hidden_power");
        if (s.grothObtained() && !s.grothPending()) award(p, "anger");
        if (s.eleineObtained() && !s.eleinePending()) award(p, "lament");
        if (s.hoenirObtained() && !s.hoenirPending()) award(p, "determination");
        if (s.ulvObtained() && !s.ulvPending()) award(p, "falling_petals");
        if (s.juliusObtained() && !s.juliusPending()) award(p, "blighted_throne");
        if (s.fadenObtained() && !s.fadenPending()) award(p, "separation");
        if (s.guardiansObtained() && !s.guardiansPending()) award(p, "purification");
        if (s.talismanLevel() >= WhiteWitchTalisman.maxLevel()) award(p, "dawn_prayer");
    }
    public static void migrate(ServerPlayer p) {
        if (p.getPersistentData().getBoolean(MIGRATION)) return;
        // Datapack reload errors must not consume the migration opportunity.
        for (String id : IDS) if (p.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath("purified_undead", "journey/" + id)) == null) return;
        progression(p, WarriorProgressStorage.load(p));
        Item[] warriors = {ModItems.GROTH_WARRIOR.get(), ModItems.ELEINE_WARRIOR.get(), ModItems.HOENIR_WARRIOR.get(),
                ModItems.ULV_WARRIOR.get(), ModItems.JULIUS_WARRIOR.get(), ModItems.FADEN_WARRIOR.get(), ModItems.GUARDIAN_WARRIORS.get()};
        for (int i=0;i<warriors.length;i++) if (evidence(p, warriors[i])) award(p, IDS[i+1]);
        for (Item relic : relics()) if (evidence(p, relic)) {award(p,"witch_relic");break;}
        if (evidence(p,ModItems.PURE_TOUCH.get()) || evidence(p,ModItems.BLIGHTED_GUARDIAN.get())) award(p,"frontier_witch");
        if (evidence(p,ModItems.LILY_DIARY.get()) || dev.purifiedundead.purification.LilyDiaryProgress.count(p)>0) award(p,"rebirth");
        p.getPersistentData().putBoolean(MIGRATION,true);
    }
    public static Item[] relics() {return new Item[]{ModItems.BLOODSTAINED_RIBBON.get(),ModItems.ANCIENT_DRAGON_CLAW.get(),
            ModItems.WEATHERED_WARRIOR_NECKLACE.get(),ModItems.SOILED_SILVER_ROSARY.get(),ModItems.WHITE_PRIESTESS_STATUE.get(),
            ModItems.BLIGHTED_FINGER.get(),ModItems.KINGS_SHIELD_BADGE.get(),ModItems.WHITE_PRIESTESS_EARRINGS.get()};}
    private static boolean evidence(ServerPlayer p, Item item) {
        if (p.getStats().getValue(Stats.ITEM_PICKED_UP.get(item))>0 || p.getStats().getValue(Stats.ITEM_CRAFTED.get(item))>0) return true;
        for (int i=0;i<p.getInventory().getContainerSize();i++) if (p.getInventory().getItem(i).is(item)) return true;
        for (int i=0;i<p.getEnderChestInventory().getContainerSize();i++) if (p.getEnderChestInventory().getItem(i).is(item)) return true;
        return CuriosApi.getCuriosInventory(p).map(h -> h.getCurios().values().stream().anyMatch(v -> {
            for (var stacks : new top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler[]{v.getStacks(),v.getCosmeticStacks()})
                for (int i=0;i<stacks.getSlots();i++) if(stacks.getStackInSlot(i).is(item)) return true;
            return false;
        })).orElse(false);
    }
    private ModAdvancements() {}
}
