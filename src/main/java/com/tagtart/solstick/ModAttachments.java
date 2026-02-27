package com.tagtart.solstick;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> MOD_ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SOLStick.MODID);

    public static final Supplier<AttachmentType<Integer>> TEST_ATTACHMENT = MOD_ATTACHMENTS.register(
            "test_attachment", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build()
    );

    public static void register(IEventBus modBus) {
        MOD_ATTACHMENTS.register(modBus);
    }
}
