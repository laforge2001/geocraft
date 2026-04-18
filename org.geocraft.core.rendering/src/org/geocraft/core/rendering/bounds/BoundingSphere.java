package org.geocraft.core.rendering.bounds;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Intersectionf;
import org.joml.Vector3f;

public class BoundingSphere extends BoundingVolume {
    private final Vector3f center;
    private final float radius;

    public BoundingSphere(Vector3f center, float radius) {
        this.center = new Vector3f(center);
        this.radius = radius;
    }

    @Override
    public Vector3f getCenter() { return new Vector3f(center); }
    public float getRadius() { return radius; }

    @Override
    public boolean intersectsRay(Ray ray) {
        return Intersectionf.testRaySphere(
            ray.origin.x, ray.origin.y, ray.origin.z,
            ray.direction.x, ray.direction.y, ray.direction.z,
            center.x, center.y, center.z, radius * radius);
    }

    @Override
    public boolean contains(Vector3f p) {
        return new Vector3f(p).sub(center).length() <= radius;
    }
}
