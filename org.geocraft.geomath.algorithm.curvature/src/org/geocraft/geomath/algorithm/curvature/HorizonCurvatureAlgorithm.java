package org.geocraft.geomath.algorithm.curvature;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.curvature.attribute.AzimuthAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.ContourCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.CurvednessAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.DipAngleAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.DipCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.GaussianCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.MaximumCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.MeanCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.MinimumCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.NegativeCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.PositiveCurvatureAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.ShapeIndexAttribute;
import org.geocraft.geomath.algorithm.curvature.attribute.StrikeCurvatureAttribute;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.TextBox;
import org.geocraft.ui.form2.field.TextField;


public class HorizonCurvatureAlgorithm extends StandaloneAlgorithm {

  /**
   * Enumeration of available curvature methods to use
   */
  public enum CurvatureMethod {
    MEAN_CURVATURE("Mean curvature", new MeanCurvatureAttribute()),
    GAUSSIAN_CURVATURE("Gaussian curvature", new GaussianCurvatureAttribute()),
    DIP_ANGLE("Dip angle", new DipAngleAttribute()),
    AZIMUTH("Azimuth", new AzimuthAttribute()),
    MAXIMUM_CURVATURE("Maximum curvature", new MaximumCurvatureAttribute()),
    MINIMUM_CURVATURE("Minimum curvature", new MinimumCurvatureAttribute()),
    POSITIVE_CURVATURE("Positive curvature", new PositiveCurvatureAttribute()),
    NEGATIVE_CURVATURE("Negative curvature", new NegativeCurvatureAttribute()),
    SHAPE_INDEX("Shape index", new ShapeIndexAttribute()),
    DIP_CURVATURE("Dip curvature", new DipCurvatureAttribute()),
    STRIKE_CURVATURE("Strike curvature", new StrikeCurvatureAttribute()),
    CONTOUR_CURVATURE("Contour curvature", new ContourCurvatureAttribute()),
    CURVEDNESS("Curvedness", new CurvednessAttribute());

    private String _method;

    private AbstractAttribute _curvatureAttribute;

    CurvatureMethod(String method, AbstractAttribute attr) {
      _method = method;
      _curvatureAttribute = attr;
    }

    public int getNumCurvatures() {
      return CurvatureMethod.values().length;
    }

    public AbstractAttribute getCurvatureAttribute() {
      return _curvatureAttribute;
    }

    @Override
    public String toString() {
      return _method;
    }
  }

  // DEFAULTS
  static final int DEFAULT_APERTURE = 5;

  // List of curvatures
  private AbstractAttribute attrs[];

  /** The input grid. */
  private EntityProperty<Grid3d> _horizonProp;

  /** The curvature method property. */
  private EnumProperty<CurvatureMethod> _curvatureMethodProp;

  /** Curvature aperture parameter property. */
  private IntegerProperty _apertureProp;

  /** The name of the output file. */
  private StringProperty _nameProp;

  /** The output comments. */
  private StringProperty _commentsProp;

  private ComboField _curvatureField;

  private TextField _outputNameField;

  /**
   * Define and initialize all the GUI components constituting the algorithm's UI.
   */
  public HorizonCurvatureAlgorithm() {
    super();

    // Create list of available curvatures
    CurvatureMethod[] curvatures = CurvatureMethod.values();
    int numCurvatures = curvatures.length;
    attrs = new AbstractAttribute[numCurvatures];
    for (int i = 0; i < numCurvatures; i++) {
      attrs[i] = curvatures[i].getCurvatureAttribute();
    }
    _horizonProp = addEntityProperty("Horizon", Grid3d.class);

    //Set the default curvature (Gaussian)
    _curvatureMethodProp = addEnumProperty("Curvature Method", CurvatureMethod.class,
        CurvatureMethod.GAUSSIAN_CURVATURE);
    _apertureProp = addIntegerProperty("Aperture", DEFAULT_APERTURE);

    _nameProp = addStringProperty("Name", "_" + _curvatureMethodProp.get().getCurvatureAttribute().getSymbol()
        + DEFAULT_APERTURE);

    _commentsProp = addStringProperty("Comments", "");
  }

