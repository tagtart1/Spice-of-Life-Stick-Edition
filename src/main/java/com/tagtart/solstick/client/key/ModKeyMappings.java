package com.tagtart.solstick.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.tagtart.solstick.SOLStick;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories.solstick";
    public static final KeyMapping STOMACH_OVERLAY = new KeyMapping(
            "key.solstick.stomach_overlay",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY);

    private ModKeyMappings() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(STOMACH_OVERLAY);
    }
}
