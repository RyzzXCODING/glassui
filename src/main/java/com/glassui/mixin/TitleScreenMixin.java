/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.mixin;

import com.glassui.GlassConfig;
import com.glassui.ui.DockMenu;
import java.util.ArrayList;
import net.minecraft.class_2561;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_7077;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_442.class})
public abstract class TitleScreenMixin
extends class_437 {
    protected TitleScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void glassui$dock(CallbackInfo ci) {
        if (!GlassConfig.get().dockMenu) {
            return;
        }
        ArrayList<class_364> remove = new ArrayList<class_364>();
        for (class_364 e : this.method_25396()) {
            if (!(e instanceof class_4185) || e instanceof class_7077) continue;
            remove.add(e);
        }
        for (class_364 e : remove) {
            this.method_37066(e);
        }
        DockMenu.Layout layout = DockMenu.create(this);
        this.method_37060(layout.bar());
        for (class_4185 b : layout.items()) {
            this.method_37063(b);
        }
    }
}

