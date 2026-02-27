package org.minecraftclone.gfx;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public final class Texture {
    private final int id;

    public Texture(String resourcePath) {
        id = glGenTextures();
        bind();

        // Texture parameters (good defaults)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        STBImage.stbi_set_flip_vertically_on_load(false);

        ByteBuffer image;
        int width, height;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // Load from classpath -> we need a byte[] first
            byte[] bytes = ResourceLoader.readAllBytes(resourcePath);
            ByteBuffer buffer = stack.malloc(bytes.length);
            buffer.put(bytes).flip();

            image = STBImage.stbi_load_from_memory(buffer, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture " + resourcePath + " : " + STBImage.stbi_failure_reason());
            }

            width = w.get(0);
            height = h.get(0);
        }

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);

        STBImage.stbi_image_free(image);
        unbind();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
}
