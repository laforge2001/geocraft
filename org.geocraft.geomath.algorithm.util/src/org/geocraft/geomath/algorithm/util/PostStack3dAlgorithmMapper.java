/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.util;


import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.query.IProjectQuery;


public class PostStack3dAlgorithmMapper implements IPostStack3dMapper {

  private static int _tempID = 1;

  /** Synchronizer token object */
  protected static final Object TOKEN = new Object();

  /** The poststack3d algorithm. */
  private final IPostStack3dAlgorithm _algorithm;

  private final PostStack3dAlgorithmMapperModel _model;

  /**
   * The default constructor.
   * @param algorithm the poststack3d algorithm to use for trace computation.
   */
  public PostStack3dAlgorithmMapper(final IPostStack3dAlgorithm algorithm) {
    _algorithm = algorithm;
    _model = new PostStack3dAlgorithmMapperModel();
  }

  @Override
  // TODO
  public PostStack3dAlgorithmMapper factory(final MapperModel mapperModel) {
    return new PostStack3dAlgorithmMapper((IPostStack3dAlgorithm) mapperModel);
  }

  /**
   * @param createIndex  
   */
  public void checkTraceIndex(final boolean createIndex) {
    // Do nothing.
  }

  /**
   * @param ps3d  
   */
  public void close(final PostStack3d ps3d) {
    close();
  }

  public void close() {
    _algorithm.close();
  }

  /**
   * Implement default getBrick method by delegating to getTraces
   */
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

