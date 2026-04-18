package org.geocraft.core.rendering.material;

import org.joml.Vector4f;

public final class LightingConfig {
    public final boolean enabled;
    public final Vector4f ambient;
    public final Vector4f diffuse;
    public final Vector4f specular;
    public final float shininess;

    public LightingConfig(boolean enabled, Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess) {
        this.enabled = enabled;
        this.ambient = new Vector4f(ambient);
        this.diffuse = new Vector4f(diffuse);
        this.specular = new Vector4f(specular);
        this.shininess = shininess;
    }

    public static LightingConfig disabled() {
        return new LightingConfig(false, new Vector4f(), new Vector4f(), new Vector4f(), 0);
    }
}
