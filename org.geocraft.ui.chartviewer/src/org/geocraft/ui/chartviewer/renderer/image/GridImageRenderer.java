package org.geocraft.ui.chartviewer.renderer.image;


import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.GridImageChartViewer;
import org.geocraft.ui.chartviewer.data.GridImageData;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotImage;
import org.geocraft.ui.plot.object.PlotImage;


public class GridImageRenderer extends ChartViewRenderer<GridImageChartViewer> {

  private GridImageData _gridImageData;

  private IPlotImage _plotImage;

  private ColorBar _colorBar;

  public GridImageRenderer() {
    super("GridSet XY Renderer");
  }

  @Override
  public IChartData getRenderedChartData() {
    return _gridImageData;
  }

  @Override
  protected void addPlotShapes() {

    GridImageChartViewer viewer = getViewer();
    IModelSpaceCanvas canvas = viewer.getPlot().getModelSpaceCanvas();

    // Create new actual image for drawing from the pixel byte array.
    ColorMapModel colorMapModel = new ColorMapModel(64, new SpectrumColorMap());
    float startValue = _gridImageData.getAttributeMinimum();
    float endValue = _gridImageData.getAttributeMaximum();
    _colorBar = new ColorBar(colorMapModel, startValue, endValue, (endValue - startValue) / 10);
    PaletteData palette = new PaletteData(buildColorModel(canvas.getComposite().getBackground().getRGB(), _colorBar));
    int depth = 8;
    if (_colorBar.getNumColors() >= 256) {
      depth = 16;
    }
    int imageWidth = _gridImageData.getNumCellsX();
    int imageHeight = _gridImageData.getNumCellsY();
    byte[] imagePixels = new byte[imageWidth * imageHeight];
    ImageData imageData = new ImageData(imageWidth, imageHeight, depth, palette, 1, imagePixels);
    for (int i = 0; i < imageWidth; i++) {
      for (int j = 0; j < imageHeight; j++) {
        float value = _gridImageData.getValueByCell(i, j);
        int colorIndex = _colorBar.getColorIndex(value);
        int pixelIndex = (imageHeight - 1 - j) * imageWidth + i;
        imagePixels[pixelIndex] = (byte) colorIndex;
      }
    }
    // imageData.alpha = (int) (255 * .01f * (100 -
    // _model.getTransparency()));
    Image image = new Image(canvas.getComposite().getDisplay(), imageData);
    Point2D.Double upperLeft = new Point2D.Double(_gridImageData.getStartX(), _gridImageData.getEndY());
    Point2D.Double lowerRight = new Point2D.Double(_gridImageData.getEndX(), _gridImageData.getStartY());
    _plotImage = new PlotImage(image, _gridImageData.getDisplayName(), upperLeft, lowerRight);

    addShape(_plotImage);
  }

  /**
   * Builds the color model for the plot image.
   * 
   * @param colorBar
   *            the colorbar to use.
   * @return the color model.
   */
  private RGB[] buildColorModel(final RGB backgroundRGB, final ColorBar colorBar) {
    int numColors = colorBar.getNumColors();
    RGB[] rgbs = new RGB[numColors + 1];
    for (int i = 0; i < numColors; i++) {
      rgbs[i] = colorBar.getColor(i);
    }
    rgbs[numColors] = backgroundRGB;
    return rgbs;
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(AbstractChartViewer.DEFAULT_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _gridImageData };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _gridImageData = (GridImageData) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_gridImageData.getDisplayName());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public GridImageRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
