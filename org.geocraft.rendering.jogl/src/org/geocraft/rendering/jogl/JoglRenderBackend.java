package org.geocraft.rendering.jogl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.geocraft.core.rendering.backend.TextureLoader;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.joml.Matrix4f;

public class JoglRenderBackend implements RenderBackend {
    // OpenGL fixed-function constants. Referenced numerically because their
    // JOGL declarations live in com.jogamp.opengl.fixedfunc, which is not
    // re-exported as API by the jogl-all OSGi bundle.
    private static final int GL_PROJECTION = 0x1701;
    private static final int GL_MODELVIEW  = 0x1700;
    private static final int GL_LIGHT0     = 0x4000;
    private static final int GL_AMBIENT    = 0x1200;
    private static final int GL_DIFFUSE    = 0x1201;
    private static final int GL_SPECULAR   = 0x1202;
    private static final int GL_POSITION   = 0x1203;

    private final JoglSceneWalker walker = new JoglSceneWalker();
    private final JoglTextureLoader textureLoader = new JoglTextureLoader();
    private RenderSurface currentSurface;

    @Override
    public void initialize(RenderSurface surface) {
        this.currentSurface = surface;
        surface.makeCurrent();
        GL2 gl = GLContext.getCurrentGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glClearColor(0f, 0f, 0f, 1f);
        surface.release();
    }

    @Override
    public void renderPass(GroupNode root, Camera camera, Light[] lights) {
        renderPass(root, camera, lights, null);
    }

    @Override
    public void renderPass(GroupNode root, Camera camera, Light[] lights, RenderMaterial overrideMaterial) {
        if (currentSurface == null) throw new IllegalStateException("not initialized");
        currentSurface.makeCurrent();
        try {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            gl.glViewport(0, 0, currentSurface.getWidth(), currentSurface.getHeight());
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            Matrix4f proj = camera.getProjectionMatrix();
            Matrix4f view = camera.getViewMatrix();
            float[] m = new float[16];
            gl.glMatrixMode(GL_PROJECTION);
            proj.get(m);
            gl.glLoadMatrixf(m, 0);
            gl.glMatrixMode(GL_MODELVIEW);
            view.get(m);
            gl.glLoadMatrixf(m, 0);

            applyLights(gl, lights);
            walker.walk(gl, root, overrideMaterial);
        } finally {
            currentSurface.release();
        }
    }

    private void applyLights(GL2 gl, Light[] lights) {
        if (lights == null || lights.length == 0) return;
        for (int i = 0; i < Math.min(lights.length, 8); i++) {
            Light l = lights[i];
            int id = GL_LIGHT0 + i;
            if (!l.isEnabled()) { gl.glDisable(id); continue; }
            gl.glEnable(id);
            float[] amb = toArray(l.getAmbient());
            float[] dif = toArray(l.getDiffuse());
            float[] spc = toArray(l.getSpecular());
            gl.glLightfv(id, GL_AMBIENT, amb, 0);
            gl.glLightfv(id, GL_DIFFUSE, dif, 0);
            gl.glLightfv(id, GL_SPECULAR, spc, 0);
            if (l.getType() == Light.Type.DIRECTIONAL) {
                org.joml.Vector3f d = l.getDirection();
                float[] pos = { -d.x, -d.y, -d.z, 0f };
                gl.glLightfv(id, GL_POSITION, pos, 0);
            } else {
                org.joml.Vector3f p = l.getPosition();
                float[] pos = { p.x, p.y, p.z, 1f };
                gl.glLightfv(id, GL_POSITION, pos, 0);
            }
        }
    }

    private float[] toArray(org.joml.Vector4f v) { return new float[] { v.x, v.y, v.z, v.w }; }

    @Override
    public RenderSurface createOffscreenSurface(int width, int height) {
        return new JoglOffscreenSurface(width, height);
    }

    @Override
    public BufferedImage readPixels(RenderSurface surface) {
        surface.makeCurrent();
        try {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            int w = surface.getWidth();
            int h = surface.getHeight();
            ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
            gl.glReadBuffer(GL.GL_BACK);
            gl.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int i = ((h - 1 - y) * w + x) * 4;
                    int r = buf.get(i)     & 0xFF;
                    int g = buf.get(i + 1) & 0xFF;
                    int b = buf.get(i + 2) & 0xFF;
                    int a = buf.get(i + 3) & 0xFF;
                    img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            return img;
        } finally {
            surface.release();
        }
    }

    @Override
    public TextureLoader getTextureLoader() { return textureLoader; }

    @Override
    public void dispose() { /* no global state */ }
}
