package org.geocraft.geomath.algorithm.example.generator;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.example.generator.entity.CultureGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.EarthModelGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.GeologicHorizonGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.GridGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.PointSetGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.PostStack2dGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.PostStack3dGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.TestHorizonModel;
import org.geocraft.geomath.algorithm.example.generator.entity.WaveletGenerator;
import org.geocraft.geomath.algorithm.example.generator.entity.WellGenerator;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.SpinnerField;


public class TestDataGenerator extends StandaloneAlgorithm {

  private DoubleProperty _width;

  private DoubleProperty _height;

  private IntegerProperty _numHorizons;

  private IntegerProperty _numGridsPerHorizon;

  private IntegerProperty _numPointsetsPerHorizon;

  private IntegerProperty _numWells;

  private IntegerProperty _numLogsPerWell;

  private BooleanProperty _showPicks;

  private IntegerProperty _numPointFeatures;

  private IntegerProperty _numPolylineFeatures;

  private IntegerProperty _numPolygonFeatures;

  private IntegerProperty _numSurveys3d;

  private IntegerProperty _numSurveys2d;

  private IntegerProperty _numVolumes;

  private IntegerProperty _numFaults;

  private BooleanProperty _showSeisHorizons;

  private IntegerProperty _numWavelets;

  private Point3d _origin = new Point3d(40000, 1000000, 0);

  private static final String[] OPERATORS = { "AddOilCo", "PlusOilCo", "MinusOilCo", "DivideOilCo", "MultiplyOilCo",
      "ModOilCo" };

  public static String getRandomOperator() {
    int iOperator = (int) (Math.random() * OPERATORS.length);
    return TestDataGenerator.OPERATORS[iOperator];
  }

  public static Point3d[] createBoundary(final double xmin, final double ymin, final double width, final double height) {
    Point3d[] boundaryPoints = new Point3d[4];

    double xmax = xmin + width;
    double ymax = ymin + height;
    double z = 0;

    boundaryPoints[0] = new Point3d(xmin, ymin, z);
    boundaryPoints[1] = new Point3d(xmax, ymin, z);
    boundaryPoints[2] = new Point3d(xmax, ymax, z);
    boundaryPoints[3] = new Point3d(xmin, ymax, z);

    return boundaryPoints;
  }

