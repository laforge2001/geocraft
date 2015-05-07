package org.geocraft.geomath.algorithm.horizon.clip;


import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.math.Clip.ClipType;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


/**
 * Clips a grid between a minimum and maximum limit. The clipped values can be
 * replaced with nulls, the clip limit or a constant value.
 */
public class GridClip extends StandaloneAlgorithm {

  /** The input grid property. */
  protected final EntityProperty<Grid3d> _inputGrid;

  /** The input grid's minimum value property. */
  protected final FloatProperty _gridMinimum;

  /** The input grid's maximum value property. */
  protected final FloatProperty _gridMaximum;

  /** The use-area-of-interest property. */
  protected final BooleanProperty _useAOI;

  /** The area-of-interest property. */
  protected final EntityProperty<AreaOfInterest> _aoi;

  /** The clipping minimum limit property. */
  protected final FloatProperty _clipMin;

  /** The clipping maximum limit property. */
  protected final FloatProperty _clipMax;

  /** The clipping method property. */
  protected final EnumProperty<ClipType> _clipType;

  /** The clipping constant value (only for REPLACE_WITH_CONSTANT option). */
  protected final FloatProperty _clipConstant;

  /** The output grid name property. */
  protected final StringProperty _outputGridName;

  public GridClip() {
    super();
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _gridMinimum = addFloatProperty("Minimum", 0);
    _gridMaximum = addFloatProperty("Maximum", 0);
    _useAOI = addBooleanProperty("Use Area of Interest", false);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _clipMin = addFloatProperty("Clip Minimum", -100f);
    _clipMax = addFloatProperty("Clip Maximum", 100f);
    _clipType = addEnumProperty("Clip Type", ClipType.class, ClipType.REPLACE_WITH_NULLS);
    _clipConstant = addFloatProperty("Clip Constant", 0);
    _outputGridName = addStringProperty("Output Grid Name", "");
  }

  @Override
  public void buildView(final IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);

    ComboField inputGrid = inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    inputGrid.setTooltip("Select the grid to clip.");

    inputSection.addLabelField(_gridMinimum);
    inputSection.addLabelField(_gridMaximum);

    ComboField aoi = inputSection.addEntityComboField(_aoi, AreaOfInterest.class);
    aoi.setTooltip("Select an area-of-interest (optional).");
    aoi.showActiveFieldToggle(_useAOI);

    // Build the clip parameters section.
    FormSection clipSection = form.addSection("Clip Parameters", false);

    TextField clipMin = clipSection.addTextField(_clipMin);
    clipMin.setTooltip("Set the clipping minimum extent.");

    TextField clipMax = clipSection.addTextField(_clipMax);
    clipMax.setTooltip("Set the clipping maximum extent.");

    RadioGroupField clipType = clipSection.addRadioGroupField(_clipType, ClipType.values());
    clipType.setTooltip("Select the clipping strategy.");

    TextField clipValue = clipSection.addTextField(_clipConstant);
    clipValue.setTooltip("Set the constant clip value.");
    clipValue.setEnabled(false);

    // Build the output parameters section.
    FormSection outputSection = form.addSection("Output", false);

