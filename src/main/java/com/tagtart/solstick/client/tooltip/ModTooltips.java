package com.tagtart.solstick.client.tooltip;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.tooltip.LunchBagTooltipComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class ModTooltips {
    private ModTooltips() {
    }

    @SubscribeEvent
    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(LunchBagTooltipComponent.class, LunchBagClientTooltipComponent::new);
    }
}
