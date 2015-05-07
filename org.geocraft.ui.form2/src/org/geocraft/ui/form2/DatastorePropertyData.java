/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;


public class DatastorePropertyData {

  public IDatastoreAccessor _accessor;

  public MapperModel _model;

  public IMapper _mapper;

  public DatastorePropertyData(IDatastoreAccessor accessor) {
    _accessor = accessor;
    _model = accessor.createMapperModel(IOMode.OUTPUT);
    _mapper = accessor.createMapper(IOMode.OUTPUT, _model);
  }

  public DatastorePropertyData(IDatastoreAccessor accessor, MapperModel model, IMapper mapper) {
    _accessor = accessor;
    _model = model;
    _mapper = mapper;
  }

  @Override
  public String toString() {
    if (_accessor == null) {
      return "In-Memory";
    }
    return _accessor.getName();
  }
}
