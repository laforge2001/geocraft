/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.flood;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.specification.GridSpecification;
import org.geocraft.core.model.specification.SeismicDatasetSpecification;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.AbstractSpecification;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ConstantOrGridSelection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodType;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ReferenceSelection;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


public class VelocityFloodAlgorithm extends StandaloneAlgorithm {

  //DEFAULTS
  /** Default output volume suffix */
  static final String OUT_SUFFIX = "_vf1";

  // UI TYPES
  // Input section
  /** Input volume of background velocities. */
  private EntityProperty<PostStack3d> _velocityVolume;

  /** Area of Interest property */
  private EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The use-area-of-interest property. */
  private BooleanProperty _useAOI;

  // Parameter section
  /** The flooding strategy (constant, gradient, dataset). */
  private EnumProperty<FloodType> _floodType;

  /** The flooding direction (below, above, between) */
  private EnumProperty<FloodDirection> _floodDirection;

  /** Flood parameters selection: use a constant value or a variable grid */
  private EnumProperty<ConstantOrGridSelection> _constantSelection;

  /** Constant flood velocity value */
  private FloatProperty _floodVelocityConstant;

  /** Constant flood gradient value */
  private FloatProperty _gradientConstant;

  /** Input grid which contain velocities */
  private EntityProperty<Grid3d> _velocityGrid;

  /** Input grid which contain gradients */
  private EntityProperty<Grid3d> _gradientGrid;

  /** Gradient reference selection: use a constant depth or a variable grid */
  private EnumProperty<ReferenceSelection> _referenceSelection;

  /** Input reference depth constant */
  private FloatProperty _referenceDepthConstant;

  /** Input reference grid */
  private EntityProperty<Grid3d> _referenceDepthGrid;

  /** Volume used when using a dataset to flood values */
  private EntityProperty<PostStack3d> _datasetVolume;

  /** Input grid properties (e.g. tops and bases salt). */
  private EntityProperty<Grid3d> _topGrid;

  private EnumProperty<Unit> _gridUnits;

  private EntityProperty<Grid3d> _baseGrid;

  private FloatProperty _multiplier;

  private FloatProperty _adder;

  // Output section
  /** The output volume. */
  private StringProperty _outputVolumeName;

  // UI FIELDS

  ComboField _volumeField;

  LabelField _gridUnitsLabel;

  TextField _outputNameField;

  public static final String VEL_VELOCITY = "Velocity Volume";

  public static final String AOI = "Area of Interest";

  public static final String USE_AOI = "UseAOI";

  public static final String CONSTANT_SELECTION = "Constant selection";

  public static final String FLOOD_TYPE = "Flood Type";

  public static final String FLOOD_DIRECTION = "Flood Direction";

  public static final String FLOOD_VELOCITY = "Flood Velocity(V0)";

  public static final String FLOOD_GRADIENT = "Flood Gradient(G)";

  public static final String VELOCITY_GRID = "Velocity(V0) Grid";

  public static final String GRADIENT_GRID = "Gradient(G) Grid";

  public static final String REF_DEPTH_OR_HORIZON = "Reference(Z0)";

  public static final String REF_DEPTH = "Reference(Z0) Depth";

  public static final String REF_GRID = "Reference(Z0) Grid";

  public static final String DATA_SET_VOL = "Dataset Volume";

  public static final String GRID_UNITS = "Grid units";

  public static final String BASE_GRID = "Base Grid";

  public static final String TOP_GRID = "Top Grid";

  public static final String MULTIPLIER = "Multiplier";

  public static final String ADDER = "Adder";

  public static final String OUTPUT_VOL_NAME = "Output Volume Name";

