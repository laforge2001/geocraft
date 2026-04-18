package org.geocraft.core.rendering.bounds;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Vector3f;

public abstract class BoundingVolume {
    public abstract boolean intersectsRay(Ray ray);
    public abstract boolean contains(Vector3f point);
    public abstract Vector3f getCenter();
}
