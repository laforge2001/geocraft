package org.geocraft.core.rendering.material;

import org.geocraft.core.rendering.backend.FilterMode;
import org.geocraft.core.rendering.backend.TextureHandle;

public final class TextureLayer {
    public enum CombineMode { REPLACE, MODULATE, DECAL }

    public final TextureHandle texture;
    public final FilterMode magFilter;
    public final FilterMode minFilter;
    public final CombineMode combineMode;

    public TextureLayer(TextureHandle texture, FilterMode mag, FilterMode min, CombineMode combine) {
        this.texture = texture;
        this.magFilter = mag;
        this.minFilter = min;
        this.combineMode = combine;
    }
}
