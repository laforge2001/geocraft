package org.geocraft.core.rendering.backend;

import java.awt.image.BufferedImage;

public interface TextureLoader {
    TextureHandle loadTexture(BufferedImage image, FilterMode mag, FilterMode min);
    void disposeTexture(TextureHandle handle);
}
