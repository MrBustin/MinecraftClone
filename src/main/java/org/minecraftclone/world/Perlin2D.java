package org.minecraftclone.world;

public final class Perlin2D {
    private final int[] perm = new int[512];

    public Perlin2D(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;

        // Fisher–Yates shuffle using a tiny seeded RNG
        long s = seed;
        for (int i = 255; i > 0; i--) {
            s = s * 6364136223846793005L + 1442695040888963407L;
            int r = (int) ((s >>> 33) % (i + 1));
            int tmp = p[i];
            p[i] = p[r];
            p[r] = tmp;
        }

        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    // returns roughly [-1, 1]
    public double noise(double x, double y) {
        int X = fastFloor(x) & 255;
        int Y = fastFloor(y) & 255;

        double xf = x - fastFloor(x);
        double yf = y - fastFloor(y);

        double u = fade(xf);
        double v = fade(yf);

        int aa = perm[X + perm[Y]];
        int ab = perm[X + perm[Y + 1]];
        int ba = perm[X + 1 + perm[Y]];
        int bb = perm[X + 1 + perm[Y + 1]];

        double x1 = lerp(grad(aa, xf, yf),     grad(ba, xf - 1, yf),     u);
        double x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);

        return lerp(x1, x2, v);
    }

    // Fractal Brownian Motion (nicer terrain)
    public double fbm(double x, double y, int octaves, double lacunarity, double gain) {
        double sum = 0.0;
        double amp = 1.0;
        double freq = 1.0;
        double max = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += noise(x * freq, y * freq) * amp;
            max += amp;
            amp *= gain;
            freq *= lacunarity;
        }
        return sum / max; // normalize to ~[-1,1]
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double fade(double t) {
        // 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y) {
        // 4-direction gradient (fast + good enough for now)
        return switch (hash & 3) {
            case 0 ->  x + y;
            case 1 -> -x + y;
            case 2 ->  x - y;
            default -> -x - y;
        };
    }
}
