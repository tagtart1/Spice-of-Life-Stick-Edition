package com.tagtart.solstick.network;

import com.tagtart.solstick.SOLStick;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SOLStick.MODID)
public final class ModPayloads {
    private ModPayloads() {
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                LunchBagSelectSlotPayload.TYPE,
                LunchBagSelectSlotPayload.STREAM_CODEC,
                LunchBagSelectSlotPayload::handle);
    }
}