  public TraceData getInline(final PostStack3d ps3d, final float inline, final float xlineStart, final float xlineEnd,
      final float zStart, final float zEnd) {
    synchronized (TOKEN) {
      float xlineDelta = Math.abs(ps3d.getSurvey().getXlineDelta());
      if (xlineEnd < xlineStart) {
        xlineDelta = -xlineDelta;
      }

      // Build the array of inlines/xlines to read.
      int numTraces = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        inlines[i] = inline;
        xlines[i] = xlineStart + i * xlineDelta;
      }

      // Get the traces and return them.
      return getTraces(ps3d, inlines, xlines, zStart, zEnd);
    }
  }

  public TraceData getXline(final PostStack3d ps3d, final float xline, final float inlineStart, final float inlineEnd,
      final float zStart, final float zEnd) {
    synchronized (TOKEN) {
      float inlineDelta = Math.abs(ps3d.getSurvey().getInlineDelta());

      if (inlineEnd < inlineStart) {
        inlineDelta = -inlineDelta;
      }

      // Build the array of inlines/xlines to read.
      int numTraces = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];

      for (int i = 0; i < numTraces; i++) {
        inlines[i] = inlineStart + i * inlineDelta;
        xlines[i] = xline;
      }

      // Get the traces and return them.
      return getTraces(ps3d, inlines, xlines, zStart, zEnd);
    }
  }

  public TraceData getTraces(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float zStart,
      final float zEnd) {
    synchronized (TOKEN) {
      int numTraces = inlines.length;
      Trace[] traces = new Trace[numTraces];
      for (int i = 0; i < numTraces; i++) {
        traces[i] = _algorithm.computeTrace(ps3d, inlines[i], xlines[i], zStart, zEnd);
      }
      return new TraceData(traces);
    }
  }

  public float[] getSamples(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float[] zs) {
    synchronized (TOKEN) {
      int numTraces = inlines.length;
      float[] data = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        Trace trace = _algorithm.computeTrace(ps3d, inlines[i], xlines[i], zs[i], zs[i]);
        data[i] = trace.getDataReference()[0];
      }
      return data;
    }
  }

  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order) {
    return getSlice(ps3d, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order, 0);
  }

  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float missingValue) {
    synchronized (TOKEN) {
      float inlineDelta = ps3d.getInlineDelta();
      float xlineDelta = ps3d.getXlineDelta();
      int numInlines = 1 + Math.round((inlineEnd - inlineStart) / ps3d.getInlineDelta());
      int numXlines = 1 + Math.round((xlineEnd - xlineStart) / ps3d.getXlineDelta());
      int numTraces = numInlines * numXlines;
      float[] data = new float[numTraces];
      int index = 0;
      for (int i = 0; i < numInlines; i++) {
        float inline = inlineStart + i * inlineDelta;
        for (int j = 0; j < numXlines; j++) {
          float xline = xlineStart + j * xlineDelta;
          Trace trace = _algorithm.computeTrace(ps3d, inline, xline, z, z);
          if (order.equals(SliceBufferOrder.INLINE_XLINE)) {
            index = i * numXlines + j;
          } else if (order.equals(SliceBufferOrder.XLINE_INLINE)) {
            index = j * numInlines + i;
          } else {
            throw new IllegalArgumentException("Invalid slice buffer order.");
          }
          data[index] = trace.getDataReference()[0];
          if (trace.isMissing()) {
            data[index] = missingValue;
          }
        }
      }
      return data;
    }
  }

  /**
   * @param ps3d  
   */
  public StorageOrder getStorageOrder(final PostStack3d ps3d) {
    return _algorithm.getStorageOrder();
  }

  public StorageOrganization getStorageOrganization() {
    return StorageOrganization.TRACE;
  }

  public void putTraces(final PostStack3d ps3d, final TraceData traceData) {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  /**
   * @param ps3d  
   * @param inlines 
   * @param xlines 
   * @param zStart 
   * @param zEnd 
   * @param traceData 
   */
  public void putTraces(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float zStart,
      final float zEnd, final TraceData traceData) {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  public void putSamples(final PostStack3d ps3d, final float[] inline, final float[] xline, final float[] z,
      final float[] samples) {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  public void putSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float[] samples) {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  /**
   * @param storageOrder  
   */
  public void setStorageOrder(final StorageOrder storageOrder) {
    // Do nothing.
  }

  /**
   * @param entity  
   * @throws CoreException 
   */
  public Object createInStore(final PostStack3d ps3d) throws CoreException {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  public Properties getProperties() {
    return new Properties();
  }

  public String getUniqueID() {
    return _algorithm.getAlgorithmType() + " " + _tempID++;
  }

  public void read(final PostStack3d ps3d, final IProgressMonitor monitor) {
    // Do nothing.
    _algorithm.update(ps3d);
  }

  /**
   * @param entity  
   */
  public void remove(final PostStack3d ps3d) {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  public boolean shutdown() {
    return false;
  }

  public boolean supportsWrite() {
    return false;
  }

  /**
   * @param entity  
   * @throws CoreException 
   */
  public void writeToStore(final PostStack3d ps3d) throws CoreException {
    throw new UnsupportedOperationException("Not yet supported.");
  }

  /**
   * @param entity  
   * @throws IOException 
   */
  @Override
  public void create(final PostStack3d ps3d) throws IOException {
    // TODO Auto-generated method stub

  }

  /**
   * @throws IOException  
   */
  @Override
  public void delete(final PostStack3d ps3d) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public PostStack3dAlgorithmMapperModel getModel() {
    return _model;
  }

  /**
   * @param entity  
   * @throws IOException 
   */
  @Override
  public void update(final PostStack3d ps3d) throws IOException {
    // TODO Auto-generated method stub

  }

  public StorageFormat getStorageFormat() {
    return _algorithm.getStorageFormat();
  }

  @Override
  public void setStorageFormat(final StorageFormat storageFormat) {
    // TODO Auto-generated method stub

  }

  public String getStorageDirectory() {
    return "";
  }

  public boolean existsInStore() {
    return _model.existsInStore();
  }

  public boolean existsInStore(String proposedName) {
    return _model.existsInStore(proposedName);
  }

  @Override
  public IStatus validateName(String proposedName) {
    return _model.validateName(proposedName);
  }

  @Override
  public void checkTraceIndex() {
    // TODO Auto-generated method stub

  }

  @Override
  public void reinitialize() {
    // TODO Auto-generated method stub

  }

  public void setDomain(final Domain domain) {
    _model.setDomain(domain);
  }

  public void setStorageOrganizationAndFormat(final StorageOrganization storageOrganization,
      final StorageFormat storageFormat, final BrickType brickType, final float fidelity) {
    // TODO:
  }

  public String canCreate(StorageOrganization storageOrganization, StorageFormat storageFormat) {
    return storageOrganization + " volumes not supported by an algorithm mapper.";
  }

  public String getDatastoreEntryDescription() {
    return _algorithm.getAlgorithmType();
  }

  public String getDatastore() {
    return "Algorithm";
  }

  public IProjectQuery getProjectQuery() {
    return null;
  }

  public String createOutputDisplayName(String inputDisplayName, String nameSuffix) {
    return inputDisplayName + nameSuffix;
  }
}
