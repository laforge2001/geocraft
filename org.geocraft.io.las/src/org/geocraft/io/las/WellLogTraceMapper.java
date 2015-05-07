/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import java.io.IOException;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellLogTrace;


public class WellLogTraceMapper extends AbstractMapper<WellLogTrace> {

  private final WellLogTraceMapperModel _model;

  public WellLogTraceMapper(final WellLogTraceMapperModel model) {
    _model = model;
  }

  @Override
  protected WellLogTraceMapperModel getInternalModel() {
    return _model;
  }

  public WellLogTraceMapperModel getModel() {
    return new WellLogTraceMapperModel(_model);
  }

  public WellLogTraceMapper factory(final MapperModel mapperModel) {
    return new WellLogTraceMapper((WellLogTraceMapperModel) mapperModel);
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "LAS Well Log Trace";
  }

  public String getDatastore() {
    return "LAS";
  }

  @Override
  protected void createInStore(final WellLogTrace logTrace) throws IOException {
    // Call the create method on the parent well bore's mapper.
    logTrace.getWell();
  }

  @Override
  protected void readFromStore(final WellLogTrace logTrace) throws IOException {
    Well well = logTrace.getWell();
    WellMapper mapper = (WellMapper) well.getMapper();
    mapper.readLogTrace(logTrace);
  }

  @Override
  protected void updateInStore(final WellLogTrace logTrace) throws IOException {
    Well well = logTrace.getWell();

    WellMapperModel model = (WellMapperModel) well.getMapper().getModel();
    String[] selectedLogs = model.getSelectedColumnNames();
    String findMe = logTrace.getDisplayName() + " (" + logTrace.getDataUnit().toString() + ")";
    if (!isStringInArray(findMe, selectedLogs)) {
      String[] updatedSelectedLogs = new String[selectedLogs.length + 1];
      System.arraycopy(selectedLogs, 0, updatedSelectedLogs, 0, selectedLogs.length);
      updatedSelectedLogs[updatedSelectedLogs.length - 1] = findMe;
      model.setValueObject(WellMapperModel.SELECTED_COLUMN_NAMES, updatedSelectedLogs);
    }

    well.update();
  }

  @Override
  protected void deleteFromStore(final WellLogTrace logTrace) {
    // TODO: This does nothing, but perhaps should remove the log from the LAS file.
  }

  private boolean isStringInArray(final String findMe, final String[] arrayOfStrings) {
    String findMeWithoutUnits = findMe.substring(0, findMe.lastIndexOf('(')).trim();
    for (int i = 0; i < arrayOfStrings.length; ++i) {
      if (findMeWithoutUnits.equals(arrayOfStrings[i].toString().substring(0, arrayOfStrings[i].lastIndexOf('('))
          .trim())) {
        return true;
      }
    }
    return false;
  }
}
