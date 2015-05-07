/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.calculator.basicmath;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.shell.ICommandExecutor;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


public class BasicMath extends StandaloneAlgorithm {

  /** Enumeration for the difference operations */
  public enum OperationType {
    ADD("b + c", "add", " + "),
    SUB("b - c", "sub", " - "),
    POW("b ** c", "pow", " ** "),
    MUL("b * c", "mul", " * "),
    DIV("b / c", "div", " / "),
    ABS("abs(b)", "abs", ""),
    MAX("max(b,c)", "max", ","),
    MIN("min(b,c)", "min", ","),
    AVG("avg(b,c)", "avg", ",");

    private final String _text;

    private final String _name;

    private final String _seperator;

    private OperationType(final String text, final String name, final String seperator) {
      _text = text;
      _name = name;
      _seperator = seperator;
    }

    /**
     * @see com.cop.spark.geomath.algorithm.waveletprocessing.waveletmath.WaveletMathModel.OperationType#getEquation()
     */
    public String getEquation(final String resultName, final String b, final String c) {
      // A blank separator corresponds to equation of type: a = name(b)
      if (_seperator.equals("")) {
        return resultName + " = " + _name + "(" + b + ")";
        // A comma separator corresponds to equation of type: a = name(b,c)  
      } else if (_seperator.equals(",")) {
        return resultName + " = " + _name + "(" + b + "," + c + ")";
        // otherwise the separator is the operator that is used in the calculation: 
        // Example: a = b + c  
      } else {
        return resultName + " = " + b + _seperator + c;
      }
    }

    /**
     * @see com.cop.spark.geomath.algorithm.waveletprocessing.waveletmath.WaveletMathModel.OperationType#numArguments()
     */
    public int numArguments() {
      int numArguments = 2;
      // A blank separator corresponds to equation of type: a = name(b)
      if (_seperator.equals("")) {
        numArguments = 1;
        // A comma separator corresponds to equation of type: a = name(b,c)  
      } else if (_seperator.equals(",")) {
        numArguments = 2;
        // otherwise the separator is the operator that is used in the calculation: 
        // Example: a = b + c  
      } else {
        numArguments = 2;
      }
      return numArguments;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  // UI TYPES
  // Input section

  // Input horizon #1
  protected final EntityProperty<Grid3d> _b;

  // Use a constant
  protected final BooleanProperty _useConstant1;

  protected final DoubleProperty _constant1;

  protected final EntityProperty<Grid3d> _c;

  // Use a constant
  protected final BooleanProperty _useConstant2;

  protected final DoubleProperty _constant2;

  protected final EnumProperty<OperationType> _operationType;

  protected final StringProperty _operationTypeLabel;

  protected final StringProperty _a;// = "a1";

  /** The output grid comments property. */
  protected final StringProperty _outputComments;

  public BasicMath() {
    super();
    _b = addEntityProperty("b", Grid3d.class);
    _constant1 = addDoubleProperty("constant b", 0);
    _useConstant1 = addBooleanProperty("Use Constant for b", false);
    _c = addEntityProperty("c", Grid3d.class);
    _constant2 = addDoubleProperty("constant c", 0);
    _useConstant2 = addBooleanProperty("Use Constant for c", false);

    _operationType = addEnumProperty("The operation to perform", OperationType.class, OperationType.ADD);
    _operationTypeLabel = addStringProperty("Example", "a = b + c");
    _a = addStringProperty("a", "a1");
    _outputComments = addStringProperty("Comments", "");
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_useConstant1.getKey())) {
      setFieldVisible(_b, !_useConstant1.get());
      setFieldVisible(_constant1, _useConstant1.get());
    } else if (key.equals(_useConstant2.getKey())) {
      setFieldVisible(_c, !_useConstant2.get());
      setFieldVisible(_constant2, _useConstant2.get());
    } else if (key.equals(_operationType.getKey())) {
      int numArguments = _operationType.get().numArguments();
      // The c argument is not needed if the number of arguments is one
      if (numArguments == 1) {
        setFieldVisible(_c, false);
        setFieldVisible(_constant2, false);
        setFieldVisible(_useConstant2, false);
      } else {
        setFieldVisible(_c, !_useConstant2.get());
        setFieldVisible(_constant2, _useConstant2.get());
        setFieldVisible(_useConstant2, true);
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    if (!_useConstant1.get() && _b.isNull()) {
      results.error(_b, "b cannot be empty or null");
    }

    int numArguments = _operationType.get().numArguments();
    if (!_useConstant2.get() && numArguments > 1 && _c.isNull()) {
      results.error(_c, "c cannot be empty or null");
    }

    // Validate the output horizon name.
    if (_a.isEmpty()) {
      results.error(_a, "No output horizon name specified.");
    } else {
      if (!_b.isNull()) {
        IStatus status = DataSource.validateName(_b.get(), _a.get());
        if (!status.isOK()) {
          results.setStatus(_a, status);
        }
      }
    }

    // make sure the 2 horizons have the same geometry
    if (!_b.isNull() && !_c.isNull()) {
      GridGeometry3d bGeometry = _b.get().getGeometry();
      GridGeometry3d cGeometry = _c.get().getGeometry();
      if (!bGeometry.matchesGeometry(cGeometry)) {
        results.error(_c, "The 2 horizons should have the same geometry..");
      }
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(_b, Grid3d.class);
    TextField constant1Field = inputSection.addTextField(_constant1);
    constant1Field.setVisible(false);
    inputSection.addCheckboxField(_useConstant1);
    inputSection.addEntityComboField(_c, Grid3d.class);
    TextField constant2Field = inputSection.addTextField(_constant2);
    constant2Field.setVisible(false);
    inputSection.addCheckboxField(_useConstant2);
    RadioGroupField operationTypeField = inputSection.addRadioGroupField(_operationType, OperationType.values());
    operationTypeField.setTooltip("Select Operation to perform");
    inputSection.addLabelField(_operationTypeLabel);

    FormSection outputSection = modelForm.addSection("Output");
    TextField outputHorizonName = outputSection.addTextField(_a);
    outputHorizonName.setTooltip("The name of the output horizon");
    outputSection.addTextBox(_outputComments);
  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    String resultName = _a.get();
    String b;
    if (_useConstant1.get()) {
      b = Double.toString(_constant1.get());
    } else {
      b = repository.lookupVariableName(_b.get());
    }
    String c;
    if (_useConstant2.get()) {
      c = Double.toString(_constant2.get());
    } else {
      c = repository.lookupVariableName(_c.get());
    }

    String equation = _operationType.get().getEquation(resultName, b, c);
    String comments = _outputComments.get();

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] extensions = registry
        .getConfigurationElementsFor("org.geocraft.core.shell.commandexecutor");
    for (IConfigurationElement extension : extensions) {
      try {
        ICommandExecutor commandExecutor = (ICommandExecutor) extension.createExecutableExtension("class");
        commandExecutor.executeCommand(equation);
        if (comments.length() > 0) {
          commandExecutor.executeCommand(resultName + ".setComment(\"" + comments + "\")");
        }
      } catch (Exception e) {
        logger.error("Failed executing the command:" + e.getMessage());
      }
    }
  }

}
