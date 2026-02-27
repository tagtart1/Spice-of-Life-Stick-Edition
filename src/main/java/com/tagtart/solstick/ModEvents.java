package com.tagtart.solstick;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = SOLStick.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player.level().isClientSide()) {
            return;
        }
        int currentTest = player.getData(ModAttachments.TEST_ATTACHMENT);
        player.setData(ModAttachments.TEST_ATTACHMENT, currentTest + 1);
    }
}
