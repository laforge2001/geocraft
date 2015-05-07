/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.sql.Timestamp;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatMeasurement;
import org.geocraft.core.model.datatypes.FloatMeasurementSeries;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.InMemoryMapperModel;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.math.wavelet.RickerWavelet;
import org.geocraft.math.wavelet.WaveletFilter;


/**
 * 
 */
public class PostStack3dMapper extends AbstractMapper<PostStack3d> implements IPostStack3dMapper {

  //private PostStack3d _ps3d;

  private HeaderDefinition _traceHeaderDef;

  private final TestHorizonModel _horizonModel;

  private final float _sig2Noise;

  // private VelocityModel1d _velModel;
  private static int _uniqueIDHack;

  private final int _uniqueID;

  private StorageOrder _storageOrder = StorageOrder.INLINE_XLINE_Z;

  private InMemoryMapperModel _model;

  private Timestamp _lastModificationDate;

  public PostStack3dMapper(final TestHorizonModel horizonModel, final float sig2Noise) {
    _horizonModel = horizonModel;
    _sig2Noise = sig2Noise;
    _uniqueID = _uniqueIDHack++;
    _model = new InMemoryMapperModel("" + _uniqueID);
    // _velModel= new VelocityModel1d( 5000, .5, Unit.SECOND, Unit.FOOT);
    _traceHeaderDef = new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.INLINE_NO,
        TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y });
    _lastModificationDate = new Timestamp(System.currentTimeMillis());
  }

  public IMapper create(final String name) {
    return new PostStack3dMapper(_horizonModel, _sig2Noise);
  }

  @Override
  public String getUniqueID() {
    return "Test Data Generator PostStack3d : " + _uniqueID;
  }

  public InMemoryMapperModel getModel() {
    return _model;
  }

  @Override
  public IStatus validateName(String proposedName) {
    return ValidationStatus.ok();
  }

  @Override
  protected void createInStore(PostStack3d ps3d) {
    throw new UnsupportedOperationException("Cannot create in this test data generator datastore.");
  }

  @Override
  protected void deleteFromStore(PostStack3d ps3d) {
    // No action necessary.
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "Test Data Generator PostStack3d";
  }

  @Override
  protected InMemoryMapperModel getInternalModel() {
    return _model;
  }

  @Override
  protected void readFromStore(PostStack3d ps3d) {
    ps3d.setTraceHeaderDefinition(_traceHeaderDef);
    ps3d.setLastModifiedDate(_lastModificationDate);
  }

  @Override
  protected void updateInStore(PostStack3d ps3d) {
    // No action necessary.
  }

  @Override
  public String canCreate(StorageOrganization storageOrganization, StorageFormat storageFormat) {
    return "Cannot create in the test data generator datastore.";
  }

  @Override
  public void checkTraceIndex() {
    // No action necessary.
  }

  /**
   * Implement default getBrick method by delegating to getTraces
   */
  @Override
  public TraceData getBrick(final PostStack3d ps3d, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final float zStart, final float zEnd) {
    float xlineDelta = Math.abs(ps3d.getSurvey().getXlineDelta());
    float inlineDelta = Math.abs(ps3d.getSurvey().getInlineDelta());

    if (xlineEnd < xlineStart) {
      xlineDelta = -xlineDelta;
    }
    if (inlineEnd < inlineStart) {
      inlineDelta = -inlineDelta;
    }

    // Build the array of inlines/xlines to read.
    int numXlTraces = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
    int numIlTraces = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
    int numTraces = numXlTraces * numIlTraces;
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];

    int ndx = 0;
    for (int l = 0; l < numIlTraces; l++) {
      float il = inlineStart + l * inlineDelta;
      for (int t = 0; t < numXlTraces; t++, ndx++) {
        inlines[ndx] = il;
        xlines[ndx] = xlineStart + t * xlineDelta;
      }
    }

    // Get the traces and return them.
    return getTraces(ps3d, inlines, xlines, zStart, zEnd);
  }

  public TraceData getInline(PostStack3d ps3d, final float inline, final float xlineStart, final float xlineEnd,
      final float zStart, final float zEnd) {
    float zDelta = ps3d.getZDelta();
    int numSamples = (int) (1 + (zEnd - zStart) / zDelta);
    int numTraces = 0;
    float delta = ps3d.getXlineDelta();
    if ((xlineEnd - xlineStart) / ps3d.getXlineDelta() > 0) {
      numTraces = (int) (1 + (xlineEnd - xlineStart) / ps3d.getXlineDelta());
    } else {
      numTraces = (int) (1 - (xlineEnd - xlineStart) / ps3d.getXlineDelta());
    }

    float[] data = new float[numSamples * numTraces];
    int k = 0;
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inline;
      xlines[i] = xlineStart + delta * i;
    }
    CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);

    for (int i = 0; i < numTraces; i++) {
      float[] trace = getTrace(ps3d, coords.getPoint(i));
      for (int j = 0; j < numSamples; j++) {
        data[k] = trace[j];
        k++;
      }
    }

    Trace[] traces = new Trace[numTraces];
    for (int i = 0; i < numTraces; i++) {
      double x = coords.getX(i);
      double y = coords.getY(i);
      float[] samples = new float[numSamples];
      System.arraycopy(data, i * numSamples, samples, 0, numSamples);

      // Create a header and set the values for inline, xline, x and y.
      Header header = new Header(_traceHeaderDef);
      header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
      header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xlines[i]));
      header.putDouble(TraceHeaderCatalog.X, x);
      header.putDouble(TraceHeaderCatalog.Y, y);
      traces[i] = new Trace(zStart, zDelta, ps3d.getZUnit(), samples, Status.Live, header);
    }

    return new TraceData(traces);
  }

  public TraceData getTraces(PostStack3d ps3d, final float[] inlines, final float[] xlines, final float zStart,
      final float zEnd) {
    float zDelta = ps3d.getZDelta();
    int numTraces = inlines.length;
    int numSamples = (int) (1 + (zEnd - zStart) / zDelta);
    float[] data = new float[numSamples * numTraces];
    int k = 0;
    CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
    for (int i = 0; i < numTraces; i++) {
      float[] trace = getTrace(ps3d, coords.getPoint(i));
      for (int j = 0; j < numSamples; j++) {
        data[k] = trace[j];
        k++;
      }
    }

    Trace[] traces = new Trace[numTraces];

    for (int i = 0; i < numTraces; i++) {
      double x = coords.getX(i);
      double y = coords.getY(i);
      float[] samples = new float[numSamples];
      System.arraycopy(data, i * numSamples, samples, 0, numSamples);

      Header header = new Header(_traceHeaderDef);
      header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inlines[i]));
      header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xlines[i]));
      header.putDouble(TraceHeaderCatalog.X, x);
      header.putDouble(TraceHeaderCatalog.Y, y);
      traces[i] = new Trace(zStart, zDelta, ps3d.getZUnit(), samples, Status.Live, header);
    }

    return new TraceData(traces);
  }

  public TraceData getXline(PostStack3d ps3d, final float xline, final float inlineStart, final float inlineEnd,
      final float zStart, final float zEnd) {
    float zDelta = ps3d.getZDelta();
    int numSamples = (int) (1 + (zEnd - zStart) / zDelta);
    int numTraces;
    float delta = ps3d.getInlineDelta();
    if ((inlineEnd - inlineStart) / ps3d.getXlineDelta() > 0) {
      numTraces = (int) (1 + (inlineEnd - inlineStart) / ps3d.getInlineDelta());
    } else {
      numTraces = (int) (1 - (inlineEnd - inlineStart) / ps3d.getInlineDelta());
    }

    float[] data = new float[numSamples * numTraces];
    int k = 0;
    float[] xlines = new float[numTraces];
    float[] inlines = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inlineStart + delta * i;
      xlines[i] = xline;
    }
    CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);

    for (int i = 0; i < numTraces; i++) {
      float[] trace = getTrace(ps3d, coords.getPoint(i));
      for (int j = 0; j < numSamples; j++) {
        data[k] = trace[j];
        k++;
      }
    }

    Trace[] traces = new Trace[numTraces];
    for (int i = 0; i < numTraces; i++) {
      double x = coords.getX(i);
      double y = coords.getY(i);
      float[] samples = new float[numSamples];
      System.arraycopy(data, i * numSamples, samples, 0, numSamples);

      Header header = new Header(_traceHeaderDef);
      header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inlines[i]));
      header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
      header.putDouble(TraceHeaderCatalog.X, x);
      header.putDouble(TraceHeaderCatalog.Y, y);
      traces[i] = new Trace(zStart, zDelta, ps3d.getZUnit(), samples, Status.Live, header);
    }

    return new TraceData(traces);
  }

  public float[] getSamples(final float[] inline, final float[] xline, final FloatMeasurementSeries z) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public float[] getSlice(final FloatMeasurement z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float missingValue) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  private float[] getTrace(PostStack3d ps3d, final Point3d point) {

    int n = ps3d.getNumSamplesPerTrace();

    float[] refl = new float[n];

    // generate random noise of amplitude = +/- 1
    for (int i = 0; i < n; i++) {
      refl[i] = (float) (2 * (Math.random() - .5));
    }

    // set the reflectivity at the horizon depths to sig2noise
    for (int i = 0; i < _horizonModel.getNumHorizons(); i++) {
      int iSamp = (int) (_horizonModel.getHorizonZ(i, point.getX(), point.getY()) / ps3d.getZDelta());

      if (iSamp < refl.length - 1 && iSamp > 0) {
        refl[iSamp] = _sig2Noise;
      }
    }

    // generate a Ricker wavelet
    float sampleRate = ps3d.getZDelta();
    float freq = 1 / (30 * sampleRate);
    freq = 30;

    // filter the reflectivity trace with the Ricker wavelet
    WaveletFilter filter = new WaveletFilter(RickerWavelet.createWavelet(freq, sampleRate, 31));
    float[] trace = filter.filterTrace(refl, n, 0, n);

    // shift the trace by the number of points in the wavelet (not sure why this is necessary ...)
    // one would normally think it would just be by the half length to account for time zero of
    // the wavelet.....
    // System.arraycopy(trace, 31, trace, 0, n - 31);

    return trace;

  }

  @Override
  public float[] getSamples(PostStack3d ps3d, float[] inlines, float[] xlines, float[] z) {
    // Validate the array lengths are non-null and of equal length.
    if (inlines == null || xlines == null || z == null) {
      throw new IllegalArgumentException("Array Error: One or more of the coordinate arrays is null.");
    }
    if (inlines.length != xlines.length && inlines.length != z.length) {
      throw new IllegalArgumentException("Array Error: The coordinate arrays must be of equal length.");
    }

    // Get all the traces and then extract out the associated sample values.
    int numTraces = inlines.length;
    float zmin = Float.MAX_VALUE;
    float zmax = zmin;
    for (float element : z) {
      zmin = Math.min(zmin, element);
      zmax = Math.max(zmax, element);
    }
    TraceData traceData = getTraces(ps3d, inlines, xlines, zmin, zmax);
    float[] samples = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      Trace trace = traceData.getTrace(i);
      int zIndex = 1 + Math.round((z[i] - trace.getZStart()) / trace.getZEnd());
      samples[i] = trace.getDataReference()[zIndex];
    }
    return samples;
  }

  public float[] getSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart,
      float xlineEnd, SliceBufferOrder order) {
    return getSlice(ps3d, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order, 0);
  }

  public float[] getSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart,
      float xlineEnd, SliceBufferOrder order, final float missingValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public void putSamples(PostStack3d ps3d, float[] inline, float[] xline, float[] z, float[] samples) {
    throw new UnsupportedOperationException("Cannot put samples into this entity.");
  }

  public void putSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart, float xlineEnd,
      SliceBufferOrder order, float[] samples) {
    throw new UnsupportedOperationException("Cannot put slices into this entity.");
  }

  public void putTraces(PostStack3d ps3d, TraceData traceData) {
    throw new UnsupportedOperationException("Cannot put traces into this entity.");
  }

  public StorageOrder getStorageOrder(PostStack3d ps3d) {
    return _storageOrder;
  }

  public void setStorageOrder(StorageOrder storageOrder) {
    _storageOrder = storageOrder;
  }

  public StorageOrganization getStorageOrganization() {
    return StorageOrganization.TRACE;
  }

  public void setStorageOrganizationAndFormat(StorageOrganization storageOrganization, StorageFormat storageFormat,
      BrickType brickType, float fidelity) {
    throw new UnsupportedOperationException("Cannot modify the storage organization or format for this entity.");
  }

  public void setStorageFormat(StorageFormat storageFormat) {
    throw new UnsupportedOperationException("Cannot modify the storage format for this entity.");
  }

  public void close() {
    // No action necessary.
  }

  public void setDomain(Domain domain) {
    // TODO Auto-generated method stub

  }

  public StorageFormat getStorageFormat() {
    return StorageFormat.FLOAT_32;
  }

  public PostStack3dMapper factory(MapperModel mapperModel) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDatastore() {
    return "Test Data Generator";
  }

}
