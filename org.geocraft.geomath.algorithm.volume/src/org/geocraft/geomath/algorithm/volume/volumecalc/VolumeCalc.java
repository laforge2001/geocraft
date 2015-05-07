package org.geocraft.geomath.algorithm.volume.volumecalc;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;


public class VolumeCalc extends StandaloneAlgorithm {

  private EntityProperty<PostStack3d> _inputVolume;

  private EntityProperty<AreaOfInterest> _areaOfInterest;

  private BooleanProperty _useAreaOfInterest;

  protected final FloatProperty _b1Value;

  /** The b1 grid */
  public EntityProperty<Grid3d> _b1Horizon;

  // Use a horizon
  protected final BooleanProperty _useb1Horizon;

  protected final FloatProperty _b2Value;

  /** The b2 grid */
  public EntityProperty<Grid3d> _b2Horizon;

  // Use a Horizon
  protected final BooleanProperty _useb2Horizon;

  protected final StringProperty _equationExample;

  private StringProperty _outputVolumeName;

  /** The output volume comments property. */
  protected final StringProperty _outputComments;

  public VolumeCalc() {
    _inputVolume = addEntityProperty("Input Volume", PostStack3d.class);
    _areaOfInterest = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _useAreaOfInterest = addBooleanProperty("Use AOI", false);
    _b1Value = addFloatProperty("B1 Value", 1);
    _b1Horizon = addEntityProperty("B1 Horizon", Grid3d.class);
    _useb1Horizon = addBooleanProperty("Use a horizon for b1", false);
    _b2Value = addFloatProperty("B2 Value", 0);
    _b2Horizon = addEntityProperty("B2 Horizon", Grid3d.class);
    _useb2Horizon = addBooleanProperty("Use a horizon for b2", false);
    _equationExample = addStringProperty("Equation", "C = (Volume * B1) + B2");
    _outputVolumeName = addStringProperty("Output Volume Name", "vc1");
    _outputComments = addStringProperty("Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Input Section.
    FormSection inputSection = form.addSection("Input");

    inputSection.addEntityComboField(_inputVolume, PostStack3d.class);

    EntityComboField aoiField = inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    FormSection parametersSection = form.addSection("Parameters");

    parametersSection.addTextField(_b1Value);
    EntityComboField b1HorizonField = parametersSection.addEntityComboField(_b1Horizon, Grid3d.class);
    b1HorizonField.setVisible(false);
    parametersSection.addCheckboxField(_useb1Horizon);

    parametersSection.addTextField(_b2Value);
    EntityComboField b2HorizonField = parametersSection.addEntityComboField(_b2Horizon, Grid3d.class);
    b2HorizonField.setVisible(false);
    parametersSection.addCheckboxField(_useb2Horizon);

    parametersSection.addLabelField(_equationExample);

    // Output Section.
    FormSection outputSection = form.addSection("Output");

    outputSection.addTextField(_outputVolumeName);
    outputSection.addTextBox(_outputComments);
  }

  public void propertyChanged(String key) {
    if (key.equals(_useb1Horizon.getKey())) {
      setFieldVisible(_b1Horizon, _useb1Horizon.get());
      setFieldVisible(_b1Value, !_useb1Horizon.get());
    } else if (key.equals(_useb2Horizon.getKey())) {
      setFieldVisible(_b2Horizon, _useb2Horizon.get());
      setFieldVisible(_b2Value, !_useb2Horizon.get());
    } else if (key.equals(_inputVolume.getKey()) && !_inputVolume.isNull()) {
      PostStack3d inputVolume = _inputVolume.get();
      _outputVolumeName.set(inputVolume.getDisplayName() + "_vc1");
    }
  }

  public void validate(IValidation results) {
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
    }

    if (_useAreaOfInterest.get() && _areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No area of interest specified.");
    }

    if (_useb1Horizon.get() && _b1Horizon.isNull()) {
      results.error(_b1Horizon, "The b1 horizon has not been specified.");
    }

    if (_useb2Horizon.get() && _b2Horizon.isNull()) {
      results.error(_b2Horizon, "The b2 horizon has not been specified.");
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "Output volume name not specified.");
    } else {
      if (!_inputVolume.isNull()) {
        IStatus status = DataSource.validateName(_inputVolume.get(), _outputVolumeName.get());
        if (!status.isOK()) {
          results.setStatus(_outputVolumeName, status);
        } else if (PostStack3dFactory.existsInStore(_inputVolume.get(), _outputVolumeName.get())) {
          results.warning(_outputVolumeName, "Exists in datastore and will be overwritten.");
        }
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the properties.
    SeismicDataset inputVolume = _inputVolume.get();

    AreaOfInterest areaOfInterest = null;
    if (_useAreaOfInterest.get()) {
      areaOfInterest = _areaOfInterest.get();
    }

    float b1Value = _b1Value.get();
    GeologicInterpretation b1Horizon = null;
    if (_useb1Horizon.get()) {
      b1Horizon = _b1Horizon.get();
    }
    float b2Value = _b2Value.get();
    GeologicInterpretation b2Horizon = null;
    if (_useb2Horizon.get()) {
      b2Horizon = _b2Horizon.get();
    }
    String outputVolumeName = _outputVolumeName.get();

    // Run the calculations and create an output volume.
    try {
      if (inputVolume instanceof PostStack3d) {
        calculateVolume((PostStack3d) inputVolume, areaOfInterest, b1Value, (Grid3d) b1Horizon, b2Value,
            (Grid3d) b2Horizon, outputVolumeName, monitor, logger, repository);
      }
    } catch (Exception e1) {
      throw new CoreException(new Status(IStatus.ERROR, "VolumeCalc", "Problem with volume calc", e1));
    }
  }

  public PostStack3d calculateVolume(final PostStack3d inputVolume, final AreaOfInterest areaOfInterest,
      final float b1Constant, final Grid3d b1Grid, final float b2Constant, final Grid3d b2Grid,
      final String outputVolumeName, final IProgressMonitor monitor, final ILogger logger, IRepository repository) throws Exception {
    PostStack3d outputVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeName);

    try {

      // Initialize the progress monitor.
      monitor.beginTask("Volume Calc of \'" + inputVolume.getDisplayName() + "\'", inputVolume.getNumInlines()
          * inputVolume.getNumXlines());

      // Create a trace iterator to loop thru the volume in the most efficient direction.
      TraceIterator traceIterator = TraceIteratorFactory.create(inputVolume, areaOfInterest);
      while (traceIterator.hasNext()) {
        // Get the next collection of input traces.
        TraceData traceDataIn = traceIterator.next();
        Trace[] tracesIn = traceDataIn.getTraces();

        // Allocate the output traces.
        Trace[] tracesOut = new Trace[tracesIn.length];
        for (int i = 0; i < tracesIn.length; i++) {
          Trace traceIn = tracesIn[i];

          // Make sure input trace as data before processing
          if (traceIn.isLive()) {

            // Get the x,y coordinates.
            double x = traceIn.getX();
            double y = traceIn.getY();

            // Determine the b1 value.
            float b1Value = b1Constant;
            if (b1Grid != null) {
              b1Value = getGridValue(b1Grid, x, y);
            }

            // Determine the b2 value.
            float b2Value = b2Constant;
            if (b2Grid != null) {
              b2Value = getGridValue(b2Grid, x, y);
            }

            // Calculate the output trace.
            tracesOut[i] = calculateTrace(traceIn, b1Value, b2Value);

          } else {
            // Create a 'dead' or 'missing' trace.
            tracesOut[i] = new Trace(traceIn, new float[traceIn.getNumSamples()]);
            tracesOut[i].setStatus(traceIn.getStatus());
          }

          // Update the progress monitor.
          monitor.worked(1);
          if (monitor.isCanceled()) {
            break;
          }
        }
        // Update the progress monitor message.
        monitor.subTask(traceIterator.getMessage());

        // Write the output traces to the output volume.
        outputVolume.putTraces(new TraceData(tracesOut));
      }

      // Close the input and output volumes.
      monitor.subTask("Closing volume...");
      inputVolume.close();
      outputVolume.close();
    } catch (Exception e) {
      logger.error("Error occurred when running VolumeCalc", e);
      synchronized (this) {
        notifyAll();
      }
      monitor.done();
    }
    outputVolume.setDirty(true);

    // Return the output volume.
    return outputVolume;

  }

  /**
   * Returns the grid value at the specified x,y coordinates.
   * A value of 0 is returned if the x,y coordinates are outside the grid bounds.
   */
  private float getGridValue(final Grid3d grid, final double x, final double y) {
    float b2Value = 0;
    GridGeometry3d geometry = grid.getGeometry();
    double[] rowcol = geometry.transformXYToRowCol(x, y, true);
    int row = Math.round((float) rowcol[0]);
    int col = Math.round((float) rowcol[1]);
    if (row >= 0 && row < geometry.getNumRows() && col >= 0 && col < geometry.getNumColumns()) {
      b2Value = grid.getValueAtRowCol(row, col);
    }
    return b2Value;
  }

  /**
   * Calculate a new trace based on the equation: t' = t * b1 + b2;
   * @param trace the input trace.
   * @param b1 the constant for multiplication.
   * @param b2 the constant for addition.
   * @return the calculated trace.
   */
  public static Trace calculateTrace(final Trace trace, final float b1, final float b2) {

    float[] data = trace.getData();

    // No null traces are allowed.
    if (data.length > 1) {
      // Do the calculation.
      for (int i = 0; i < data.length; i++) {
        data[i] = data[i] * b1 + b2;
      }
    }
    return new Trace(trace, data);
  }
}
