/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.util;


import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Point3d;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;


/**
 * Helper class for the volume viewer.
 */
public class VolumeViewerHelper {

  /**
   * Transform an SWT RGB to a ColorRGBA.
   * @param rgb the RGB
   * @param transparency the transparency value
   * @return the ColorRGBA
   */
  public static ColorRGBA colorToColorRGBA(final RGB rgb, final float transparency) {
    return new ColorRGBA(rgb.red / 255f, rgb.green / 255f, rgb.blue / 255f, transparency);
  }

  /**
   * Transform an AWT color to a ColorRGBA.
   * @param col the color
   * @param alpah the alpha value value (0-1)
   * @return the ColorRGBA
   */
  public static ColorRGBA colorToColorRGBA(final Color col, final float alpha) {
    return new ColorRGBA(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, alpha);
  }

  /**
   * Transform an SWT color to a ColorRGBA.
   * @param col the color
   * @param alpha the alpha value (0-1)
   * @return the ColorRGBA
   */
  public static ColorRGBA swtColorToColorRGBA(final org.eclipse.swt.graphics.Color col, final float alpha) {
    return new ColorRGBA(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, alpha);
  }

  /**
   * Transform a RGB color to a ColorRGBA.
   * @param col the color
   * @param transparency the transparency value
   * @return the ColorRGBA
   */
  public static ColorRGBA rgbToColorRGBA(final RGB col, final float transparency) {
    return new ColorRGBA(col.red / 255f, col.green / 255f, col.blue / 255f, transparency);
  }

  /**
   * Transform ColorRGBA to a RGB color.
   * @param col the color
   * @return the RGB
   */
  public static RGB colorRGBAToRGB(final ColorRGBA col) {
    return new RGB((int) (col.getRed() * 255), (int) (col.getGreen() * 255), (int) (col.getBlue() * 255));
  }

  /**
   * Transforms an SWT color to an AWT color. 
   * @param col the SWT color
   * @return the AWT color
   */
  public static Color swtColorToColor(final RGB col) {
    return new Color(col.red, col.green, col.blue);
  }

  /**
   * Transform a model point to a Vector3.
   * @param point the model point
   * @return the corresponding Vector3
   */
  public static Vector3 point3dToVector3(final Point3d point) {
    return new Vector3((float) point.getX(), (float) point.getY(), (float) point.getZ());
  }

  /**
   * Transform an array of model points to an array of Vector3.
   * @param points the array of points
   * @return the array of Vector3
   */
  public static Vector3[] points3dToVector3(final Point3d[] points) {
    final Vector3[] vector = new Vector3[points.length];
    for (int i = 0; i < points.length; i++) {
      vector[i] = point3dToVector3(points[i]);
    }
    return vector;
  }

  /**
   * Compute and return the points with z equal to zero
   * @param points the points
   * @return the computed points
   */
  public static Point3d[] zeroZPointData(final Point3d[] points) {
    final Point3d[] newPoints = new Point3d[points.length];
    for (int i = 0; i < points.length; i++) {
      newPoints[i] = new Point3d(points[i].getX(), points[i].getY(), 0.0);
    }
    return newPoints;
  }

  //  public static List<VolumeView> getOpenedVolumeViews() {
  //    final List<VolumeView> views = new ArrayList<VolumeView>();
  //    final IWorkbench workbench = PlatformUI.getWorkbench();
  //    for (final IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
  //      for (final IWorkbenchPage page : window.getPages()) {
  //        for (final IViewReference part : page.getViewReferences()) {
  //          if (part.getId().equals("org.geocraft.ui.volumeviewer.VolumeView")) {
  //            final VolumeView view = (VolumeView) part.getView(false);
  //            if (view != null) {
  //              views.add(view);
  //            }
  //          }
  //        }
  //      }
  //    }
  //    return views;
  //  }
}
