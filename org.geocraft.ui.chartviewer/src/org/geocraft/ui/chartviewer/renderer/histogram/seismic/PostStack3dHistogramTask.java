/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.seismic;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.histogram.seismic.PostStack3dRendererModel.DataBounds;


public class PostStack3dHistogramTask extends BackgroundTask {

  private PostStack3d _ps3d;

  private PostStack3dRendererModel _model;

  private PostStack3dRenderer _renderer;

  public PostStack3dHistogramTask(PostStack3d ps3d, PostStack3dRendererModel model, PostStack3dRenderer renderer) {
    _ps3d = ps3d;
    _model = model;
    _renderer = renderer;
  }

  @Override
  public Object compute(ILogger logger, IProgressMonitor monitor) {
    float minValue = Float.MAX_VALUE;
    float maxValue = -Float.MAX_VALUE;
    List<Float> values = new ArrayList<Float>();

    AreaOfInterest aoi = null;
    int increment = 1;
    DataBounds dataBounds = _model.getDataBounds();
    switch (dataBounds) {
      case USE_ALL_DATA:
        increment = 1;
        aoi = null;
        break;
      case USE_SPARSE_DATA:
        increment = 10;
        aoi = null;
        break;
      case USE_AOI:
        increment = 1;
        aoi = _model.getAOI();
        break;
      default:
        throw new IllegalArgumentException("Invalid data bounds: " + dataBounds);
    }

    int totalWork = 1;

    StorageOrder storageOrder = _ps3d.getPreferredOrder();
    if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
      // Read inlines.
      totalWork = 1 + (_ps3d.getNumInlines() - 1) / increment;
      monitor.beginTask("Scanning data...", totalWork);
      for (int i = 0; i < _ps3d.getNumInlines() && !monitor.isCanceled(); i += increment) {
        float inline = _ps3d.getInlineStart() + i * _ps3d.getInlineDelta();
        TraceData traceData = _ps3d.getInline(inline, _ps3d.getXlineStart(), _ps3d.getXlineEnd(), _ps3d.getZStart(),
            _ps3d.getZEnd());
        for (int j = 0; j < traceData.getNumTraces(); j++) {
          Trace trace = traceData.getTrace(j);
          if (!trace.isMissing()) {
            if (aoi == null || aoi.contains(trace.getX(), trace.getY())) {
              for (int k = 0; k < trace.getNumSamples(); k++) {
                values.add(trace.getDataReference()[k]);
              }
            }
          }
        }
        monitor.worked(1);
      }
    } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
      // Read xlines.
      totalWork = 1 + (_ps3d.getNumXlines() - 1) / increment;
      monitor.beginTask("Scanning data...", totalWork);
      for (int i = 0; i < _ps3d.getNumXlines() && !monitor.isCanceled(); i += increment) {
        float xline = _ps3d.getXlineStart() + i * _ps3d.getXlineDelta();
        TraceData traceData = _ps3d.getXline(xline, _ps3d.getInlineStart(), _ps3d.getInlineEnd(), _ps3d.getZStart(),
            _ps3d.getZEnd());
        for (int j = 0; j < traceData.getNumTraces(); j++) {
          Trace trace = traceData.getTrace(j);
          if (!trace.isMissing()) {
            if (aoi == null || aoi.contains(trace.getX(), trace.getY())) {
              for (int k = 0; k < trace.getNumSamples(); k++) {
                values.add(trace.getDataReference()[k]);
              }
            }
          }
        }
        monitor.worked(1);
      }
    }

    // Create and return the histogram data object.
    float[] values1D = new float[values.size()];
    for (int i = 0; i < values1D.length; i++) {
      values1D[i] = values.get(i);
      minValue = Math.min(minValue, values1D[i]);
      maxValue = Math.max(maxValue, values1D[i]);
    }

    monitor.done();

    HistogramData histogramData = new HistogramData(_ps3d.getDisplayName(), values1D, -Float.MAX_VALUE, _model
        .getNumCells(), minValue, maxValue, _model.getColor());
    _renderer.updateHistogramData(histogramData);
    return histogramData;
  }

}
