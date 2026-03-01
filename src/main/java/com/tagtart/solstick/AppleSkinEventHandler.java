package com.tagtart.solstick;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;

public class AppleSkinEventHandler {

    @SubscribeEvent
    public void onFoodValuesEvent(FoodValuesEvent event) {
        PlayerStomach playerStomach = event.player.getData(ModAttachments.PLAYER_STOMACH.get());

        ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(event.itemStack.getItem());
        float foodEffectiveness = playerStomach.getFoodEffectiveness(foodId);
        FoodProperties baseProperties = event.modifiedFoodProperties;
        int newNutrition = Math.round(baseProperties.nutrition() * foodEffectiveness);

        // Match the mixin behavior: scale nutrition and keep saturation value unchanged.
        event.modifiedFoodProperties = new FoodProperties(
                newNutrition,
                baseProperties.saturation(),
                baseProperties.canAlwaysEat(),
                baseProperties.eatSeconds(),
                baseProperties.usingConvertsTo(),
                baseProperties.effects());

    }
}
