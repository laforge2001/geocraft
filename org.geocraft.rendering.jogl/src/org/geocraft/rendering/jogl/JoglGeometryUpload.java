package org.geocraft.rendering.jogl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.IdentityHashMap;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.joml.Vector4f;

/**
 * Pushes scene-graph geometry through the GL2 fixed-function pipeline using
 * client-side vertex arrays. Each mesh is drawn with a single
 * {@code glDrawElements} / {@code glDrawArrays} call (one JNI boundary crossing)
 * rather than the per-vertex {@code glVertex3f} immediate-mode pattern that
 * the Ardor3D → JOGL port started with — which made the 3D viewer unusably
 * slow on large seismic volumes (~10⁵ triangles × 30 fps × 10+ GL calls per
 * vertex = tens of millions of JNI trips per second).
 */
public class JoglGeometryUpload {

    private com.jogamp.opengl.glu.GLU glu;
    private com.jogamp.opengl.glu.GLUquadric sphereQuadric;

    // JOGL's client-side vertex-array API requires direct NIO buffers (it
    // hands a raw native pointer to the GL driver). Many renderers allocate
    // via FloatBuffer.allocate(...) which is a heap buffer — that throws
    // "Argument 'ptr' is not a direct buffer" at draw time. We transparently
    // copy heap buffers into a cached direct buffer on first use. Keyed by
    // identity because FloatBuffer.equals walks the whole content.
    private final IdentityHashMap<FloatBuffer, FloatBuffer> directFloatCache = new IdentityHashMap<>();
    private final IdentityHashMap<IntBuffer, IntBuffer> directIntCache = new IdentityHashMap<>();

    private FloatBuffer ensureDirect(FloatBuffer src) {
        if (src == null || src.isDirect()) return src;
        FloatBuffer cached = directFloatCache.get(src);
        if (cached != null && cached.capacity() == src.capacity()) {
            cached.clear();
            src.rewind();
            cached.put(src);
            cached.rewind();
            return cached;
        }
        FloatBuffer direct = ByteBuffer.allocateDirect(src.capacity() * Float.BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
        src.rewind();
        direct.put(src);
        direct.rewind();
        directFloatCache.put(src, direct);
        return direct;
    }

    private IntBuffer ensureDirect(IntBuffer src) {
        if (src == null || src.isDirect()) return src;
        IntBuffer cached = directIntCache.get(src);
        if (cached != null && cached.capacity() == src.capacity()) {
            cached.clear();
            src.rewind();
            cached.put(src);
            cached.rewind();
            return cached;
        }
        IntBuffer direct = ByteBuffer.allocateDirect(src.capacity() * Integer.BYTES)
            .order(ByteOrder.nativeOrder()).asIntBuffer();
        src.rewind();
        direct.put(src);
        direct.rewind();
        directIntCache.put(src, direct);
        return direct;
    }

    public void drawMesh(GL2 gl, MeshGeometry mesh) {
        FloatBuffer v = ensureDirect(mesh.getVertices());
        if (v == null) return;
        FloatBuffer n = ensureDirect(mesh.getNormals());
        FloatBuffer uv = ensureDirect(mesh.getTexCoords());
        FloatBuffer colors = ensureDirect(mesh.getColors());
        IntBuffer idx = ensureDirect(mesh.getIndices());
        v.rewind();
        if (n != null) n.rewind();
        if (uv != null) uv.rewind();
        if (colors != null) colors.rewind();
        if (idx != null) idx.rewind();

        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, v);
        if (n != null) {
            gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, n);
        }
        if (colors != null) {
            gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 0, colors);
        }
        if (uv != null) {
            gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, uv);
        }

        if (idx != null) {
            gl.glDrawElements(GL.GL_TRIANGLES, mesh.getTriangleCount() * 3, GL.GL_UNSIGNED_INT, idx);
        } else {
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, (mesh.getVertexCount() / 3) * 3);
        }

        gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        if (n != null) gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        if (colors != null) gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
        if (uv != null) gl.glDisableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
    }

    public void drawLine(GL2 gl, LineGeometry line) {
        FloatBuffer v = ensureDirect(line.getVertices());
        if (v == null || line.getVertexCount() == 0) return;
        v.rewind();
        Vector4f c = line.getColor();
        gl.glLineWidth(line.getLineWidth());
        gl.glColor4f(c.x, c.y, c.z, c.w);
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, v);
        gl.glDrawArrays(GL.GL_LINES, 0, line.getVertexCount());
        gl.glDisableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
    }

    public void drawSphere(GL2 gl, SphereGeometry sphere) {
        // Cache the GLU + quadric — the previous implementation allocated
        // a fresh quadric and deleted it every frame, which is both GC
        // pressure and driver overhead.
        if (glu == null) glu = new com.jogamp.opengl.glu.GLU();
        if (sphereQuadric == null) sphereQuadric = glu.gluNewQuadric();
        Vector4f c = sphere.getColor();
        gl.glColor4f(c.x, c.y, c.z, c.w);
        glu.gluSphere(sphereQuadric, sphere.getRadius(), 16, 16);
    }
}
