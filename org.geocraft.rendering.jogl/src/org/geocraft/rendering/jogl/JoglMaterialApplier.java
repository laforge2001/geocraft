package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.material.BlendFactor;
import org.geocraft.core.rendering.material.BlendMode;
import org.geocraft.core.rendering.material.DepthTestConfig;
import org.geocraft.core.rendering.material.LightingConfig;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.material.TextureLayer;
import org.geocraft.core.rendering.material.WireframeMode;

public class JoglMaterialApplier {
    // OpenGL fixed-function constants. Referenced numerically because their
    // JOGL declarations live in com.jogamp.opengl.fixedfunc, which is not
    // re-exported as API by the jogl-all OSGi bundle.
    private static final int GL_LIGHTING  = 0x0B50;
    private static final int GL_AMBIENT   = 0x1200;
    private static final int GL_DIFFUSE   = 0x1201;
    private static final int GL_SPECULAR  = 0x1202;
    private static final int GL_SHININESS = 0x1601;

    public void apply(GL2 gl, RenderMaterial m) {
        if (m == null) { resetDefaults(gl); return; }

        BlendMode b = m.getBlendMode();
        if (b != null) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(toGl(b.srcFactor), toGl(b.dstFactor));
        } else {
            gl.glDisable(GL.GL_BLEND);
        }

        DepthTestConfig d = m.getDepthTestConfig();
        if (d != null && d.enabled) {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(toGl(d.func));
        } else if (d != null) {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }

        WireframeMode w = m.getWireframeMode();
        if (w != null) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, w.enabled ? GL2.GL_LINE : GL2.GL_FILL);
            gl.glLineWidth(w.lineWidth);
            if (w.antialiased) gl.glEnable(GL.GL_LINE_SMOOTH);
            else               gl.glDisable(GL.GL_LINE_SMOOTH);
        }

        LightingConfig l = m.getLightingConfig();
        if (l != null && l.enabled) {
            gl.glEnable(GL_LIGHTING);
            float[] amb = { l.ambient.x, l.ambient.y, l.ambient.z, l.ambient.w };
            float[] dif = { l.diffuse.x, l.diffuse.y, l.diffuse.z, l.diffuse.w };
            float[] spc = { l.specular.x, l.specular.y, l.specular.z, l.specular.w };
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL_AMBIENT, amb, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL_DIFFUSE, dif, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL_SPECULAR, spc, 0);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL_SHININESS, l.shininess);
        } else {
            gl.glDisable(GL_LIGHTING);
        }

        TextureLayer t = m.getTextureLayer();
        if (t != null && t.texture instanceof JoglTextureHandle) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, ((JoglTextureHandle) t.texture).getGlId());
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
    }

    private void resetDefaults(GL2 gl) {
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glDisable(GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    private int toGl(BlendFactor f) {
        switch (f) {
            case ZERO:                  return GL.GL_ZERO;
            case ONE:                   return GL.GL_ONE;
            case SRC_ALPHA:             return GL.GL_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA:   return GL.GL_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA:             return GL.GL_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA:   return GL.GL_ONE_MINUS_DST_ALPHA;
            case SRC_COLOR:             return GL.GL_SRC_COLOR;
            case ONE_MINUS_SRC_COLOR:   return GL.GL_ONE_MINUS_SRC_COLOR;
            case DST_COLOR:             return GL.GL_DST_COLOR;
            case ONE_MINUS_DST_COLOR:   return GL.GL_ONE_MINUS_DST_COLOR;
            default:                    return GL.GL_ONE;
        }
    }

    private int toGl(DepthTestConfig.CompareFunc f) {
        switch (f) {
            case NEVER:             return GL.GL_NEVER;
            case LESS:              return GL.GL_LESS;
            case EQUAL:             return GL.GL_EQUAL;
            case LESS_OR_EQUAL:     return GL.GL_LEQUAL;
            case GREATER:           return GL.GL_GREATER;
            case NOT_EQUAL:         return GL.GL_NOTEQUAL;
            case GREATER_OR_EQUAL:  return GL.GL_GEQUAL;
            case ALWAYS:            return GL.GL_ALWAYS;
            default:                return GL.GL_LEQUAL;
        }
    }
}
