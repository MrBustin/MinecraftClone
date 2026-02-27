package org.minecraftclone.gfx;

import java.io.IOException;
import java.io.InputStream;

public final class ResourceLoader {
    private ResourceLoader() {}

    public static byte[] readAllBytes(String path) {
        // path like "/textures/test.png"
        try (InputStream in = ResourceLoader.class.getResourceAsStream(path)) {
            if (in == null) throw new RuntimeException("Resource not found: " + path);
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed reading resource: " + path, e);
        }
    }
}
