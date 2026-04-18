package org.geocraft.rendering.jogl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.joml.Vector4f;

public class JoglGeometryUpload {

    public void drawMesh(GL2 gl, MeshGeometry mesh) {
        FloatBuffer v = mesh.getVertices();
        FloatBuffer n = mesh.getNormals();
        FloatBuffer uv = mesh.getTexCoords();
        FloatBuffer colors = mesh.getColors();
        IntBuffer idx = mesh.getIndices();
        if (v == null) return;
        v.rewind();
        if (n != null) n.rewind();
        if (uv != null) uv.rewind();
        if (colors != null) colors.rewind();
        if (idx != null) idx.rewind();

        gl.glBegin(GL.GL_TRIANGLES);
        int triCount = idx != null ? mesh.getTriangleCount() : mesh.getVertexCount() / 3;
        for (int t = 0; t < triCount; t++) {
            for (int k = 0; k < 3; k++) {
                int i = idx != null ? idx.get(t * 3 + k) : (t * 3 + k);
                if (n != null) gl.glNormal3f(n.get(i * 3), n.get(i * 3 + 1), n.get(i * 3 + 2));
                if (colors != null) gl.glColor4f(colors.get(i * 4), colors.get(i * 4 + 1), colors.get(i * 4 + 2), colors.get(i * 4 + 3));
                if (uv != null) gl.glTexCoord2f(uv.get(i * 2), uv.get(i * 2 + 1));
                gl.glVertex3f(v.get(i * 3), v.get(i * 3 + 1), v.get(i * 3 + 2));
            }
        }
        gl.glEnd();
    }

    public void drawLine(GL2 gl, LineGeometry line) {
        FloatBuffer v = line.getVertices();
        if (v == null || line.getVertexCount() == 0) return;
        v.rewind();
        Vector4f c = line.getColor();
        gl.glLineWidth(line.getLineWidth());
        gl.glColor4f(c.x, c.y, c.z, c.w);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glBegin(GL.GL_LINES);
        for (int i = 0; i < line.getVertexCount(); i++) {
            gl.glVertex3f(v.get(), v.get(), v.get());
        }
        gl.glEnd();
        v.rewind();
    }

    public void drawSphere(GL2 gl, SphereGeometry sphere) {
        // Simple icosphere-like approximation via GLU quadric
        com.jogamp.opengl.glu.GLU glu = new com.jogamp.opengl.glu.GLU();
        com.jogamp.opengl.glu.GLUquadric q = glu.gluNewQuadric();
        Vector4f c = sphere.getColor();
        gl.glColor4f(c.x, c.y, c.z, c.w);
        glu.gluSphere(q, sphere.getRadius(), 16, 16);
        glu.gluDeleteQuadric(q);
    }
}
