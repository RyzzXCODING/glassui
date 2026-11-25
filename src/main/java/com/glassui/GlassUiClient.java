/*
 * Decompiled with CFR 0.152.
 */
package com.glassui;

import com.glassui.GlassConfig;
import com.glassui.hud.ChatSound;
import com.glassui.hud.CombatFx;
import com.glassui.hud.GlassHotbar;
import com.glassui.hud.MediaControl;
import com.glassui.hud.MusicToggle;
import com.glassui.hud.StreamHud;
import com.glassui.hud.StreamHudRenderer;
import com.glassui.music.LocalPlayer;
import com.glassui.music.MusicHub;
import com.glassui.ui.MusicScreen;
import com.glassui.ui.StreamHudConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_746;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlassUiClient
implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"glassui");
    private static boolean keyDown;
    private static boolean musicKeyDown;
    private static boolean cassetteKeyDown;
    private static boolean skipKeyDown;
    private static boolean volChanged;
    private static int volHold;

    private static boolean hideVanillaBars() {
        GlassConfig cfg = GlassConfig.get();
        class_746 p = class_310.method_1551().field_1724;
        return cfg.hideVanillaBars && cfg.hudEnabled && cfg.profile.on && p != null && !p.method_68878() && !p.method_7325();
    }

    public void onInitializeClient() {
        GlassConfig.load();
        HudElementRegistry.attachElementAfter((class_2960)VanillaHudElements.MISC_OVERLAYS, (class_2960)class_2960.method_60655((String)"glassui", (String)"stream_hud"), StreamHudRenderer::render);
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.HOTBAR, orig -> (ctx, tc) -> {
            if (!GlassHotbar.render(ctx)) {
                orig.render(ctx, tc);
            }
        });
        for (class_2960 id : new class_2960[]{VanillaHudElements.HEALTH_BAR, VanillaHudElements.ARMOR_BAR, VanillaHudElements.FOOD_BAR, VanillaHudElements.AIR_BAR}) {
            HudElementRegistry.replaceElement((class_2960)id, orig -> (ctx, tc) -> {
                if (!GlassUiClient.hideVanillaBars()) {
                    orig.render(ctx, tc);
                }
            });
        }
        HudElementRegistry.attachElementAfter((class_2960)VanillaHudElements.MISC_OVERLAYS, (class_2960)class_2960.method_60655((String)"glassui", (String)"damage_numbers"), CombatFx::render);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            class_1309 le;
            if (world.method_8608() && entity instanceof class_1309 && !((le = (class_1309)entity) instanceof class_1531) && player == class_310.method_1551().field_1724) {
                CombatFx.onAttack(le);
            }
            return class_1269.field_5811;
        });
        StreamHud.startup();
        LocalPlayer.probeAsync();
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            StreamHud.stop();
            LocalPlayer.stop();
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean cdown;
            boolean mdown;
            boolean down;
            ChatSound.tick();
            CombatFx.tick(mc);
            GlassConfig cfg = GlassConfig.get();
            boolean bl = down = cfg.menuKey > 0 && mc.field_1755 == null && mc.field_1687 != null && GLFW.glfwGetKey((long)GLFW.glfwGetCurrentContext(), (int)cfg.menuKey) == 1;
            if (down && !keyDown) {
                mc.method_1507((class_437)new StreamHudConfigScreen(null));
            }
            keyDown = down;
            boolean bl2 = mdown = cfg.musicKey > 0 && mc.field_1755 == null && mc.field_1687 != null && GLFW.glfwGetKey((long)GLFW.glfwGetCurrentContext(), (int)cfg.musicKey) == 1;
            if (mdown && !musicKeyDown) {
                mc.method_1507((class_437)new MusicScreen(null));
            }
            musicKeyDown = mdown;
            boolean bl3 = cdown = cfg.cassetteKey > 0 && mc.field_1755 == null && mc.field_1687 != null && GLFW.glfwGetKey((long)GLFW.glfwGetCurrentContext(), (int)cfg.cassetteKey) == 1;
            if (cdown && !cassetteKeyDown) {
                MusicToggle.toggle();
            }
            cassetteKeyDown = cdown;
            GlassUiClient.arrowKeys(mc, cfg);
        });
    }

    private static void arrowKeys(class_310 mc, GlassConfig cfg) {
        int dir;
        boolean dn;
        long win = GLFW.glfwGetCurrentContext();
        boolean active = cfg.arrowKeys && mc.field_1755 == null && mc.field_1687 != null;
        boolean right = active && GLFW.glfwGetKey((long)win, (int)262) == 1;
        boolean up = active && GLFW.glfwGetKey((long)win, (int)265) == 1;
        boolean bl = dn = active && GLFW.glfwGetKey((long)win, (int)264) == 1;
        if (right && !skipKeyDown && StreamHud.DATA.song != null) {
            MediaControl.send(MediaControl.Cmd.NEXT);
            mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)"Next song"), false);
        }
        skipKeyDown = right;
        int n = up == dn ? 0 : (dir = up ? 1 : -1);
        if (dir == 0) {
            volHold = 0;
            if (volChanged) {
                volChanged = false;
                MusicHub.commitVolume();
            }
            return;
        }
        boolean step = volHold == 0 || volHold >= 10 && (volHold - 10) % 2 == 0;
        ++volHold;
        if (!step) {
            return;
        }
        if (!MusicHub.canVolume()) {
            if (volHold == 1) {
                mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)"Volume can't be changed for this source"), false);
            }
            return;
        }
        MusicHub.setVolume(MusicHub.volume() + dir * 5);
        volChanged = true;
        mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)("Music volume: " + MusicHub.volume() + "%")), false);
    }
}

