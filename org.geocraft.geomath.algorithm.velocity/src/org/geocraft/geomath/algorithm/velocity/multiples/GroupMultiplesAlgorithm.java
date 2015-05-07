/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.multiples;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.specification.SeismicDatasetUnitsSpecification;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.AbstractSpecification;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityListField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


public class GroupMultiplesAlgorithm extends StandaloneAlgorithm {

  public enum GroupMultiples {
    SET_1("Set #1 (Maximum of 5 horizons)"),
    SET_2("Set #2:(Maximum of 3 horizons)");

    private final String _displayName;

    GroupMultiples(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  // DEFAULTS
  /** Default horizon output prefix */
  static final String OUT_PREFIX = "hm";

  // UI PARAMETERS
  // Input Section
  /** Ordered list of horizons used to generate the multiples */
  private EntityArrayProperty<Grid3d> _horizons;

  /** private Interval velocity volume */
  private EntityProperty<PostStack3d> _intervalVelocityVol;

  // Parameters Section
  /** The group of multiples (5 or 3 max) */
  private EnumProperty<GroupMultiples> _groupMultiples;

  /** The conversion method */
  private EnumProperty<Method> _conversionMethods;

  // Output Section
  /** The prefix of the output horizons */
  private StringProperty _outHorizonPrefix;

  // UI Fields
  // Input Section
  EntityListField _horizonsField;

  ComboField _velocityVolumeField;

  // Parameters Section
  RadioGroupField _groupMultipleRadios, _convMethodRadios;

  // Output Section
  TextField _horizonPrefixField;

  // KEYS
  // Input Section
  public static final String HORIZONS = "Horizons";

  public static final String INTERVAL_VELOCITY_VOLUME = "Velocity Volume";

  // Parameters Section
  public static final String GROUPS_OF_MULTIPLES = "Predefined Groups of Multiples";

  public static final String CONVERSION_METHOD = "Conversion Method";

  // Output Section
  public static final String HORIZON_PREFIX = "Horizon Prefix";

  // FILTERS
  private final AbstractSpecification _postStack3dFilter = new TypeSpecification(PostStack3d.class);

  // FILTER SETTERS
  private void setVelocityVolumeFilter(ISpecification filter) {
    setFieldFilter(_intervalVelocityVol, filter);
  }

  // GETTERS
  public Grid3d[] getInputHorizons() {
    return _horizons.get();
  }

  public PostStack3d getVelocityVolume() {
    return _intervalVelocityVol.get();
  }

  public Method getConversionMethod() {
    return _conversionMethods.get();
  }

  public int getGroupNum() {
    int groupNum = 1;
    if (_groupMultiples.get() == GroupMultiples.SET_1) {
      groupNum = 1;
    } else if (_groupMultiples.get() == GroupMultiples.SET_2) {
      groupNum = 2;
    }
    return groupNum;
  }

  public String getOutHorizonPrefix() {
    return _outHorizonPrefix.get();
  }

  public GroupMultiplesAlgorithm() {
    super();
    _horizons = addEntityArrayProperty(HORIZONS, Grid3d.class);
    _intervalVelocityVol = addEntityProperty(INTERVAL_VELOCITY_VOLUME, PostStack3d.class);

    _groupMultiples = addEnumProperty(GROUPS_OF_MULTIPLES, GroupMultiples.class, GroupMultiples.SET_1);

    _conversionMethods = addEnumProperty(CONVERSION_METHOD, Method.class, Method.KneeBased);

    // Set the default prefix of the output horizons
    _outHorizonPrefix = addStringProperty(HORIZON_PREFIX, OUT_PREFIX);
  }

  /* (non-Javadoc)
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.ui.form2.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    // Build Input Section
    FormSection input = modelForm.addSection("Input");
    //NOTE: Currently only using 3D horizons
    _horizonsField = input.addEntityListField(_horizons, Grid3d.class);
    _horizonsField.setTooltip("An ordered list of the horizons used to generate the multiples.");

    _velocityVolumeField = input.addEntityComboField(_intervalVelocityVol, PostStack3d.class);
    _velocityVolumeField
        .setTooltip("Velocity volume used to convert horizons from depth to time and later back to depth");
    // set the velocity volume filter
    AbstractSpecification fpsFilter = new SeismicDatasetUnitsSpecification(Unit.FEET_PER_SECOND.toString());
    AbstractSpecification mpsFilter = new SeismicDatasetUnitsSpecification(Unit.METERS_PER_SECOND.toString());
    ISpecification filter = fpsFilter.or(mpsFilter);
    filter = filter.and(_postStack3dFilter);
    setVelocityVolumeFilter(filter);
    _velocityVolumeField.setEnabled(false);

    // Build Parameters Section
    FormSection parameters = modelForm.addSection("Parameters");
    _groupMultipleRadios = parameters.addRadioGroupField(_groupMultiples, GroupMultiples.values());
    _convMethodRadios = parameters.addRadioGroupField(_conversionMethods, Method.values());

    // Build Output Section
    FormSection output = modelForm.addSection("Output");
    _horizonPrefixField = output.addTextField(_outHorizonPrefix);
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    Grid3d outputHorizon = new GroupMultiplesEval().compute(monitor, logger, repository, this);

    //NOTE: Horizons are automatically persisted to the datastore
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_horizons.getKey())) {
      _velocityVolumeField.setEnabled(isVelocityVolumeRequired());
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModel#validate(org.geocraft.core.model.validation.IValidation)
   */
  @Override
  public void validate(IValidation results) {
    // Check if there is a least 1 horizon
    if (_horizons.get() == null || _horizons.isEmpty()) {
      results.error(_horizons, "Please select at least one input horizon");
    } else {
      Grid3d[] horizons = _horizons.get();
      // Make sure the user does not select too many input Horizons
      if (horizons.length > 5 && _groupMultiples.get().equals(GroupMultiples.SET_1)) {
        results.error(_groupMultiples, "Select no more than 5 horizons for the group multiples Set #1");
      } else if (horizons.length > 3 && _groupMultiples.get().equals(GroupMultiples.SET_2)) {
        results.error(_groupMultiples, "Select no more than 3 horizons for the group multiples Set #2");
      }
    }

    // Check if the velocity volume is required but not set
    if (isVelocityVolumeRequired()) {
      if (_intervalVelocityVol.get() == null) {
        results.error(_intervalVelocityVol, "Velocity volume required with depth horizons");
      }
    }
  }

  private boolean isVelocityVolumeRequired() {
    // Determine if the velocity volume is required, i.e., if at least one input horizon
    // is in depth.
    boolean velocityVolumeRequired = false;
    Grid3d[] horizons = _horizons.get();
    for (Grid3d property : horizons) {
      if (property.isDepthGrid()) {
        if (!velocityVolumeRequired) {
          velocityVolumeRequired = true;
        }
      }
    }
    return velocityVolumeRequired;
  }
}
