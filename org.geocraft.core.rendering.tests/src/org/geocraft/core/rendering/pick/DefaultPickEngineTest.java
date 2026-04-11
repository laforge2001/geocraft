package org.geocraft.core.rendering.pick;

import java.nio.FloatBuffer;
import java.util.List;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultPickEngineTest {
    private MeshGeometry makeTriangle(float z) {
        FloatBuffer v = FloatBuffer.wrap(new float[] {
            -1, -1, z,
             1, -1, z,
             0,  1, z
        });
        MeshGeometry m = new MeshGeometry("tri");
        m.setVertices(v, 3);
        return m;
    }

    @Test
    public void rayHitsTriangle() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));

        DefaultPickEngine engine = new DefaultPickEngine();
        List<PickResult> results = engine.pickTriangles(root, r);

        assertEquals(1, results.size());
        assertEquals(PickType.TRIANGLE, results.get(0).getType());
        assertEquals(0f, results.get(0).getWorldPosition().z, 1e-5);
    }

    @Test
    public void rayMissesTriangle() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        Ray r = new Ray(new Vector3f(10, 10, 10), new Vector3f(0, 0, -1));
        DefaultPickEngine engine = new DefaultPickEngine();
        assertTrue(engine.pickTriangles(root, r).isEmpty());
    }

    @Test
    public void multipleHitsSortedByDistance() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        root.addChild(makeTriangle(5));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));
        DefaultPickEngine engine = new DefaultPickEngine();
        List<PickResult> results = engine.pickTriangles(root, r);
        assertEquals(2, results.size());
        // Triangle at z=5 is closer to camera at z=10
        assertTrue(results.get(0).getDistance() < results.get(1).getDistance());
    }
}
