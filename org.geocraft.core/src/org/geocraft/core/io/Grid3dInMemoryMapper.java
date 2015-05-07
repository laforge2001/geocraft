package org.geocraft.core.io;


import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IGrid3dMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.mapper.MapperModel;


public class Grid3dInMemoryMapper extends InMemoryMapper<Grid3d> implements IGrid3dMapper {

  public Grid3dInMemoryMapper() {
    super(Grid3d.class);
  }

  public void setDataUnit(final Unit dataUnit) {
    // Does not apply.
  }

  public void setFileName(final String name) {
    // Does not apply.
  }

  public void setOnsetType(final OnsetType onsetType) {
    // Does not apply.
  }

  public void setStorageDirectory(final String directory) {
    // Does not apply.
  }

  public void setXYUnit(final Unit xyUnit) {
    // Does not apply.
  }

  @Override
  public Grid3dInMemoryMapper factory(final MapperModel mapperModel) {
    return new Grid3dInMemoryMapper();
  }
}
