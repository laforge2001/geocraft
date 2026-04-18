package org.geocraft.core.rendering.camera;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class CameraTest {
    @Test
    public void perspectiveProjectionIsValidMatrix() {
        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1.0f, 0.1f, 100f);
        Matrix4f p = c.getProjectionMatrix();
        // Standard perspective: m33 is 0, m32 is -1 (w = -z)
        assertEquals(-1f, p.m23(), 1e-5);
    }

    @Test
    public void viewMatrixAtOriginLookingDownZ() {
        Camera c = new Camera();
        c.setLocation(new Vector3f(0, 0, 10));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        Matrix4f v = c.getViewMatrix();
        // Point at origin should map to (0,0,-10) in view space
        org.joml.Vector4f worldOrigin = new org.joml.Vector4f(0, 0, 0, 1);
        worldOrigin.mul(v);
        assertEquals(0f, worldOrigin.x, 1e-5);
        assertEquals(0f, worldOrigin.y, 1e-5);
        assertEquals(-10f, worldOrigin.z, 1e-5);
    }

    @Test
    public void pickRayFromScreenCenterPointsForward() {
        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1.0f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 10));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        c.setViewport(100, 100);
        Ray r = c.getPickRay(new Vector2f(50, 50));
        assertEquals(0f, r.direction.x, 1e-3);
        assertEquals(0f, r.direction.y, 1e-3);
        assertTrue(r.direction.z < 0); // looking down -z
    }
}
