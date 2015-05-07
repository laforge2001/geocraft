/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.util;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.util.geom.BufferUtils;


/**
 * A simple factory class to generate ribbon strips.
 * 
 * @author Joshua Slack
 */
public class RibbonFactory {

  /**
   * Generate a triangle strip complete with surface normals and texture coordinates.
   * @param centerLine 
   *            the center points of our ribbon line.  The ribbon will be extruded along the line on either side of these points.
   * @param radius
   *            the distance to project up or down from the line.  If this is 0, the ribbon will be a single flat strip.
   * @param height
   *            the distance to project on either side of the center line
   * @param upVector
   *            the world up vector. Must be a unit vector.
   * @return our new ribbon Mesh.
   */
  public static Mesh generateRibbon(final List<Vector3> centerLine, final float radius, final float height,
      final Vector3 upVector) {
    if (centerLine == null || centerLine.size() < 2 || radius == 0) {
      return null;
    }

    if (height == 0) {
      return generateFlatRibbon(centerLine, radius, upVector);
    }

    return generateDiamondRibbon(centerLine, radius, height, upVector);
  }

  private static Mesh generateFlatRibbon(final List<Vector3> centerLine, final float radius, final Vector3 upVector) {

    Mesh ribbon = new Mesh("ribbon");
    ribbon.getMeshData().setIndexMode(IndexMode.TriangleStrip);

    final int size = centerLine.size() * 2;
    FloatBuffer verts = BufferUtils.createVector3Buffer(size);
    FloatBuffer norms = BufferUtils.createVector3Buffer(size);
    FloatBuffer texUVs = BufferUtils.createVector2Buffer(size);
    IntBuffer indices = BufferUtils.createIntBuffer(size);

    // populate our ribbon vertices...
    Vector3 prev = null;
    Vector3 current = null;
    Vector3 next = centerLine.get(0);
    Vector3 direction = new Vector3();
    Vector3 left = new Vector3();
    Vector3 working = new Vector3();
    Vector3 normal = new Vector3();
    for (int i = 1; i <= centerLine.size(); i++) {
      // Shuffle next, current, prev... Also start populating our
      // direction vector
      prev = current;
      current = next;
      int sides = 0;
      if (i < centerLine.size()) {
        next = centerLine.get(i);
        direction.set(next).subtractLocal(current);
        sides++;
      } else {
        next = null;
        direction.zero();
      }

      if (prev != null) {
        direction.addLocal(current).subtractLocal(prev);
        sides++;
      }

      if (sides != 1) {
        // must be 2. Take average direction
        direction.multiplyLocal(0.5f);
      }
      direction.normalizeLocal();

      // Use direction and world up to determine side vector
      upVector.normalize(Vector3.getTempInstance()).cross(direction, left);

      // Recross to determine normal vector
      direction.cross(left, normal);

      // Determine uv progress:
      float uv = (i - 1) / (float) (centerLine.size() - 1);

      // Now we can populate our verts using our radius
      left.multiplyLocal(radius);

      // left side
      working.set(current).addLocal(left);
      verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
      norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
      texUVs.put(0).put(uv);

      // right side
      working.set(current).subtractLocal(left);
      verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
      norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
      texUVs.put(1).put(uv);
    }

    // populate our indices -- would be nice if we could have jME assume
    // these.
    for (int i = 0; i < size; i++) {
      indices.put(i);
    }

    ribbon.getMeshData().setVertexBuffer(verts);
    ribbon.getMeshData().setNormalBuffer(norms);
    ribbon.getMeshData().setIndexBuffer(indices);
    ribbon.getMeshData().setTextureCoords(new TexCoords(texUVs, 2), 0);

    return ribbon;
  }

