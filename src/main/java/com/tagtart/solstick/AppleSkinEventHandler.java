package com.tagtart.solstick;

import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;

public class AppleSkinEventHandler {

    @SubscribeEvent
    public void onFoodValuesEvent(FoodValuesEvent event) {
        PlayerStomach playerStomach = event.player.getData(ModAttachments.PLAYER_STOMACH.get());
        FoodProperties baseProperties = event.modifiedFoodProperties;
        if (baseProperties == null) {
            return;
        }

        ResourceLocation foodId = resolveTrackedFoodId(event.itemStack);
        float foodEffectiveness = playerStomach.getFoodEffectiveness(foodId);
        int newNutrition = Math.round(baseProperties.nutrition() * foodEffectiveness);
        float newSaturation = 0.0F;
        if (baseProperties.nutrition() > 0) {
            float saturationModifier = baseProperties.saturation() / (baseProperties.nutrition() * 2.0F);
            newSaturation = newNutrition * saturationModifier * 2.0F;
        }

        event.modifiedFoodProperties = new FoodProperties(
                newNutrition,
                newSaturation,
                baseProperties.canAlwaysEat(),
                baseProperties.eatSeconds(),
                baseProperties.usingConvertsTo(),
                baseProperties.effects());
    }

    private static ResourceLocation resolveTrackedFoodId(ItemStack stack) {
        if (stack.is(ModItems.LUNCH_BAG.get())) {
            ItemStack selectedFood = LunchBagItem.getSelectedFoodStack(stack);
            if (!selectedFood.isEmpty()) {
                return BuiltInRegistries.ITEM.getKey(selectedFood.getItem());
            }
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
