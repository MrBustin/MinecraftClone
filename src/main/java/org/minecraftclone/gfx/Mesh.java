package org.minecraftclone.gfx;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

public final class Mesh {
    private final int vao;
    private final int vbo;
    private final int vertexCount;

    /**
     * @param data 2D positions: x,y,x,y,...
     */
    public Mesh(float[] data) {
        // data layout: x y z u v  x y z u v  ...
        this.vertexCount = data.length / 5;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer fb = MemoryUtil.memAllocFloat(data.length);
        fb.put(data).flip();
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        MemoryUtil.memFree(fb);

        int stride = 5 * Float.BYTES;

        // location = 0 : vec3 position (x,y,z)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // location = 1 : vec2 uv (u,v) starts after 3 floats
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
