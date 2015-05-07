/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;


public class AxisEditorDialog extends ModelDialog {

  private final IPlot _plot;

  private final IAxis _axis;

  public AxisEditorDialog(final Shell shell, final IPlot plot, final IAxis axis) {
    super(shell, "Axis Editor");
    _plot = plot;
    _axis = axis;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    FormSection label = form.addSection("Label", false);

    TextField labelText = label.addTextField(Axis.LABEL_TEXT);
    labelText.setLabel("Text");

    FormSection anno = form.addSection("Annotation", false);

    LabelField axisUnit = anno.addLabelField(Axis.AXIS_UNIT);
    axisUnit.setLabel("Unit");

    if (_axis.isScaleEditable()) {
      RadioGroupField axisScale = anno.addRadioGroupField(Axis.AXIS_SCALE, AxisScale.values());
      axisScale.setLabel("Scale");
    } else {
      LabelField axisScale = anno.addLabelField(Axis.AXIS_SCALE);
      axisScale.setLabel("Scale");
    }

    LabelField axisOrientation = anno.addLabelField(Axis.AXIS_ORIENTATION);
    axisOrientation.setLabel("Orientation");

    AxisDirection[] directions = new AxisDirection[0];
    if (_axis.getOrientation().equals(Orientation.HORIZONTAL)) {
      directions = new AxisDirection[] { AxisDirection.LEFT_TO_RIGHT, AxisDirection.RIGHT_TO_LEFT };
    } else if (_axis.getOrientation().equals(Orientation.VERTICAL)) {
      directions = new AxisDirection[] { AxisDirection.BOTTOM_TO_TOP, AxisDirection.TOP_TO_BOTTOM };
    }
    RadioGroupField axisDirection = anno.addRadioGroupField(Axis.AXIS_DIRECTION, directions);
    axisDirection.setLabel("Direction");

    TextField viewableStart = anno.addTextField(Axis.VIEWABLE_START);
    viewableStart.setLabel("Start");

    TextField viewableEnd = anno.addTextField(Axis.VIEWABLE_END);
    viewableEnd.setLabel("End");
  }

  @Override
  protected IModel createModel() {
    return new AxisEditorModel(_axis);
  }

  @Override
  protected void applySettings() {
    AxisEditorModel model = (AxisEditorModel) _model;
    AxisEditorModel modelUndo = (AxisEditorModel) _modelUndo;
    _axis.getLabel().setText(model.getLabelText());
    _axis.setScale(model.getAxisScale());
    _axis.setDirection(model.getAxisDirection());
    _axis.setViewableRange(model.getViewableStart(), model.getViewableEnd());
    _axis.setDefaultRange(model.getViewableStart(), model.getViewableEnd());
    _plot.updateAll();
  }
}
