package org.geocraft.io.modspec;


import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IGrid3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;


/**
 * The mapper for ModSpec grid files.
 */
public class GridMapper extends AbstractMapper<Grid3d> implements IGrid3dMapper, ModSpecGridConstants {

  /** The model of mapper parameters. */
  private final GridMapperModel _model;

  /**
   * The parameterized constructor.
   * @param properties for mapping a ModSpec grid file to a Grid entity.
   */
  public GridMapper(final GridMapperModel model) {
    _model = model;
  }

  @Override
  public GridMapper factory(final MapperModel mapperModel) {
    return new GridMapper((GridMapperModel) mapperModel);
  }

  @Override
  public GridMapperModel getInternalModel() {
    return _model;
  }

  public GridMapperModel getModel() {
    return new GridMapperModel(_model);
  }

  public String getName() {
    return _model.getFileName();
  }

  /**
   * Creates a ModSpec grid file on disk from a grid property.
   * 
   * @param entity the grid property entity to create as a ModSpec grid file.
   * @throws IOException thrown if the ModSpec grid file already exists.
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
    readFromStore(grid, new NullProgressMonitor());
  }

  @Override
  public void readFromStore(final Grid3d grid, final IProgressMonitor monitor) throws IOException {
    GridReader reader = new GridReader(_model);
    reader.read(grid, getFilePath(), monitor);
  }

  /**
   * @param entity
   */
  private void validateEntity(final Entity entity) {
    if (!entity.getClass().equals(Grid3d.class)) {
      throw new IllegalArgumentException("Invalid entity type: " + entity.getClass() + ".");
    }
  }

  /**
   * Writes a grid property to a ModSpec grid file on disk.
   * 
   * @param entity the grid property entity to write to a ModSpec grid file.
   */
  @Override
  public void updateInStore(final Grid3d grid) throws IOException {
    // Check if the grid if rectangular (required for ModSpec).
    GridGeometry3d geometry = grid.getGeometry();
    if (!geometry.isRectangular()) {
      throw new IOException("The Grid must be rectangular for the ModSpec format.");
    }

    // Check the format to write (default is ASCII).
    File file = new File(getFilePath());
    // Check if the specified file already exists.
    if (file.exists()) {
      // Check if the specified file is actually a directory.
      if (file.isDirectory()) {
        throw new IOException("The path represents a directory, not a ModSpec grid file.");
      }
      // Attempt to delete the existing file.
      if (!file.delete()) {
        throw new IOException("Could not delete the existing ModSpec grid file.");
      }
    }
    GridWriter writer = new GridWriter(_model);
    writer.write(grid, getFilePath());
  }

  /**
   * Deletes a ModSpec grid file on disk.
   */
  @Override
  public void deleteFromStore(final Grid3d grid) throws IOException {
    File gridFile = new File(getFilePath());
    // Check if the grid file exists.
    if (gridFile.exists()) {
      // Try to delete the grid file.
      if (!gridFile.delete()) {
        throw new IOException("Could not delete ModSpec grid file \'" + getFilePath() + "\'.");
      }
    } else {
      throw new IOException("ModSpec grid file \'" + getFilePath() + "\' does not exist.");
    }
  }

  protected String getFilePath() {
    return _model.getFilePath();
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "ModSpec Grid";
  }

  public String getDatastore() {
    return "ModSpec";
  }

  @Override
  public String getStorageDirectory() {
    return _model.getDirectory();
  }

  public void setDataUnit(final Unit dataUnit) {
    _model.setDataUnit(dataUnit);
  }

  public void setFileName(final String name) {
    _model.setFileName(name);
  }

  public void setOnsetType(final OnsetType onsetType) {
    _model.setOnsetType(onsetType);
  }

  public void setStorageDirectory(final String directory) {
    _model.setDirectory(directory);
  }

  public void setXYUnit(final Unit xyUnit) {
    _model.setXyUnit(xyUnit);
  }
}
