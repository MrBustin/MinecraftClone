package org.minecraftclone.entity.core;

public record BoxUV(
        UV front,
        UV back,
        UV left,
        UV right,
        UV top,
        UV bottom
) {
    public static BoxUV all(UV uv) {
        return new BoxUV(uv, uv, uv, uv, uv, uv);
    }
}
