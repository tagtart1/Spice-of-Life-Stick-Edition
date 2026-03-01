package com.tagtart.solstick.item;

import java.util.function.Supplier;

import com.tagtart.solstick.SOLStick;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, SOLStick.MODID);

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }

    public static final Supplier<CreativeModeTab> SOLSTICK_TAB = CREATIVE_MODE_TABS.register("solstick",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LUNCH_BAG.get()))
                    .title(Component.translatable("creative.solstick.title"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LUNCH_BAG.get());
                    }).build());
}
