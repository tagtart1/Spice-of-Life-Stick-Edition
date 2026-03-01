package com.tagtart.solstick.mixin;

import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.SOLStick;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerFoodMixin {
        @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
        private void solstick$onPlayerEat(
                        FoodData foodData,
                        FoodProperties invokedFoodProperties,
                        Level level,
                        ItemStack food,
                        FoodProperties methodFoodProperties) {

                Player player = (Player) (Object) this;

                ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(food.getItem());
                int nutrition = invokedFoodProperties.nutrition();

                PlayerStomach playerStomach = player.getData(ModAttachments.PLAYER_STOMACH.get());
                float foodEffectiveness = playerStomach.getFoodEffectiveness(foodId);
                int newNutrition = Math.round(nutrition * foodEffectiveness);
                float newSaturation = 0.0F;
                if (nutrition > 0) {
                        // FoodProperties stores absolute saturation points; keep the original modifier ratio.
                        float saturationModifier = invokedFoodProperties.saturation() / (nutrition * 2.0F);
                        newSaturation = newNutrition * saturationModifier * 2.0F;
                }

                FoodProperties adjustedFoodProperties = new FoodProperties(
                                newNutrition,
                                newSaturation,
                                invokedFoodProperties.canAlwaysEat(),
                                invokedFoodProperties.eatSeconds(),
                                invokedFoodProperties.usingConvertsTo(),
                                invokedFoodProperties.effects());

                // Write the food to the stomach, server only
                if (player instanceof ServerPlayer serverPlayer) {
                        playerStomach.addFood(foodId);
                        serverPlayer.setData(ModAttachments.PLAYER_STOMACH.get(), playerStomach);
                }

                SOLStick.LOGGER.info(
                                "Player {} ate {}: nutrition={}, saturation={}",
                                player.getGameProfile().getName(),
                                foodId,
                                newNutrition,
                                newSaturation);

                // Use the FoodProperties overload to avoid applying saturation conversion twice.
                foodData.eat(adjustedFoodProperties);
        }
}
