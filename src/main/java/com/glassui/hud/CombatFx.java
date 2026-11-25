/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.hud.ComboTracker;
import com.glassui.ui.Glass;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_124;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_5498;
import net.minecraft.class_746;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;

public final class CombatFx {
    private static final float LIFE = 1.0f;
    private static final List<Pending> PENDING = new ArrayList<Pending>();
    private static final List<Num> NUMS = new ArrayList<Num>();
    private static final Random RND = new Random();
    private static class_243 eyePrev;
    private static class_243 eyeCur;
    private static float yawPrev;
    private static float yawCur;
    private static float pitchPrev;
    private static float pitchCur;
    private static long tickNs;

    private CombatFx() {
    }

    public static void onAttack(class_1309 le) {
        boolean combo;
        GlassConfig cfg = GlassConfig.get();
        boolean bl = combo = cfg.hudEnabled && cfg.combo.on;
        if (!combo && !cfg.damageNumbers) {
            return;
        }
        if (le.method_29504() || PENDING.size() > 16) {
            return;
        }
        for (Pending p : PENDING) {
            if (p.e != le) continue;
            return;
        }
        PENDING.add(new Pending(le));
    }

    public static void tick(class_310 mc) {
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1687 == null) {
            PENDING.clear();
            NUMS.clear();
            ComboTracker.reset();
            eyeCur = null;
            return;
        }
        eyePrev = eyeCur == null ? p.method_33571() : eyeCur;
        yawPrev = eyeCur == null ? p.method_36454() : yawCur;
        pitchPrev = eyeCur == null ? p.method_36455() : pitchCur;
        eyeCur = p.method_33571();
        yawCur = p.method_36454();
        pitchCur = p.method_36455();
        tickNs = System.nanoTime();
        for (int i = PENDING.size() - 1; i >= 0; --i) {
            Pending pd = PENDING.get(i);
            ++pd.age;
            float now = pd.e.method_6032();
            if (now < pd.before - 0.01f) {
                CombatFx.confirm(mc, pd.e, pd.before - now, now <= 0.0f);
                PENDING.remove(i);
                continue;
            }
            if (pd.age < 14 && !pd.e.method_31481()) continue;
            PENDING.remove(i);
        }
    }

    private static void confirm(class_310 mc, class_1309 e, float dmg, boolean kill) {
        GlassConfig cfg = GlassConfig.get();
        if (cfg.hudEnabled && cfg.combo.on) {
            ComboTracker.hit();
        }
        if (!cfg.damageNumbers) {
            return;
        }
        if (NUMS.size() >= 24) {
            NUMS.remove(0);
        }
        String s = CombatFx.fmt(dmg);
        int rgb = kill ? 16766474 : (dmg >= 14.0f ? 16731450 : (dmg >= 7.0f ? 16752394 : 0xFFFFFF));
        float mult = kill ? 1.35f : (dmg >= 14.0f ? 1.25f : 1.0f);
        NUMS.add(new Num(e.method_23317() + (RND.nextDouble() - 0.5) * 0.5, e.method_23318() + (double)e.method_17682() * 0.9, e.method_23321() + (RND.nextDouble() - 0.5) * 0.5, s, rgb, mult, (RND.nextFloat() - 0.5f) * 36.0f, mc.field_1772));
    }

    private static String fmt(float d) {
        float r = (float)Math.round(d * 10.0f) / 10.0f;
        return r == (float)((int)r) ? Integer.toString((int)r) : Float.toString(r);
    }

    public static void render(class_332 ctx, class_9779 tc) {
        if (NUMS.isEmpty() || eyeCur == null) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        class_327 tr = mc.field_1772;
        double td = Math.max(0.0, Math.min(1.0, (double)(System.nanoTime() - tickNs) / 5.0E7));
        class_243 eye = eyePrev.method_35590(eyeCur, td);
        float yaw = yawPrev + (yawCur - yawPrev) * (float)td;
        if (Math.abs(yawCur - yawPrev) > 180.0f) {
            yaw = yawCur;
        }
        float pitch = pitchPrev + (pitchCur - pitchPrev) * (float)td;
        double ry = Math.toRadians(yaw);
        double rp = Math.toRadians(pitch);
        double fx = -Math.sin(ry) * Math.cos(rp);
        double fy = -Math.sin(rp);
        double fz = Math.cos(ry) * Math.cos(rp);
        class_5498 persp = mc.field_1690.method_31044();
        double cx = eye.field_1352;
        double cy = eye.field_1351;
        double cz = eye.field_1350;
        if (!persp.method_31034()) {
            double back = persp.method_31035() ? -4.0 : 4.0;
            cx -= fx * back;
            cy -= fy * back;
            cz -= fz * back;
            if (persp.method_31035()) {
                fx = -fx;
                fy = -fy;
                fz = -fz;
            }
        }
        double rx = -Math.cos(Math.toRadians(persp.method_31035() ? (double)(yaw + 180.0f) : (double)yaw));
        double rz = -Math.sin(Math.toRadians(persp.method_31035() ? (double)(yaw + 180.0f) : (double)yaw));
        double ux = -rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy;
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        double tanHalf = Math.tan(Math.toRadians(((Integer)mc.field_1690.method_41808().method_41753()).intValue()) / 2.0);
        double aspect = (double)sw / (double)sh;
        long now = System.nanoTime();
        Matrix3x2fStack m = ctx.method_51448();
        for (int i = NUMS.size() - 1; i >= 0; --i) {
            Num n = NUMS.get(i);
            float age = (float)(now - n.born) / 1.0E9f;
            if (age >= 1.0f) {
                NUMS.remove(i);
                continue;
            }
            double dx = n.x - cx;
            float t = age / 1.0f;
            float rise = 1.0f - (1.0f - t) * (1.0f - t);
            double dy = n.y + 0.9 * (double)rise - cy;
            double dz = n.z - cz;
            double zc = dx * fx + dy * fy + dz * fz;
            if (zc < 0.2) continue;
            double xc = dx * rx + dz * rz;
            double yc = dx * ux + dy * uy + dz * uz;
            float sx = (float)((double)sw / 2.0 + xc / zc / (tanHalf * aspect) * (double)sw / 2.0) + n.drift * rise;
            float sy = (float)((double)sh / 2.0 - yc / zc / tanHalf * (double)sh / 2.0) - 10.0f * rise;
            if (sx < -60.0f || sx > (float)(sw + 60) || sy < -30.0f || sy > (float)(sh + 30)) continue;
            float pt = Math.min(1.0f, age / 0.14f) - 1.0f;
            float pop = 1.0f + 2.70158f * pt * pt * pt + 1.70158f * pt * pt;
            float sc = (float)Math.max(0.8, Math.min(2.0, 8.0 / zc)) * n.mult * pop;
            float a = t > 0.65f ? 1.0f - (t - 0.65f) / 0.35f : 1.0f;
            int A = (int)(255.0f * Math.max(0.0f, Math.min(1.0f, a)));
            if (A < 10 || sc <= 0.05f) continue;
            m.pushMatrix();
            m.translate(sx, sy);
            m.scale(sc, sc);
            ctx.method_51439(tr, n.text, -n.w / 2, -4, Glass.c(A, n.rgb), true);
            m.popMatrix();
        }
    }

    private static final class Pending {
        final class_1309 e;
        final float before;
        int age;

        Pending(class_1309 e) {
            this.e = e;
            this.before = e.method_6032();
        }
    }

    private static final class Num {
        final double x;
        final double y;
        final double z;
        final class_2561 text;
        final int w;
        final int rgb;
        final float mult;
        final float drift;
        final long born = System.nanoTime();

        Num(double x, double y, double z, String s, int rgb, float mult, float drift, class_327 tr) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rgb = rgb;
            this.mult = mult;
            this.drift = drift;
            this.text = class_2561.method_43470((String)s).method_27692(class_124.field_1067);
            this.w = tr.method_27525((class_5348)this.text);
        }
    }
}

