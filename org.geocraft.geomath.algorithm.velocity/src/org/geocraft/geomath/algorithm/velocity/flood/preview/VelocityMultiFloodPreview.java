package org.geocraft.geomath.algorithm.velocity.flood.preview;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.property.EntityArrayProperty;
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
import org.geocraft.ui.form2.field.EntityListField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.TextField;


public class VelocityMultiFloodPreview extends StandaloneAlgorithm {

  private static int MAX_FLOODS = 10;

  private EntityProperty<PostStack3d> _velocityVolume;

  private EnumProperty<FloodDirection> _floodDirection;

  private EnumProperty<FloodType> _floodType;

  private FloatProperty[] _velocityConstants;

  private EntityArrayProperty<Grid3d> _topGrids;

  private EntityArrayProperty<Grid3d> _baseGrids;

  private StringProperty _outputVolumeName;

  public VelocityMultiFloodPreview() {
    _velocityVolume = addEntityProperty("Velocity Volume", PostStack3d.class);
    _floodDirection = addEnumProperty("Flood Direction", FloodDirection.class, FloodDirection.Between);
    _topGrids = addEntityArrayProperty("Top Grid(s)", Grid3d.class);
    _baseGrids = addEntityArrayProperty("Base Grid(s)", Grid3d.class);
    _floodType = addEnumProperty("Flood Type", FloodType.class, FloodType.Constant);
    _velocityConstants = new FloatProperty[MAX_FLOODS];
    for (int i = 0; i < MAX_FLOODS; i++) {
      _velocityConstants[i] = addFloatProperty("Velocity Constant #" + (i + 1), 5000);
    }
    _outputVolumeName = addStringProperty("Output Volume Name", "");
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input");
    EntityComboField velocityVolumeField = section.addEntityComboField(_velocityVolume, PostStack3d.class);

    section = form.addSection("Parameters");
    LabelField floodDirectionField = section.addLabelField(_floodDirection);
    EntityListField topGridsField = section.addEntityListField(_topGrids, Grid3d.class);
    EntityListField baseGridsField = section.addEntityListField(_baseGrids, Grid3d.class);
    LabelField floodTypeField = section.addLabelField(_floodType);
    for (int i = 0; i < MAX_FLOODS; i++) {
      TextField velocityConstantField = section.addTextField(_velocityConstants[i]);
    }

    section = form.addSection("Output");
    TextField outVolumeNameField = section.addTextField(_outputVolumeName);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_velocityVolume.getKey()) && !_velocityVolume.isNull()) {
      _outputVolumeName.set(_velocityVolume.get().getDisplayName() + "_multiflood");
    }
    if (key.equals(_topGrids.getKey())) {
      Grid3d[] topGrids = _topGrids.get();
      int numGrids = topGrids.length;
      for (int i = 0; i < MAX_FLOODS; i++) {
        setFieldVisible(_velocityConstants[i], i < numGrids);
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
      if (dataDomain != Domain.VELOCITY) {
        results.error(_velocityVolume, "Not a velocity volume.");
      }
      Domain zDomain = _velocityVolume.get().getZDomain();
      for (Grid3d topGrid : _topGrids.get()) {
        if (topGrid.getDataDomain() != zDomain) {
          results.error(_topGrids, "All top grids must be in the " + zDomain.getTitle().toLowerCase());
        }
      }
      for (Grid3d baseGrid : _baseGrids.get()) {
        if (baseGrid.getDataDomain() != zDomain) {
          results.error(_topGrids, "All base grids must be in the " + zDomain.getTitle().toLowerCase());
        }
      }
    }

    // Validate the # of top and base grids match.
    int numTopGrids = _topGrids.get().length;
    int numBaseGrids = _baseGrids.get().length;
    if (numTopGrids != numBaseGrids) {
      results.error(_baseGrids, "The # of top and base grids must be the same.");
    }

    // Validate the flood direction and the top and base grids.
    FloodDirection floodDirection = _floodDirection.get();
    if (_floodDirection.isNull()) {
      results.error(_floodDirection, "No flood direction specified.");
    } else {
      if (floodDirection != FloodDirection.Below && _topGrids.isEmpty()) {
        results.error(_topGrids, "No top grid(s) specified.");
      }
      if (floodDirection != FloodDirection.Above && _baseGrids.isEmpty()) {
        results.error(_baseGrids, "No base grid(s) specified.");
      }
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    PostStack3d velocityVolume = _velocityVolume.get();
    AreaOfInterest areaOfInterest = null;
    Grid3d velocityHorizon = null;
    Grid3d gradientHorizon = null;
    Grid3d referenceHorizon = null;
    FloodType floodType = _floodType.get();
    FloodDirection floodDirection = _floodDirection.get();
    Grid3d[] topGrids = _topGrids.get();
    Grid3d[] baseGrids = _baseGrids.get();
    int numFloods = topGrids.length;
    ConstantOrGridSelection constantSelection = ConstantOrGridSelection.Constant;
    ReferenceSelection referenceZDataSource = ReferenceSelection.Constant;
    float[] velocityConstants = new float[MAX_FLOODS];
    for (int i = 0; i < MAX_FLOODS; i++) {
      velocityConstants[i] = _velocityConstants[i].get();
    }
    float velocityConstant = 5000.0f;
    float gradient = 1.0f;
    float referenceDepth = 0.0f;
    PostStack3d dataSetVolume = null;
    float multiplier = 1.0f;
    float adder = 0.0f;
    PostStack3d outputVelocityVolume = null;

    for (int floodIndex = 0; floodIndex < numFloods && !monitor.isCanceled(); floodIndex++) {
      Grid3d topGrid = topGrids[floodIndex];
      Grid3d baseGrid = baseGrids[floodIndex];
      velocityConstant = velocityConstants[floodIndex];

      String outputVolumeName = _outputVolumeName.get();
      int ie = outputVolumeName.length() - 1;
      if (outputVolumeName.substring(ie).equals("1")) {
        outputVolumeName = outputVolumeName.substring(0, ie) + String.valueOf(floodIndex + 1);
      } else {
        outputVolumeName = outputVolumeName + "_" + String.valueOf(floodIndex + 1);
      }

      try {
        IPostStack3dAlgorithm algorithm = new ExampleVelocityFloodAlgorithm(velocityVolume, areaOfInterest, floodType,
            constantSelection, velocityConstant, velocityHorizon, constantSelection, gradient, gradientHorizon,
            referenceZDataSource, referenceDepth, referenceHorizon, floodDirection, topGrid, baseGrid, dataSetVolume,
            multiplier, adder, outputVolumeName, "");

        // Create the algorithm mapper.
        IPostStack3dMapper mapper = new PostStack3dAlgorithmMapper(algorithm);

        // Create the poststack3d.
        outputVelocityVolume = PostStack3dFactory.create(outputVolumeName, mapper);
        outputVelocityVolume.load();

        outputVelocityVolume.setDirty(true);
        repository.add(outputVelocityVolume);

        // Set the output velocity volume to be the input volume for the next velocity flood
        velocityVolume = outputVelocityVolume;
      } catch (Exception ex) {
        throw new CoreException(ValidationStatus.error(ex.getMessage()));
      }
    }
  }
}
