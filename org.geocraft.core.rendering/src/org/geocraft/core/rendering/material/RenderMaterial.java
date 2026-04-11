package org.geocraft.core.rendering.material;

public class RenderMaterial {
    private BlendMode blendMode;
    private TextureLayer textureLayer;
    private LightingConfig lightingConfig;
    private WireframeMode wireframeMode;
    private DepthTestConfig depthTestConfig;

    public BlendMode getBlendMode() { return blendMode; }
    public RenderMaterial withBlendMode(BlendMode b) { this.blendMode = b; return this; }

    public TextureLayer getTextureLayer() { return textureLayer; }
    public RenderMaterial withTextureLayer(TextureLayer t) { this.textureLayer = t; return this; }

    public LightingConfig getLightingConfig() { return lightingConfig; }
    public RenderMaterial withLighting(LightingConfig l) { this.lightingConfig = l; return this; }

    public WireframeMode getWireframeMode() { return wireframeMode; }
    public RenderMaterial withWireframe(WireframeMode w) { this.wireframeMode = w; return this; }

    public DepthTestConfig getDepthTestConfig() { return depthTestConfig; }
    public RenderMaterial withDepthTest(DepthTestConfig d) { this.depthTestConfig = d; return this; }
}
