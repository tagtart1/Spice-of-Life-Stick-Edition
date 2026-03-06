package com.tagtart.solstick.sound;

import com.tagtart.solstick.SOLStick;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, SOLStick.MODID);

    public static final Supplier<SoundEvent> LUNCH_BAG_OPEN = registerSoundEvent("lunch_bag_open");
    public static final Supplier<SoundEvent> LUNCH_BAG_CLOSE = registerSoundEvent("lunch_bag_close");
    public static final Supplier<SoundEvent> LUNCH_BAG_INSERT = registerSoundEvent("lunch_bag_insert");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SOLStick.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
