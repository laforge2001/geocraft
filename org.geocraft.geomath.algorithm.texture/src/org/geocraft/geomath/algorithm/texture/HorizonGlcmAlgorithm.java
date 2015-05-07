package org.geocraft.geomath.algorithm.texture;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class HorizonGlcmAlgorithm extends StandaloneAlgorithm {

  public EntityProperty<Grid3d> _inputHorizon;

  public EntityProperty<AreaOfInterest> _areaOfInterest;

  public IntegerProperty _searchRadius;// = 2;

  public IntegerProperty _searchVolumeProp;// = 10;

  public StringProperty _outputHorizonName;// = "";

  public StringProperty _horizonComments;

  public static final String INPUT_HORIZON = "Horizon";

  public static final String AREA_OF_INTEREST = "areaOfInterest";

  public static final String SEARCH_RADIUS = "Search distance (grid cells)";

  public static final String SEARCH_VOLUME = "Volume distance (grid cells)";

  public static final String OUTPUT_HORIZON_NAME = "Name prefix";

  public static final String OUTPUT_HORIZON_COMMENTS = "Comments";

  public static final int SIZE = 64;

  int[] _offsets = new int[] { 0, 1, 0, -1, -1, 0, 1, 0, -1, 1, -1, -1, 1, -1, 1, 1 };

  /** scale multiplier for the GLCM offsets. */
  int _offset;

  /** the pixel size of the region to use in the computation of the GLCM - typically a few hundred meters */
  int _searchVolume;

  /** the quantized horizon data. */
  float[][] _horizon;

  int _numRows;

  int _numCols;

  public HorizonGlcmAlgorithm() {
    _inputHorizon = addEntityProperty(INPUT_HORIZON, Grid3d.class);
    _areaOfInterest = addEntityProperty(AREA_OF_INTEREST, AreaOfInterest.class);
    _searchRadius = addIntegerProperty(SEARCH_RADIUS, 2);
    _searchVolumeProp = addIntegerProperty(SEARCH_VOLUME, 10);
    _outputHorizonName = addStringProperty(OUTPUT_HORIZON_NAME, "");
    _horizonComments = addStringProperty(OUTPUT_HORIZON_COMMENTS, "");
  }

  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(INPUT_HORIZON, Grid3d.class);

    FormSection paramSection = modelForm.addSection("GLCM Parameters");
    paramSection.addCommentField("The search radius is the offset used to comput the GLCM. Search << Volume");
    paramSection.addSpinnerField(SEARCH_RADIUS, 0, 100, 0, 2);
    paramSection.addCommentField("The Volume distance is the size of the region used to compute the GLCM");
    paramSection.addSpinnerField(SEARCH_VOLUME, 0, 100, 0, 2);

    FormSection outputSection = modelForm.addSection("Output");
    outputSection.addTextField(OUTPUT_HORIZON_NAME);
    outputSection.addTextBox(OUTPUT_HORIZON_COMMENTS);

  }

  public void quantize(final ILogger logger, final IProgressMonitor monitor) throws Exception {

    Grid3d horizon = _inputHorizon.get();

    _numRows = horizon.getNumRows();
    _numCols = horizon.getNumColumns();

    monitor.beginTask("Computing textures", 2 * _numRows);

    monitor.subTask("Computing sample data range");
    monitor.worked(1);
    _horizon = horizon.getValues();

    float[] values1D = MathUtil.convert(_horizon);
    ProbabilisticQuantizer q = new ProbabilisticQuantizer(values1D, horizon.getNullValue(), SIZE);

    for (int row = 0; row < _numRows; row++) {
      monitor.subTask("Scaling row: " + row);
      monitor.worked(1);
      for (int col = 0; col < _numCols; col++) {
        if (!horizon.isNull(row, col)) {
          float val = _horizon[row][col];
          int bin = q.getBin(val);
          _horizon[row][col] = bin;
        } else {
          _horizon[row][col] = -1;
        }
      }
    }
  }

  /**
   * Multiply the initial offset kernel by the user specified
   * search radius.  
   */
  private void initializeSearchRadius() {
    _searchVolume = _searchVolumeProp.get();
    _offset = _searchRadius.get();
    for (int i = 0; i < _offsets.length; i++) {
      _offsets[i] *= _offset;
    }
  }

  private GLCM2d computeLocalGLCM(final Grid3d horizon, final int row, final int col, final String className) {

    int[][] window = new int[2 * _searchVolume + 1][2 * _searchVolume + 1];

    for (int i = -_searchVolume; i < _searchVolume + 1; i++) {
      for (int j = -_searchVolume; j < _searchVolume + 1; j++) {

        // can't compute the GLCM at the edges of the grid. 
        if (row + i < 0 || col + j < 0 || row + i >= _numRows || col + j >= _numCols) {
          return null;
        }

        // TODO maybe it can still work with a few null values?
        if (horizon.isNull(row + i, col + j)) {
          return null;
        }

        window[i + _searchVolume][j + _searchVolume] = (int) _horizon[row + i][col + j];

      }
    }

    GLCM2d result = new GLCM2d(SIZE, window, _offsets, className);
    //result.dump();

    return result;
  }

  /**
   * @throws CoreException  
   */
  public Object computeAttributes(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    Grid3d horizon = _inputHorizon.get();

    initializeSearchRadius();

    float nullValue = horizon.getNullValue();

    float[][] entropy = MathUtil.createArray(_numRows, _numCols, nullValue);
    float[][] energy = MathUtil.createArray(_numRows, _numCols, nullValue);
    float[][] inertia = MathUtil.createArray(_numRows, _numCols, nullValue);
    float[][] homogeneity = MathUtil.createArray(_numRows, _numCols, nullValue);

    for (int row = 0; row < _numRows; row++) {
      monitor.subTask("Computing GLCM texture attributes for row: " + row);
      monitor.worked(1);

      skipNulls: for (int col = 0; col < _numCols; col++) {

        if (horizon.isNull(row, col)) {
          continue skipNulls;
        }

        GLCM2d glcm = computeLocalGLCM(horizon, row, col, "none");
        if (glcm == null) {
          continue skipNulls;
        }

        energy[row][col] = glcm.getEnergy();
        entropy[row][col] = glcm.getEntropy();
        inertia[row][col] = glcm.getInertia();
        homogeneity[row][col] = glcm.getHomogeneity();

      }
    }

    GridGeometry3d geom = horizon.getGeometry();

    String energyName = horizon.getMapper().createOutputDisplayName(_outputHorizonName.get(), "_energy");
    Grid3dFactory.create(repository, horizon, energy, energyName);

    String entropyName = horizon.getMapper().createOutputDisplayName(_outputHorizonName.get(), "_entropy");
    Grid3dFactory.create(repository, horizon, entropy, entropyName);

    String homogName = horizon.getMapper().createOutputDisplayName(_outputHorizonName.get(), "_homog");
    Grid3dFactory.create(repository, horizon, homogeneity, homogName);

    String inertiaName = horizon.getMapper().createOutputDisplayName(_outputHorizonName.get(), "_inertia");
    Grid3dFactory.create(repository, horizon, inertia, inertiaName);

    String quantizedName = horizon.getMapper().createOutputDisplayName(_outputHorizonName.get(), "_quantized");
    Grid3d quantized = Grid3dFactory.create(repository, horizon, _horizon, quantizedName, geom);
    quantized.setNullValue(-1);
    try {
      quantized.update();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }

    monitor.done();
    return null;
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(INPUT_HORIZON)) {
      _outputHorizonName.set(_inputHorizon.get().getDisplayName());
    }

  }

  @Override
  public void validate(IValidation results) {
    if (_inputHorizon.get() == null) {
      results.error(INPUT_HORIZON, "input horizon required");
    }

    if ("".equals(_outputHorizonName.get())) {
      results.error(OUTPUT_HORIZON_NAME, "output prefix required");
    }

  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    try {
      quantize(logger, monitor);
      computeAttributes(logger, monitor, repository);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
