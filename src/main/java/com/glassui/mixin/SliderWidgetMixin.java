/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.mixin;

import com.glassui.GlassConfig;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_357;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_357.class})
public abstract class SliderWidgetMixin {
    @Shadow
    protected double field_22753;

    @Inject(method={"method_48579"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void glassui$render(class_332 ctx, int mx, int my, float dt, CallbackInfo ci) {
        if (!GlassConfig.get().glassButtons) {
            return;
        }
        class_339 w = (class_339)this;
        float a = w.method_75798();
        int x = w.method_46426();
        int y = w.method_46427();
        int wd = w.method_25368();
        int h = w.method_25364();
        int r = Math.min(10, h / 2);
        boolean hot = w.method_49606() || w.method_25370();
        Glass.inset(ctx, x, y, wd, h, r, w.method_25370(), a);
        int hw = 10;
        int hx = x + 1 + Math.round((float)this.field_22753 * (float)(wd - 2 - hw));
        int fillW = Math.max(hw + 2, hx - x + hw);
        int top = Glass.c((int)(176.0f * a), 5939455);
        int bot = Glass.c((int)(176.0f * a), 3107808);
        Glass.fill(ctx, x + 1, y + 1, Math.min(wd - 2, fillW), h - 2, Math.max(1, r - 1), top, bot);
        Glass.fill(ctx, hx, y + 1, hw, h - 2, Math.max(1, r - 1), Glass.c((int)(255.0f * a), 0xFFFFFF), Glass.c((int)((float)(hot ? 255 : 232) * a), 14476532));
        Glass.ring(ctx, hx, y + 1, hw, h - 2, Math.max(1, r - 1), 0.5f, Glass.c((int)(96.0f * a), 0), Glass.c((int)(48.0f * a), 0));
        class_327 tr = class_310.method_1551().field_1772;
        int tw = Fx.w(tr, w.method_25369());
        Fx.draw(ctx, tr, w.method_25369(), x + (wd - tw) / 2, y + (h - 8) / 2, Glass.c(Math.max(64, (int)(255.0f * a)), 0xFFFFFF), true);
        ci.cancel();
    }
}

