package org.geocraft.io.ascii.aoi;


import java.io.File;
import java.io.IOException;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.mapper.MapperModel;


public class AsciiAOIMapper extends AbstractMapper<AreaOfInterest> {

  /** The model of mapper parameters. */
  private final AsciiAOIMapperModel _model;

  /**
   * The parameterized constructor.
   * 
   * @param properties
   *          for mapping a ASCII AOI file to a Grid entity.
   */
  public AsciiAOIMapper(final AsciiAOIMapperModel model) {
    _model = model;
  }

  @Override
  public AsciiAOIMapper factory(final MapperModel mapperModel) {
    return new AsciiAOIMapper((AsciiAOIMapperModel) mapperModel);
  }

  @Override
  public AsciiAOIMapperModel getInternalModel() {
    return _model;
  }

  public AsciiAOIMapperModel getModel() {
    return new AsciiAOIMapperModel(_model);
  }

  /**
   * Creates a ASCII AOI file on disk from a grid property.
   * 
   * @param entity
   *          the grid property entity to create as a ASCII AOI file.
   * @throws IOException
   *           thrown if the ASCII AOI file already exists.
   */
  @Override
  public void createInStore(final AreaOfInterest aoi) throws IOException {
    String filePath = getFilePath();
    File file = new File(filePath);
    if (!file.createNewFile()) {
      throw new IOException("The file \'" + filePath + "\' already exists.");
    }
  }

  @Override
  public void readFromStore(final AreaOfInterest aoi) throws IOException {
    String filePath = getFilePath();
    if (MapPolygonAOI.class.isAssignableFrom(aoi.getClass())) {
      MapPolygonAOI aoiMap = (MapPolygonAOI) aoi;
      MapPolygonAOIReaderWriter reader = new MapPolygonAOIReaderWriter();
      reader.readFromStore(aoiMap, filePath);
      aoi.setDirty(false);
    } else if (SeismicSurvey3dAOI.class.isAssignableFrom(aoi.getClass())) {
      SeismicSurvey3dAOI aoiSurvey3d = (SeismicSurvey3dAOI) aoi;
      SeismicSurvey3dAOIReaderWriter reader = new SeismicSurvey3dAOIReaderWriter();
      reader.readFromStore(aoiSurvey3d, filePath);
      aoi.setDirty(false);
    } else if (SeismicSurvey2dAOI.class.isAssignableFrom(aoi.getClass())) {
      SeismicSurvey2dAOI aoiSurvey2d = (SeismicSurvey2dAOI) aoi;
      SeismicSurvey2dAOIReaderWriter reader = new SeismicSurvey2dAOIReaderWriter();
      reader.readFromStore(aoiSurvey2d, filePath);
      aoi.setDirty(false);
    } else {
      throw new IOException("Unsupported AOI type: " + aoi.getClass());
    }
  }

  @Override
  protected void updateInStore(final AreaOfInterest aoi) throws IOException {
    String filePath = getFilePath();
    if (MapPolygonAOI.class.isAssignableFrom(aoi.getClass())) {
      MapPolygonAOI aoiMap = (MapPolygonAOI) aoi;
      MapPolygonAOIReaderWriter writer = new MapPolygonAOIReaderWriter();
      writer.updateInStore(aoiMap, filePath);
      aoi.setDirty(false);
    } else if (SeismicSurvey3dAOI.class.isAssignableFrom(aoi.getClass())) {
      SeismicSurvey3dAOI aoiSurvey3d = (SeismicSurvey3dAOI) aoi;
      SeismicSurvey3dAOIReaderWriter writer = new SeismicSurvey3dAOIReaderWriter();
      writer.updateInStore(aoiSurvey3d, filePath);
      aoi.setDirty(false);
    } else if (SeismicSurvey2dAOI.class.isAssignableFrom(aoi.getClass())) {
      SeismicSurvey2dAOI aoiSurvey2d = (SeismicSurvey2dAOI) aoi;
      SeismicSurvey2dAOIReaderWriter writer = new SeismicSurvey2dAOIReaderWriter();
      writer.updateInStore(aoiSurvey2d, filePath);
      aoi.setDirty(false);
    } else {
      throw new IOException("Unsupported AOI type: " + aoi.getClass());
    }
  }

  @Override
  public String getStorageDirectory() {
    return _model.getDirectory();
  }

  @Override
  protected void deleteFromStore(final AreaOfInterest aoi) throws IOException {
    File gridFile = new File(getFilePath());
    // Check if the grid file exists.
    if (gridFile.exists()) {
      // Try to delete the grid file.
      if (!gridFile.delete()) {
        throw new IOException("Could not delete ASCII AOI file \'" + getFilePath() + "\'.");
      }
    } else {
      throw new IOException("ASCII AOI file \'" + getFilePath() + "\' does not exist.");
    }
  }

  protected String getFilePath() {
    return _model.getFilePath();
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "ASCII File AOI";
  }

  public String getDatastore() {
    return "ASCII File";
  }

}
