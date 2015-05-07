package org.geocraft.io.asciigrid;


import java.io.File;
import java.io.IOException;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IGrid3dMapper;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * The mapper for ASCII grid files.
 */
public class AsciiHorizonMapper extends AbstractMapper<Grid3d> implements IGrid3dMapper, AsciiFileConstants {

  /** The model of mapper parameters. */
  protected final AsciiHorizonMapperModel _model;

  /**
   * The parameterized constructor.
   * @param properties for mapping a ASCII grid file to a Grid entity.
   */
  public AsciiHorizonMapper(final AsciiHorizonMapperModel model) {
    _model = model;
  }

  @Override
  public AsciiHorizonMapper factory(final MapperModel mapperModel) {
    return new AsciiHorizonMapper((AsciiHorizonMapperModel) mapperModel);
  }

  @Override
  public AsciiHorizonMapperModel getInternalModel() {
    return _model;
  }

  public AsciiHorizonMapperModel getModel() {
    return new AsciiHorizonMapperModel(_model);
  }

  public String getName() {
    return _model.getFileName();
  }

  /**
   * Creates a Ascii file on disk from a grid property.
   * 
   * @param entity the grid property entity to create as a Ascii file.
   * @throws IOException thrown if the ASCII grid file already exists.
   */
  @Override
  public void createInStore(final Grid3d grid) throws IOException {
    String filePath = getFilePath();
    File file = new File(filePath);
    if (!file.createNewFile()) {
      throw new IOException("The file \'" + filePath + "\' already exists.");
    }
  }

  @Override
  public void readFromStore(final Grid3d grid) throws IOException {
    AbstractAsciiHorizonReader reader = new AsciiHorizonReader(_model);

    String filePath = getFilePath();

    // make sure the file is a valid file
    if (existsInStore()) {
      int maxLine = 10;
      int nLines = reader.getNumOfLines(filePath, maxLine);

      // Don't try to use file until it contains at least 10 lines with data
      if (nLines > 9) {
        reader.read(grid, getFilePath());
      }
    }
  }

  /**
   * Writes a grid property to a Ascii file on disk.
   * 
   * @param entity
   *            the grid property entity to write to a Ascii horizon grid
   *            file.
   */
  @Override
  public void updateInStore(final Grid3d grid) throws IOException {
    // Check if the grid if rectangular (required for Ascii horizon grid
    // file).
    GridGeometry3d geometry = grid.getGeometry();
    if (!geometry.isRectangular()) {
      throw new IOException("The Grid must be rectangular for the Ascii format.");
    }
    File file = new File(getFilePath());
    // Check if the specified file already exists.
    if (file.exists()) {
      // Check if the specified file is actually a directory.
      if (file.isDirectory()) {
        throw new IOException("The path represents a directory, not a Ascii file.");
      }
      // Attempt to delete the existing file.
      if (!file.delete()) {
        throw new IOException("Could not delete the existing Ascii file.");
      }
    }
    AbstractAsciiHorizonWriter writer = new AsciiHorizonWriter(_model);
    writer.writeHorizon(grid, grid.getGeometry(), grid.getNullValue(), _model.getDirectory(), _model.getFileName());
  }

  /**
   * Deletes a Ascii file on disk.
   */
  @Override
  public void deleteFromStore(final Grid3d grid) throws IOException {
    File gridFile = new File(getFilePath());
    // Check if the grid file exists.
    if (gridFile.exists()) {
      // Try to delete the grid file.
      if (!gridFile.delete()) {
        throw new IOException("Could not delete Ascii file \'" + getFilePath() + "\'.");
      }
    } else {
      throw new IOException("Ascii file \'" + getFilePath() + "\' does not exist.");
    }
  }

  protected String getFilePath() {
    return _model.getFilePath();
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "ASCII File Grid3d";
  }

  public String getDatastore() {
    return "ASCII File";
  }

  public void setDataUnit(Unit dataUnit) {
    _model.setDataUnits(dataUnit);
  }

  public void setFileName(String name) {
    _model.setFileName(name);
  }

  public void setOnsetType(OnsetType onsetType) {
    _model.setOnsetType(onsetType);
  }

  public void setStorageDirectory(String directory) {
    _model.setDirectory(directory);
  }

  public void setXYUnit(Unit xyUnit) {
    _model.setXyUnits(xyUnit);
  }

}
