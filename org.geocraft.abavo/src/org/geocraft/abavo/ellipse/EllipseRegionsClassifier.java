/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import org.geocraft.abavo.classbkg.IRegionsClassifier;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;


/**
 * The process for classifying points using the ellipse regions model.
 */
public class EllipseRegionsClassifier implements IRegionsClassifier {

  protected double _minEllipseSlope;

  protected double _minEllipseLength;

  protected double _minEllipseWidth;

  protected double _maxEllipseSlope;

  protected double _maxEllipseLength;

  protected double _maxEllipseWidth;

  protected double[] _regLineOuterX;

  protected double[] _regLineOuterY;

  protected double[] _regLineInnerX;

  protected double[] _regLineInnerY;

  protected double[][] _classRegionsX;

  protected double[][] _classRegionsY;

  protected int[] _numClassRegions;

  protected double _normalization;

  protected double _xMin;

  protected double _xMax;

  protected double _yMin;

  protected double _yMax;

  protected double _costerm;

  protected double _sinterm;

  protected double _xWeight;

  protected double _yWeight;

  protected double _xCenter;

  protected double _yCenter;

  protected int _lumCount = 6;

  protected boolean _isValid;

  /**
   * The default constructor.
   * @param ellipseModel the ellipse regions model.
   * @param xmin the minimum x-coordinate.
   * @param xmax the maximum x-coordinate.
   * @param ymin the minimum y-coordinate.
   * @param ymax the maximum y-coordinate.
   * @exception Exception thrown on setup errors.
   */
  public EllipseRegionsClassifier(final EllipseRegionsModel ellipseModel, final double normalization, final double xmin, final double xmax, final double ymin, final double ymax) {
    this(ellipseModel.getEllipseModel(EllipseType.Background).getSlope(), ellipseModel.getEllipseModel(
        EllipseType.Background).getLength(), ellipseModel.getEllipseModel(EllipseType.Background).getWidth(),
        ellipseModel.getEllipseModel(EllipseType.Maximum).getSlope(), ellipseModel.getEllipseModel(EllipseType.Maximum)
            .getLength(), ellipseModel.getEllipseModel(EllipseType.Maximum).getWidth(), ellipseModel
            .getRegionBoundariesOuterXs(EllipseRegionsModel.getRegionBoundaries()), ellipseModel
            .getRegionBoundariesOuterYs(EllipseRegionsModel.getRegionBoundaries()), ellipseModel
            .getRegionBoundariesInnerXs(EllipseRegionsModel.getRegionBoundaries()), ellipseModel
            .getRegionBoundariesInnerYs(EllipseRegionsModel.getRegionBoundaries()), normalization, xmin, xmax, ymin,
        ymax, ellipseModel.getEllipseModel(EllipseType.Background).getCenterX(), ellipseModel.getEllipseModel(
            EllipseType.Background).getCenterY());
  }

  public EllipseRegionsClassifier(final double ellipseMinSlope, final double ellipseMinLength, final double ellipseMinWidth, final double ellipseMaxSlope, final double ellipseMaxLength, final double ellipseMaxWidth, final double[] outerXs, final double[] outerYs, final double[] innerXs, final double[] innerYs, final double normalization, final double xmin, final double xmax, final double ymin, final double ymax, final double xCenter, final double yCenter) {

    _normalization = normalization;

    _xMin = xmin;
    _xMax = xmax;
    _yMin = ymin;
    _yMax = ymax;
    _xCenter = xCenter;
    _yCenter = yCenter;
    _numClassRegions = new int[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    _classRegionsX = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES][5];
    _classRegionsY = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES][5];
    _regLineOuterX = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    _regLineOuterY = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    _regLineInnerX = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    _regLineInnerY = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    _minEllipseSlope = ellipseMinSlope;
    _minEllipseLength = ellipseMinLength;
    _minEllipseWidth = ellipseMinWidth;
    _maxEllipseSlope = ellipseMaxSlope;
    _maxEllipseLength = ellipseMaxLength;
    _maxEllipseWidth = ellipseMaxWidth;
    RegionsBoundary[] regionBounds = { RegionsBoundary.P1toP2, RegionsBoundary.P2toP3, RegionsBoundary.P3toP4,
        RegionsBoundary.P4toNULL, RegionsBoundary.NULLtoN1, RegionsBoundary.N1toN2, RegionsBoundary.N2toN3,
        RegionsBoundary.N3toN4, RegionsBoundary.N4toNULL, RegionsBoundary.NULLtoP1 };

