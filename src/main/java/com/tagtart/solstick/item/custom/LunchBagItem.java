package com.tagtart.solstick.item.custom;

import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.tooltip.LunchBagTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
        ItemStack selectedFood = getSelectedFoodStack(heldItem, player);
        FoodProperties selectedFoodProperties = selectedFood.getFoodProperties(player);
        if (!selectedFood.isEmpty() && selectedFoodProperties != null
                && player.canEat(selectedFoodProperties.canAlwaysEat())) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(heldItem);
        }

        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    @Nullable
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        ItemStack selectedFood = getSelectedFoodStack(stack, entity);
        return selectedFood.isEmpty() ? null : selectedFood.getFoodProperties(entity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack selectedFood = getSelectedFoodStack(stack, entity);
        return selectedFood.isEmpty() ? super.getUseDuration(stack, entity) : selectedFood.getUseDuration(entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ItemStack selectedFood = getSelectedFoodStack(stack, null);
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
        int selectedSlot = resolveActiveFoodSlotIndex(stack, livingEntity);
        if (selectedSlot < 0) {
            return stack;
        }
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

    public static ItemStack getSelectedFoodStack(ItemStack lunchBag, @Nullable LivingEntity entity) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return ItemStack.EMPTY;
        }

        int activeSlot = resolveActiveFoodSlotIndex(lunchBag, entity);
        if (activeSlot < 0) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        ItemStack selectedFood = storedItems.get(activeSlot);
        return isFood(selectedFood) ? selectedFood : ItemStack.EMPTY;
    }

    public static boolean hasFoodAtSlot(ItemStack lunchBag, int slotIndex) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return false;
        }
        if (slotIndex < 0 || slotIndex >= LunchBagConstants.SLOT_COUNT) {
            return false;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        return isFood(storedItems.get(slotIndex));
    }

    public static int getNextSelectableSlot(ItemStack lunchBag, int currentSlotIndex, int direction) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return -1;
        }

        int step = direction >= 0 ? 1 : -1;
        int nextSlot = normalizeSelectableSlot(currentSlotIndex);
        boolean hiddenBestAvailable = hasAnyFood(lunchBag);
        for (int i = 0; i < LunchBagConstants.TOTAL_SELECTABLE_SLOTS; i++) {
            nextSlot = normalizeSelectableSlot(nextSlot + step);
            if (isHiddenBestSlot(nextSlot)) {
                if (hiddenBestAvailable) {
                    return nextSlot;
                }
                continue;
            }
            if (hasFoodAtSlot(lunchBag, nextSlot)) {
                return nextSlot;
            }
        }

        return -1;
    }

    public static boolean isHiddenBestSlot(int slotIndex) {
        return normalizeSelectableSlot(slotIndex) == LunchBagConstants.HIDDEN_BEST_SLOT_INDEX;
    }

    public static int normalizeSelectableSlot(int slotIndex) {
        return Math.floorMod(slotIndex, LunchBagConstants.TOTAL_SELECTABLE_SLOTS);
    }

    public static int getSelectedSlotIndex(ItemStack lunchBag) {
        int selected = normalizeSelectableSlot(
                lunchBag.getOrDefault(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0));
        if (isHiddenBestSlot(selected) && !hasAnyFood(lunchBag)) {
            return 0;
        }
        return selected;
    }

    public static boolean hasAnyFood(ItemStack lunchBag) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return false;
        }
        ItemContainerContents contents = lunchBag.getOrDefault(
                ModDataComponents.LUNCH_BAG_CONTENTS.get(),
                ItemContainerContents.EMPTY);
        return !contents.equals(ItemContainerContents.EMPTY);
    }

    private static int resolveActiveFoodSlotIndex(ItemStack lunchBag, @Nullable LivingEntity entity) {
        int selectedSlot = getSelectedSlotIndex(lunchBag);
        if (isHiddenBestSlot(selectedSlot)) {
            return getBestFoodSlotIndex(lunchBag, entity);
        }
        return hasFoodAtSlot(lunchBag, selectedSlot) ? selectedSlot : -1;
    }

    public static int getBestFoodSlotIndex(ItemStack lunchBag, @Nullable LivingEntity entity) {
        if (!(lunchBag.getItem() instanceof LunchBagItem)) {
            return -1;
        }

        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        int bestSlot = -1;
        int bestNutrition = Integer.MIN_VALUE;
        float bestSaturation = Float.NEGATIVE_INFINITY;
        int bestCount = Integer.MIN_VALUE;

        for (int slot = 0; slot < LunchBagConstants.SLOT_COUNT; slot++) {
            ItemStack stack = storedItems.get(slot);
            if (!isFood(stack)) {
                continue;
            }

            FoodProperties foodProperties = stack.getFoodProperties(entity);
            if (foodProperties == null) {
                continue;
            }

            float effectiveness = getFoodEffectiveness(stack, entity);
            int modifiedNutrition = computeModifiedNutrition(foodProperties, effectiveness);
            float modifiedSaturation = computeModifiedSaturation(foodProperties, modifiedNutrition);
            int stackCount = stack.getCount();
            if (modifiedNutrition > bestNutrition
                    || (modifiedNutrition == bestNutrition && Float.compare(modifiedSaturation, bestSaturation) > 0)
                    || (modifiedNutrition == bestNutrition
                            && Float.compare(modifiedSaturation, bestSaturation) == 0
                            && stackCount > bestCount)) {
                bestNutrition = modifiedNutrition;
                bestSaturation = modifiedSaturation;
                bestCount = stackCount;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private static float getFoodEffectiveness(ItemStack foodStack, @Nullable LivingEntity entity) {
        float effectiveness = 1.0F;
        if (entity instanceof Player player) {
            PlayerStomach stomach = player.getData(ModAttachments.PLAYER_STOMACH.get());
            ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(foodStack.getItem());
            effectiveness = stomach.getFoodEffectiveness(foodId);
        }
        return effectiveness;
    }

    private static int computeModifiedNutrition(FoodProperties foodProperties, float effectiveness) {
        return Math.round(foodProperties.nutrition() * effectiveness);
    }

    private static float computeModifiedSaturation(FoodProperties foodProperties, int modifiedNutrition) {
        int baseNutrition = foodProperties.nutrition();
        if (baseNutrition <= 0) {
            return 0.0F;
        }
        float saturationModifier = foodProperties.saturation() / (baseNutrition * 2.0F);
        return modifiedNutrition * saturationModifier * 2.0F;
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
        boolean hasStoredItems = false;
        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            if (!storedItems.get(i).isEmpty()) {
                hasStoredItems = true;
                break;
            }
        }
        if (!hasStoredItems) {
            lunchBag.set(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0);
        }
    }

    /* Storage capacity bar */
    @Override
    public boolean isBarVisible(ItemStack lunchBag) {
        return hasAnyFood(lunchBag);
    }

    @Override
    public int getBarWidth(ItemStack lunchBag) {
        NonNullList<ItemStack> storedItems = getStoredItems(lunchBag);
        int filledSlots = 0;
        for (ItemStack stack : storedItems) {
            if (!stack.isEmpty()) {
                filledSlots++;
            }
        }
        float maxBarWidth = 13.0F; // 13 pixels wide, native vanilla behavior
        return Math.round(maxBarWidth * (float) filledSlots / LunchBagConstants.SLOT_COUNT);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x5555FF; // Classic blue
    }
}
