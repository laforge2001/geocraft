/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.light;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.common.GridLayoutHelper;


public class LightSourceCanvas extends Canvas implements PaintListener, MouseListener, MouseMoveListener,
    MouseTrackListener {

  private LightSourceModel _model;

  public static final int RADIUS = 10;

  public static final Color WHITE = new Color(null, 255, 255, 255);

  public static final Color BLACK = new Color(null, 0, 0, 0);

  public static final Color RED = new Color(null, 255, 0, 0);

  public static final Color YELLOW = new Color(null, 255, 255, 0);

  public LightSourceCanvas(final Composite parent, final LightSourceModel model) {
    super(parent, SWT.DOUBLE_BUFFERED);
    _model = model;
    addPaintListener(this);
    setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));
    setVisible(true);
    addMouseListener(this);
    addMouseMoveListener(this);
    addMouseTrackListener(this);
    setBackground(WHITE);
  }

  /**
   * Draws the target graphic
   * 
   * @param g the Graphics object.
   */
  public void paintControl(final PaintEvent event) {
    GC gc = event.gc;

    Rectangle rect = getBounds();
    int width = (int) (0.8 * rect.width);
    int x = (int) (0.1 * rect.width);
    int mid = (int) (0.5 * rect.width);

    // cross hairs
    gc.setLineWidth(1);
    gc.drawLine(x, mid, x + width, mid); // -
    gc.drawLine(mid, x, mid, x + width); // |

    // outer circle
    gc.setLineWidth(2);
    gc.setForeground(BLACK);
    gc.drawArc(x, x, width, width, 0, 360);

    // inner circle
    width = (int) (0.4 * rect.width);
    x = (int) (0.3 * rect.width);
    gc.setLineWidth(1);
    gc.drawArc(x, x, width, width, 0, 360);

    // the sun location
    int[] pt = getPixelCoords(_model.getElevation(), _model.getAzimuth());
    gc.setBackground(YELLOW);
    gc.fillArc(pt[0], pt[1], 10, 10, 0, 360);
    gc.setForeground(RED);
    gc.setLineWidth(2);
    gc.drawArc(pt[0], pt[1], 10, 10, 0, 360);

  }

  public int[] getPixelCoords(final int elevation, final int azimuth) {
    Rectangle rect = getBounds();
    int midPoint = (int) (0.5 * rect.width);
    int radius = (int) (0.4 * rect.width);

    double distance = (1. - elevation / 90.) * radius;
    double ang = Math.toRadians(azimuth);
    int xp = midPoint + (int) (Math.sin(ang) * distance);
    int yp = midPoint - (int) (Math.cos(ang) * distance);

    return new int[] { xp - RADIUS / 2, yp - RADIUS / 2 };
  }

  public int[] getElevationAndAzimuth(final int px, final int py) {
    Rectangle rect = getBounds();

    int midPoint = (int) (0.5 * rect.width);
    int radius = (int) (0.4 * rect.width);

    double distance = Math.hypot(px - midPoint, py - midPoint);

    //System.out.println(midPoint + " " + radius + " " + px + " " + py + " " + distance);

    double ratio = Math.max(0, 1.0 - distance / radius);
    int elevation = (int) (90 * ratio);

    double az = (int) Math.toDegrees(Math.atan2(py - midPoint, px - midPoint));
    az = az + 90; // angle from north
    if (az < 0) {
      az += 360;
    }

    return new int[] { elevation, (int) az };

  }

  @Override
  public void mouseDoubleClick(final MouseEvent e) {
    // System.out.println("Mouse event " + e);

  }

  @Override
  public void mouseDown(final MouseEvent e) {
    int[] sun = getElevationAndAzimuth(e.x, e.y);
    _model.setElevation(sun[0]);
    _model.setAzimuth(sun[1]);
    redraw();
  }

  @Override
  public void mouseUp(final MouseEvent e) {
    // System.out.println("Mouse event " + e);

  }

  @Override
  public void mouseMove(final MouseEvent e) {
    // TODO support mouse dragging 
  }

  @Override
  public void mouseEnter(final MouseEvent e) {
    // System.out.println("Mouse event " + e);

  }

  @Override
  public void mouseExit(final MouseEvent e) {
    // System.out.println("Mouse event " + e);

  }

  @Override
  public void mouseHover(final MouseEvent e) {
    // System.out.println("Mouse event " + e);

  }

}
