/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import java.io.File;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class PointSetMapperModel extends MapperModel {

  public static final String PLUGIN_ID = "org.geocraft.io.gocad";

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String XY_UNIT = "X,Y Unit";

  public static final String Z_UNIT = "Z Unit";

  private StringProperty _directory;

  private StringProperty _fileName;

  private EnumProperty<Unit> _xyUnit;

  private EnumProperty<Unit> _zUnit;

  public PointSetMapperModel() {
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _xyUnit = addEnumProperty(XY_UNIT, Unit.class, Unit.UNDEFINED);
    _zUnit = addEnumProperty(Z_UNIT, Unit.class, Unit.UNDEFINED);
  }

  public PointSetMapperModel(final PointSetMapperModel model) {
    this();
    updateFrom(model);
  }

  public String getDirectory() {
    return _directory.get();
  }

  public String getFileName() {
    return _fileName.get();
  }

  public Unit getXYUnit() {
    return _xyUnit.get();
  }

  public Unit getZUnit() {
    return _zUnit.get();
  }

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName();
  }

  @Override
  public void updateUniqueId(final String name) {
    // TODO: Implement uniqueID update.
  }

  public void validate(IValidation validation) {
    if (_directory.isEmpty()) {
      validation.error(_directory, "Directory not specified");
    }

    if (_fileName.isEmpty()) {
      validation.error(_fileName, "File name not specified");
    }

    if (_xyUnit.isNull() || _xyUnit.get().equals(Unit.UNDEFINED)) {
      validation.error(_xyUnit, "Undefined x,y unit");
    }

    if (_zUnit.isNull() || _zUnit.get().equals(Unit.UNDEFINED)) {
      validation.error(_zUnit, "Undefined z unit");
    }
  }

  @Override
  public boolean existsInStore() {
    return existsOnDisk(getFilePath());
  }

  @Override
  public boolean existsInStore(String name) {
    return existsOnDisk(_directory.get() + File.separator + name);
  }

  public String getFilePath() {
    return _directory.get() + File.separator + _fileName.get();
  }

  private boolean existsOnDisk(final String filePath) {
    File file = new File(filePath);
    return file.exists() && file.canRead();
  }

}
