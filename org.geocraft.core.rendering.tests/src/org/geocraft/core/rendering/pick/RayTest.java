package org.geocraft.core.rendering.pick;

import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class RayTest {
    @Test
    public void constructNormalizedRay() {
        Ray r = new Ray(new Vector3f(0, 0, 0), new Vector3f(0, 0, -2));
        assertEquals(0f, r.origin.x, 1e-6);
        assertEquals(-1f, r.direction.z, 1e-6); // direction normalized
        assertEquals(1f, r.direction.length(), 1e-6);
    }

    @Test
    public void pointAtDistance() {
        Ray r = new Ray(new Vector3f(1, 2, 3), new Vector3f(1, 0, 0));
        Vector3f p = r.pointAt(5f);
        assertEquals(6f, p.x, 1e-6);
        assertEquals(2f, p.y, 1e-6);
        assertEquals(3f, p.z, 1e-6);
    }
}
