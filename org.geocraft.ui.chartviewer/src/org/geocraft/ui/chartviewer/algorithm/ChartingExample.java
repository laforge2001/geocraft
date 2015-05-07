package org.geocraft.ui.chartviewer.algorithm;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.chartviewer.ChartUtil;
import org.geocraft.ui.chartviewer.ChartViewerFactory;
import org.geocraft.ui.chartviewer.GridImageChartViewer;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.chartviewer.PieChartViewer;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextField;
import org.geocraft.ui.multiplot.MultiPlotFactory;
import org.geocraft.ui.multiplot.MultiPlotPart;
import org.geocraft.ui.plot.defs.PointStyle;


public class ChartingExample extends StandaloneAlgorithm {

  public static final String SCATTER_NUM_POINTS = "ScatterNumPoints";

  public static final String SCATTER_POINT_STYLE = "ScatterPointStyle";

  public static final String SCATTER_POINT_SIZE = "ScatterPointSize";

  public static final String HISTOGRAM_NUM_CELLS = "HistogramNumCells";

  public static final String HISTOGRAM_MIN_VALUE = "HistogramMinValue";

  public static final String HISTOGRAM_MAX_VALUE = "HistogramMaxValue";

  public static final String PIE_NUM_WEDGES = "PieNumWedges";

  public static final String GRID_IMAGE_NUM_ROWS = "GridImageNumRows";

  public static final String GRID_IMAGE_NUM_COLS = "GridImageNumCols";

  private IntegerProperty _scatterNumPoints;

  private EnumProperty<PointStyle> _scatterPointStyle;

  private IntegerProperty _scatterPointSize;

  private IntegerProperty _histogramNumCells;

  private IntegerProperty _histogramMinValue;

  private IntegerProperty _histogramMaxValue;

  private IntegerProperty _pieNumWedges;

  private IntegerProperty _gridImageNumRows;

  private IntegerProperty _gridImageNumCols;

  public ChartingExample() {
    super();
    _scatterNumPoints = addIntegerProperty(SCATTER_NUM_POINTS, 10);
    _scatterPointStyle = addEnumProperty(SCATTER_POINT_STYLE, PointStyle.class, PointStyle.CIRCLE);
    _scatterPointSize = addIntegerProperty(SCATTER_POINT_SIZE, 5);
    _histogramNumCells = addIntegerProperty(HISTOGRAM_NUM_CELLS, 10);
    _histogramMinValue = addIntegerProperty(HISTOGRAM_MIN_VALUE, 100);
    _histogramMaxValue = addIntegerProperty(HISTOGRAM_MAX_VALUE, 200);
    _pieNumWedges = addIntegerProperty(PIE_NUM_WEDGES, 10);
    _gridImageNumRows = addIntegerProperty(GRID_IMAGE_NUM_ROWS, 20);
    _gridImageNumCols = addIntegerProperty(GRID_IMAGE_NUM_COLS, 20);
  }

  @Override
  public void buildView(final IModelForm form) {
    FormSection scatter = form.addSection("Scatter Chart");

    TextField scatterNumPoints = scatter.addTextField(SCATTER_NUM_POINTS);
    scatterNumPoints.setLabel("Number of Points");

    ComboField scatterPointStyle = scatter.addComboField(SCATTER_POINT_STYLE, PointStyle.values());
    scatterPointStyle.setLabel("Point Style");

    TextField scatterPointSize = scatter.addTextField(SCATTER_POINT_SIZE);
    scatterPointSize.setLabel("Point Size");

    FormSection histogram = form.addSection("Histogram Chart");

    TextField histoNumCells1 = histogram.addTextField(HISTOGRAM_NUM_CELLS);
    histoNumCells1.setLabel("Number of Cells");

    TextField histoMinValue = histogram.addTextField(HISTOGRAM_MIN_VALUE);
    histoMinValue.setLabel("Minimum Value");

    TextField histoMaxValue = histogram.addTextField(HISTOGRAM_MAX_VALUE);
    histoMaxValue.setLabel("Maximum Value");

    FormSection pie = form.addSection("Pie Chart");

    TextField pieNumWedges = pie.addTextField(PIE_NUM_WEDGES);
    pieNumWedges.setLabel("Number of Wedges");

    FormSection gridImage = form.addSection("Grid Image Chart");

    TextField gridImageNumRows = gridImage.addTextField(GRID_IMAGE_NUM_ROWS);
    gridImageNumRows.setLabel("Number of Rows");

    TextField gridImageNumCols = gridImage.addTextField(GRID_IMAGE_NUM_COLS);
    gridImageNumCols.setLabel("Number of Columns");
  }

