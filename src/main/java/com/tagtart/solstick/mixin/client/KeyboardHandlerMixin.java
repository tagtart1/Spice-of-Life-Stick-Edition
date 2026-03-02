package com.tagtart.solstick.mixin.client;

import com.tagtart.solstick.client.overlay.LunchBagOverlayInputHandler;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void solstick$onKeyPress(
            long windowPointer,
            int key,
            int scanCode,
            int action,
            int modifiers,
            CallbackInfo ci) {
        if (LunchBagOverlayInputHandler.onKeyboardKeyPress(key, action)) {
            ci.cancel();
        }
    }
}
