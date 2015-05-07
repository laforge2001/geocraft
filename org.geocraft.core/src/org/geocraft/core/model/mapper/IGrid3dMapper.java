/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;


public interface IGrid3dMapper extends IMapper<Grid3d> {

  //void setProjectName(String projectName);

  void setDataUnit(Unit dataUnit);

  void setXYUnit(Unit xyUnit);

  void setOnsetType(OnsetType onsetType);

  void setStorageDirectory(String directory);

  void setFileName(String name);
}
