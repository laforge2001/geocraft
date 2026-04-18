package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.geocraft.core.rendering.scene.VisibilityHint;
import org.joml.Matrix4f;

public class JoglSceneWalker {
    private final JoglMaterialApplier materialApplier = new JoglMaterialApplier();
    private final JoglGeometryUpload geometryUpload = new JoglGeometryUpload();

    public void walk(GL2 gl, SceneNode node, RenderMaterial overrideMaterial) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;

        gl.glPushMatrix();
        // Use LOCAL transform: parent transforms are already on the GL matrix stack via recursion.
        Matrix4f local = node.getLocalTransform();
        float[] m = new float[16];
        local.get(m);
        gl.glMultMatrixf(m, 0);

        RenderMaterial mat = overrideMaterial != null ? overrideMaterial : node.getMaterial();
        materialApplier.apply(gl, mat);

        if (node instanceof MeshGeometry) {
            geometryUpload.drawMesh(gl, (MeshGeometry) node);
        } else if (node instanceof LineGeometry) {
            geometryUpload.drawLine(gl, (LineGeometry) node);
        } else if (node instanceof SphereGeometry) {
            geometryUpload.drawSphere(gl, (SphereGeometry) node);
        }

        for (SceneNode child : node.getChildren()) {
            walk(gl, child, overrideMaterial);
        }

        gl.glPopMatrix();
    }
}
