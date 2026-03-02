package com.tagtart.solstick.item.custom;

import com.tagtart.solstick.menu.custom.LunchBagMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LunchBagItem extends Item {
    private static final Component TITLE = Component.translatable("container.solstick.lunch_bag");

    public LunchBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, ignoredPlayer) -> new LunchBagMenu(containerId, playerInventory),
                    TITLE));
        }

        return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide());
    }
}
