package org.geocraft.core.rendering.bounds;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.pick.Ray;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoundingVolumeTest {
    @Test
    public void boxFromVertices() {
        FloatBuffer verts = FloatBuffer.wrap(new float[] {
            -1, -1, -1,
             1,  1,  1,
             0,  0,  0
        });
        BoundingBox b = BoundingBox.fromVertices(verts, 3);
        assertEquals(-1f, b.getMin().x, 1e-6);
        assertEquals( 1f, b.getMax().y, 1e-6);
    }

    @Test
    public void rayHitsBox() {
        BoundingBox b = new BoundingBox(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));
        assertTrue(b.intersectsRay(r));
    }

    @Test
    public void rayMissesBox() {
        BoundingBox b = new BoundingBox(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
        Ray r = new Ray(new Vector3f(10, 10, 10), new Vector3f(1, 1, 1));
        assertFalse(b.intersectsRay(r));
    }

    @Test
    public void sphereContainsCenter() {
        BoundingSphere s = new BoundingSphere(new Vector3f(0, 0, 0), 5f);
        assertTrue(s.contains(new Vector3f(2, 2, 2)));
        assertFalse(s.contains(new Vector3f(10, 0, 0)));
    }
}
