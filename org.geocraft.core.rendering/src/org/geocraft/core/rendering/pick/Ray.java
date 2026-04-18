package org.geocraft.core.rendering.pick;

import org.joml.Vector3f;

public final class Ray {
    public final Vector3f origin;
    public final Vector3f direction;

    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction).normalize();
    }

    public Vector3f pointAt(float t) {
        return new Vector3f(direction).mul(t).add(origin);
    }
}
