package com.tagtart.solstick.client.render.item;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class ModItemClientExtensions {
    private static final ModelResourceLocation LUNCH_BAG_BASE_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(SOLStick.MODID, "item/lunch_bag_base"));

    private static final IClientItemExtensions LUNCH_BAG_EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return LunchBagItemRenderer.INSTANCE;
        }
    };

    private ModItemClientExtensions() {
    }

    @SubscribeEvent
    public static void registerClientItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(LUNCH_BAG_EXTENSIONS, ModItems.LUNCH_BAG.get());
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(LUNCH_BAG_BASE_MODEL);
    }
}