  public TestDataGenerator() {
    _width = addDoubleProperty("Width of Area (miles)", 4);
    _height = addDoubleProperty("Height of Area (miles)", 4);
    _numHorizons = addIntegerProperty("Number of Horizons", 3);
    _numGridsPerHorizon = addIntegerProperty("Number of Grids per Horizon", 2);
    _numPointsetsPerHorizon = addIntegerProperty("Number of Pointsets per Horizon", 1);
    _numWells = addIntegerProperty("Number of Wells", 3);
    _numLogsPerWell = addIntegerProperty("Number of Logs per Well", 4);
    _showPicks = addBooleanProperty("Show Picks", true);
    _numPointFeatures = addIntegerProperty("Number of Platforms", 10);
    _numPolylineFeatures = addIntegerProperty("Number of Pipelines", 10);
    _numPolygonFeatures = addIntegerProperty("Number of Lease Blocks", 5);
    _numSurveys2d = addIntegerProperty("Number of 2D Surveys", 2);
    _numSurveys3d = addIntegerProperty("Number of 3D Surveys", 1);
    _numVolumes = addIntegerProperty("Number of Volumes per 3D Survey", 2);
    _numFaults = addIntegerProperty("Number of Faults per 3D Survey", 1);
    _showSeisHorizons = addBooleanProperty("Show Seismic Horizons", true);
    _numWavelets = addIntegerProperty("Number of Wavelets", 4);
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Grids");

    SpinnerField numHorizons = section.addSpinnerField(_numHorizons, 0, 20, 0, 1);
    numHorizons.setTooltip("The number of horizons to generate");

    SpinnerField numGridsPerHorizon = section.addSpinnerField(_numGridsPerHorizon, 0, 20, 0, 1);
    numGridsPerHorizon.setTooltip("The number of grids per horizon");

    SpinnerField numPointsetsPerHorizon = section.addSpinnerField(_numPointsetsPerHorizon, 0, 5, 0, 1);
    numPointsetsPerHorizon.setTooltip("The number of point sets per horizon");

    section = form.addSection("Wells");

    SpinnerField numWells = section.addSpinnerField(_numWells, 0, 20, 0, 1);
    numWells.setTooltip("The number of wells to generate");

    SpinnerField numLogsPerBore = section.addSpinnerField(_numLogsPerWell, 0, 10, 0, 1);
    numLogsPerBore.setTooltip("The number of logs per well");

    section.addCheckboxField(_showPicks);

    section = form.addSection("Area Size");

    SpinnerField width = section.addSpinnerField(_width, 0, 200, 1, 5);
    width.setTooltip("The width of the entire area (in miles)");

    SpinnerField height = section.addSpinnerField(_height, 0, 200, 1, 5);
    height.setTooltip("The height of the entire area (in miles)");

    section = form.addSection("Culture");

    SpinnerField numPointFeatures = section.addSpinnerField(_numPointFeatures, 0, 200, 0, 10);
    numPointFeatures.setTooltip("The number of platforms to generate");

    SpinnerField numPolylines = section.addSpinnerField(_numPolylineFeatures, 0, 200, 0, 10);
    numPolylines.setTooltip("The number of pipelines to generate");

    SpinnerField numPolygonFeatures = section.addSpinnerField(_numPolygonFeatures, 0, 50, 0, 1);
    numPolygonFeatures.setTooltip("The number of lease blocks to generate");

    section = form.addSection("Wavelets");

    SpinnerField numWavelets = section.addSpinnerField(_numWavelets, 0, 5, 0, 1);
    numWavelets.setTooltip("The number of wavelets to generate");

    section = form.addSection("3D Seismic Surveys");

    SpinnerField numSurveys3d = section.addSpinnerField(_numSurveys3d, 0, 5, 0, 1);
    numSurveys3d.setTooltip("The number of 3D surveys to generate");

    SpinnerField numVolumes = section.addSpinnerField(_numVolumes, 0, 5, 0, 1);
    numVolumes.setTooltip("The number of volumes per 3D survey");

    SpinnerField numFaults = section.addSpinnerField(_numFaults, 0, 5, 0, 1);
    numFaults.setTooltip("The number of faults per 3D survey");

    //SpinnerField numFaults = section.addSpinnerField(_numFaults, 0, 20, 0, 1);
    //numFaults.setTooltip("The number of faults per 3D survey");

    section.addCheckboxField(_showSeisHorizons);

    section = form.addSection("2D Seismic Surveys");

    SpinnerField numSurveys2d = section.addSpinnerField(_numSurveys2d, 0, 5, 0, 1);
    numSurveys2d.setTooltip("The number of 2d surveys to generate");
  }

  @Override
  public void propertyChanged(String key) {
    // No updates required.
  }

  @Override
  public void validate(IValidation results) {
    // No validation required.
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the parameters.
    double width = _width.get();
    double height = _height.get();
    int numHorizons = _numHorizons.get();
    int numGridsPerHorizon = _numGridsPerHorizon.get();
    int numPointsetsPerHorizon = _numPointsetsPerHorizon.get();
    int numWells = _numWells.get();
    int numLogsPerBore = _numLogsPerWell.get();
    boolean showPicks = _showPicks.get();
    int numPointFeatures = _numPointFeatures.get();
    int numPolylineFeatures = _numPolylineFeatures.get();
    int numPolygonFeatures = _numPolygonFeatures.get();
    int numSurveys3d = _numSurveys3d.get();
    int numSurveys2d = _numSurveys2d.get();
    int numVolumes = _numVolumes.get();
    int numFaults = _numFaults.get();
    boolean showSeisHorizons = _showSeisHorizons.get();
    int numWavelets = _numWavelets.get();

    int totalWork = 1 + numPointFeatures + numPolylineFeatures + numPolygonFeatures + numHorizons + numGridsPerHorizon
        + numPointsetsPerHorizon + numWells + numSurveys3d + numSurveys2d + numWavelets;

    monitor.beginTask("Creating Test Data", totalWork);
    monitor.worked(1);

    CoordinateSystem _coordSys = new CoordinateSystem("UTM15", Domain.DISTANCE);

    ApplicationPreferences.getInstance().setDepthCoordinateSystem(_coordSys);
    ApplicationPreferences.getInstance().setSeismicDatumElevation(0.0f);
    UnitPreferences.getInstance().setTimeUnit(Unit.MILLISECONDS);
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);
    UnitPreferences.getInstance().setHorizontalDistanceUnit(Unit.FOOT);

