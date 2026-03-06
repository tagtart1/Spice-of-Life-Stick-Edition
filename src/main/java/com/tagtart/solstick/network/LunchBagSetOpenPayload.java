package com.tagtart.solstick.network;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.sound.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LunchBagSetOpenPayload(boolean open, boolean offhand) implements CustomPacketPayload {
    public static final Type<LunchBagSetOpenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SOLStick.MODID, "lunch_bag_set_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LunchBagSetOpenPayload> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.BOOL,
                    LunchBagSetOpenPayload::open,
                    ByteBufCodecs.BOOL,
                    LunchBagSetOpenPayload::offhand,
                    LunchBagSetOpenPayload::new);

    @Override
    public Type<LunchBagSetOpenPayload> type() {
        return TYPE;
    }

    public static void handle(LunchBagSetOpenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack heldStack = serverPlayer.getItemInHand(hand);
            if (!heldStack.is(ModItems.LUNCH_BAG.get())) {
                return;
            }

            heldStack.set(ModDataComponents.LUNCH_BAG_OPEN.get(), payload.open);
            serverPlayer.playNotifySound(
                    payload.open ? ModSounds.LUNCH_BAG_OPEN.get() : ModSounds.LUNCH_BAG_CLOSE.get(),
                    SoundSource.PLAYERS,
                    0.8F,
                    0.8F);
        });
    }
}
