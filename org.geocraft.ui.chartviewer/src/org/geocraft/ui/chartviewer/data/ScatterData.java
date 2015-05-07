package org.geocraft.ui.chartviewer.data;


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.defs.PointStyle;


public class ScatterData extends AbstractChartData {

  private final int _numPoints;

  private final float[] _xs;

  private final float[] _ys;

  private final RGB _pointRGB;

  private final PointStyle _pointStyle;

  private final int _pointSize;

  private boolean _connectPoints;

  private String[] _labels;

  private Font _labelFont;

  public ScatterData(final String name, final float[] xs, final float[] ys, final RGB rgb, final PointStyle style, final int size) {
    super(name);
    if (xs.length != ys.length) {
      throw new IllegalArgumentException("The length of the x array (" + xs.length
          + ") does not equals the length of the y array (" + ys.length + ").");
    }
    _numPoints = xs.length;
    _xs = copyArray(xs);
    _ys = copyArray(ys);
    _pointRGB = rgb;
    _pointStyle = style;
    _pointSize = size;
    _connectPoints = false;
  }

  public void setConnectPoints(final boolean connect) {
    _connectPoints = connect;
  }

  public boolean getConnectPoints() {
    return _connectPoints;
  }

  public int getNumPoints() {
    return _numPoints;
  }

  public float getX(final int index) {
    return _xs[index];
  }

  public float getY(final int index) {
    return _ys[index];
  }

  public float[] getXs() {
    return copyArray(_xs);
  }

  public float[] getYs() {
    return copyArray(_ys);
  }

  public RGB getPointRGB() {
    return _pointRGB;
  }

  public PointStyle getPointStyle() {
    return _pointStyle;
  }

  public int getPointSize() {
    return _pointSize;
  }

  private static float[] copyArray(final float[] in) {
    float[] out = new float[in.length];
    System.arraycopy(in, 0, out, 0, in.length);
    return out;
  }

  public void addLabels(final String[] labels, final Font labelFont) {
    _labels = labels;
    _labelFont = labelFont;
  }

  public String getLabel(final int index) {
    return _labels[index];
  }

  public Font getLabelFont() {
    return _labelFont;
  }

  /**
   * @return
   */
  public boolean hasLabels() {
    return _labels != null && _labels.length == _numPoints;
  }

}
