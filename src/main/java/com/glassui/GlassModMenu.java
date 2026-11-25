/*
 * Decompiled with CFR 0.152.
 */
package com.glassui;

import com.glassui.ui.StreamHudConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class GlassModMenu
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return StreamHudConfigScreen::new;
    }
}

