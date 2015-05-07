package org.geocraft.geomath.algorithm.velocity.flood.preview;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.util.IPostStack3dAlgorithm;
import org.geocraft.geomath.algorithm.util.PostStack3dAlgorithmMapper;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ConstantOrGridSelection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodType;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ReferenceSelection;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


public class VelocityFloodPreview extends StandaloneAlgorithm {

  private EntityProperty<PostStack3d> _velocityVolume;

  private EntityProperty<AreaOfInterest> _aoi;

  private BooleanProperty _aoiFlag;

  private EnumProperty<FloodDirection> _floodDirection;

  private EntityProperty<Grid3d> _topGrid;

  private EnumProperty<Unit> _gridUnits;

  private EntityProperty<Grid3d> _baseGrid;

  private EnumProperty<FloodType> _floodType;

  private EntityProperty<PostStack3d> _datasetVolume;

  private FloatProperty _multiplier;

  private FloatProperty _adder;

  private EnumProperty<ConstantOrGridSelection> _constantSelection;

  private FloatProperty _floodVelocityConstant;

  private EntityProperty<Grid3d> _velocityGrid;

  private FloatProperty _gradientConstant;

  private EntityProperty<Grid3d> _gradientGrid;

  private EnumProperty<ReferenceSelection> _referenceSelection;

  private FloatProperty _referenceDepthConstant;

  private EntityProperty<Grid3d> _referenceDepthGrid;

  private StringProperty _outputVolumeName;

  public VelocityFloodPreview() {
    _velocityVolume = addEntityProperty("Velocity Volume", PostStack3d.class);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _aoiFlag = addBooleanProperty("Use AOI", false);
    _floodDirection = addEnumProperty("Flood Direction", FloodDirection.class, FloodDirection.Below);
    _gridUnits = addEnumProperty("Grid Units", Unit.class, UnitPreferences.getInstance().getVerticalDistanceUnit());
    _topGrid = addEntityProperty("Top Grid", Grid3d.class);
    _baseGrid = addEntityProperty("Base Grid", Grid3d.class);
    _floodType = addEnumProperty("Flood Type", FloodType.class, FloodType.Constant);
    _datasetVolume = addEntityProperty("Dataset Volume", PostStack3d.class);
    _multiplier = addFloatProperty("Multiplier", 1);
    _adder = addFloatProperty("Adder", 1);
    _constantSelection = addEnumProperty("Constant Selection", ConstantOrGridSelection.class,
        ConstantOrGridSelection.Constant);
    _floodVelocityConstant = addFloatProperty("Flood Velocity(V0)", 5000);
    _velocityGrid = addEntityProperty("Velocity(V0) Grid", Grid3d.class);
    _gradientConstant = addFloatProperty("Flood Gradient(G)", 1);
    _gradientGrid = addEntityProperty("Gradient(G) Grid", Grid3d.class);
    _referenceSelection = addEnumProperty("Reference(Z0)", ReferenceSelection.class, ReferenceSelection.Constant);
    _referenceDepthConstant = addFloatProperty("Reference Depth(Z0)", 0);
    _referenceDepthGrid = addEntityProperty("Reference Depth(Z0) Grid", Grid3d.class);
    _outputVolumeName = addStringProperty("Output Volume Name", "flood1");
  }

