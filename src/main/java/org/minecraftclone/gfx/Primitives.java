package org.minecraftclone.gfx;

public final class Primitives {
    private Primitives() {}

    public static float[] cubePUV() {
        return new float[] {
                // FRONT (+Z)
                -0.5f,-0.5f, 0.5f,  0f,0f,
                0.5f,-0.5f, 0.5f,  1f,0f,
                0.5f, 0.5f, 0.5f,  1f,1f,
                0.5f, 0.5f, 0.5f,  1f,1f,
                -0.5f, 0.5f, 0.5f,  0f,1f,
                -0.5f,-0.5f, 0.5f,  0f,0f,

                // BACK (-Z)
                0.5f,-0.5f,-0.5f,  0f,0f,
                -0.5f,-0.5f,-0.5f,  1f,0f,
                -0.5f, 0.5f,-0.5f,  1f,1f,
                -0.5f, 0.5f,-0.5f,  1f,1f,
                0.5f, 0.5f,-0.5f,  0f,1f,
                0.5f,-0.5f,-0.5f,  0f,0f,

                // LEFT (-X)
                -0.5f,-0.5f,-0.5f,  0f,0f,
                -0.5f,-0.5f, 0.5f,  1f,0f,
                -0.5f, 0.5f, 0.5f,  1f,1f,
                -0.5f, 0.5f, 0.5f,  1f,1f,
                -0.5f, 0.5f,-0.5f,  0f,1f,
                -0.5f,-0.5f,-0.5f,  0f,0f,

                // RIGHT (+X)
                0.5f,-0.5f, 0.5f,  0f,0f,
                0.5f,-0.5f,-0.5f,  1f,0f,
                0.5f, 0.5f,-0.5f,  1f,1f,
                0.5f, 0.5f,-0.5f,  1f,1f,
                0.5f, 0.5f, 0.5f,  0f,1f,
                0.5f,-0.5f, 0.5f,  0f,0f,

                // TOP (+Y)
                -0.5f, 0.5f, 0.5f,  0f,0f,
                0.5f, 0.5f, 0.5f,  1f,0f,
                0.5f, 0.5f,-0.5f,  1f,1f,
                0.5f, 0.5f,-0.5f,  1f,1f,
                -0.5f, 0.5f,-0.5f,  0f,1f,
                -0.5f, 0.5f, 0.5f,  0f,0f,

                // BOTTOM (-Y)
                -0.5f,-0.5f,-0.5f,  0f,0f,
                0.5f,-0.5f,-0.5f,  1f,0f,
                0.5f,-0.5f, 0.5f,  1f,1f,
                0.5f,-0.5f, 0.5f,  1f,1f,
                -0.5f,-0.5f, 0.5f,  0f,1f,
                -0.5f,-0.5f,-0.5f,  0f,0f
        };
    }
}
