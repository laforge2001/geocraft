package org.geocraft.core.rendering.scene;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextOverlay extends SceneNode {
    private String text;
    private Vector2f screenPosition = new Vector2f();
    private int fontSize = 12;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public TextOverlay(String name, String text) {
        super(name);
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String t) { this.text = t; }
    public Vector2f getScreenPosition() { return new Vector2f(screenPosition); }
    public void setScreenPosition(Vector2f p) { this.screenPosition.set(p); }
    public int getFontSize() { return fontSize; }
    public void setFontSize(int s) { this.fontSize = s; }
    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
