package org.geocraft.core.rendering.material;

public final class BlendMode {
    public final BlendFactor srcFactor;
    public final BlendFactor dstFactor;

    public BlendMode(BlendFactor src, BlendFactor dst) {
        this.srcFactor = src;
        this.dstFactor = dst;
    }

    public static BlendMode alphaBlend() {
        return new BlendMode(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static BlendMode additive() {
        return new BlendMode(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
    }
}
