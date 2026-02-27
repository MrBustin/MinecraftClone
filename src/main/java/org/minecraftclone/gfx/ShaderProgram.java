package org.minecraftclone.gfx;


import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL33.*;

public final class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexSrc, String fragmentSrc) {
        int vert = compile(GL_VERTEX_SHADER, vertexSrc);
        int frag = compile(GL_FRAGMENT_SHADER, fragmentSrc);

        programId = glCreateProgram();
        glAttachShader(programId, vert);
        glAttachShader(programId, frag);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            throw new RuntimeException("Shader program link failed:\n" + log);
        }

        glDeleteShader(vert);
        glDeleteShader(frag);
    }

    private static int compile(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(id);
            throw new RuntimeException("Shader compile failed:\n" + log);
        }
        return id;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }

    public void setMat4(String name, Matrix4f mat) {
        int loc = glGetUniformLocation(programId, name);
        if (loc == -1) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(loc, false, mat.get(stack.mallocFloat(16)));
        }
    }
}
