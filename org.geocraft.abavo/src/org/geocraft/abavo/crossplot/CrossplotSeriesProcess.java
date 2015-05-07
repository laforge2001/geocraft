/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.input.AbstractInputProcess;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.math.regression.RegressionType;


public class CrossplotSeriesProcess {

  protected final IABavoCrossplot _crossplot;

  protected int _dataPointCount = 0;

  protected float[] _dataA = null;

  protected float[] _dataB = null;

  protected double[] _dataX = null;

  protected double[] _dataY = null;

  protected float[] _dataZ = null;

  protected final Unit _xyUnit;

  protected final Unit _zUnit;

  protected final Domain _domain;

  protected List<Float> _tempA;

  protected List<Float> _tempB;

  protected List<Double> _tempX;

  protected List<Double> _tempY;

  protected List<Float> _tempZ;

  protected final RegressionType _regressionType;

  public CrossplotSeriesProcess(final IABavoCrossplot crossplot, final Unit xyUnit, final Unit zUnit, final Domain domain, final RegressionType regressionType) {
    _crossplot = crossplot;
    _xyUnit = xyUnit;
    _zUnit = zUnit;
    _domain = domain;
    _regressionType = regressionType;

    _dataPointCount = 0;
    _dataA = null;
    _dataB = null;
    _dataX = null;
    _dataY = null;
    _dataZ = null;
    _tempA = Collections.synchronizedList(new ArrayList<Float>());
    _tempB = Collections.synchronizedList(new ArrayList<Float>());
    _tempX = Collections.synchronizedList(new ArrayList<Double>());
    _tempY = Collections.synchronizedList(new ArrayList<Double>());
    _tempZ = Collections.synchronizedList(new ArrayList<Float>());
  }

  public String getName() {
    return "Crossplot Series";
  }

  public boolean isTraceGenerator() {
    return false;
  }

  public float[] getDataA() {
    return _dataA;
  }

  public float[] getDataB() {
    return _dataB;
  }

  public double[] getDataX() {
    return _dataX;
  }

  public double[] getDataY() {
    return _dataY;
  }

  public float[] getDataZ() {
    return _dataZ;
  }

  public TraceData[] process(final TraceData[] data) {
    if (data.length <= 0) {
      return data;
    }
    if (data.length != 2) {
      throw new RuntimeException("AB Crossplot: Number of input trace data objects must be 2!");
    }
    TraceData dataA = data[AbstractInputProcess.PRE_PROCESSES_A_TRACE];
    TraceData dataB = data[AbstractInputProcess.PRE_PROCESSES_B_TRACE];
    if (dataA.getNumTraces() != dataB.getNumTraces()) {
      throw new RuntimeException("AB Crossplot: Number of input traces do not match!");
    }
    for (int i = 0; i < dataA.getNumTraces(); i++) {
      Trace traceA = dataA.getTrace(i);
      Trace traceB = dataB.getTrace(i);
      double rx = traceA.getX();
      double ry = traceA.getY();
      for (int j = 0; j < traceA.getNumSamples(); j++) {
        if (!Double.isNaN(traceA.getData()[j])) {
          double z = traceA.getZStart() + j * traceA.getZDelta();
          int k = (int) Math.round((z - traceB.getZStart()) / traceB.getZDelta());
          if (k >= 0 && k < traceB.getNumSamples()) {
            float a = traceA.getData()[j];
            float b = traceB.getData()[k];
            if (!Float.isNaN(a) && !Float.isNaN(b)) {
              _tempA.add(new Float(a));
              _tempB.add(new Float(b));
              _tempX.add(new Double(rx));
              _tempY.add(new Double(ry));
              _tempZ.add(new Float(z));
              _dataPointCount++;
            }
            /*
             * if(_dataPointCount == 0) { _minimumA = a; _maximumA = a; _minimumB =
             * b; _maximumB = b; _minimumZ = z; _maximumZ = z; } else { _minimumA =
             * Math.min(_minimumA, a); _maximumA = Math.max(_maximumA, a); _minimumB =
             * Math.min(_minimumB, b); _maximumB = Math.max(_maximumB, b); _minimumZ =
             * Math.min(_minimumZ, z); _maximumZ = Math.max(_maximumZ, z); }
             * _dataPointCount++;
             */
          }
        }
      }
    }
    return data;
  }

  public void cleanup() {
    if (_dataPointCount > 0) {
      _dataA = new float[_dataPointCount];
      _dataB = new float[_dataPointCount];
      _dataX = new double[_dataPointCount];
      _dataY = new double[_dataPointCount];
      _dataZ = new float[_dataPointCount];
      for (int i = 0; i < _dataPointCount; i++) {
        _dataA[i] = _tempA.get(i).floatValue();
        _dataB[i] = _tempB.get(i).floatValue();
        _dataX[i] = _tempX.get(i).doubleValue();
        _dataY[i] = _tempY.get(i).doubleValue();
        _dataZ[i] = _tempZ.get(i).floatValue();
      }
    }

    if (_dataPointCount > 0) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          //System.out.println("Adding " + _dataPointCount + " points to crossplot " + _crossplot);
          if (_crossplot != null) {
            ABDataSeries series = new ABDataSeries("", 0, _dataPointCount, _dataA, _dataB, _dataX, _dataY, _dataZ,
                _xyUnit, _zUnit, _domain, _regressionType);
            _crossplot.addObjects(new Object[] { series });
            int status = 0;
          }
        }
      });
    }
    _tempA.clear();
    _tempB.clear();
    _tempX.clear();
    _tempY.clear();
    _tempZ.clear();

    System.gc();
  }

}
