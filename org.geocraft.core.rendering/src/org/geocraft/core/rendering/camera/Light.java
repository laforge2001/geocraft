package org.geocraft.core.rendering.camera;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Light {
    public enum Type { DIRECTIONAL, POINT }

    private Type type = Type.DIRECTIONAL;
    private final Vector3f direction = new Vector3f(0, -1, 0);
    private final Vector3f position = new Vector3f();
    private final Vector4f diffuse = new Vector4f(1, 1, 1, 1);
    private final Vector4f specular = new Vector4f(1, 1, 1, 1);
    private final Vector4f ambient = new Vector4f(0.3f, 0.3f, 0.3f, 1f);
    private boolean enabled = true;

    public Type getType() { return type; }
    public void setType(Type t) { this.type = t; }
    public Vector3f getDirection() { return new Vector3f(direction); }
    public void setDirection(Vector3f d) { this.direction.set(d).normalize(); }
    public Vector3f getPosition() { return new Vector3f(position); }
    public void setPosition(Vector3f p) { this.position.set(p); }
    public Vector4f getDiffuse() { return new Vector4f(diffuse); }
    public void setDiffuse(Vector4f c) { this.diffuse.set(c); }
    public Vector4f getSpecular() { return new Vector4f(specular); }
    public void setSpecular(Vector4f c) { this.specular.set(c); }
    public Vector4f getAmbient() { return new Vector4f(ambient); }
    public void setAmbient(Vector4f c) { this.ambient.set(c); }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
}
