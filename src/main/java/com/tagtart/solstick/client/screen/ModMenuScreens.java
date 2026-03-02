package com.tagtart.solstick.client.screen;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = SOLStick.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LUNCH_BAG.get(), LunchBagScreen::new);
    }
}
