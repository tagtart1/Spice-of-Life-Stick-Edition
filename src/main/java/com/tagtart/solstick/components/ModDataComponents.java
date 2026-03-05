package com.tagtart.solstick.components;

import com.tagtart.solstick.SOLStick;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, SOLStick.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> LUNCH_BAG_CONTENTS = DATA_COMPONENTS
            .registerComponentType(
                    "lunch_bag_contents",
                    builder -> builder
                            .persistent(ItemContainerContents.CODEC)
                            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                            .cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LUNCH_BAG_SELECTED_SLOT = DATA_COMPONENTS
            .registerComponentType(
                    "lunch_bag_selected_slot",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LUNCH_BAG_OPEN = DATA_COMPONENTS
            .registerComponentType(
                    "lunch_bag_open",
                    builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .cacheEncoding());

    private ModDataComponents() {
    }

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