    TextField outputGridName = outputSection.addTextField(_outputGridName);
    outputGridName.setTooltip("Enter the name for the output grid.");
  }

  public void propertyChanged(String key) {
    if (key.equals(_clipType.getKey())) {
      // Disable the constant field if the clip option does not require it.
      setFieldEnabled(_clipConstant, _clipType.get().equals(ClipType.REPLACE_WITH_CONSTANT));
    } else if (key.equals(_inputGrid.getKey()) && !_inputGrid.isNull()) {
      // Auto-generate an output name based on the name of the input grid.
      Grid3d inputGrid = _inputGrid.get();
      _gridMinimum.set(inputGrid.getMinValue());
      _gridMaximum.set(inputGrid.getMaxValue());
      _outputGridName.set(inputGrid.getDisplayName() + "_clip");

      String outputDisplayName = inputGrid.createOutputDisplayName(inputGrid.getDisplayName(), "_clip");
      _outputGridName.set(outputDisplayName);
    }
  }

  public void validate(final IValidation results) {
    // Validate the input grid is non-null.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    if (_useAOI.get()) {
      if (_aoi.isNull()) {
        results.error(_aoi, "No area-of-interest specified.");
      }
    }

    // Validate the maximum is greater than the minimum.
    if (_clipMax.get() < _clipMin.get()) {
      results.warning(_clipMin,
          "The clipping maximum (" + _clipMax.get() + ") is less than the minimum (" + _clipMin.get() + ").");
      results.warning(_clipMax,
          "The clipping maximum (" + _clipMax.get() + ") is less than the minimum (" + _clipMin.get() + ").");
    }

    // Validate the output name is non-zero length.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    }

    // Check if an entry already exists in the datastore.
    if (!_inputGrid.isNull() && !_outputGridName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputGrid.get(), _outputGridName.get())) {
        results.warning(_outputGridName, "Exists in datastore and will be overwritten.");
      } else {
        IStatus status = DataSource.validateName(_inputGrid.get(), _outputGridName.get());
        if (!status.isOK()) {
          results.setStatus(_outputGridName, status);
        }
      }
    }
  }

  @Override
  public void run(final IProgressMonitor monitor, final ILogger logger, final IRepository repository) {

    // Unpack the model parameters.
    Grid3d inputGrid = _inputGrid.get();
    AreaOfInterest aoi = null;
    if (_useAOI.get()) {
      aoi = _aoi.get();
    }
    ClipType clipType = _clipType.get();
    float clipMin = _clipMin.get();
    float clipMax = _clipMax.get();
    float clipValue = _clipConstant.get();
    String outputGridName = _outputGridName.get();
    int numRows = inputGrid.getNumRows();

    // Start the progress monitor.
    monitor.beginTask("Clipping Grid...", numRows);

    // Clip the grid data.
    float[][] outputData = clipGridData(monitor, logger, inputGrid, aoi, clipType, clipMin, clipMax, clipValue);

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid3d outputGrid = Grid3dFactory.create(repository, inputGrid, outputData, outputGridName);
        outputGrid.update();
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    // Task is done.
    monitor.done();
  }

  public float[][] clipGridData(final IProgressMonitor monitor, final ILogger logger, Grid3d inputGrid,
      AreaOfInterest aoi, ClipType clipType, float clipMin, float clipMax, float clipValue) {
    float nullValue = inputGrid.getNullValue();
    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();
    float[][] outputData = new float[numRows][numCols];

    // Loop over the rows.
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      // Loop over the columns.
      for (int col = 0; col < numCols; col++) {

        // Determine the x,y coordinates.
        double[] xy = geometry.transformRowColToXY(row, col);

        // If no AOI specified, or the coordinate is contained in the
        // AOI, then continue to the next test.
        if (aoi == null || aoi.contains(xy[0], xy[1])) {

          // If the input grid value if non-null, then continue to the
          // next test.
          if (!inputGrid.isNull(row, col)) {
            // Get the input grid value and clip it if necessary.
            float value = inputGrid.getValueAtRowCol(row, col);
            outputData[row][col] = value;
            if (value < clipMin) {
              switch (clipType) {
                case REPLACE_WITH_LIMITS:
                  outputData[row][col] = clipMin;
                  break;
                case REPLACE_WITH_NULLS:
                  outputData[row][col] = nullValue;
                  break;
                case REPLACE_WITH_CONSTANT:
                  outputData[row][col] = clipValue;
                  break;
              }
            } else if (value > clipMax) {
              switch (clipType) {
                case REPLACE_WITH_LIMITS:
                  outputData[row][col] = clipMax;
                  break;
                case REPLACE_WITH_NULLS:
                  outputData[row][col] = nullValue;
                  break;
                case REPLACE_WITH_CONSTANT:
                  outputData[row][col] = clipValue;
                  break;
              }
            }
          } else {
            // The input grid value is null, then set the output
            // grid value to null.
            outputData[row][col] = nullValue;
          }
        } else {
          // The coordinate is not contained in the AOI, then set the
          // output grid value to null.
          outputData[row][col] = nullValue;
        }
      }

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
    }
    return outputData;
  }
}
