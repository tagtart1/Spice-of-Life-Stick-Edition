package com.tagtart.solstick.menu.custom;

import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.menu.ModMenuTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class LunchBagMenu extends AbstractContainerMenu {
    public static final int ROWS = 1;
    public static final int COLUMNS = 7;
    public static final int SLOT_COUNT = ROWS * COLUMNS;

    private final Container lunchBagInventory;

    public LunchBagMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    public LunchBagMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ItemStack.EMPTY);
    }

    public LunchBagMenu(int containerId, Inventory playerInventory, ItemStack lunchBagStack) {
        super(ModMenuTypes.LUNCH_BAG.get(), containerId);
        Container lunchBagInventory = new SimpleContainer(SLOT_COUNT);
        checkContainerSize(lunchBagInventory, SLOT_COUNT);
        this.lunchBagInventory = lunchBagInventory;
        fillContainerSlots(this.lunchBagInventory, lunchBagStack);
        this.lunchBagInventory.startOpen(playerInventory.player);

        int xOffset = 8;
        int yOffset = 18;

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int slotIndex = column + row * COLUMNS;
                addSlot(new Slot(this.lunchBagInventory, slotIndex, xOffset + column * 18, yOffset + row * 18));
            }
        }
    }

    private void fillContainerSlots(Container lunchBagInventory, ItemStack lunchBagStack) {
        if (lunchBagStack.isEmpty()) {
            return;
        }

        ItemContainerContents contents = lunchBagStack.getOrDefault(
                ModDataComponents.LUNCH_BAG_CONTENTS.get(),
                ItemContainerContents.EMPTY);
        NonNullList<ItemStack> storedItems = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        contents.copyInto(storedItems);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            lunchBagInventory.setItem(slot, storedItems.get(slot));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // View-only menu: ignore all click actions.
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.lunchBagInventory.stopOpen(player);
    }

}
