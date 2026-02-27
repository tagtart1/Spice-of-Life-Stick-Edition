package com.tagtart.solstick;

import net.neoforged.bus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;

public class AppleSkinEventHandler {

    @SubscribeEvent
    public void onFoodValuesEvent(FoodValuesEvent event) {
        SOLStick.LOGGER.info("Food values received");

    }
}
