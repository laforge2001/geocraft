package org.geocraft.core.model.grid;


public class CubeGridGeometry {

  double xOrigin;

  double yOrigin;

  int numXGridValues;

  int numYGridValues;

  int numEvents;

  int startXIndex;

  int endXIndex;

  int[] xDeltas;

  int startYIndex;

  int endYIndex;

  int[] yDeltas;

  int xSampling;

  int ySampling;

  public CubeGridGeometry(final double x, final double y, final int numX, final int numY, final int numEvents1, final int startX, final int endX, final int xSampling1, final int startY, final int endY, final int ySampling1, final int[] xDeltas1, final int[] yDeltas1) {
    xOrigin = x;
    yOrigin = y;
    numXGridValues = numX;
    numYGridValues = numY;
    this.numEvents = numEvents1;
    this.xDeltas = new int[numX];
    this.yDeltas = new int[numY];
    this.startXIndex = startX;
    this.startYIndex = startY;
    this.endXIndex = endX;
    this.endYIndex = endY;
    this.xSampling = xSampling1;
    this.ySampling = ySampling1;
    System.arraycopy(xDeltas1, 0, this.xDeltas, 0, xDeltas1.length);
    System.arraycopy(yDeltas1, 0, this.yDeltas, 0, yDeltas1.length);
  }

  /**
   * @return the xOrigin
   */
  public double getxOrigin() {
    return xOrigin;
  }

  /**
   * @return the yOrigin
   */
  public double getyOrigin() {
    return yOrigin;
  }

  /**
   * @return the numXGridValues
   */
  public int getNumXGridValues() {
    return numXGridValues;
  }

  /**
   * @return the numYGridValues
   */
  public int getNumYGridValues() {
    return numYGridValues;
  }

  /**
   * @return the xDeltas
   */
  public int[] getxDeltas() {
    return xDeltas;
  }

  /**
   * @return the yDeltas
   */
  public int[] getyDeltas() {
    return yDeltas;
  }

  /**
   * @return the startXIndex
   */
  public int getStartXIndex() {
    return startXIndex;
  }

  /**
   * @return the endXIndex
   */
  public int getEndXIndex() {
    return endXIndex;
  }

  /**
   * @return the startYIndex
   */
  public int getStartYIndex() {
    return startYIndex;
  }

  /**
   * @return the endYIndex
   */
  public int getEndYIndex() {
    return endYIndex;
  }

  public int getXsampling() {
    return xSampling;
  }

  public int getYsampling() {
    return ySampling;
  }

  public int getNumEvents() {
    return numEvents;
  }

}
