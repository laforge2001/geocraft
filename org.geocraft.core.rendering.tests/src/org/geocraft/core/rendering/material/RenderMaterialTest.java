package org.geocraft.core.rendering.material;

import org.junit.Test;
import static org.junit.Assert.*;

public class RenderMaterialTest {
    @Test
    public void defaultMaterialHasNoStates() {
        RenderMaterial m = new RenderMaterial();
        assertNull(m.getBlendMode());
        assertNull(m.getTextureLayer());
        assertNull(m.getLightingConfig());
        assertNull(m.getWireframeMode());
        assertNull(m.getDepthTestConfig());
    }

    @Test
    public void builderComposesStates() {
        RenderMaterial m = new RenderMaterial()
            .withBlendMode(BlendMode.alphaBlend())
            .withWireframe(new WireframeMode(true, 1.5f, true))
            .withDepthTest(new DepthTestConfig(true, DepthTestConfig.CompareFunc.LESS_OR_EQUAL));
        assertNotNull(m.getBlendMode());
        assertTrue(m.getWireframeMode().enabled);
        assertEquals(1.5f, m.getWireframeMode().lineWidth, 1e-6);
    }

    @Test
    public void alphaBlendHasStandardFactors() {
        BlendMode b = BlendMode.alphaBlend();
        assertEquals(BlendFactor.SRC_ALPHA, b.srcFactor);
        assertEquals(BlendFactor.ONE_MINUS_SRC_ALPHA, b.dstFactor);
    }
}
