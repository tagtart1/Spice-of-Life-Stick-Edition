package com.tagtart.solstick.client.overlay;

import com.mojang.blaze3d.platform.InputConstants;
import com.tagtart.solstick.LunchBagOverlayState;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagConstants;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import com.tagtart.solstick.network.LunchBagSelectSlotPayload;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class LunchBagOverlayInputHandler {
    private LunchBagOverlayInputHandler() {
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != InputConstants.PRESS || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        ItemStack lunchBag = getHeldLunchBag(minecraft.player);
        if (lunchBag.isEmpty()) {
            return;
        }

        LunchBagOverlayState.toggle();
        if (LunchBagOverlayState.isVisible()) {
            lockAndRestoreHotbar(minecraft.player);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldHandleOverlayInput(minecraft)) {
            return;
        }

        InteractionHand hand = getHeldLunchBagHand(minecraft.player);
        if (hand == null) {
            LunchBagOverlayState.hide();
            return;
        }
        ItemStack lunchBag = minecraft.player.getItemInHand(hand);

        if (event.getScrollDeltaY() != 0.0D) {
            int current = lunchBag.getOrDefault(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0);
            int delta = event.getScrollDeltaY() > 0.0D ? -1 : 1;
            int nextSlot = LunchBagItem.getNextFilledFoodSlot(lunchBag, current, delta);
            if (nextSlot >= 0) {
                updateSelectedSlot(lunchBag, hand, nextSlot);
            }
        }

        event.setCanceled(true);
        lockAndRestoreHotbar(minecraft.player);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!LunchBagOverlayState.isVisible()) {
            LunchBagOverlayState.clearLockedHotbarSlot();
            return;
        }

        if (minecraft.player == null) {
            return;
        }

        ItemStack lunchBag = getHeldLunchBag(minecraft.player);
        if (lunchBag.isEmpty()) {
            LunchBagOverlayState.hide();
            return;
        }

        lockAndRestoreHotbar(minecraft.player);
    }

    public static boolean onKeyboardKeyPress(int key, int action) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldHandleOverlayInput(minecraft) || action != InputConstants.PRESS) {
            return false;
        }

        InteractionHand hand = getHeldLunchBagHand(minecraft.player);
        if (hand == null) {
            LunchBagOverlayState.hide();
            return false;
        }
        ItemStack lunchBag = minecraft.player.getItemInHand(hand);

        int hotbarIndex = mapNumberKeyToHotbarIndex(key);
        if (hotbarIndex < 0) {
            return false;
        }

        if (hotbarIndex >= 0 && hotbarIndex < LunchBagConstants.SLOT_COUNT
                && LunchBagItem.hasFoodAtSlot(lunchBag, hotbarIndex)) {
            updateSelectedSlot(lunchBag, hand, hotbarIndex);
        }

        lockAndRestoreHotbar(minecraft.player);
        return true;
    }

    private static boolean shouldHandleOverlayInput(Minecraft minecraft) {
        return LunchBagOverlayState.isVisible() && minecraft.player != null && minecraft.screen == null;
    }

    private static ItemStack getHeldLunchBag(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.LUNCH_BAG.get())) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(ModItems.LUNCH_BAG.get())) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    @Nullable
    private static InteractionHand getHeldLunchBagHand(Player player) {
        if (player.getMainHandItem().is(ModItems.LUNCH_BAG.get())) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().is(ModItems.LUNCH_BAG.get())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static void lockAndRestoreHotbar(Player player) {
        int lockedSlot = LunchBagOverlayState.getLockedHotbarSlot();
        if (lockedSlot < 0) {
            lockedSlot = player.getInventory().selected;
            LunchBagOverlayState.setLockedHotbarSlot(lockedSlot);
        }

        if (player.getInventory().selected != lockedSlot) {
            player.getInventory().selected = lockedSlot;
        }
    }

    private static int mapNumberKeyToHotbarIndex(int key) {
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            return key - GLFW.GLFW_KEY_1;
        }

        if (key >= GLFW.GLFW_KEY_KP_1 && key <= GLFW.GLFW_KEY_KP_9) {
            return key - GLFW.GLFW_KEY_KP_1;
        }

        return -1;
    }

    private static void updateSelectedSlot(ItemStack lunchBag, InteractionHand hand, int selectedSlot) {
        int normalizedSlot = Math.floorMod(selectedSlot, LunchBagConstants.SLOT_COUNT);
        lunchBag.set(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), normalizedSlot);
        PacketDistributor.sendToServer(new LunchBagSelectSlotPayload(normalizedSlot, hand == InteractionHand.OFF_HAND));
    }

}
