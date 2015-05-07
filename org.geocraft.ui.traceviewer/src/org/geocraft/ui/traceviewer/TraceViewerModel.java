package org.geocraft.ui.traceviewer;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.model.IModelSpace;


public class TraceViewerModel extends Model {

  private final IModelSpace _modelSpace;

  public TraceViewerModel(final IModelSpace modelSpace) {
    _modelSpace = modelSpace;
  }

  public void validate(final IValidation results) {
    // TODO Auto-generated method stub

  }

}