  @Override
  public void buildView(IModelForm form) {

    FormSection inputSection = form.addSection("Input");
    EntityComboField velocityVolumeField = inputSection.addEntityComboField(_velocityVolume, PostStack3d.class);
    velocityVolumeField.setTooltip("Select a velocity volume (PostStack3d)");
    EntityComboField aoiField = inputSection.addEntityComboField(_aoi, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_aoiFlag);

    FormSection gridSection = form.addSection("Grids");
    RadioGroupField floodDirectionField = gridSection.addRadioGroupField(_floodDirection, FloodDirection.values());
    floodDirectionField.setTooltip("Select a flood direction");
    gridSection.addLabelField(_gridUnits);
    EntityComboField topGridField = gridSection.addEntityComboField(_topGrid, Grid3d.class);
    topGridField.setTooltip("Select a grid containing the starting depths for flooding");
    EntityComboField baseGridField = gridSection.addEntityComboField(_baseGrid, Grid3d.class);
    baseGridField.setTooltip("Select a grid containing the ending depths for flooding");
    baseGridField.setVisible(false);

    FormSection parametersSection = form.addSection("Parameters");
    RadioGroupField floodTypeField = parametersSection.addRadioGroupField(_floodType, FloodType.values());
    floodTypeField
        .setTooltip("Constant: use constant value for flooding\nGradient: use a 'v0 + g*z' gradient for flood values\nDataset: use a dataset for flood values");

    EntityComboField dataSetVolumeField = parametersSection.addEntityComboField(_datasetVolume, PostStack3d.class);
    dataSetVolumeField.setTooltip("Select a volume used when flooding with a dataset");
    dataSetVolumeField.setVisible(false);

    TextField multiplierField = parametersSection.addTextField(_multiplier);
    multiplierField.setTooltip("Specify a multiplier to scale the output volume");
    multiplierField.setVisible(false);

    TextField adderField = parametersSection.addTextField(_adder);
    adderField.setTooltip("Specify an add factor to scale the output volume");
    adderField.setVisible(false);

    RadioGroupField constantSelectionField = parametersSection.addRadioGroupField(_constantSelection,
        ConstantOrGridSelection.values());
    constantSelectionField.setTooltip("Specify constant flood parameters or select grids");

    TextField floodVelocityField = parametersSection.addTextField(_floodVelocityConstant);
    floodVelocityField.setTooltip("Specify a constant flood velocity");

    EntityComboField velocityGridField = parametersSection.addEntityComboField(_velocityGrid, Grid3d.class);
    velocityGridField.setTooltip("Select a grid with velocities");
    velocityGridField.setVisible(false);

    TextField gradientConstantField = parametersSection.addTextField(_gradientConstant);
    gradientConstantField.setTooltip("Specify a constant flood gradient");
    gradientConstantField.setVisible(false);

    EntityComboField gradientGridField = parametersSection.addEntityComboField(_gradientGrid, Grid3d.class);
    gradientGridField.setTooltip("Select a grid with gradients");
    gradientGridField.setVisible(false);

    RadioGroupField referenceSelectionField = parametersSection.addRadioGroupField(_referenceSelection,
        ReferenceSelection.values());
    referenceSelectionField.setTooltip("Specify a constant reference depth or select a reference grid");
    referenceSelectionField.setVisible(false);

    TextField referenceDepthConstantField = parametersSection.addTextField(_referenceDepthConstant);
    referenceDepthConstantField.setTooltip("Specify a constant reference depth");
    referenceDepthConstantField.setVisible(false);

    EntityComboField referenceGridField = parametersSection.addEntityComboField(_referenceDepthGrid, Grid3d.class);
    referenceGridField.setTooltip("Select a grid containing reference depths");
    referenceGridField.setVisible(false);

    FormSection outputSection = form.addSection("Output");
    TextField outVolumeNameField = outputSection.addTextField(_outputVolumeName);
    outVolumeNameField.setTooltip("The name of the output volume");
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_velocityVolume.getKey()) && !_velocityVolume.isNull()) {
      _outputVolumeName.set(_velocityVolume.get().getDisplayName() + "_flood1");
    }
    if (key.equals(_floodDirection.getKey())) {
      FloodDirection floodDirection = _floodDirection.get();
      boolean useTopGrid = floodDirection != FloodDirection.Above;
      boolean useBaseGrid = floodDirection != FloodDirection.Below;
      setFieldVisible(_topGrid, useTopGrid);
      setFieldVisible(_baseGrid, useBaseGrid);
    } else if (key.equals(_floodType.getKey()) || key.equals(_constantSelection.getKey())
        || key.equals(_referenceSelection.getKey())) {
      FloodType floodType = _floodType.get();
      ConstantOrGridSelection constantSelection = _constantSelection.get();
      ReferenceSelection referenceSelection = _referenceSelection.get();
      setFieldVisible(_datasetVolume, floodType == FloodType.Dataset);
      setFieldVisible(_multiplier, floodType == FloodType.Dataset);
      setFieldVisible(_adder, floodType == FloodType.Dataset);
      setFieldVisible(_constantSelection, floodType != FloodType.Dataset);
      setFieldVisible(_floodVelocityConstant, floodType != FloodType.Dataset
          && constantSelection == ConstantOrGridSelection.Constant);
      setFieldVisible(_velocityGrid, floodType != FloodType.Dataset
          && constantSelection == ConstantOrGridSelection.Grid);
      setFieldVisible(_gradientConstant, floodType == FloodType.Gradient
          && constantSelection == ConstantOrGridSelection.Constant);
      setFieldVisible(_gradientGrid, floodType == FloodType.Gradient
          && constantSelection == ConstantOrGridSelection.Grid);
      setFieldVisible(_referenceSelection, floodType == FloodType.Gradient);
      setFieldVisible(_referenceDepthConstant, floodType == FloodType.Gradient
          && referenceSelection == ReferenceSelection.Constant);
      setFieldVisible(_referenceDepthGrid, floodType == FloodType.Gradient
          && referenceSelection == ReferenceSelection.Grid);
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the velocity volume.
    if (_velocityVolume.isNull()) {
      results.error(_velocityVolume, "No velocity volume specified.");
    } else {
      Domain dataDomain = _velocityVolume.get().getDataDomain();
      if (!dataDomain.equals(Domain.VELOCITY)) {
        results.error(_velocityVolume, "Not a velocity volume.");
      }
    }

    // Validate the flood direction and the top and base grids.
    FloodDirection floodDirection = _floodDirection.get();
    if (_floodDirection.isNull()) {
      results.error(_floodDirection, "No flood direction specified.");
    } else {
      if (floodDirection != FloodDirection.Above) {
        if (_topGrid.isNull()) {
          results.error(_topGrid, "No top grid specified.");
        } else if (!_velocityVolume.isNull()) {
          Domain zDomain = _velocityVolume.get().getZDomain();
          if (_topGrid.get().getDataDomain() != zDomain) {
            results.error(_topGrid, "Top grid must be in the " + zDomain.getTitle().toLowerCase());
          }
        }
      }
      if (floodDirection != FloodDirection.Below) {
        if (_baseGrid.isNull()) {
          results.error(_baseGrid, "No base grid specified.");
        } else if (!_velocityVolume.isNull()) {
          Domain zDomain = _velocityVolume.get().getZDomain();
          if (_baseGrid.get().getDataDomain() != zDomain) {
            results.error(_baseGrid, "Base grid must be in the " + zDomain.getTitle().toLowerCase());
          }
        }
      }
    }

    // Validate the domain of the gradient grid.
    if (!_gradientGrid.isNull() && _gradientGrid.get().getDataDomain() != Domain.VELOCITY_GRADIENT) {
      results.error(_gradientGrid, "Not a velocity gradient grid.");
    }

    // Validate the domain of the reference depth grid.
    if (!_referenceDepthGrid.isNull() && _referenceDepthGrid.get().getDataDomain() != Domain.DISTANCE) {
      results.error(_referenceDepthGrid, "Not a depth grid.");
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the model parameters.
    PostStack3d inputVelocityVolume = _velocityVolume.get();
    AreaOfInterest areaOfInterest = _aoi.get();
    if (!_aoiFlag.get()) {
      areaOfInterest = null;
    }
    Grid3d topGrid = _topGrid.get();
    Grid3d baseGrid = _baseGrid.get();
    FloodType floodType = _floodType.get();
    ConstantOrGridSelection constantSelection = _constantSelection.get();
    Grid3d velocityGrid = _velocityGrid.get();
    Grid3d gradientGrid = _gradientGrid.get();
    Grid3d referenceDepthGrid = _referenceDepthGrid.get();
    float floodVelocity = _floodVelocityConstant.get();
    float gradientConstant = _gradientConstant.get();
    float referenceDepthConstant = _referenceDepthConstant.get();
    PostStack3d datasetVolume = _datasetVolume.get();
    FloodDirection floodDirection = _floodDirection.get();
    String outputVolumeName = _outputVolumeName.get();
    float multiplier = _multiplier.get();
    float adder = _adder.get();
    ReferenceSelection referenceDepthSelection = _referenceSelection.get();

    // Initialize the progress monitor.
    monitor.beginTask("Velocity Flood of " + inputVelocityVolume.getDisplayName(), 2);

    try {

      // Update the progress monitor message.
      monitor.subTask("Creating output volume...");

      IPostStack3dAlgorithm algorithm = new ExampleVelocityFloodAlgorithm(inputVelocityVolume, areaOfInterest,
          floodType, constantSelection, floodVelocity, velocityGrid, constantSelection, gradientConstant, gradientGrid,
          referenceDepthSelection, referenceDepthConstant, referenceDepthGrid, floodDirection, topGrid, baseGrid,
          datasetVolume, multiplier, adder, outputVolumeName, "");

      // Create the algorithm mapper.
      IPostStack3dMapper mapper = new PostStack3dAlgorithmMapper(algorithm);

      // Create the output volume.
      PostStack3d outputVelocityVolume = PostStack3dFactory.create(outputVolumeName, mapper);
      outputVelocityVolume.load();
      outputVelocityVolume.setDirty(true);

      // Update the progress monitor.
      monitor.worked(1);

      // Add the output volume to the repository.
      repository.add(outputVelocityVolume);

      // Update the progress monitor.
      monitor.worked(1);
      monitor.done();
    } catch (Exception ex) {
      throw new CoreException(ValidationStatus.error(ex.getMessage()));
    }
  }

}
