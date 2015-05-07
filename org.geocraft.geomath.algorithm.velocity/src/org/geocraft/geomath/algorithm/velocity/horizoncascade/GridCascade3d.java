package org.geocraft.geomath.algorithm.velocity.horizoncascade;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.horizonstretch.GridTimeDepthConversion;
import org.geocraft.internal.geomath.algorithm.velocity.DepthGridSpecification;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityVolumeSpecification;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextField;


public class GridCascade3d extends StandaloneAlgorithm {

  private EntityArrayProperty<Grid3d> _inputGrids;

  private EntityProperty<PostStack3d> _velocityVolume1;

  private EntityProperty<PostStack3d> _velocityVolume2;

  private EntityProperty<AreaOfInterest> _aoi;

  private BooleanProperty _aoiFlag;

  private EnumProperty<Method> _conversionMethod;

  private BooleanProperty _saveTimeHorizon;

  private StringProperty _d2tGridSuffix;

  private StringProperty _t2dGridSuffix;

  public GridCascade3d() {
    _inputGrids = addEntityArrayProperty("Depth Grid(s)", Grid3d.class);
    _velocityVolume1 = addEntityProperty("1st Velocity Volume", PostStack3d.class);
    _velocityVolume2 = addEntityProperty("2nd Velocity Volume", PostStack3d.class);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _aoiFlag = addBooleanProperty("Use Area of Interest", false);
    _conversionMethod = addEnumProperty("Conversion Method", Method.class, Method.KneeBased);
    _saveTimeHorizon = addBooleanProperty("Save a time horizon?", false);
    _d2tGridSuffix = addStringProperty("Depth-to-Time Suffix", "time");
    _t2dGridSuffix = addStringProperty("Time-to-Depth Suffix", "depth");
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input");

    ISpecification depthGridFilter = new DepthGridSpecification();
    section.addEntityListField(_inputGrids, depthGridFilter);

    ISpecification velocityVolumeFilter = new VelocityVolumeSpecification();
    section.addEntityComboField(_velocityVolume1, velocityVolumeFilter);
    section.addEntityComboField(_velocityVolume2, velocityVolumeFilter);

    ComboField aoiField = section.addEntityComboField(_aoi, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_aoiFlag);

    section = form.addSection("Parameters");
    section.addRadioGroupField(_conversionMethod, Method.values());

    section = form.addSection("Output");
    section.addCheckboxField(_saveTimeHorizon);
    TextField textField = section.addTextField(_d2tGridSuffix);
    textField.setEnabled(_saveTimeHorizon.get());
    section.addTextField(_t2dGridSuffix);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_saveTimeHorizon.getKey())) {
      // Disable the time suffix if we are not saving the time horizon
      setFieldEnabled(_d2tGridSuffix, _saveTimeHorizon.get());
    }
  }

  @Override
  public void validate(IValidation results) {
    // Check that at least 1 input grid is specified.
    if (_inputGrids.isEmpty()) {
      results.error(_inputGrids, "No depth grid(s) specified.");
    }

    // Check that the 1st volume is specified and a velocity volume.
    if (_velocityVolume1.isNull()) {
      results.error(_velocityVolume1, "The 1st velocity volume not specified.");
    } else {
      // Make sure the velocity units are correct
      Unit velocityUnit = _velocityVolume1.get().getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume1, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol()
            + " or " + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
    }

    // Check that the 2nd volume is specified and a velocity volume.
    if (_velocityVolume2.isNull()) {
      results.error(_velocityVolume2, "The 2nd velocity volume not specified.");
    } else {
      // Make sure the velocity units are correct
      Unit velocityUnit = _velocityVolume2.get().getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume2, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol()
            + " or " + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
    }

    // Check that suffixes has been supplied for the output grids.
    if (_d2tGridSuffix.isEmpty()) {
      results.error(_d2tGridSuffix, "No depth-to-time output suffix specified.");
    }
    if (_t2dGridSuffix.isEmpty()) {
      results.error(_d2tGridSuffix, "No time-to-depth output suffix specified.");
    }
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the parameters.
    Grid3d[] inputGrids = _inputGrids.get();
    PostStack3d velocityVolume1 = _velocityVolume1.get();
    PostStack3d velocityVolume2 = _velocityVolume2.get();
    Method conversionMethod = _conversionMethod.get();
    AreaOfInterest aoi = _aoi.get();
    if (!_aoiFlag.get()) {
      aoi = null;
    }
    boolean saveTimeHorizon = _saveTimeHorizon.get();
    String d2tSuffix = _d2tGridSuffix.get();
    String t2dSuffix = _t2dGridSuffix.get();

    // When converting grid from depth to time use repository when saving the time horizon
    IRepository d2tRepository = null;
    if (saveTimeHorizon) {
      d2tRepository = repository;
    }

    monitor.beginTask("Cascading Grids (D->T->D)", 2 * inputGrids.length);

    for (Grid3d inputGrid : inputGrids) {
      String inputGridName = inputGrid.getDisplayName();
      monitor.subTask("Cascading " + inputGridName);
      String gridNameD2T = inputGrid.getMapper().createOutputDisplayName(inputGridName, "_" + d2tSuffix);
      String gridNameT2D = inputGrid.getMapper().createOutputDisplayName(inputGridName, " " + t2dSuffix);

      // Set up to run grid stretch
      GridTimeDepthConversion gridStretch = new GridTimeDepthConversion();

      // Convert input grid from depth to time using the 1st velocity volume.
      Grid3d gridD2T = gridStretch.convertGrid(inputGrid, velocityVolume1, conversionMethod, aoi, gridNameD2T,
          inputGrid, new SubProgressMonitor(monitor, 1), logger, d2tRepository);

      // Convert the converted grid from time back to depth using the 2nd velocity volume.
      gridStretch.convertGrid(gridD2T, velocityVolume2, conversionMethod, aoi, gridNameT2D, inputGrid,
          new SubProgressMonitor(monitor, 1), logger, repository);

      // Update the progress monitor.
      monitor.subTask("Completed grid: " + inputGrid.getDisplayName());
      if (monitor.isCanceled()) {
        break;
      }
    }
  }

}
