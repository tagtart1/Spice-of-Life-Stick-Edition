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
}