  public VelocityFloodAlgorithm() {
    super();
    _velocityVolume = addEntityProperty(VEL_VELOCITY, PostStack3d.class);
    _areaOfInterest = addEntityProperty(AOI, AreaOfInterest.class);
    _useAOI = addBooleanProperty(USE_AOI, false);
    // Set the default flood type to Constant
    _floodType = addEnumProperty(FLOOD_TYPE, FloodType.class, FloodType.Constant);
    // Set the default flood direction to Below
    _floodDirection = addEnumProperty(FLOOD_DIRECTION, FloodDirection.class, FloodDirection.Below);
    // Set the flood parameters default to use a constant
    _constantSelection = addEnumProperty(CONSTANT_SELECTION, ConstantOrGridSelection.class,
        ConstantOrGridSelection.Constant);
    _floodVelocityConstant = addFloatProperty(FLOOD_VELOCITY, 5000.0f);
    _gradientConstant = addFloatProperty(FLOOD_GRADIENT, 1.0f);
    _velocityGrid = addEntityProperty(VELOCITY_GRID, Grid3d.class);
    _gradientGrid = addEntityProperty(GRADIENT_GRID, Grid3d.class);
    // Set the  gradient reference to use a constant depth
    _referenceSelection = addEnumProperty(REF_DEPTH_OR_HORIZON, ReferenceSelection.class, ReferenceSelection.Constant);
    _referenceDepthConstant = addFloatProperty(REF_DEPTH, 0.0f);
    _referenceDepthGrid = addEntityProperty(REF_GRID, Grid3d.class);
    _datasetVolume = addEntityProperty(DATA_SET_VOL, PostStack3d.class);
    _gridUnits = addEnumProperty(GRID_UNITS, Unit.class, UnitPreferences.getInstance().getVerticalDistanceUnit());
    // Set the default grid for the default flood direction
    _topGrid = addEntityProperty(TOP_GRID, Grid3d.class);
    _baseGrid = addEntityProperty(BASE_GRID, Grid3d.class);
    _multiplier = addFloatProperty(MULTIPLIER, 1.0f);
    _adder = addFloatProperty(ADDER, 0.0f);
    // Set the default name of the output volume
    _outputVolumeName = addStringProperty(OUTPUT_VOL_NAME, OUT_SUFFIX);
  }

  // FILTERS
  private final AbstractSpecification _postStack3dFilter = new TypeSpecification(PostStack3d.class);

  // FILTER SETTERS
  private void setVolumeFilter(ISpecification filter) {
    setFieldFilter(_velocityVolume, filter);
  }

  private void setTopGridFilter(ISpecification filter) {
    setFieldFilter(_topGrid, filter);
  }

  private void setBaseGridFilter(ISpecification filter) {
    setFieldFilter(_baseGrid, filter);
  }

  private void setReferenceGridFilter(ISpecification filter) {
    setFieldFilter(_referenceDepthGrid, filter);
  }

  private void setDatasetVolumeFilter(ISpecification filter) {
    setFieldFilter(_datasetVolume, filter);
  }

  private void setVelocityGridFilter(ISpecification filter) {
    setFieldFilter(_velocityGrid, filter);
  }

  private void setGradientGridFilter(ISpecification filter) {
    setFieldFilter(_gradientGrid, filter);
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    new VelocityFloodLogic().compute(monitor, logger, repository, this);

    //NOTE: Volumes are automatically persisted to the datastore

    // Task is done.
    monitor.done();
  }

