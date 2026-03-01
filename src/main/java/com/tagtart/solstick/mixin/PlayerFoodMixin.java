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
                float saturationModifier = invokedFoodProperties.saturation();

                PlayerStomach playerStomach = player.getData(ModAttachments.PLAYER_STOMACH.get());
                int newNutrition = Math.round(nutrition * playerStomach.getFoodEffectiveness(foodId));

                // Write the food to the stomach, server only
                if (player instanceof ServerPlayer serverPlayer) {
                        playerStomach.addFood(foodId);
                        serverPlayer.setData(ModAttachments.PLAYER_STOMACH.get(), playerStomach);
                }

                SOLStick.LOGGER.info(
                                "Player {} ate {}: nutrition={}, saturationModifier={}",
                                player.getGameProfile().getName(),
                                foodId,
                                newNutrition,
                                saturationModifier);

                // Replace the FoodProperties overload with explicit values.
                foodData.eat(newNutrition, saturationModifier);
        }
}
