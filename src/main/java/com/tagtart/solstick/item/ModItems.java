package com.tagtart.solstick.item;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.custom.LunchBagItem;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SOLStick.MODID);

    public static final DeferredItem<Item> LUNCH_BAG = ITEMS.register(
            "lunch_bag",
            () -> new LunchBagItem(new Item.Properties()));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

}
