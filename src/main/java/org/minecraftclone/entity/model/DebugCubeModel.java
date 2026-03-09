package org.minecraftclone.entity.model;

public class DebugCubeModel extends EntityModel {

    public DebugCubeModel() {
        super(makeDebugCube());
    }

    private static float[] makeDebugCube() {
        float s = 0.3f;

        return new float[] {
                // front
                -s, 0,  s, 0, 0, 1f,
                s, 0,  s, 1, 0, 1f,
                s, 0.6f, s, 1, 1, 1f,

                -s, 0,  s, 0, 0, 1f,
                s, 0.6f, s, 1, 1, 1f,
                -s, 0.6f, s, 0, 1, 1f,

                // back
                s, 0, -s, 0, 0, 1f,
                -s, 0, -s, 1, 0, 1f,
                -s, 0.6f,-s, 1, 1, 1f,

                s, 0, -s, 0, 0, 1f,
                -s, 0.6f,-s, 1, 1, 1f,
                s, 0.6f,-s, 0, 1, 1f,

                // left
                -s, 0, -s, 0, 0, 0.85f,
                -s, 0,  s, 1, 0, 0.85f,
                -s, 0.6f, s, 1, 1, 0.85f,

                -s, 0, -s, 0, 0, 0.85f,
                -s, 0.6f, s, 1, 1, 0.85f,
                -s, 0.6f,-s, 0, 1, 0.85f,

                // right
                s, 0,  s, 0, 0, 0.85f,
                s, 0, -s, 1, 0, 0.85f,
                s, 0.6f,-s, 1, 1, 0.85f,

                s, 0,  s, 0, 0, 0.85f,
                s, 0.6f,-s, 1, 1, 0.85f,
                s, 0.6f, s, 0, 1, 0.85f,

                // top
                -s, 0.6f, s, 0, 0, 1.15f,
                s, 0.6f, s, 1, 0, 1.15f,
                s, 0.6f,-s, 1, 1, 1.15f,

                -s, 0.6f, s, 0, 0, 1.15f,
                s, 0.6f,-s, 1, 1, 1.15f,
                -s, 0.6f,-s, 0, 1, 1.15f,

                // bottom
                -s, 0, -s, 0, 0, 0.7f,
                s, 0, -s, 1, 0, 0.7f,
                s, 0,  s, 1, 1, 0.7f,

                -s, 0, -s, 0, 0, 0.7f,
                s, 0,  s, 1, 1, 0.7f,
                -s, 0,  s, 0, 1, 0.7f,
        };
    }
}
