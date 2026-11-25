/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import net.minecraft.class_7532;
import net.minecraft.class_8685;

public final class PlayerHead {
    private PlayerHead() {
    }

    public static boolean draw(class_332 ctx, int x, int y, int size) {
        class_746 p = class_310.method_1551().field_1724;
        if (p == null) {
            return false;
        }
        class_7532.method_52722((class_332)ctx, (class_8685)p.method_52814(), (int)x, (int)y, (int)size);
        return true;
    }
}

