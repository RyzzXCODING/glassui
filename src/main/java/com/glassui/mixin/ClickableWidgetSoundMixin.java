/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.mixin;

import com.glassui.GlassConfig;
import com.glassui.ui.UiSound;
import net.minecraft.class_1144;
import net.minecraft.class_339;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_339.class})
public abstract class ClickableWidgetSoundMixin {
    @Inject(method={"method_25354"}, at={@At(value="HEAD")}, cancellable=true)
    private void glassui$pop(class_1144 sm, CallbackInfo ci) {
        if (!GlassConfig.get().uiSound) {
            return;
        }
        if (UiSound.click()) {
            ci.cancel();
        }
    }
}

