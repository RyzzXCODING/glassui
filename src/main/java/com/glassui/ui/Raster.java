/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Raster {
    private static final Map<Key, Op[]> CACHE = new LinkedHashMap<Key, Op[]>(64, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Op[]> e) {
            return this.size() > 1024;
        }
    };
    private static final int AA_LEVELS = 5;

    private Raster() {
    }

    private static int mix(int a, int b, float t) {
        return Math.round((float)a + (float)(b - a) * t);
    }

    static int lerp(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return Raster.mix(c1 >>> 24, c2 >>> 24, t) << 24 | Raster.mix(c1 >> 16 & 0xFF, c2 >> 16 & 0xFF, t) << 16 | Raster.mix(c1 >> 8 & 0xFF, c2 >> 8 & 0xFF, t) << 8 | Raster.mix(c1 & 0xFF, c2 & 0xFF, t);
    }

    static double sd(double px, double py, double w, double h, double r) {
        double qx = Math.abs(px - w / 2.0) - (w / 2.0 - r);
        double qy = Math.abs(py - h / 2.0) - (h / 2.0 - r);
        double ox = Math.max(qx, 0.0);
        double oy = Math.max(qy, 0.0);
        return Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(qx, qy), 0.0) - r;
    }

    private static double cover(double px, double py, int w, int h, int r, int t) {
        double d = Raster.sd(px, py, w, h, r);
        if (t > 0) {
            d = Math.abs(d + (double)t / 2.0) - (double)t / 2.0;
        }
        return Math.max(0.0, Math.min(1.0, 0.5 - d));
    }

    private static int[] op(int x0, int y0, int x1, int y1, int a) {
        return new int[]{x0, y0, x1, y1, a, y0, y0};
    }

    private static Op[] build(int w, int h, int r, int t, boolean hard) {
        ArrayList<int[]> out = new ArrayList<int[]>();
        int half = (w + 1) / 2;
        int[] row = new int[half];
        ArrayList prev = new ArrayList();
        for (int i = 0; i < r; ++i) {
            int c;
            int j = h - 1 - i;
            double py = (double)i + 0.5;
            int lim = Math.min(r, half);
            for (int c2 = 0; c2 < lim; ++c2) {
                row[c2] = Raster.alpha(Raster.cover((double)c2 + 0.5, py, w, h, r, t), hard);
            }
            if (half > lim) {
                int mid = Raster.alpha(Raster.cover((double)lim + 0.5, py, w, h, r, t), hard);
                for (c = lim; c < half; ++c) {
                    row[c] = mid;
                }
            }
            ArrayList<Grp> next = new ArrayList<Grp>();
            c = 0;
            while (c < half) {
                int e;
                int a = row[c];
                for (e = c + 1; e < half && row[e] == a; ++e) {
                }
                if (a > 0) {
                    Grp g = null;
                    for (Grp p : prev) {
                        if (p.c != c || p.e != e || p.a != a) continue;
                        g = p;
                        break;
                    }
                    if (g != null) {
                        g.extend();
                    } else {
                        int[][] bt;
                        int[][] tp;
                        if (e >= half) {
                            tp = new int[][]{Raster.op(c, i, w - c, i + 1, a)};
                            bt = new int[][]{Raster.op(c, j, w - c, j + 1, a)};
                        } else {
                            tp = new int[][]{Raster.op(c, i, e, i + 1, a), Raster.op(w - e, i, w - c, i + 1, a)};
                            bt = new int[][]{Raster.op(c, j, e, j + 1, a), Raster.op(w - e, j, w - c, j + 1, a)};
                        }
                        for (int[] o : tp) {
                            out.add(o);
                        }
                        for (int[] o : bt) {
                            out.add(o);
                        }
                        g = new Grp(c, e, a, tp, bt);
                    }
                    next.add(g);
                }
                c = e;
            }
            prev = next;
        }
        if (h - 2 * r > 0) {
            if (t == 0) {
                out.add(new int[]{0, r, w, h - r, 255, r, h - r});
            } else {
                int tt = Math.min(t, w / 2);
                out.add(new int[]{0, r, tt, h - r, 255, r, h - r});
                out.add(new int[]{w - tt, r, w, h - r, 255, r, h - r});
            }
        }
        Op[] res = new Op[out.size()];
        for (int k = 0; k < res.length; ++k) {
            int[] o = (int[])out.get(k);
            res[k] = new Op(o[0], o[1], o[2], o[3], o[4], o[5], o[6]);
        }
        return res;
    }

    private static int alpha(double cov, boolean hard) {
        if (hard) {
            return cov >= 0.5 ? 255 : 0;
        }
        return (int)Math.round(cov * 5.0) * 255 / 5;
    }

    public static int opCount(int w, int h, int r, int t) {
        return Raster.opCount(w, h, r, t, false);
    }

    public static int opCount(int w, int h, int r, int t, boolean hard) {
        r = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        return Raster.build(w, h, r, t, hard).length;
    }

    public static void roundRect(Sink s, int x, int y, int w, int h, int r, int t, int top, int bot) {
        Raster.roundRect(s, x, y, w, h, r, t, top, bot, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void roundRect(Sink s, int x, int y, int w, int h, int r, int t, int top, int bot, boolean hard) {
        Op[] ops;
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((r = Math.max(0, Math.min(r, Math.min(w, h) / 2))) == 0) {
            if (t == 0) {
                s.fill(x, y, x + w, y + h, top, bot);
            } else {
                int tt = Math.max(1, t);
                s.fill(x, y, x + w, y + tt, top, Raster.lerp(top, bot, (float)tt / (float)h));
                s.fill(x, y + h - tt, x + w, y + h, Raster.lerp(top, bot, (float)(h - tt) / (float)h), bot);
                if (h > 2 * tt) {
                    float f0 = (float)tt / (float)h;
                    float f1 = (float)(h - tt) / (float)h;
                    s.fill(x, y + tt, x + tt, y + h - tt, Raster.lerp(top, bot, f0), Raster.lerp(top, bot, f1));
                    s.fill(x + w - tt, y + tt, x + w, y + h - tt, Raster.lerp(top, bot, f0), Raster.lerp(top, bot, f1));
                }
            }
            return;
        }
        Key k = new Key(w, h, r, t, hard);
        Map<Key, Op[]> f1 = CACHE;
        synchronized (f1) {
            ops = CACHE.computeIfAbsent(k, kk -> Raster.build(Raster.w0(kk), kk.h, kk.r, kk.t, kk.hard));
        }
        boolean flat = top == bot;
        int tA = top >>> 24;
        int tRgb = top & 0xFFFFFF;
        float hh = Math.max(1, h);
        for (Op o : ops) {
            if (flat) {
                int a = Math.round((float)(tA * o.a) / 255.0f);
                if (a == 0) continue;
                s.fill(x + o.x0, y + o.y0, x + o.x1, y + o.y1, a << 24 | tRgb, a << 24 | tRgb);
                continue;
            }
            float f0 = ((float)o.r0 + 0.5f) / hh;
            float f12 = ((float)o.r1 + (o.r1 == o.r0 ? 0.5f : 0.0f)) / hh;
            int c0 = Raster.lerp(top, bot, f0);
            int c1 = o.r0 == o.r1 ? c0 : Raster.lerp(top, bot, f12);
            c0 = Math.round((float)((c0 >>> 24) * o.a) / 255.0f) << 24 | c0 & 0xFFFFFF;
            c1 = Math.round((float)((c1 >>> 24) * o.a) / 255.0f) << 24 | c1 & 0xFFFFFF;
            if (c0 >>> 24 == 0 && c1 >>> 24 == 0) continue;
            s.fill(x + o.x0, y + o.y0, x + o.x1, y + o.y1, c0, c1);
        }
    }

    private static int w0(Key k) {
        return k.w;
    }

    private static final class Grp {
        final int c;
        final int e;
        final int a;
        final int[][] top;
        final int[][] bot;

        Grp(int c, int e, int a, int[][] top, int[][] bot) {
            this.c = c;
            this.e = e;
            this.a = a;
            this.top = top;
            this.bot = bot;
        }

        void extend() {
            for (int[] o : this.top) {
                o[3] = o[3] + 1;
                o[5] = o[1];
                o[6] = o[3];
            }
            for (int[] o : this.bot) {
                o[1] = o[1] - 1;
                o[5] = o[1];
                o[6] = o[3];
            }
        }
    }

    record Op(int x0, int y0, int x1, int y1, int a, int r0, int r1) {
    }

    public static interface Sink {
        public void fill(int var1, int var2, int var3, int var4, int var5, int var6);
    }

    private record Key(int w, int h, int r, int t, boolean hard) {
    }
}

