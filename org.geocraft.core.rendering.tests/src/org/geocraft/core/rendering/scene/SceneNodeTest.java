package org.geocraft.core.rendering.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class SceneNodeTest {
    @Test
    public void defaultTransformIsIdentity() {
        SceneNode n = new GroupNode("root");
        Matrix4f m = n.getLocalTransform();
        assertTrue(m.equals(new Matrix4f(), 1e-6f));
    }

    @Test
    public void worldTransformComposesWithParent() {
        GroupNode parent = new GroupNode("parent");
        parent.setTranslation(new Vector3f(10, 0, 0));
        GroupNode child = new GroupNode("child");
        child.setTranslation(new Vector3f(0, 5, 0));
        parent.addChild(child);
        Matrix4f w = child.getWorldTransform();
        Vector3f t = new Vector3f();
        w.getTranslation(t);
        assertEquals(10f, t.x, 1e-6);
        assertEquals(5f, t.y, 1e-6);
    }

    @Test
    public void defaultVisibilityIsDynamic() {
        SceneNode n = new GroupNode("n");
        assertEquals(VisibilityHint.DYNAMIC, n.getVisibility());
    }
}
