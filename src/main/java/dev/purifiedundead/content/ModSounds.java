package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.Map;
import java.util.LinkedHashMap;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PurifiedUndead.MOD_ID);
    private static final Map<String, RegistryObject<SoundEvent>> EVENTS = new LinkedHashMap<>();

    static {
        for (String name :
                new String[] {
                    "ancient_contract",
                    "groth",
                    "julius",
                    "guardians",
                    "ulv",
                    "eleine",
                    "faden",
                    "talisman_max"
                }) {
            EVENTS.put(
                    name,
                    SOUNDS.register(
                            name,
                            () ->
                                    SoundEvent.createVariableRangeEvent(
                                            new ResourceLocation(PurifiedUndead.MOD_ID, name))));
        }
    }

    private ModSounds() {}

    public static SoundEvent find(String name) {
        RegistryObject<SoundEvent> event = EVENTS.get(name);
        return event == null ? null : event.get();
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