    for (int i = 0; i < regionBounds.length; i++) {
      _regLineOuterX[i] = outerXs[i];
      _regLineOuterY[i] = outerYs[i];
      _regLineInnerX[i] = innerXs[i];
      _regLineInnerY[i] = innerYs[i];
    }
    _isValid = false;

    if (_minEllipseSlope != _maxEllipseSlope) {
      _isValid = false;
      throw new RuntimeException("The background and maximum ellipse slopes do not match.");
    }

    initializeClassRegions(_xMin, _yMin, _xMax, _yMax, _minEllipseSlope, _regLineOuterX, _regLineOuterY,
        _regLineInnerX, _regLineInnerY);

    double m = _minEllipseSlope;

    _costerm = 1 / Math.sqrt(1 + m * m);
    _sinterm = _costerm * m;
    _xWeight = (_maxEllipseLength - _minEllipseLength) / _lumCount;
    _yWeight = (_maxEllipseWidth - _minEllipseWidth) / _lumCount;

    _isValid = true;
  }

  public String getName() {
    return "Ellipse Regions Classification";
  }

  /**
   * Processes the A,B coordinate.
   * @param a the A coordinate.
   * @param b the B coordinate.
   * @return the class background value.
   */
  public double processAB(final double a, final double b) {

    double value = Double.NaN;
    if (!_isValid) {
      return value;
    }

    double[] sign = { 1, 1, 1, 0, -1, -1, -1, -1, 0, 1 };
    double[] sv = { 0.25, 0.5, 0.75, 0, 0, .25, .5, .75, 0, 0 };
    int i;
    int ir = 0;
    int jr;
    int jrp;
    double sqterm = 0;

    double xold = a - _xCenter;
    double yold = b - _yCenter;

    // Rotate pixels to slope of regression
    double xnew = xold * _costerm + yold * _sinterm;
    double ynew = yold * _costerm - xold * _sinterm;
    for (i = 0; i <= _lumCount; i++) {
      double xscd = xnew / (_minEllipseLength + i * _xWeight);
      double yscd = ynew / (_minEllipseWidth + i * _yWeight);
      sqterm = xscd * xscd + yscd * yscd;
      if (sqterm < 1) {
        break;
      }
    }
    double gradation = (i + 1) * 0.03125 - 0.015625;

    // If inside background ellipse, set to 0.
    if (i == 0) {
      value = Double.NaN;
    } else {

      for (ir = 0; ir < 10; ir++) {

        for (jr = 1, jrp = 0; jr < 4; jrp = jr, jr++) {
          if ((_classRegionsY[ir][jr] - b) * (_classRegionsX[ir][jrp] - a) - (_classRegionsX[ir][jr] - a)
              * (_classRegionsY[ir][jrp] - b) < 0) {
            break;
          }
        }
        if (jr >= 4) {
          break;
        }
      }
      if (ir < 10) {
        value = sign[ir] * _normalization * (sv[ir] + gradation);
      }
    }

    return value;
  }

  /**
   * Initialize the class regions.
   * @param xmin the minimum x-coordinate.
   * @param ymin the minimum y-coordinate.
   * @param xmax the maximum x-coordinate.
   * @param ymax the maximum y-coordinate.
   * @param slope the slope of the ellipses
   * @param outerX the array of region bound outer x-cooridnates.
   * @param outerY the array of region bound outer y-cooridnates.
   * @param innerX the array of region bound inner x-cooridnates.
   * @param innerY the array of region bound inner y-cooridnates.
   * @exception Exception thrown on region prep errors.
   */
  public void initializeClassRegions(final double xmin, final double ymin, final double xmax, final double ymax,
      final double slope, final double[] outerX, final double[] outerY, final double[] innerX, final double[] innerY) {

    if (Double.isNaN(slope)) {
      throw new IllegalArgumentException("Invalid ellipse regions model.");
    }
    int i;
    int j;
    int k;

    for (i = 0; i < EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES; i++) {
      _numClassRegions[i] = 0;
    }
    if (slope < 0) {

      for (i = 0, j = 9, k = 0; i < EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES; j = i, i++, k++) {

        _classRegionsX[i][0] = outerX[k];
        _classRegionsY[i][0] = outerY[k];
        _classRegionsX[i][1] = innerX[k];
        _classRegionsY[i][1] = innerY[k];
        _classRegionsX[j][2] = _classRegionsX[i][1];
        _classRegionsY[j][2] = _classRegionsY[i][1];
        _classRegionsX[j][3] = _classRegionsX[i][0];
        _classRegionsY[j][3] = _classRegionsY[i][0];
      }
    } else {

      for (i = 5, j = 6, k = 0; k < EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES; j = i, i--, k++) {

        if (i < 0) {
          i = 9;
        }
        _classRegionsX[i][0] = outerX[k];
        _classRegionsY[i][0] = outerY[k];
        _classRegionsX[i][1] = innerX[k];
        _classRegionsY[i][1] = innerY[k];
        _classRegionsX[j][2] = _classRegionsX[i][1];
        _classRegionsY[j][2] = _classRegionsY[i][1];
        _classRegionsX[j][3] = _classRegionsX[i][0];
        _classRegionsY[j][3] = _classRegionsY[i][0];
      }
    }
    for (i = 0; i < EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES; i++) {

      int regionId = 0;

      if (i == 3 || i == 8) {
        regionId = 0;
      } else {

        if (i == 9) {
          regionId = 1;
        } else {
          if (i < 3) {
            regionId = i + 2;
          } else {
            regionId = 3 - i;
          }
        }
      }
      if (_classRegionsX[i][3] == xmin) {

        if (_classRegionsX[i][0] == xmin) {
          _numClassRegions[i] = 4; // ok
        } else {

          if (_classRegionsY[i][0] == ymin) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmin;
            _classRegionsY[i][4] = ymin;
          } else if (_classRegionsY[i][0] == ymax) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmin;
            _classRegionsY[i][4] = ymax;
          } else {
            throw new IllegalArgumentException("Closure of Region A " + regionId
                + "\nMake sure region bounds do not cross.");
          }
        }
      } else if (_classRegionsX[i][3] == xmax) {

        if (_classRegionsX[i][0] == xmax) {
          _numClassRegions[i] = 4; // ok
        } else {

          if (_classRegionsY[i][0] == ymin) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmax;
            _classRegionsY[i][4] = ymin;
          } else if (_classRegionsY[i][0] == ymax) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmax;
            _classRegionsY[i][4] = ymax;
          } else {
            throw new IllegalArgumentException("Closure of Region B " + regionId
                + "\nMake sure region bounds do not cross.");
          }
        }
      } else if (_classRegionsY[i][3] == ymin) {

        if (_classRegionsY[i][0] == ymin) {
          _numClassRegions[i] = 4; // ok
        } else {

          if (_classRegionsX[i][0] == xmin) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmin;
            _classRegionsY[i][4] = ymin;
          } else if (_classRegionsX[i][0] == xmax) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmax;
            _classRegionsY[i][4] = ymin;
          } else {
            throw new IllegalArgumentException("Closure of Region C " + regionId
                + "\nMake sure region bounds do not cross.");
          }
        }
      } else if (_classRegionsY[i][3] == ymax) {

        if (_classRegionsY[i][0] == ymax) {
          _numClassRegions[i] = 4; // ok
        } else {

          if (_classRegionsX[i][0] == xmin) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmin;
            _classRegionsY[i][4] = ymax;
          } else if (_classRegionsX[i][0] == xmax) {
            _numClassRegions[i] = 5; // ok
            _classRegionsX[i][4] = xmax;
            _classRegionsY[i][4] = ymax;
          } else {
            throw new IllegalArgumentException("Closure of Region D " + regionId
                + "\nMake sure region bounds do not cross.");
          }
        }
      }// else {
      //  throw new Exception("Closure of RegionE " + regionId);
      //}
      for (j = 2; j < _numClassRegions[i]; j++) {
        if ((_classRegionsY[i][j] - _classRegionsY[i][0]) * (_classRegionsX[i][j - 1] - _classRegionsX[i][0])
            - (_classRegionsX[i][j] - _classRegionsX[i][0]) * (_classRegionsY[i][j - 1] - _classRegionsY[i][0]) < 0) {
          throw new IllegalArgumentException("Region " + regionId + " not Convex"
              + "\nMake sure region bounds do not cross.");
        }
      }
    }
  }
}
