/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer.renderer.util;


import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Point3d;
import org.joml.Vector3f;
import org.joml.Vector4f;


/**
 * Helper class for the volume viewer.
 */
public class VolumeViewerHelper {

  /**
   * Transform an SWT RGB to a color vector.
   */
  public static Vector4f colorToColorRGBA(final RGB rgb, final float transparency) {
    return new Vector4f(rgb.red / 255f, rgb.green / 255f, rgb.blue / 255f, transparency);
  }

  /**
   * Transform an AWT color to a color vector.
   */
  public static Vector4f colorToColorRGBA(final Color col, final float alpha) {
    return new Vector4f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, alpha);
  }

  /**
   * Transform an SWT color to a color vector.
   */
  public static Vector4f swtColorToColorRGBA(final org.eclipse.swt.graphics.Color col, final float alpha) {
    return new Vector4f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, alpha);
  }

  /**
   * Transform a RGB color to a color vector.
   */
  public static Vector4f rgbToColorRGBA(final RGB col, final float transparency) {
    return new Vector4f(col.red / 255f, col.green / 255f, col.blue / 255f, transparency);
  }

  /**
   * Transform a color vector to an SWT RGB.
   */
  public static RGB colorRGBAToRGB(final Vector4f col) {
    return new RGB((int) (col.x * 255), (int) (col.y * 255), (int) (col.z * 255));
  }

  /**
   * Transforms an SWT color to an AWT color.
   */
  public static Color swtColorToColor(final RGB col) {
    return new Color(col.red, col.green, col.blue);
  }

  /**
   * Transform a model point to a Vector3f.
   */
  public static Vector3f point3dToVector3(final Point3d point) {
    return new Vector3f((float) point.getX(), (float) point.getY(), (float) point.getZ());
  }

  /**
   * Transform an array of model points to an array of Vector3f.
   */
  public static Vector3f[] points3dToVector3(final Point3d[] points) {
    final Vector3f[] vector = new Vector3f[points.length];
    for (int i = 0; i < points.length; i++) {
      vector[i] = point3dToVector3(points[i]);
    }
    return vector;
  }

  /**
   * Compute and return the points with z equal to zero.
   */
  public static Point3d[] zeroZPointData(final Point3d[] points) {
    final Point3d[] newPoints = new Point3d[points.length];
    for (int i = 0; i < points.length; i++) {
      newPoints[i] = new Point3d(points[i].getX(), points[i].getY(), 0.0);
    }
    return newPoints;
  }
}