    // Create the culture data first so we can also use to to plot debug outlines, etc.
    monitor.subTask("Creating culture data...");
    CultureGenerator cultureGenerator = new CultureGenerator(repository, _origin, _coordSys);

    for (int i = 0; i < numPointFeatures; i++) {
      cultureGenerator.addPointFeature("Platform " + i, width, height, logger);
    }
    monitor.worked(numPointFeatures);

    for (int i = 0; i < numPolylineFeatures; i++) {
      cultureGenerator.addPolylineFeature("Pipeline " + i, width, height, logger);
    }
    monitor.worked(numPolylineFeatures);

    for (int i = 0; i < numPolygonFeatures; i++) {
      cultureGenerator.addPolygonFeature("Lease " + i, width, height, logger);
    }
    monitor.worked(numPolygonFeatures);

    // Create the earth models.
    EarthModelGenerator earthModelGenerator = new EarthModelGenerator(repository);
    earthModelGenerator.addEarthModel("Time model", Domain.TIME);
    earthModelGenerator.addEarthModel("Depth model", Domain.DISTANCE);

    // Create the geologic horizons.
    monitor.subTask("Creating horizons...");
    GeologicHorizonGenerator horizonGenerator = new GeologicHorizonGenerator(repository);
    for (int i = 0; i < numHorizons; i++) {
      horizonGenerator.addHorizon(i);
    }
    monitor.worked(numHorizons);

    Point3d faultOrigin = new Point3d(_origin.getX() + 10000, _origin.getY() + 10000, 0);
    double faultWidth = 500;
    double faultLength = 2500;
    double structureXsize = 7000;
    double structureYsize = 8500;

    TestHorizonModel horizonModel = new TestHorizonModel(cultureGenerator, numHorizons, _origin, structureXsize,
        structureYsize, faultOrigin, faultWidth, faultLength);

    // Create the grids for each geologic horizon.
    monitor.subTask("Creating grid(s)...");
    GridGenerator gridGenerator = new GridGenerator(repository, _origin, _coordSys, horizonModel);
    for (int j = 0; j < numGridsPerHorizon; j++) {
      gridGenerator.addGrid3d(j, horizonGenerator.getHorizons());
    }
    monitor.worked(numGridsPerHorizon);

    // Create the point sets for each geologic horizon.
    monitor.subTask("Creating pointset(s)...");
    PointSetGenerator pointsetGenerator = new PointSetGenerator(repository, _origin, _coordSys, horizonModel);
    GeologicHorizon[] horizons = horizonGenerator.getHorizons();
    for (int i = 0; i < horizons.length; i++) {
      for (int j = 0; j < numPointsetsPerHorizon; j++) {
        pointsetGenerator.addHorizonPointSet(j, width, height, horizons[i], i);
      }
    }
    monitor.worked(numPointsetsPerHorizon);

    // Create the wells.
    monitor.subTask("Creating well(s)...");
    for (int i = 0; i < numWells; i++) {
      int wellNumber = i + 1;
      new WellGenerator(repository, horizonModel, _coordSys).addWell(wellNumber, width, height, numLogsPerBore,
          showPicks);
    }
    monitor.worked(numWells);

    // Create the 3D seismic datasets.
    monitor.subTask("Creating 3D volume(s)...");
    PostStack3dGenerator poststack3dGenerator = new PostStack3dGenerator(horizonModel, repository, _coordSys);
    for (int i = 0; i < numSurveys3d; i++) {
      poststack3dGenerator.add3dSurvey(i, numVolumes, showSeisHorizons, numFaults, horizonGenerator.getHorizons());
    }
    monitor.worked(numSurveys3d);

    // Create the 2D seismic datasets.
    monitor.subTask("Creating 2D volume(s)...");
    PostStack2dGenerator poststack2dGenerator = new PostStack2dGenerator(repository, _coordSys);
    for (int i = 0; i < numSurveys2d; i++) {
      poststack2dGenerator.addSurvey(i);
    }
    monitor.worked(numSurveys2d);

    // Create the wavelets.
    monitor.subTask("Creating wavelet(s)...");
    WaveletGenerator waveletGenerator = new WaveletGenerator(repository);
    for (int i = 0; i < numWavelets; i++) {
      int numSamples = 101;
      float sampleRate = 4;
      float frequency = 10 + 10 * i;
      waveletGenerator.addRickerWavelet(numSamples, sampleRate, frequency);
    }
    monitor.worked(numWavelets);

    // End of job.
    monitor.done();
  }

}
