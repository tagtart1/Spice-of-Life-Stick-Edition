package com.tagtart.solstick.network;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagConstants;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LunchBagSelectSlotPayload(int selectedSlot, boolean offhand) implements CustomPacketPayload {
    public static final Type<LunchBagSelectSlotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SOLStick.MODID, "lunch_bag_select_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunchBagSelectSlotPayload> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.VAR_INT,
                    LunchBagSelectSlotPayload::selectedSlot,
                    ByteBufCodecs.BOOL,
                    LunchBagSelectSlotPayload::offhand,
                    LunchBagSelectSlotPayload::new);

    @Override
    public Type<LunchBagSelectSlotPayload> type() {
        return TYPE;
    }

    public static void handle(LunchBagSelectSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack heldStack = serverPlayer.getItemInHand(hand);
            if (!heldStack.is(ModItems.LUNCH_BAG.get())) {
                return;
            }

            int normalizedSlot = Math.floorMod(payload.selectedSlot, LunchBagConstants.TOTAL_SELECTABLE_SLOTS);
            if (LunchBagItem.isHiddenBestSlot(normalizedSlot) && !LunchBagItem.hasAnyFood(heldStack)) {
                return;
            }

            if (!LunchBagItem.isHiddenBestSlot(normalizedSlot)
                    && !LunchBagItem.hasFoodAtSlot(heldStack, normalizedSlot)) {
                return;
            }

            heldStack.set(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), normalizedSlot);
        });
    }
}