  public void validate(final IValidation results) {
    if (_scatterNumPoints.get() < 1) {
      results.error(SCATTER_NUM_POINTS, "The number of scatter points must be > 0.");
    }

    if (_histogramNumCells.get() < 1) {
      results.error(HISTOGRAM_NUM_CELLS, "The number of histogram cells must be > 0.");
    }

    if (_histogramMinValue.get() > _histogramMaxValue.get()) {
      results.error(HISTOGRAM_MIN_VALUE, "The histogram minimum value must be less than the maximum value.");
    }

    if (_pieNumWedges.get() < 2) {
      results.error(PIE_NUM_WEDGES, "The number of pie wedges must be >= 2.");
    }

    if (_gridImageNumRows.get() < 1) {
      results.error(GRID_IMAGE_NUM_ROWS, "The number of grid image rows must be > 0.");
    }

    if (_gridImageNumCols.get() < 1) {
      results.error(GRID_IMAGE_NUM_COLS, "The number of grid image columns must be > 0.");
    }
  }

  public void modelUpdated(String key) {
    // No conditional view logic.
  }

  //  @Override
  /**
   * @throws CoreException  
   */
  @Override
  public void run(final IProgressMonitor monitor, final ILogger logger, final IRepository repository) throws CoreException {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        int numColumns = 2;
        MultiPlotPart part;
        try {
          part = MultiPlotFactory.createPart(numColumns);
          part.setPartTitle("Chart Examples");
        } catch (PartInitException ex) {
          logger.error(ex.toString(), ex);
          monitor.done();
          return;
        }

        // Create a scatter chart and add it to the multi-chart part.
        ScatterChartViewer scatterChart = ChartViewerFactory.createScatterChart(part.getViewerParent(),
            "Scatter Chart", "X", "Y");
        part.addViewer(scatterChart);

        // Create the scatter data and add it to the scatter chart.
        int numPoints = _scatterNumPoints.get();
        float[] xs = new float[numPoints];
        float[] ys = new float[numPoints];
        for (int k = 0; k < numPoints; k++) {
          xs[k] = (float) (Math.random() * 100);
          ys[k] = (float) (Math.random() * 100);
        }
        scatterChart.addData("Scatter Data", xs, ys, ChartUtil.createRandomRGB(), _scatterPointStyle.get(),
            _scatterPointSize.get(), true);

        // Create a histogram chart and add it to the multi-chart part.
        HistogramChartViewer histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(),
            "Histogram Chart", "X", "Percentage");
        part.addViewer(histogramChart);

        // Create the histogram data and add it to the histogram chart.
        int numCells = _histogramNumCells.get();
        float[] histogramValues = new float[1000];
        for (int j = 0; j < 1000; j++) {
          float sum = 0;
          for (int k = 0; k < 10; k++) {
            float diff = _histogramMaxValue.get() - _histogramMinValue.get();
            sum += _histogramMinValue.get() + Math.random() * diff;
          }
          float avg = sum / 10;
          histogramValues[j] = avg;
        }
        histogramChart.addData("Histogram Data #2", histogramValues, numCells, _histogramMinValue.get(),
            _histogramMaxValue.get(), ChartUtil.createRandomRGB());

        // Create a pie chart and add it to the multi-chart part.
        PieChartViewer pieChart = ChartViewerFactory.createPieChart(part.getViewerParent(), "Pie Chart");
        part.addViewer(pieChart);

        // Create the pie data and add it to the pie chart.
        int numWedges = _pieNumWedges.get();
        String[] entryNames = new String[numWedges];
        float[] entryValues = new float[numWedges];
        RGB[] rgbs = new RGB[numWedges];
        for (int k = 0; k < numWedges; k++) {
          entryNames[k] = "Pie Wedge " + (k + 1);
          entryValues[k] = (float) Math.random();
          rgbs[k] = ChartUtil.createRandomRGB();
        }
        pieChart.addData("Pie Data", entryNames, entryValues, rgbs);

        // Create a grid image chart and add it to the multi-chart part.
        GridImageChartViewer gridImageChart = ChartViewerFactory.createGridImageChart(part.getViewerParent(),
            "Grid Image Chart", "X", "Y");
        part.addViewer(gridImageChart);

        // Create the grid image data and add it to the grid image chart.
        int nx = _gridImageNumCols.get();
        int ny = _gridImageNumRows.get();
        float[][] data = new float[nx][ny];
        for (int j = 0; j < nx; j++) {
          for (int k = 0; k < ny; k++) {
            data[j][k] = (float) (Math.random() * 100);
          }
        }
        float xEnd = (float) (100 * Math.random());
        float yEnd = (float) (100 * Math.random());

        // Create the grid image data and add it to the grid image chart.
        gridImageChart.addData("Image Data", 0, xEnd, 0, yEnd, data);
      }

    });
  }

  public void propertyChanged(String key) {
    // Nothing to do.
  }

}
