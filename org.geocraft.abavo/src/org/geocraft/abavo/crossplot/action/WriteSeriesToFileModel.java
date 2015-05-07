/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class WriteSeriesToFileModel extends Model {

  public static final String DIRECTORY = "directory";

  public static final String FILE_NAME = "fileName";

  public static final String COLUMN_A = "columnA";

  public static final String COLUMN_B = "columnB";

  public static final String COLUMN_X = "columnX";

  public static final String COLUMN_Y = "columnY";

  public static final String COLUMN_Z = "columnZ";

  private StringProperty directory;

  private StringProperty fileName;

  private BooleanProperty columnA;

  private BooleanProperty columnB;

  private BooleanProperty columnX;

  private BooleanProperty columnY;

  private BooleanProperty columnZ;

  public WriteSeriesToFileModel() {
    directory = addStringProperty(DIRECTORY, Utilities.getHomeDirectory());
    fileName = addStringProperty(FILE_NAME, "");
    columnA = addBooleanProperty(COLUMN_A, true);
    columnB = addBooleanProperty(COLUMN_B, true);
    columnX = addBooleanProperty(COLUMN_X, false);
    columnY = addBooleanProperty(COLUMN_Y, false);
    columnZ = addBooleanProperty(COLUMN_Z, true);
  }

  public String getDirectory() {
    return directory.get();
  }

  public String getFileName() {
    return fileName.get();
  }

  public void setFileName(String name) {
    this.fileName.set(name);
  }

  public boolean getColumnA() {
    return columnA.get();
  }

  public boolean getColumnB() {
    return columnB.get();
  }

  public boolean getColumnX() {
    return columnX.get();
  }

  public boolean getColumnY() {
    return columnY.get();
  }

  public boolean getColumnZ() {
    return columnZ.get();
  }

  @Override
  public void validate(IValidation results) {
    if (directory.isEmpty()) {
      results.error(DIRECTORY, "Directory not specified");
    }
    if (fileName.isEmpty()) {
      results.error(FILE_NAME, "File name not specified");
    }
  }

}