  @Override
  public void buildView(IModelForm modelForm) {
    // Build the input parameters section.
    FormSection inputSection = modelForm.addSection("Input", false);
    EntityComboField horizonField = inputSection.addEntityComboField(_horizonProp, Grid3d.class);
    horizonField.setTooltip("Select a horizon (grid)");

    // Build the horizon curvature's parameters section.
    FormSection parmsSection = modelForm.addSection("Parameters", false);
    _curvatureField = parmsSection.addComboField(_curvatureMethodProp, CurvatureMethod.values());
    _curvatureField.setTooltip("Select a curvature attribute");

    TextField apertureField = parmsSection.addTextField(_apertureProp);
    apertureField.setTooltip("Specify an aperture (wavelength)");

    // Build the output parameters section.
    FormSection outputSection = modelForm.addSection("Output", false);
    _outputNameField = outputSection.addTextField(_nameProp);
    _outputNameField.setTooltip("Specify the name of the output horizon");

    TextBox commentsField = outputSection.addTextBox(_commentsProp);
    commentsField.setTooltip("(Optional) Specify horizon property comments");
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Get all the model parameters
    Grid3d inputHorizon = _horizonProp.get();
    int numRows = inputHorizon.getNumRows();
    AbstractAttribute attributeParm = _curvatureMethodProp.get().getCurvatureAttribute();
    int apertureParm = _apertureProp.get();
    String outputHorizonName = _nameProp.get();
    String comments = _commentsProp.get();

    // Start the progress monitor.
    monitor.beginTask("Horizon Curvature...", numRows);

    // Compute horizon's curvature
    // determine the geometry of the input property
    GridGeometry3d geometry = inputHorizon.getGeometry();

    // apply the curvature to the horizon
    float[][] outputData = attributeParm.attribute(geometry, inputHorizon, apertureParm, monitor);

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid3d outputGrid = Grid3dFactory.create(repository, inputHorizon, outputData, outputHorizonName);
        outputGrid.setComment(comments);
        outputGrid.update();
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    // Task is done.
    monitor.done();
  }

  public void updateOutputName(String rootName, String nameSuffix) {

    String outputName = _horizonProp.isNull() ? "" : _horizonProp.get().getMapper()
        .createOutputDisplayName(rootName, nameSuffix);
    _outputNameField.updateField(outputName);
    _nameProp.set(outputName);

  }

  public void propertyChanged(String key) {
    String outputName = "";
    int suffixIdx = _curvatureField.getCombo().getSelectionIndex();
    String nameSuffix = "_" + attrs[suffixIdx].getSymbol() + _apertureProp.get();

    // If the input horizon changes, change the name of the output horizon
    if (key.equals(_horizonProp.getKey()) || key.equals(_apertureProp.getKey())
        || key.equals(_curvatureMethodProp.getKey())) {
      updateOutputName(_horizonProp.isNull() ? "" : _horizonProp.get().getDisplayName(), nameSuffix);
    }
  }

  public void validate(IValidation results) {
    // Validate the input horizon is non-null and of the correct type (grid).
    if (_horizonProp.isNull()) {
      results.error(_horizonProp, "No input horizon (grid) specified");
    }

    // Validate the output name is non-zero length.
    if (_nameProp.isEmpty()) {
      results.error(_nameProp, "No output horizon name specified.");
    }

    // Validate the aperture is an integer between 1 and 100
    int aperture = _apertureProp.get();
    if (aperture < 1 || aperture > 100) {
      results.error(_apertureProp, "Aperture should be an integer between 1 and 100");
    }

    // Check if the output horizon already exists in the datastore.
    if (!_horizonProp.isNull() && !_nameProp.isEmpty()) {
      if (Grid3dFactory.existsInStore(_horizonProp.get(), _nameProp.get())) {
        results.warning(_nameProp, " exists in datastore and will be overwritten.");
      }
    }
  }
}
