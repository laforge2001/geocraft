package org.geocraft.core.rendering.scene;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MeshGeometry extends SceneNode {
    private FloatBuffer vertices;    // xyz triples
    private FloatBuffer normals;     // xyz triples, optional
    private FloatBuffer texCoords;   // uv pairs, optional
    private IntBuffer indices;       // triangle indices, optional
    private int vertexCount;
    private int triangleCount;

    public MeshGeometry(String name) { super(name); }

    public FloatBuffer getVertices() { return vertices; }
    public void setVertices(FloatBuffer v, int count) {
        this.vertices = v;
        this.vertexCount = count;
    }

    public FloatBuffer getNormals() { return normals; }
    public void setNormals(FloatBuffer n) { this.normals = n; }

    public FloatBuffer getTexCoords() { return texCoords; }
    public void setTexCoords(FloatBuffer t) { this.texCoords = t; }

    public IntBuffer getIndices() { return indices; }
    public void setIndices(IntBuffer i, int triangleCount) {
        this.indices = i;
        this.triangleCount = triangleCount;
    }

    public int getVertexCount() { return vertexCount; }
    public int getTriangleCount() { return triangleCount; }
}
