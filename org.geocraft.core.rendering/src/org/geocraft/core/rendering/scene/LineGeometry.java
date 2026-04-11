package org.geocraft.core.rendering.scene;

import java.nio.FloatBuffer;
import org.joml.Vector4f;

public class LineGeometry extends SceneNode {
    private FloatBuffer vertices;
    private int vertexCount;
    private float lineWidth = 1.0f;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public LineGeometry(String name) { super(name); }

    public FloatBuffer getVertices() { return vertices; }
    public void setVertices(FloatBuffer v, int count) {
        this.vertices = v;
        this.vertexCount = count;
    }
    public int getVertexCount() { return vertexCount; }

    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float w) { this.lineWidth = w; }

    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
