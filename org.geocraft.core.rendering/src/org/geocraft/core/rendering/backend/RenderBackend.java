package org.geocraft.core.rendering.backend;

import java.awt.image.BufferedImage;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;

public interface RenderBackend {
    void initialize(RenderSurface surface);
    void renderPass(GroupNode root, Camera camera, Light[] lights);
    void renderPass(GroupNode root, Camera camera, Light[] lights, RenderMaterial overrideMaterial);
    RenderSurface createOffscreenSurface(int width, int height);
    BufferedImage readPixels(RenderSurface surface);
    TextureLoader getTextureLoader();
    void dispose();
}