  @Override
  public void propertyChanged(String key) {
    //    if (key.equals(_outVolumeName.getKey())) {
    //      return;
    //    }

    String fileName = "";
    PostStack3d inputVol;
    // Check if the input volume changed
    if (key.equals(_velocityVolume.getKey())) {
      if (!_velocityVolume.isNull()) {
        inputVol = _velocityVolume.get();
        // change the name of the output volume
        fileName = inputVol.getDisplayName() + OUT_SUFFIX;
        //_outputNameField.updateField(fileName);
        _outputVolumeName.set(fileName);

        // change the grid units based on the units of the input volume
        _gridUnitsLabel.updateField(inputVol.getZUnit().getName());

        //Change the filter on the top, base and reference grid fields
        // make sure that the user can only pick grids that 
        // 1) have the same domain for units as the input volume and 
        // 2) are of the same file type as the input volume
        ISpecification filter = new GridSpecification(inputVol.getZDomain());
        setTopGridFilter(filter);
        setBaseGridFilter(filter);
        setReferenceGridFilter(filter);

        //Change the filter on the dataset field
        // make sure that the user can only pick a dataset volume that 
        // 1) has the same domain as the input volume and 
        // 2) is of the same file type as the input volume
        AbstractSpecification domainFilter = new SeismicDatasetSpecification(inputVol.getZDomain().toString());
        filter = domainFilter.and(_postStack3dFilter);
        setDatasetVolumeFilter(filter);
      } else {
        _outputVolumeName.set(OUT_SUFFIX);
      }

      // Flood direction changed which changes the visibility of the base and top grids
    } else if (key.equals(_floodDirection.getKey())) {
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

      // Check if the flood type changed
      if (key.equals(_floodType.getKey())) {

        // Change the filter on the input volume which should remove the selection
        ISpecification filter;
        if (_floodType.get().equals(FloodType.Gradient)) {
          AbstractSpecification domainFilter = new SeismicDatasetSpecification(Domain.DISTANCE.toString());
          filter = domainFilter.and(_postStack3dFilter);
        } else {
          filter = _postStack3dFilter;
        }
        setVolumeFilter(filter);

        // reset the output volume name
        // NOTE: must be done after input filters changed
        inputVol = (PostStack3d) _volumeField.getCurrentSelection();
        fileName = (inputVol != null ? inputVol.getDisplayName() : "") + OUT_SUFFIX;
        //_outputNameField.updateField(fileName);
        _outputVolumeName.set(fileName);
      }
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

    // validate the area of interest
    if (_useAOI.get()) {
      if (_areaOfInterest.isNull()) {
        results.error(_areaOfInterest, "No area-of-interest specified.");
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

    // Validate the velocity grid
    if (_floodType.get().equals(FloodType.Constant) || _floodType.get().equals(FloodType.Gradient)) {
      if (_constantSelection.get().equals(ConstantOrGridSelection.Grid)) {
        if (_velocityGrid.isNull()) {
          results.error(_velocityGrid, "No velocity grid (Grid3d) specified");
        }
      }
    }

    if (_floodType.get().equals(FloodType.Gradient)) {

      // Display error if the volume domain type is incorrect for the Gradient flood type
      Domain volDomainType = null;
      if (!_velocityVolume.isNull()) {
        volDomainType = _velocityVolume.get().getZDomain();
        if (!volDomainType.equals(Domain.DISTANCE)) {
          results.error(_velocityVolume, "The input velocity must be a depth volume for gradient flood type");
        }
      }

      // Validate the velocity and gradient grid
      if (_constantSelection.get().equals(ConstantOrGridSelection.Grid)) {
        // Validate the gradient grid
        if (_gradientGrid.isNull()) {
          results.error(_gradientGrid, "No gradient grid (Grid3d) specified");
          // Validate the domain of the gradient grid.
        } else if (_gradientGrid.get().getDataDomain() != Domain.VELOCITY_GRADIENT) {
          results.error(_gradientGrid, "Not a velocity gradient grid.");
        }
      }

      // Validate the reference depth grid
      if (_referenceSelection.get().equals(ReferenceSelection.Grid)) {
        if (_referenceDepthGrid.isNull()) {
          results.error(_referenceDepthGrid, "No reference grid (Grid3d) specified");
          // Validate the domain of the reference depth grid.
        } else if (_referenceDepthGrid.get().getDataDomain() != Domain.DISTANCE) {
          results.error(_referenceDepthGrid, "Not a depth grid.");
        } else if (!_velocityVolume.isNull()) {
          if (_referenceDepthGrid.get().getDataDomain() != volDomainType) {
            if (volDomainType.equals(Domain.DISTANCE)) {
              results.error(_referenceDepthGrid, "The reference grid must be a depth grid if velocity is in depth");
            } else if (volDomainType.equals(Domain.TIME)) {
              results.error(_referenceDepthGrid, "The reference grid must be a time grid if velocity is in time");
            }
          }
        }
      }

      // validate the data set volume
    } else if (_floodType.get().equals(FloodType.Dataset)) {
      if (_datasetVolume.isNull()) {
        results.error(_datasetVolume, "No flood source Dataset volume (PostStack3d) specified");
      } else if (!_velocityVolume.isNull()) {
        // determine the volume domain type
        Domain volDomainType = _velocityVolume.get().getZDomain();
        Domain dataSetVolDomain = _datasetVolume.get().getZDomain();
        if (!volDomainType.equals(dataSetVolDomain)) {
          if (volDomainType.equals(Domain.DISTANCE)) {
            results.error(_datasetVolume,
                "The dataset velocity must be a depth volume if the input velocity is in depth");
          } else if (volDomainType.equals(Domain.TIME)) {
            results
                .error(_datasetVolume, "The dataset velocity must be a time volume if the input velocity is in time");
          }
        }
      }
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    } else {
      if (!_velocityVolume.isNull()) {
        IStatus status = DataSource.validateName(_velocityVolume.get(), _outputVolumeName.get());
        if (!status.isOK()) {
          results.setStatus(_outputVolumeName, status);
        } else if (PostStack3dFactory.existsInStore(_velocityVolume.get(), _outputVolumeName.get())) {
          results.warning(_outputVolumeName, "Exists in datastore and will be overwritten.");
        }
      }
    }
  }

  //  FormSection gridParams, parameters;

  /* (non-Javadoc)
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputParams = modelForm.addSection("Input");
    _volumeField = inputParams.addEntityComboField(_velocityVolume, PostStack3d.class);
    _volumeField.setTooltip("Select a velocity volume (PostStack3d)");

    EntityComboField aoi = inputParams.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoi.setTooltip("(Optional) Select an area-of-interest.");
    aoi.showActiveFieldToggle(_useAOI);

    FormSection gridParams = modelForm.addSection("Grids");
    RadioGroupField floodDirectionRadios = gridParams.addRadioGroupField(_floodDirection, FloodDirection.values());
    floodDirectionRadios.setTooltip("Select a flood direction");
    _gridUnitsLabel = gridParams.addLabelField(_gridUnits);

    ComboField topGridField = gridParams.addEntityComboField(_topGrid, Grid3d.class);
    topGridField.setTooltip("Select a grid containing the starting depths for flooding");

    ComboField baseGridField = gridParams.addEntityComboField(_baseGrid, Grid3d.class);
    baseGridField.setTooltip("Select a grid containing the ending depths for flooding");
    baseGridField.setVisible(false);

    FormSection parameters = modelForm.addSection("Parameters");
    RadioGroupField floodTypeRadios = parameters.addRadioGroupField(_floodType, FloodType.values());
    floodTypeRadios
        .setTooltip("Constant: use constant value for flooding\nGradient: use a 'v0 + g*z' gradient for flood values\nDataset: use a dataset for flood values");

    ComboField datasetVolumeField = parameters.addEntityComboField(_datasetVolume, PostStack3d.class);
    datasetVolumeField.setTooltip("Select a volume used when flooding with a dataset");
    datasetVolumeField.setVisible(false);

    TextField multiplierField = parameters.addTextField(_multiplier);
    multiplierField.setTooltip("Specify a multiplier to scale the output volume");
    multiplierField.setVisible(false);

    TextField adderField = parameters.addTextField(_adder);
    adderField.setTooltip("Specify an add factor to scale the output volume");
    adderField.setVisible(false);

    RadioGroupField floodParmsRadios = parameters.addRadioGroupField(_constantSelection, ConstantOrGridSelection
        .values());
    floodParmsRadios.setTooltip("Specify constant flood parameters or select grids");

    TextField constantFloodVelocityField = parameters.addTextField(_floodVelocityConstant);
    constantFloodVelocityField.setTooltip("Specify a constant flood velocity");

    TextField constantFloodGradientField = parameters.addTextField(_gradientConstant);
    constantFloodGradientField.setTooltip("Specify a constant flood gradient");
    constantFloodGradientField.setVisible(false);

    ComboField velocityGridField = parameters.addEntityComboField(_velocityGrid, Grid3d.class);
    velocityGridField.setTooltip("Select a grid with velocities");
    velocityGridField.setVisible(false);

    ComboField gradientGridField = parameters.addEntityComboField(_gradientGrid, Grid3d.class);
    gradientGridField.setTooltip("Select a grid with gradients");
    gradientGridField.setVisible(false);

    RadioGroupField referenceRadios = parameters.addRadioGroupField(_referenceSelection, ReferenceSelection.values());
    referenceRadios.setTooltip("Specify a constant reference depth or select a reference grid");
    referenceRadios.setVisible(false);

    TextField constantRefDepthField = parameters.addTextField(_referenceDepthConstant);
    constantRefDepthField.setTooltip("Specify a constant reference depth");
    constantRefDepthField.setVisible(false);

    ComboField referenceGridField = parameters.addEntityComboField(_referenceDepthGrid, Grid3d.class);
    referenceGridField.setTooltip("Select a grid containing reference depths");
    referenceGridField.setVisible(false);

    FormSection output = modelForm.addSection("Output");
    _outputNameField = output.addTextField(_outputVolumeName);

    //Set filters
    ISpecification filter;
    // make sure that user can only a picks a grid with velocity units
    filter = new GridSpecification(Domain.VELOCITY);
    setVelocityGridFilter(filter);

    // make sure that user can only pick a grid with gradient units
    filter = new GridSpecification(Domain.VELOCITY_GRADIENT);
    setGradientGridFilter(filter);
  }
}
