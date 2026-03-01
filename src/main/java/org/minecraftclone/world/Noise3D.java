package org.minecraftclone.world;

/**
 * Seeded 3D Simplex Noise + FBM helpers.
 * Output range is roughly [-1, 1] for noise(), and also ~[-1,1] for fbm().
 *
 * Use for caves by sampling a 3D "density" field and carving where density > threshold.
 */
public final class Noise3D {
    private final short[] perm = new short[512];

    // Gradients for 3D simplex
    private static final int[][] GRAD3 = {
            { 1, 1, 0}, {-1, 1, 0}, { 1,-1, 0}, {-1,-1, 0},
            { 1, 0, 1}, {-1, 0, 1}, { 1, 0,-1}, {-1, 0,-1},
            { 0, 1, 1}, { 0,-1, 1}, { 0, 1,-1}, { 0,-1,-1}
    };

    public Noise3D(long seed) {
        short[] p = new short[256];
        for (short i = 0; i < 256; i++) p[i] = i;

        // Fisher-Yates shuffle with a tiny deterministic RNG
        long s = seed;
        for (int i = 255; i > 0; i--) {
            s = s * 6364136223846793005L + 1442695040888963407L;
            int r = (int) ((s >>> 33) % (i + 1));
            short tmp = p[i];
            p[i] = p[r];
            p[r] = tmp;
        }

        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    // Raw simplex noise in [-1,1] (approx)
    public double noise(double xin, double yin, double zin) {
        // Skewing and unskewing factors for 3D
        final double F3 = 1.0 / 3.0;
        final double G3 = 1.0 / 6.0;

        // Skew the input space to determine which simplex cell we're in
        double s = (xin + yin + zin) * F3;
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);

        // Unskew back to (x,y,z) space
        double t = (i + j + k) * G3;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;

        // The x,y,z distances from the cell origin
        double x0 = xin - X0;
        double y0 = yin - Y0;
        double z0 = zin - Z0;

        // Determine which simplex we are in
        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) { i1=1; j1=0; k1=0;  i2=1; j2=1; k2=0; }      // X Y Z
            else if (x0 >= z0) { i1=1; j1=0; k1=0;  i2=1; j2=0; k2=1; } // X Z Y
            else { i1=0; j1=0; k1=1;  i2=1; j2=0; k2=1; }              // Z X Y
        } else {
            if (y0 < z0) { i1=0; j1=0; k1=1;  i2=0; j2=1; k2=1; }      // Z Y X
            else if (x0 < z0) { i1=0; j1=1; k1=0;  i2=0; j2=1; k2=1; } // Y Z X
            else { i1=0; j1=1; k1=0;  i2=1; j2=1; k2=0; }              // Y X Z
        }

        // Offsets for corners
        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;

        double x2 = x0 - i2 + 2.0*G3;
        double y2 = y0 - j2 + 2.0*G3;
        double z2 = z0 - k2 + 2.0*G3;

        double x3 = x0 - 1.0 + 3.0*G3;
        double y3 = y0 - 1.0 + 3.0*G3;
        double z3 = z0 - 1.0 + 3.0*G3;

        // Hash coords
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1  + perm[jj + 1  + perm[kk + 1 ]]] % 12;

        // Corner contributions
        double n0, n1, n2, n3;

        double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
        if (t0 < 0) n0 = 0.0;
        else { t0 *= t0; n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0, z0); }

        double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
        if (t1 < 0) n1 = 0.0;
        else { t1 *= t1; n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1, z1); }

        double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
        if (t2 < 0) n2 = 0.0;
        else { t2 *= t2; n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2, z2); }

        double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
        if (t3 < 0) n3 = 0.0;
        else { t3 *= t3; n3 = t3 * t3 * dot(GRAD3[gi3], x3, y3, z3); }

        // Scale to approx [-1,1]
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /** Fractal Brownian Motion: smoother, more cave-like density. */
    public double fbm(double x, double y, double z, int octaves, double lacunarity, double gain) {
        double sum = 0.0;
        double amp = 1.0;
        double freq = 1.0;
        double max = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += noise(x * freq, y * freq, z * freq) * amp;
            max += amp;
            amp *= gain;
            freq *= lacunarity;
        }
        return sum / max; // normalize ~[-1,1]
    }

    /** Ridged noise: good for “wormy” cave tubes when combined with a threshold. */
    public double ridged(double x, double y, double z, int octaves, double lacunarity, double gain) {
        double sum = 0.0;
        double amp = 0.5;
        double freq = 1.0;

        for (int i = 0; i < octaves; i++) {
            double n = noise(x * freq, y * freq, z * freq);
            n = 1.0 - Math.abs(n); // ridges
            sum += n * amp;
            amp *= gain;
            freq *= lacunarity;
        }
        // range roughly [0,1]
        return clamp01(sum);
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double dot(int[] g, double x, double y, double z) {
        return g[0]*x + g[1]*y + g[2]*z;
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}
