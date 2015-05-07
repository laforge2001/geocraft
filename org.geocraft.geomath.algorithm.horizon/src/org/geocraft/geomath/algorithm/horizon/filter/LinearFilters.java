package org.geocraft.geomath.algorithm.horizon.filter;


import java.io.IOException;
import java.util.Formatter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class LinearFilters extends StandaloneAlgorithm {

  /** Enumeration of available filters to use */
  public enum FilterMethod {
    GAUSSIAN_FILTER("Gaussian", new GaussianFilter(), "gauss"),
    SQUARE_MEAN_FILTER("Square mean", new SquareMeanFilter(), "sqmean"),
    CIRCULAR_MEAN_FILTER("Circular mean", new CircularMeanFilter(), "circmean");

    private String _method;

    private AbstractFilter _filter;

    private String _abbrev;

    FilterMethod(String method, AbstractFilter filter, String abbrev) {
      _method = method;
      _filter = filter;
      _abbrev = abbrev;
    }

    public int getNumFilters() {
      return FilterMethod.values().length;
    }

    public AbstractFilter getFilter() {
      return _filter;
    }

    public String getAbbrev() {
      return _abbrev;
    }

    @Override
    public String toString() {
      return _method;
    }
  }

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The filter method property. */
  EnumProperty<FilterMethod> _filterMethodProp;

  /** Sample size */
  public IntegerProperty _size;

  /** The kernel in formatted into a string */
  public StringProperty _kernelString;

  //  IFilter _defaultFilter = _filters[0];

  int _defaultSize = 5;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public LinearFilters() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    //Set the default filter (Gaussian)
    _filterMethodProp = addEnumProperty("Filter Method", FilterMethod.class, FilterMethod.GAUSSIAN_FILTER);
    _size = addIntegerProperty("Size", _defaultSize);
    _kernelString = addStringProperty("Kernel", getKernel());
    _outputGridName = addStringProperty("Output Grid Name", "filter1");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);

    inputSection.addListSelectionField(_filterMethodProp.getKey(), FilterMethod.values());

    FormSection parametersSection = form.addSection("Parameters", false);
    parametersSection.addTextField(_size);
    parametersSection.addTextBox(_kernelString);

    // Build the output parameters section.
    FormSection outputSection = form.addSection("Output", false);
    outputSection.addTextField(_outputGridName);
    outputSection.addTextBox(_outputComments);
  }

  public String getKernel() {
    float[][] kernel = _filterMethodProp.get().getFilter().getDefaultKernel(_size.get());
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter(builder);
    for (float[] row : kernel) {
      for (float element : row) {
        formatter.format(" %11.9f", element);
      }
      formatter.format("\n");
    }
    return builder.toString();
  }

  public void updateOutputName() {

    String nameSuffix = "_" + _filterMethodProp.get().getAbbrev() + _size.get();
    String outputName = _inputGrid.isNull() ? "" : _inputGrid.get().getMapper()
        .createOutputDisplayName(_inputGrid.get().getDisplayName(), nameSuffix);
    _outputGridName.set(outputName);
  }

  @Override
  public void propertyChanged(String key) {
    // Auto-generate an output name from the input grid.
    if (key.equals(_inputGrid.getKey())) {
      updateOutputName();
    } else if (key.equals(_filterMethodProp.getKey())) {
      _kernelString.set(getKernel());
      updateOutputName();
    } else if (key.equals(_size.getKey())) {
      if (_filterMethodProp.get().getFilter().validateSize(_size.get())) {
        _kernelString.set(getKernel());
        updateOutputName();
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    // Validate the output name is non-zero length.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    }

    // Validate the filter size
    if (!_filterMethodProp.get().getFilter().validateSize(_size.get())) {
      results.error(_size, _filterMethodProp.get().getFilter().getMessage());
    }

    // Check if an entry already exists in the datastore.
    if (!_inputGrid.isNull() && !_outputGridName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputGrid.get(), _outputGridName.get())) {
        results.warning(_outputGridName, "Exists in datastore and will be overwritten.");
      }
    }
  }

  /**
   * @throws CoreException
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {

    Grid3d property = _inputGrid.get();
    int size = _size.get();
    String propertyName = _outputGridName.get();
    String outputComments = _outputComments.get();
    float[][] kernel = _filterMethodProp.get().getFilter().getDefaultKernel(_size.get());

    // apply the filter on the horizon
    float[][] result = _filterMethodProp.get().getFilter().execute(property, size, kernel, monitor);

    // Create the new property
    Grid3d newProperty = Grid3dFactory.create(repository, property, result, propertyName);
    newProperty.setComment(outputComments);
    try {
      newProperty.update();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }
}