  private static Mesh generateDiamondRibbon(final List<Vector3> centerLine, final float radius, final float height,
      final Vector3 upVector) {

    Mesh ribbon = new Mesh("ribbon");
    ribbon.getMeshData().setIndexMode(IndexMode.TriangleStrip);

    final int size = centerLine.size() * 4;
    FloatBuffer verts = BufferUtils.createVector3Buffer(size);
    FloatBuffer norms = BufferUtils.createVector3Buffer(size);
    FloatBuffer texUVs = BufferUtils.createVector2Buffer(size);

    // populate our ribbon vertices...
    Vector3 prev = null;
    Vector3 current = null;
    Vector3 next = centerLine.get(0);
    Vector3 direction = new Vector3();
    Vector3 left = new Vector3();
    Vector3 up = new Vector3();
    Vector3 working = new Vector3();
    Vector3 normal = new Vector3();
    for (int i = 1; i <= centerLine.size(); i++) {
      // Shuffle next, current, prev... Also start populating our
      // direction vector
      prev = current;
      current = next;
      int sides = 0;
      if (i < centerLine.size()) {
        next = centerLine.get(i);
        direction.set(next).subtractLocal(current);
        sides++;
      } else {
        next = null;
        direction.zero();
      }

      if (prev != null) {
        direction.addLocal(current).subtractLocal(prev);
        sides++;
      }

      if (sides != 1) {
        // must be 2. Take average direction
        direction.multiplyLocal(0.5f);
      }
      direction.normalizeLocal();

      // Use direction and world up to determine side vector
      up.set(upVector).normalizeLocal().cross(direction, left);

      // Recross to determine normal vector
      direction.cross(left, normal);

      // Determine uv progress:
      float uv = (i - 1) / (float) (centerLine.size() - 1);

      // Now we can populate our verts using our radius
      left.multiplyLocal(radius);
      up.multiplyLocal(height);

      switch ((i - 1) % 4) {
        case 0:
          // left
          working.set(current).addLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) left.getX()).put((float) left.getY()).put((float) left.getZ());
          texUVs.put(0).put(uv);

          // top
          working.set(current).addLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
          texUVs.put(0.5f).put(uv);

          // right
          working.set(current).subtractLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -left.getX()).put((float) -left.getY()).put((float) -left.getZ());
          texUVs.put(1).put(uv);

          // bottom
          working.set(current).subtractLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -normal.getX()).put((float) -normal.getY()).put((float) -normal.getZ());
          texUVs.put(0.5f).put(uv);
          break;
        case 1:
          // top
          working.set(current).addLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
          texUVs.put(0.5f).put(uv);

          // right
          working.set(current).subtractLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -left.getX()).put((float) -left.getY()).put((float) -left.getZ());
          texUVs.put(1).put(uv);

          // bottom
          working.set(current).subtractLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -normal.getX()).put((float) -normal.getY()).put((float) -normal.getZ());
          texUVs.put(0.5f).put(uv);

          // left
          working.set(current).addLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) left.getX()).put((float) left.getY()).put((float) left.getZ());
          texUVs.put(0).put(uv);
          break;
        case 2:
          // right
          working.set(current).subtractLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -left.getX()).put((float) -left.getY()).put((float) -left.getZ());
          texUVs.put(1).put(uv);

          // bottom
          working.set(current).subtractLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -normal.getX()).put((float) -normal.getY()).put((float) -normal.getZ());
          texUVs.put(0.5f).put(uv);

          // left
          working.set(current).addLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) left.getX()).put((float) left.getY()).put((float) left.getZ());
          texUVs.put(0).put(uv);

          // top
          working.set(current).addLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
          texUVs.put(0.5f).put(uv);
          break;
        case 3:
          // bottom
          working.set(current).subtractLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -normal.getX()).put((float) -normal.getY()).put((float) -normal.getZ());
          texUVs.put(0.5f).put(uv);

          // left
          working.set(current).addLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) left.getX()).put((float) left.getY()).put((float) left.getZ());
          texUVs.put(0).put(uv);

          // top
          working.set(current).addLocal(up);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
          texUVs.put(0.5f).put(uv);

          // right
          working.set(current).subtractLocal(left);
          verts.put((float) working.getX()).put((float) working.getY()).put((float) working.getZ());
          norms.put((float) -left.getX()).put((float) -left.getY()).put((float) -left.getZ());
          texUVs.put(1).put(uv);
          break;
      }
    }

    // populate our indices
    IntBuffer indices = BufferUtils.createIntBuffer((centerLine.size() - 1) * 10);
    for (int s = 0, max = centerLine.size() - 1; s < max; s++) {
      indices.put(0 + s * 4);
      indices.put(1 + s * 4);
      indices.put(4 + s * 4);
      indices.put(2 + s * 4);
      indices.put(5 + s * 4);
      indices.put(3 + s * 4);
      indices.put(6 + s * 4);
      indices.put(0 + s * 4);
      indices.put(7 + s * 4);
      indices.put(4 + s * 4);
    }

    ribbon.getMeshData().setVertexBuffer(verts);
    ribbon.getMeshData().setNormalBuffer(norms);
    ribbon.getMeshData().setIndexBuffer(indices);
    ribbon.getMeshData().setTextureCoords(new TexCoords(texUVs, 2), 0);

    return ribbon;
  }
}
