/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;

public final class Spring {
    public float pos;
    public float vel;
    public float target;
    private final float k;
    private final float d;

    public Spring(float start, float target, float stiffness, float damping) {
        this.pos = start;
        this.target = target;
        this.k = stiffness;
        this.d = damping;
    }

    public void step(float dt) {
        if ((dt = Math.min(dt, 0.1f)) <= 0.0f) {
            return;
        }
        float dd = GlassConfig.get().jelly ? this.d * 0.8f : this.d;
        int n = Math.max(2, Math.min(24, (int)Math.ceil(dt * 240.0f)));
        float h = dt / (float)n;
        for (int i = 0; i < n; ++i) {
            float a = -this.k * (this.pos - this.target) - dd * this.vel;
            this.vel += a * h;
            this.pos += this.vel * h;
        }
    }

    public float squash() {
        if (!GlassConfig.get().jelly) {
            return 0.0f;
        }
        return Math.max(-0.05f, Math.min(0.05f, this.vel * 0.012f));
    }
}

