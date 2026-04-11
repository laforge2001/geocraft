package org.geocraft.rendering.jogl;

import java.awt.image.BufferedImage;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.geocraft.core.rendering.backend.FilterMode;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.backend.TextureLoader;

public class JoglTextureLoader implements TextureLoader {

    @Override
    public TextureHandle loadTexture(BufferedImage image, FilterMode mag, FilterMode min) {
        GL2 gl = GLContext.getCurrentGL().getGL2();
        Texture tex = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);
        int id = tex.getTextureObject(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, toGlFilter(mag));
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, toGlFilter(min));
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        return new JoglTextureHandle(id, image.getWidth(), image.getHeight());
    }

    @Override
    public void disposeTexture(TextureHandle handle) {
        if (!(handle instanceof JoglTextureHandle)) return;
        JoglTextureHandle jh = (JoglTextureHandle) handle;
        if (jh.isDisposed()) return;
        GL2 gl = GLContext.getCurrentGL().getGL2();
        gl.glDeleteTextures(1, new int[] { jh.getGlId() }, 0);
        jh.markDisposed();
    }

    private int toGlFilter(FilterMode f) {
        switch (f) {
            case NEAREST:   return GL.GL_NEAREST;
            case BILINEAR:  return GL.GL_LINEAR;
            case TRILINEAR: return GL.GL_LINEAR_MIPMAP_LINEAR;
            default:        return GL.GL_LINEAR;
        }
    }
}
