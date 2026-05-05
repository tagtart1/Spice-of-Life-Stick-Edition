package com.tagtart.solstick.client.overlay;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.client.key.ModKeyMappings;
import com.tagtart.solstick.client.state.StomachOverlayState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class StomachOverlayInputHandler {
    private StomachOverlayInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean shouldShowOverlay = minecraft.player != null
                && minecraft.screen == null
                && ModKeyMappings.STOMACH_OVERLAY.isDown();
        StomachOverlayState.setVisible(shouldShowOverlay);
    }
}
