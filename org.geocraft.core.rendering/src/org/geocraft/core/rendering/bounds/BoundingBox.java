package org.geocraft.core.rendering.bounds;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.pick.Ray;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class BoundingBox extends BoundingVolume {
    private final Vector3f min;
    private final Vector3f max;

    public BoundingBox(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }

    public Vector3f getMin() { return new Vector3f(min); }
    public Vector3f getMax() { return new Vector3f(max); }

    @Override
    public Vector3f getCenter() {
        return new Vector3f(min).add(max).mul(0.5f);
    }

    @Override
    public boolean intersectsRay(Ray ray) {
        Vector2f result = new Vector2f();
        return Intersectionf.intersectRayAab(
            ray.origin.x, ray.origin.y, ray.origin.z,
            ray.direction.x, ray.direction.y, ray.direction.z,
            min.x, min.y, min.z, max.x, max.y, max.z, result);
    }

    @Override
    public boolean contains(Vector3f p) {
        return p.x >= min.x && p.x <= max.x
            && p.y >= min.y && p.y <= max.y
            && p.z >= min.z && p.z <= max.z;
    }

    public static BoundingBox fromVertices(FloatBuffer verts, int vertexCount) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 0; i < vertexCount; i++) {
            float x = verts.get(i * 3);
            float y = verts.get(i * 3 + 1);
            float z = verts.get(i * 3 + 2);
            if (x < min.x) min.x = x;
            if (y < min.y) min.y = y;
            if (z < min.z) min.z = z;
            if (x > max.x) max.x = x;
            if (y > max.y) max.y = y;
            if (z > max.z) max.z = z;
        }
        return new BoundingBox(min, max);
    }
}
