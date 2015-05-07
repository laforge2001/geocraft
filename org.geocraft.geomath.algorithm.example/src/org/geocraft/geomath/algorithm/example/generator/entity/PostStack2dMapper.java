/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack2dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.query.IProjectQuery;


public class PostStack2dMapper implements IPostStack2dMapper {

  private HeaderDefinition _headerDef;

  private String _uniqueID = "";

  public PostStack2dMapper(String uniqueID) {
    _headerDef = new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.INLINE_NO, TraceHeaderCatalog.CDP_NO,
        TraceHeaderCatalog.SHOTPOINT_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y });
    _uniqueID = uniqueID;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IPostStack2dMapper#close()
   */
  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#create(org.geocraft.core.model.Entity)
   */
  @Override
  public void create(final PostStack2dLine ps2d) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#delete()
   */
  @Override
  public void delete(final PostStack2dLine ps2d) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#existsInStore()
   */
  @Override
  public boolean existsInStore() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#existsInStore(java.lang.String)
   */
  @Override
  public boolean existsInStore(final String name) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#getModel()
   */
  @Override
  public MapperModel getModel() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#getUniqueID()
   */
  @Override
  public String getUniqueID() {
    return _uniqueID;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#read(org.geocraft.core.model.Entity)
   */
  @Override
  public void read(final PostStack2dLine ps2d, final IProgressMonitor monitor) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#update(org.geocraft.core.model.Entity)
   */
  @Override
  public void update(final PostStack2dLine ps2d) {
    // TODO Auto-generated method stub

  }

  @Override
  public float[] getSamples(final PostStack2dLine ps2d, final float[] cdps, final float[] z) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TraceData getTraces(final PostStack2dLine ps2d, final float[] cdps, final float zStart, final float zEnd) {
    float zDelta = ps2d.getZDelta();
    int numTraces = cdps.length;
    int numSamples = (int) (1 + (zEnd - zStart) / zDelta);
    float[] data = new float[numSamples * numTraces];
    int k = 0;
    CoordinateSeries coords = ps2d.getSeismicLine().transformCDPsToXYs(cdps);

    for (int i = 0; i < numTraces; i++) {
      for (int j = 0; j < numSamples; j++) {
        // commented this in order to test the readout
        //        if (j < 100) {
        //          // mimics the ocean at the top of the trace. 
        //          data[k++] = 0;
        //        } else
        if (j != 0 && j % 20 == 0) {
          data[k++] = 0;
        } else {
          data[k++] = cdps[i] - 1;
        }
      }
    }

    Trace[] traces = new Trace[numTraces];
    for (int i = 0; i < numTraces; i++) {
      double x = coords.getX(i);
      double y = coords.getY(i);
      float[] samples = new float[numSamples];
      System.arraycopy(data, i * numSamples, samples, 0, numSamples);

      Header header = new Header(_headerDef);
      header.putInteger(TraceHeaderCatalog.INLINE_NO, ps2d.getLineNumber());
      header.putInteger(TraceHeaderCatalog.CDP_NO, Math.round(cdps[i]));
      header.putDouble(TraceHeaderCatalog.X, x);
      header.putDouble(TraceHeaderCatalog.Y, y);
      traces[i] = new Trace(zStart, zDelta, ps2d.getZUnit(), samples, Status.Live, header);
    }

    return new TraceData(traces);
  }

  @Override
  public void putSamples(final PostStack2dLine ps2d, final float[] cdp, final float[] z, final Unit unit,
      final float[] samples) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putTraces(final PostStack2dLine ps2d, final TraceData traceData) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setStorageFormat(final StorageFormat storageFormat) {
    // TODO Auto-generated method stub

  }

  @Override
  public IMapper factory(final MapperModel mapperModel) {
    // TODO Auto-generated method stub
    return null;
  }

  public IProjectQuery getProjectQuery() {
    return null;
  }

  public StorageFormat getStorageFormat() {
    return StorageFormat.FLOAT_32;
  }

  @Override
  public IStatus validateName(String proposedName) {
    return ValidationStatus.ok();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#reinitialize()
   */
  @Override
  public void reinitialize() {
    // TODO Auto-generated method stub

  }

  public String getStorageDirectory() {
    return "";
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.ISeismicDatasetMapper#setDomain(org.geocraft.core.model.datatypes.Domain)
   */
  @Override
  public void setDomain(Domain domain) {
    // TODO Auto-generated method stub

  }

  public String getDatastoreEntryDescription() {
    return "Test Data Generator PostStack2d";
  }

  public String getDatastore() {
    return "Test Data Generator";
  }

  public String createOutputDisplayName(String inputDisplayName, String nameSuffix) {
    return inputDisplayName + nameSuffix;
  }

}
