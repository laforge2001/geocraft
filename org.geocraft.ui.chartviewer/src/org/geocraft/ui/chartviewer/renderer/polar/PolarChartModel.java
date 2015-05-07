package org.geocraft.ui.chartviewer.renderer.polar;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.plot.defs.PointStyle;


public class PolarChartModel extends AbstractBean {

  public static final String X_CELL_PNTS = "xCellPnts";

  public static final String Y_CELL_PNTS = "yCellPnts";

  public static final String CELL_DATA = "cellData";

  public static final String SCATTER_POINT_STYLE = "scatterPointStyle";

  public static final String SCATTER_POINT_SIZE = "scatterPointSize";

  public static final String SCATTER_POINT_COLOR = "scatterPointColor";

  public static final String PLOT_COLORS = "PlotColors";

  public static final String LABEL_COLOR = "LabelColor";

  public static final String LABEL_FONT = "LabelFont";

  public static final String PLOT_TITLE = "PlotTitle";

  public static final String PLOT_HEADING = "PlotHeading";

  public static final String DATA_LABELS = "dataLabels";

  public static final String PLOT_BOUNDS = "PlotBounds";

  public static final String X_LABEL = "XLabel";

  public static final String Y_LABEL = "YLabel";

  public static final String COLOR_MAP = "ColorMap";

  public static final String GRID = "Grid";

  public static final String VOLUME = "Volume";

  private float[][] _xCellPnts;

  private float[][] _yCellPnts;

  private float[][] _cellData;

  private PointStyle _scatterPointStyle;

  private RGB _scatterPointColor;

  private int _scatterPointSize;

  private RGB _plotColor;

  private RGB _labelColor;

  private Font _labelFont;

  private String _plotTitle = "True Amplitude";

  private String _plotHeading = "True Amplitude";

  private String[] _dataLabels = { "Polar Plot Axis", "Polar Plot Data" };

  private boolean _plotBoundsWanted = false;

  private float[] _plotBounds;

  private int _nBounds = 4;

  private String _xLabel = "Offset (Km)";

  private String _yLabel = "Offset (Km)";

  private ColorMapModel _colorMap;

  private Grid3d _grid;

  private PostStack3d _volume;

  public PolarChartModel() {
    _scatterPointStyle = PointStyle.NONE;
    _scatterPointSize = 8;
    _plotColor = new RGB(0, 0, 0);
    _labelColor = new RGB(0, 0, 0);
    int fontSize = 12;
    _labelFont = new Font(null, "SansSerif", fontSize, SWT.BOLD);
  }

  public float[][] getXCellPnts() {
    return _xCellPnts;
  }

  public void setXCellPnts(final float[][] xCellPnts) {
    firePropertyChange(X_CELL_PNTS, _xCellPnts, _xCellPnts = xCellPnts);
  }

  public float[][] getYCellPnts() {
    return _yCellPnts;
  }

  public void setYCellPnts(final float[][] yCellPnts) {
    firePropertyChange(Y_CELL_PNTS, _yCellPnts, _yCellPnts = yCellPnts);
  }

  public float[][] getCellData() {
    return _cellData;
  }

  public void setCellData(final float[][] cellData) {
    firePropertyChange(CELL_DATA, _cellData, _cellData = cellData);
  }

  public ColorMapModel getColorMap() {
    return _colorMap;
  }

  public Grid3d getGrid() {
    return _grid;
  }

  public PostStack3d getVolume() {
    return _volume;
  }

  public PointStyle getScatterPointStyle() {
    return _scatterPointStyle;
  }

  public void setScatterPointStyle(final PointStyle pointStyle) {
    firePropertyChange(SCATTER_POINT_STYLE, _scatterPointStyle, _scatterPointStyle = pointStyle);
  }

  public int getScatterPointSize() {
    return _scatterPointSize;
  }

  public void setScatterPointSize(final int pointSize) {
    firePropertyChange(SCATTER_POINT_SIZE, _scatterPointSize, _scatterPointSize = pointSize);
  }

  public RGB getScatterPointColor() {
    return _scatterPointColor;
  }

  public void setScatterPointColor(final RGB Color) {
    firePropertyChange(SCATTER_POINT_COLOR, _scatterPointColor, _scatterPointColor = Color);
  }

  public RGB getPlotColor() {
    return _plotColor;
  }

  public void setPlotColor(final RGB Color) {
    firePropertyChange(PLOT_COLORS, _plotColor, _plotColor = Color);
  }

  public RGB getLabelColor() {
    return _labelColor;
  }

  public void setLabelColor(final RGB Color) {
    firePropertyChange(LABEL_COLOR, _labelColor, _labelColor = Color);
  }

  public Font getLabelFont() {
    return _labelFont;
  }

  public void setLabelFont(final Font labelFont) {
    firePropertyChange(LABEL_FONT, _labelFont, _labelFont = labelFont);
  }

  public String getPlotTitle() {
    return _plotTitle;
  }

  public void setPlotTitle(final String title) {
    firePropertyChange(PLOT_TITLE, _plotTitle, _plotTitle = title);
  }

  public String getPlotHeading() {
    return _plotHeading;
  }

  public void setPlotHeading(final String heading) {
    firePropertyChange(PLOT_HEADING, _plotHeading, _plotHeading = heading);
  }

  public String getDataLabel(int index) {
    return _dataLabels[index];
  }

  public String[] getDataLabels() {
    return _dataLabels;
  }

  public void setColorMap(ColorMapModel colorMap) {
    firePropertyChange(COLOR_MAP, _colorMap, _colorMap = colorMap);
  }

  public void setGrid(Grid3d grid) {
    firePropertyChange(GRID, _grid, _grid = grid);
  }

  public void setVolume(PostStack3d volume) {
    firePropertyChange(GRID, _volume, _volume = volume);
    _grid = null;
  }

  public void setDataLabels(final String[] labels) {
    firePropertyChange(DATA_LABELS, _dataLabels, _dataLabels = labels);
  }

  public float[] getPlotBounds() {
    return _plotBounds;
  }

  public void setPlotBounds(final float xMin, final float xMax, final float yMin, final float yMax) {
    _nBounds = 4;
    float[] plotBounds = new float[_nBounds];
    plotBounds[0] = xMin;
    plotBounds[1] = xMax;
    plotBounds[2] = yMin;
    plotBounds[3] = yMax;
    firePropertyChange(PLOT_BOUNDS, _plotBounds, _plotBounds = plotBounds);
    _plotBoundsWanted = true;
  }

  public boolean getPlotBoundsWanted() {
    return _plotBoundsWanted;
  }

  public String getXLabel() {
    return _xLabel;
  }

  public void setXLabel(final String xLabel) {
    firePropertyChange(X_LABEL, _xLabel, _xLabel = xLabel);
  }

  public String getYLabel() {
    return _yLabel;
  }

  public void setYLabel(final String yLabel) {
    firePropertyChange(Y_LABEL, _yLabel, _yLabel = yLabel);
  }

  public void compute() {

    // Run the task
    Thread t = new Thread(new Runnable() {

      public void run() {
        PolarChartTask task = new PolarChartTask(PolarChartModel.this);
        TaskRunner.runTask(task, "True Amplitude", TaskRunner.NO_JOIN);
      }
    });
    t.start();
  }
}
