package com.tagtart.solstick.mixin;

import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityUseEffectsMixin {

    @ModifyVariable(
            method = "triggerItemUseEffects(Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private ItemStack solstick$useSelectedFoodForUseEffects(ItemStack stack) {
        if (!(stack.getItem() instanceof LunchBagItem) || !LunchBagItem.isOpen(stack)) {
            return stack;
        }

        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ItemStack selectedFood = LunchBagItem.getSelectedFoodStack(stack, livingEntity);
        return selectedFood.isEmpty() ? stack : selectedFood;
    }
}
