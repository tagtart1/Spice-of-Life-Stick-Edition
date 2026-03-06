package com.tagtart.solstick.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LunchBagItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LunchBagItemRenderer INSTANCE = new LunchBagItemRenderer();

    private static final ModelResourceLocation LUNCH_BAG_BASE_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(SOLStick.MODID, "item/lunch_bag_base"));

    private LunchBagItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        LocalPlayer player = minecraft.player;

        if (shouldRenderSelectedFood(stack, displayContext, player)) {
            ItemStack selectedFood = LunchBagItem.getSelectedFoodStack(stack, player);
            itemRenderer.renderStatic(selectedFood, displayContext, packedLight, packedOverlay, poseStack, bufferSource,
                    minecraft.level, 0);
            poseStack.popPose();
            return;
        }

        BakedModel fallbackModel = minecraft.getModelManager().getModel(LUNCH_BAG_BASE_MODEL);
        itemRenderer.render(stack, displayContext, false, poseStack, bufferSource, packedLight, packedOverlay,
                fallbackModel);
        poseStack.popPose();
    }

    private static boolean shouldRenderSelectedFood(ItemStack lunchBag, ItemDisplayContext displayContext,
            @Nullable LocalPlayer player) {
        if (player == null || !isHandContext(displayContext)) {
            return false;
        }
        if (!player.isUsingItem() || player.getUseItem() != lunchBag) {
            return false;
        }
        if (!LunchBagItem.isOpen(lunchBag)) {
            return false;
        }
        return !LunchBagItem.getSelectedFoodStack(lunchBag, player).isEmpty();
    }

    private static boolean isHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
