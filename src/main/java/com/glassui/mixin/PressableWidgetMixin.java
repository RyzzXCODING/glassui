/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.mixin;

import com.glassui.GlassConfig;
import com.glassui.ui.CustomGlassWidget;
import com.glassui.ui.GlassButtonRenderer;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_4264;
import net.minecraft.class_5676;
import net.minecraft.class_7077;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4264.class})
public abstract class PressableWidgetMixin {
    @Shadow
    protected abstract void method_76256(class_332 ctx);

    @Inject(method={"method_48579"}, at={@At(value="HEAD")}, cancellable=true)
    private void glassui$render(class_332 ctx, int mx, int my, float dt, CallbackInfo ci) {
        Object self = (Object)this;
        if (self instanceof CustomGlassWidget) {
            CustomGlassWidget cg = (CustomGlassWidget)self;
            cg.renderGlass(ctx, mx, my, dt);
            this.method_76256(ctx);
            ci.cancel();
            return;
        }
        if (!GlassConfig.get().glassButtons) {
            return;
        }
        if (self instanceof class_7077) {
            return;
        }
        if (!(self instanceof class_4185) && !(self instanceof class_5676)) {
            return;
        }
        class_339 w = (class_339)self;
        if (w.method_25369().getString().isEmpty()) {
            return;
        }
        GlassButtonRenderer.render(ctx, w);
        this.method_76256(ctx);
        ci.cancel();
    }
}

