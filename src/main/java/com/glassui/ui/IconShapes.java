/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class IconShapes {
    private static final Map<Integer, float[][]> CACHE = new HashMap<Integer, float[][]>();
    public static final int LEVELS = 16;
    private static final Map<Integer, int[][]> OPS = new HashMap<Integer, int[][]>();

    private IconShapes() {
    }

    public static synchronized float[][] coverage(Shape s, int size) {
        size = Math.max(4, Math.min(256, size));
        int key = s.ordinal() * 1024 + size;
        float[][] c = CACHE.get(key);
        if (c == null) {
            c = IconShapes.build(s, size);
            CACHE.put(key, c);
        }
        return c;
    }

    public static synchronized int[][] ops(Shape s, int size) {
        return IconShapes.ops(s, size, false);
    }

    public static synchronized int[][] ops(Shape s, int size, boolean binary) {
        int key = s.ordinal() * 1024 + Math.max(4, Math.min(256, size)) + (binary ? 0x100000 : 0);
        int[][] o = OPS.get(key);
        if (o == null) {
            o = IconShapes.buildOps(IconShapes.coverage(s, size), binary);
            OPS.put(key, o);
        }
        return o;
    }

    private static int lvl(float cov, boolean binary) {
        return binary ? (cov >= 0.5f ? 16 : 0) : Math.round(cov * 16.0f);
    }

    private static int[][] buildOps(float[][] cov, boolean binary) {
        int n = cov.length;
        ArrayList<int[]> list = new ArrayList<int[]>();
        HashMap open = new HashMap();
        for (int r = 0; r < n; ++r) {
            HashMap<Integer, int[]> next = new HashMap<Integer, int[]>();
            int c = 0;
            while (c < n) {
                int e;
                int lv = IconShapes.lvl(cov[r][c], binary);
                if (lv <= 0) {
                    ++c;
                    continue;
                }
                for (e = c + 1; e < n && IconShapes.lvl(cov[r][e], binary) == lv; ++e) {
                }
                int k = (c * 512 + e) * 64 + lv;
                int[] op = (int[])open.get(k);
                if (op != null) {
                    op[3] = r + 1;
                } else {
                    op = new int[]{c, r, e, r + 1, lv};
                    list.add(op);
                }
                next.put(k, op);
                c = e;
            }
            open = next;
        }
        return (int[][])list.toArray((T[])new int[0][]);
    }

    private static float[][] build(Shape s, int n) {
        float[][] c = new float[n][n];
        int ss = n >= 40 ? 3 : 5;
        for (int r = 0; r < n; ++r) {
            for (int col = 0; col < n; ++col) {
                int hit = 0;
                for (int sy = 0; sy < ss; ++sy) {
                    for (int sx = 0; sx < ss; ++sx) {
                        double u = ((double)col + ((double)sx + 0.5) / (double)ss) / (double)n;
                        double v = ((double)r + ((double)sy + 0.5) / (double)ss) / (double)n;
                        if (!IconShapes.inside(s, u, v)) continue;
                        ++hit;
                    }
                }
                c[r][col] = (float)hit / (float)(ss * ss);
            }
        }
        return c;
    }

    public static boolean inside(Shape s, double u, double v) {
        switch (s.ordinal()) {
            case 1: {
                double x = (u - 0.5) * 2.5;
                double y = (0.5 - v) * 2.5 + 0.12;
                double a = x * x + y * y - 1.0;
                return a * a * a - x * x * y * y * y <= 0.0;
            }
            case 0: {
                double x = (u - 0.5) * 2.0;
                double y = (v - 0.5) * 2.0;
                double ext = 0.66 * (1.0 - x * x);
                boolean outer = Math.abs(y) <= ext;
                boolean inner = Math.abs(y) <= ext - 0.15;
                return outer && !inner || x * x + y * y < 0.0625;
            }
            case 2: {
                return IconShapes.rrect(u, v, 0.05, 0.1, 0.95, 0.74, 0.22) || IconShapes.rtri(u, v, 0.03, 0.18, 0.62, 0.2, 0.96, 0.46, 0.62);
            }
            case 3: {
                boolean h1 = IconShapes.circ(u, v, 0.27, 0.78, 0.18);
                boolean h2 = IconShapes.circ(u, v, 0.74, 0.7, 0.18);
                boolean s1 = IconShapes.rrect(u, v, 0.36, 0.16, 0.47, 0.78, 0.03);
                boolean s2 = IconShapes.rrect(u, v, 0.83, 0.07, 0.94, 0.7, 0.03);
                double top = 0.16 + (u - 0.36) * -0.15517241379310345;
                boolean beam = u >= 0.36 && u <= 0.94 && v >= top && v <= top + 0.15;
                return h1 || h2 || s1 || s2 || beam;
            }
            case 4: {
                return IconShapes.rtri(u, v, 0.07, 0.24, 0.12, 0.24, 0.88, 0.88, 0.5);
            }
            case 5: {
                return IconShapes.rrect(u, v, 0.18, 0.12, 0.4, 0.88, 0.08) || IconShapes.rrect(u, v, 0.6, 0.12, 0.82, 0.88, 0.08);
            }
            case 6: {
                return IconShapes.rtri(u, v, 0.06, 0.12, 0.14, 0.12, 0.86, 0.66, 0.5) || IconShapes.rrect(u, v, 0.72, 0.14, 0.88, 0.86, 0.04);
            }
            case 7: {
                return IconShapes.inside(Shape.NEXT, 1.0 - u, v);
            }
            case 8: {
                double dx = u - 0.5;
                double dy = v - 0.5;
                double r = Math.hypot(dx, dy);
                boolean ring = Math.abs(r - 0.41) <= 0.037;
                double f = Math.hypot(dx / 0.19, dy / 0.41);
                boolean mer = Math.abs(f - 1.0) <= 0.2 && r <= 0.43;
                boolean eq = Math.abs(dy) <= 0.032 && r <= 0.43;
                boolean lat = (Math.abs(dy - 0.2) <= 0.03 || Math.abs(dy + 0.2) <= 0.03) && Math.abs(dx) <= Math.sqrt(Math.max(0.0, 0.12809999999999996));
                return ring || mer || eq || lat;
            }
            case 9: {
                boolean hole;
                double dx = u - 0.5;
                double dy = v - 0.5;
                double r = Math.hypot(dx, dy);
                boolean bl = hole = r < 0.16;
                if (hole) {
                    return false;
                }
                if (r <= 0.33) {
                    return true;
                }
                if (r > 0.45) {
                    return false;
                }
                double step = 0.7853981633974483;
                double ang = Math.atan2(dy, dx);
                double m = Math.abs((ang % step + step) % step - step / 2.0);
                return step / 2.0 - m <= 0.2;
            }
            case 10: {
                double dx = u - 0.5;
                double dy = v - 0.5;
                double r = Math.hypot(dx, dy);
                if (r < 0.105) {
                    return true;
                }
                double ang = Math.abs(Math.atan2(dy, Math.abs(dx)));
                boolean side = ang < Math.toRadians(52.0);
                return side && (Math.abs(r - 0.26) <= 0.036 || Math.abs(r - 0.42) <= 0.036);
            }
            case 11: {
                return IconShapes.rrect(u, v, 0.1, 0.1, 0.46, 0.46, 0.11) || IconShapes.rrect(u, v, 0.54, 0.1, 0.9, 0.46, 0.11) || IconShapes.rrect(u, v, 0.1, 0.54, 0.46, 0.9, 0.11) || IconShapes.rrect(u, v, 0.54, 0.54, 0.9, 0.9, 0.11);
            }
            case 12: {
                double dx = u - 0.5;
                double dy = v - 0.56;
                double r = Math.hypot(dx, dy);
                double angUp = Math.abs(Math.atan2(dx, -dy));
                boolean ring = Math.abs(r - 0.34) <= 0.048 && angUp > Math.toRadians(36.0);
                boolean bar = IconShapes.rrect(u, v, 0.455, 0.06, 0.545, 0.52, 0.045);
                return ring || bar;
            }
            case 13: {
                boolean head = IconShapes.circ(u, v, 0.5, 0.3, 0.19);
                double bx = (u - 0.5) / 0.37;
                double by = (v - 0.96) / 0.46;
                boolean body = bx * bx + by * by <= 1.0 && v <= 0.92;
                return head || body;
            }
            case 14: {
                boolean outer = IconShapes.rrect(u, v, 0.07, 0.12, 0.93, 0.88, 0.17);
                boolean inner = IconShapes.rrect(u, v, 0.15, 0.2, 0.85, 0.8, 0.1);
                boolean bar = IconShapes.rrect(u, v, 0.26, 0.3, 0.74, 0.42, 0.05);
                boolean blk = IconShapes.rrect(u, v, 0.26, 0.52, 0.48, 0.7, 0.05);
                return outer && !inner || bar || blk;
            }
            case 16: 
            case 17: {
                boolean body;
                boolean bl = body = IconShapes.rrect(u, v, 0.08, 0.38, 0.3, 0.62, 0.04) || IconShapes.inTri(u, v, 0.26, 0.38, 0.52, 0.14, 0.52, 0.86) || IconShapes.inTri(u, v, 0.26, 0.38, 0.26, 0.62, 0.52, 0.86);
                if (body) {
                    return true;
                }
                double dx = u - 0.5;
                double dy = v - 0.5;
                double r = Math.hypot(dx, dy);
                if (s == Shape.VOLUME) {
                    double ang = Math.abs(Math.atan2(dy, dx));
                    return dx > 0.04 && ang < Math.toRadians(48.0) && (Math.abs(r - 0.24) <= 0.04 || Math.abs(r - 0.38) <= 0.04);
                }
                return IconShapes.seg(u, v, 0.64, 0.36, 0.9, 0.64) <= 0.045 || IconShapes.seg(u, v, 0.9, 0.36, 0.64, 0.64) <= 0.045;
            }
            case 18: {
                return IconShapes.seg(u, v, 0.08, 0.74, 0.36, 0.46) <= 0.05 || IconShapes.seg(u, v, 0.36, 0.46, 0.54, 0.64) <= 0.05 || IconShapes.seg(u, v, 0.54, 0.64, 0.88, 0.26) <= 0.05 || IconShapes.seg(u, v, 0.88, 0.26, 0.64, 0.26) <= 0.05 || IconShapes.seg(u, v, 0.88, 0.26, 0.88, 0.5) <= 0.05;
            }
            case 19: {
                double r = Math.hypot(u - 0.5, v - 0.5);
                return Math.abs(r - 0.4) <= 0.05 || IconShapes.seg(u, v, 0.5, 0.5, 0.5, 0.25) <= 0.045 || IconShapes.seg(u, v, 0.5, 0.5, 0.68, 0.6) <= 0.045;
            }
            case 20: {
                boolean roof = IconShapes.inTri(u, v, 0.5, 0.1, 0.06, 0.48, 0.94, 0.48);
                boolean body = IconShapes.rrect(u, v, 0.16, 0.46, 0.84, 0.9, 0.04);
                boolean door = IconShapes.rrect(u, v, 0.42, 0.6, 0.58, 0.9, 0.03);
                return (roof || body) && !door;
            }
            case 21: {
                double r = Math.hypot(u - 0.42, v - 0.42);
                return Math.abs(r - 0.28) <= 0.065 || IconShapes.seg(u, v, 0.63, 0.63, 0.9, 0.9) <= 0.07;
            }
            case 22: {
                return IconShapes.rrect(u, v, 0.1, 0.12, 0.26, 0.88, 0.03) || IconShapes.rrect(u, v, 0.34, 0.12, 0.5, 0.88, 0.03) || IconShapes.inTri(u, v, 0.6, 0.16, 0.74, 0.1, 0.62, 0.9) || IconShapes.inTri(u, v, 0.74, 0.1, 0.9, 0.84, 0.62, 0.9);
            }
            case 15: {
                double e = 0.62;
                double a = Math.pow(Math.abs(u - 0.44), e) + Math.pow(Math.abs(v - 0.56), e);
                double b = Math.pow(Math.abs(u - 0.8), e) + Math.pow(Math.abs(v - 0.2), e);
                return a <= Math.pow(0.36, e) || b <= Math.pow(0.15, e);
            }
        }
        return false;
    }

    private static boolean circ(double u, double v, double cx, double cy, double r) {
        double dx = u - cx;
        double dy = v - cy;
        return dx * dx + dy * dy <= r * r;
    }

    private static boolean rrect(double u, double v, double x0, double y0, double x1, double y1, double r) {
        double cy;
        double dy;
        if (u < x0 || u > x1 || v < y0 || v > y1) {
            return false;
        }
        double cx = Math.max(x0 + (r = Math.min(r, Math.min(x1 - x0, y1 - y0) / 2.0)), Math.min(x1 - r, u));
        double dx = u - cx;
        return dx * dx + (dy = v - (cy = Math.max(y0 + r, Math.min(y1 - r, v)))) * dy <= r * r;
    }

    private static boolean rtri(double px, double py, double rr, double ax, double ay, double bx, double by, double cx, double cy) {
        double c2y;
        double c2x;
        double b2y;
        double b2x;
        double a2y;
        double a = Math.hypot(bx - cx, by - cy);
        double b = Math.hypot(cx - ax, cy - ay);
        double c = Math.hypot(ax - bx, ay - by);
        double per = a + b + c;
        double ix = (a * ax + b * bx + c * cx) / per;
        double iy = (a * ay + b * by + c * cy) / per;
        double area = Math.abs((bx - ax) * (cy - ay) - (cx - ax) * (by - ay)) / 2.0;
        double rho = 2.0 * area / per;
        double k = (rho - (rr = Math.min(rr, rho * 0.9))) / rho;
        double a2x = ix + (ax - ix) * k;
        if (IconShapes.inTri(px, py, a2x, a2y = iy + (ay - iy) * k, b2x = ix + (bx - ix) * k, b2y = iy + (by - iy) * k, c2x = ix + (cx - ix) * k, c2y = iy + (cy - iy) * k)) {
            return true;
        }
        double d = Math.min(IconShapes.seg(px, py, a2x, a2y, b2x, b2y), Math.min(IconShapes.seg(px, py, b2x, b2y, c2x, c2y), IconShapes.seg(px, py, c2x, c2y, a2x, a2y)));
        return d <= rr;
    }

    private static double seg(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double l2 = dx * dx + dy * dy;
        double t = l2 == 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, ((px - ax) * dx + (py - ay) * dy) / l2));
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    private static boolean inTri(double px, double py, double ax, double ay, double bx, double by, double cx, double cy) {
        double d1 = (px - bx) * (ay - by) - (ax - bx) * (py - by);
        double d2 = (px - cx) * (by - cy) - (bx - cx) * (py - cy);
        double d3 = (px - ax) * (cy - ay) - (cx - ax) * (py - ay);
        boolean neg = d1 < 0.0 || d2 < 0.0 || d3 < 0.0;
        boolean pos = d1 > 0.0 || d2 > 0.0 || d3 > 0.0;
        return !neg || !pos;
    }

    public static enum Shape {
        EYE,
        HEART,
        CHAT,
        NOTE,
        PLAY,
        PAUSE,
        NEXT,
        PREV,
        GLOBE,
        GEAR,
        STREAM,
        GRID,
        POWER,
        PERSON,
        LAYOUT,
        SPARKLE,
        VOLUME,
        MUTE,
        TREND,
        CLOCK,
        HOME,
        SEARCH,
        LIBRARY;

    }
}

