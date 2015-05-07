/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class WellLogTraceMapperModel extends MapperModel {

  private StringProperty _traceDisplayName;

  private ObjectProperty<WellMapperModel> _wellMapperModel;

  //  private WellMapperModel _wellMapperModel;

  public static final String TRACE_DISPLAY_NAME = "Trace Display Name";

  public static final String WELL_MAPPER_MODEL = "Well Mapper Model";

  public WellLogTraceMapperModel() {
    _wellMapperModel = addObjectProperty(WELL_MAPPER_MODEL, WellMapperModel.class);
    _traceDisplayName = addStringProperty(TRACE_DISPLAY_NAME, "");
  }

  /**
   * @param model
   */
  public WellLogTraceMapperModel(WellLogTraceMapperModel model) {
    this();
    updateFrom(model);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.MapperModel#existsInStore()
   */
  @Override
  public boolean existsInStore() {
    // TODO Auto-generated method stub
    return _wellMapperModel.get().existsInStore();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.MapperModel#existsInStore(java.lang.String)
   */
  @Override
  public boolean existsInStore(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.MapperModel#getUniqueId()
   */
  @Override
  public String getUniqueId() {
    return _wellMapperModel.get().getUniqueId() + _traceDisplayName.get();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.MapperModel#updateUniqueId(java.lang.String)
   */
  @Override
  public void updateUniqueId(String name) {
    _traceDisplayName.set(name);

  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModel#validate(org.geocraft.core.model.validation.IValidation)
   */
  @Override
  public void validate(IValidation results) {
    // TODO Auto-generated method stub

  }

}
