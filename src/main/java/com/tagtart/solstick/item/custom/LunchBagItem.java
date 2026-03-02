package com.tagtart.solstick.item.custom;

import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.tooltip.LunchBagTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LunchBagItem extends Item {
    public LunchBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        ItemStack selectedFood = getSelectedFoodStack(heldItem);
        FoodProperties selectedFoodProperties = selectedFood.getFoodProperties(player);
        if (!selectedFood.isEmpty() && selectedFoodProperties != null && player.canEat(selectedFoodProperties.canAlwaysEat())) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(heldItem);
        }

        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    @Nullable
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        ItemStack selectedFood = getSelectedFoodStack(stack);
        return selectedFood.isEmpty() ? null : selectedFood.getFoodProperties(entity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack selectedFood = getSelectedFoodStack(stack);
        return selectedFood.isEmpty() ? super.getUseDuration(stack, entity) : selectedFood.getUseDuration(entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ItemStack selectedFood = getSelectedFoodStack(stack);
        return selectedFood.isEmpty() ? super.getUseAnimation(stack) : selectedFood.getUseAnimation();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemContainerContents contents = stack.getOrDefault(
                ModDataComponents.LUNCH_BAG_CONTENTS.get(),
                ItemContainerContents.EMPTY);
        if (contents.equals(ItemContainerContents.EMPTY)) {
            return Optional.empty();
        }

        int selectedSlot = stack.getOrDefault(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0);
        return Optional.of(new LunchBagTooltipComponent(contents, selectedSlot));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        int selectedSlot = getSelectedSlotIndex(stack);
        NonNullList<ItemStack> storedItems = getStoredItems(stack);
        ItemStack selectedFood = storedItems.get(selectedSlot);
        if (selectedFood.isEmpty() || selectedFood.getFoodProperties(livingEntity) == null) {
            return stack;
        }

        ItemStack postEatStack = selectedFood.finishUsingItem(level, livingEntity);
        storedItems.set(selectedSlot, postEatStack);
        setStoredItems(stack, storedItems);
        return stack;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) {
            if (player.level().isClientSide()) {
                return true;
            }

            ItemStack extracted = removeFirstStoredStack(stack);
            if (extracted.isEmpty()) {
                return false;
            }

            ItemStack remainder = slot.safeInsert(extracted);
            if (!remainder.isEmpty()) {
                insertFoodIntoBag(stack, remainder);
            }

            if (remainder.getCount() == extracted.getCount()) {
                return false;
            }

            slot.setChanged();
            return true;
        }

        if (!slot.mayPickup(player) || !isFood(slotStack)) {
            return false;
        }

        if (player.level().isClientSide()) {
            return true;
        }

        int inserted = insertFoodIntoBag(stack, slotStack);
        if (inserted <= 0) {
            return false;
        }

        slotStack.shrink(inserted);
        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action,
            Player player, SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (otherStack.isEmpty()) {
            if (player.level().isClientSide()) {
                return true;
            }

            ItemStack extracted = removeFirstStoredStack(stack);
            if (extracted.isEmpty()) {
                return false;
            }

            if (!slotAccess.set(extracted)) {
                insertFoodIntoBag(stack, extracted);
                return false;
            }

            slot.setChanged();
            return true;
        }

        if (!isFood(otherStack)) {
            return false;
        }

        if (player.level().isClientSide()) {
            return true;
        }

        int inserted = insertFoodIntoBag(stack, otherStack);
        if (inserted <= 0) {
            return false;
        }

        ItemStack remainder = otherStack.copy();
        remainder.shrink(inserted);
        slotAccess.set(remainder);
        slot.setChanged();
        return true;
    }

    private static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD);
    }

    public static ItemStack getSelectedFoodStack(ItemStack lunchBag) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        ItemStack selectedFood = storedItems.get(getSelectedSlotIndex(lunchBag));
        return isFood(selectedFood) ? selectedFood : ItemStack.EMPTY;
    }

    public static boolean hasFoodAtSlot(ItemStack lunchBag, int slotIndex) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return false;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        int normalizedSlot = Math.floorMod(slotIndex, LunchBagConstants.SLOT_COUNT);
        return isFood(storedItems.get(normalizedSlot));
    }

    public static int getNextFilledFoodSlot(ItemStack lunchBag, int currentSlotIndex, int direction) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return -1;
        }

        int step = direction >= 0 ? 1 : -1;
        int nextSlot = Math.floorMod(currentSlotIndex, LunchBagConstants.SLOT_COUNT);
        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            nextSlot = Math.floorMod(nextSlot + step, LunchBagConstants.SLOT_COUNT);
            if (hasFoodAtSlot(lunchBag, nextSlot)) {
                return nextSlot;
            }
        }

        return -1;
    }

    private static int insertFoodIntoBag(ItemStack lunchBag, ItemStack source) {
        if (!isFood(source)) {
            return 0;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        int remaining = source.getCount();

        for (int i = 0; i < LunchBagConstants.SLOT_COUNT && remaining > 0; i++) {
            ItemStack existing = storedItems.get(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, source)) {
                continue;
            }

            int stackLimit = Math.min(existing.getMaxStackSize(), source.getMaxStackSize());
            int freeSpace = stackLimit - existing.getCount();
            if (freeSpace <= 0) {
                continue;
            }

            int amountToMove = Math.min(freeSpace, remaining);
            existing.grow(amountToMove);
            remaining -= amountToMove;
        }

        for (int i = 0; i < LunchBagConstants.SLOT_COUNT && remaining > 0; i++) {
            if (!storedItems.get(i).isEmpty()) {
                continue;
            }

            int amountToMove = Math.min(source.getMaxStackSize(), remaining);
            storedItems.set(i, source.copyWithCount(amountToMove));
            remaining -= amountToMove;
        }

        int inserted = source.getCount() - remaining;
        if (inserted > 0) {
            setStoredItems(lunchBag, storedItems);
        }
        return inserted;
    }

    private static ItemStack removeFirstStoredStack(ItemStack lunchBag) {
        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            ItemStack stack = storedItems.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            storedItems.set(i, ItemStack.EMPTY);
            setStoredItems(lunchBag, storedItems);
            return stack;
        }

        return ItemStack.EMPTY;
    }

    private static NonNullList<ItemStack> getStoredItems(ItemStack lunchBag) {
        ItemContainerContents contents = lunchBag.getOrDefault(
                ModDataComponents.LUNCH_BAG_CONTENTS.get(),
                ItemContainerContents.EMPTY);
        NonNullList<ItemStack> storedItems = NonNullList.withSize(LunchBagConstants.SLOT_COUNT, ItemStack.EMPTY);
        contents.copyInto(storedItems);
        return storedItems;
    }

    private static void setStoredItems(ItemStack lunchBag, NonNullList<ItemStack> storedItems) {
        lunchBag.set(ModDataComponents.LUNCH_BAG_CONTENTS.get(), ItemContainerContents.fromItems(storedItems));
    }

    private static int getSelectedSlotIndex(ItemStack lunchBag) {
        return Math.floorMod(
                lunchBag.getOrDefault(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0),
                LunchBagConstants.SLOT_COUNT);
    }
}
