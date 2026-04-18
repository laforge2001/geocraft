package org.geocraft.core.rendering.scene;

import org.joml.Vector4f;

public class SphereGeometry extends SceneNode {
    private float radius;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public SphereGeometry(String name, float radius) {
        super(name);
        this.radius = radius;
    }

    public float getRadius() { return radius; }
    public void setRadius(float r) { this.radius = r; }

    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
