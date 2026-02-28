package com.tagtart.solstick.mixin;

import com.tagtart.solstick.SOLStick;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.server.level.ServerPlayer;

@Mixin(Player.class)
public abstract class PlayerFoodMixin {
        @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
        private void solstick$onApplyFoodValues(
                        FoodData foodData,
                        FoodProperties foodProperties) {

                Player player = (Player) (Object) this;
                if (!(player instanceof ServerPlayer serverPlayer)) {
                        return;
                }

                int nutrition = foodProperties.nutrition();
                float saturationModifier = foodProperties.saturation();

                SOLStick.LOGGER.info(
                                "Player {} ate food: nutrition={}, saturationModifier={}",
                                player.getGameProfile().getName(),
                                nutrition,
                                saturationModifier);

                foodData.eat(nutrition, saturationModifier);
        }
}
