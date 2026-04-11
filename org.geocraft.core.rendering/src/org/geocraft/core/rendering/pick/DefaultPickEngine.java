package org.geocraft.core.rendering.pick;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.geocraft.core.rendering.bounds.BoundingBox;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.VisibilityHint;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class DefaultPickEngine implements PickEngine {

    @Override
    public List<PickResult> pickTriangles(GroupNode root, Ray ray) {
        List<PickResult> results = new ArrayList<>();
        collectTrianglePicks(root, ray, results);
        results.sort(Comparator.comparingDouble(PickResult::getDistance));
        return results;
    }

    @Override
    public List<PickResult> pickBounds(GroupNode root, Ray ray) {
        List<PickResult> results = new ArrayList<>();
        collectBoundsPicks(root, ray, results);
        results.sort(Comparator.comparingDouble(PickResult::getDistance));
        return results;
    }

    private void collectTrianglePicks(SceneNode node, Ray ray, List<PickResult> out) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;
        if (node instanceof MeshGeometry) {
            pickMesh((MeshGeometry) node, ray, out);
        }
        for (SceneNode child : node.getChildren()) {
            collectTrianglePicks(child, ray, out);
        }
    }

    private void collectBoundsPicks(SceneNode node, Ray ray, List<PickResult> out) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;
        if (node instanceof MeshGeometry) {
            MeshGeometry mesh = (MeshGeometry) node;
            FloatBuffer verts = mesh.getVertices();
            if (verts != null && mesh.getVertexCount() > 0) {
                BoundingBox box = BoundingBox.fromVertices(verts, mesh.getVertexCount());
                if (box.intersectsRay(ray)) {
                    out.add(new PickResult(node, ray.origin.distance(box.getCenter()),
                                           box.getCenter(), PickType.BOUNDS));
                }
            }
        }
        for (SceneNode child : node.getChildren()) {
            collectBoundsPicks(child, ray, out);
        }
    }

    private void pickMesh(MeshGeometry mesh, Ray ray, List<PickResult> out) {
        FloatBuffer verts = mesh.getVertices();
        if (verts == null) return;
        Matrix4f world = mesh.getWorldTransform();
        IntBuffer idx = mesh.getIndices();
        int triCount = idx != null ? mesh.getTriangleCount() : mesh.getVertexCount() / 3;
        for (int t = 0; t < triCount; t++) {
            int i0, i1, i2;
            if (idx != null) {
                i0 = idx.get(t * 3);
                i1 = idx.get(t * 3 + 1);
                i2 = idx.get(t * 3 + 2);
            } else {
                i0 = t * 3;
                i1 = t * 3 + 1;
                i2 = t * 3 + 2;
            }
            Vector3f v0 = transform(verts, i0, world);
            Vector3f v1 = transform(verts, i1, world);
            Vector3f v2 = transform(verts, i2, world);
            Vector2f uv = new Vector2f();
            float hit = Intersectionf.intersectRayTriangle(
                ray.origin.x, ray.origin.y, ray.origin.z,
                ray.direction.x, ray.direction.y, ray.direction.z,
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z, 1e-6f);
            if (hit >= 0f) {
                Vector3f hitPos = ray.pointAt(hit);
                out.add(new PickResult(mesh, hit, hitPos, PickType.TRIANGLE));
            }
        }
    }

    private Vector3f transform(FloatBuffer verts, int i, Matrix4f world) {
        float x = verts.get(i * 3);
        float y = verts.get(i * 3 + 1);
        float z = verts.get(i * 3 + 2);
        Vector4f v = new Vector4f(x, y, z, 1f).mul(world);
        return new Vector3f(v.x, v.y, v.z);
    }
}
