package org.geocraft.core.rendering.camera;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    private ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    private float fov = (float) Math.toRadians(60);
    private float aspect = 1f;
    private float near = 0.1f;
    private float far = 1000f;
    private float left = -1, right = 1, bottom = -1, top = 1;

    private final Vector3f location = new Vector3f(0, 0, 10);
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f up = new Vector3f(0, 1, 0);

    private int viewportWidth = 1, viewportHeight = 1;

    public void setPerspective(float fovRadians, float aspect, float near, float far) {
        this.projectionType = ProjectionType.PERSPECTIVE;
        this.fov = fovRadians;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
    }

    public void setParallel(float left, float right, float bottom, float top, float near, float far) {
        this.projectionType = ProjectionType.PARALLEL;
        this.left = left; this.right = right;
        this.bottom = bottom; this.top = top;
        this.near = near; this.far = far;
    }

    public void setLocation(Vector3f loc) { this.location.set(loc); }
    public Vector3f getLocation() { return new Vector3f(location); }

    public void lookAt(Vector3f target, Vector3f up) {
        this.target.set(target);
        this.up.set(up);
    }

    public void setViewport(int w, int h) {
        this.viewportWidth = w;
        this.viewportHeight = h;
        if (projectionType == ProjectionType.PERSPECTIVE) {
            this.aspect = (float) w / (float) h;
        }
    }

    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }

    public ProjectionType getProjectionType() { return projectionType; }

    public Matrix4f getProjectionMatrix() {
        if (projectionType == ProjectionType.PERSPECTIVE) {
            return new Matrix4f().perspective(fov, aspect, near, far);
        }
        return new Matrix4f().ortho(left, right, bottom, top, near, far);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(location, target, up);
    }

    public Ray getPickRay(Vector2f screenPos) {
        // Unproject near and far points
        Matrix4f pv = new Matrix4f(getProjectionMatrix()).mul(getViewMatrix());
        Matrix4f inv = new Matrix4f(pv).invert();
        float ndcX = (2f * screenPos.x / viewportWidth) - 1f;
        float ndcY = 1f - (2f * screenPos.y / viewportHeight);

        Vector4f nearP = new Vector4f(ndcX, ndcY, -1f, 1f).mul(inv);
        Vector4f farP  = new Vector4f(ndcX, ndcY,  1f, 1f).mul(inv);
        nearP.div(nearP.w);
        farP.div(farP.w);

        Vector3f origin = new Vector3f(nearP.x, nearP.y, nearP.z);
        Vector3f dir = new Vector3f(farP.x - nearP.x, farP.y - nearP.y, farP.z - nearP.z);
        return new Ray(origin, dir);
    }

    public Vector3f getWorldCoordinates(Vector2f screenPos, float depth) {
        Matrix4f pv = new Matrix4f(getProjectionMatrix()).mul(getViewMatrix());
        Matrix4f inv = new Matrix4f(pv).invert();
        float ndcX = (2f * screenPos.x / viewportWidth) - 1f;
        float ndcY = 1f - (2f * screenPos.y / viewportHeight);
        float ndcZ = 2f * depth - 1f;
        Vector4f world = new Vector4f(ndcX, ndcY, ndcZ, 1f).mul(inv);
        world.div(world.w);
        return new Vector3f(world.x, world.y, world.z);
    }
}
