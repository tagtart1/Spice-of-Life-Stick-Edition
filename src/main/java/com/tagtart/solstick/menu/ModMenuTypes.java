package com.tagtart.solstick.menu;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.menu.custom.LunchBagMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, SOLStick.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LunchBagMenu>> LUNCH_BAG = MENU_TYPES.register(
            "lunch_bag",
            () -> IMenuTypeExtension.create(LunchBagMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENU_TYPES.register(modBus);
    }
}
