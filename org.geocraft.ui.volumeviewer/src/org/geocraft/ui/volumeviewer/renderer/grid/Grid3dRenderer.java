package org.geocraft.ui.volumeviewer.renderer.grid;


import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Orientation;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTTextureUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.NormalsMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.util.geom.BufferUtils;


/**
 * Renders a <code>Grid3d</code> entity in the 3D viewer.
 */
public class Grid3dRenderer extends VolumeViewRenderer {

  /** A color cache for the texture image. */
  private static Map<Color, ColorRGBA> _colorCache = new HashMap<Color, ColorRGBA>();

  /** The default value for the not chosen points. */
  private static final int NOT_CHOSEN_POINT = -1;

  /** The structural grid being rendered. */
  private Grid3d _surfaceGrid;

  /** The geometry of the structural grid. */
  private GridGeometry3d _gridGeometry;

  /** The property that provides the coloring of the surface. */
  private Grid3d _rgbGrid;

  /** The number of rows. */
  private int _numRows;

  /** The number of columns. */
  private int _numCols;

  /** The spatial object that provides the 3D view of the grid. */
  private Mesh _meshSpatial;

  /** The decimate factor. */
  private int _decimationFactor = 1;

  /** The texture image width. */
  private int _imageWidth;

  /** The texture image height. */
  private int _imageHeight;

  /** If the decimate is enabled. */
  private boolean _decimateEnabled;

  /** If the manual decimate is enabled. */
  private boolean _decimateManual;

  /** The model of renderer properties. */
  private final Grid3dRendererModel _model;

  //private HorizonPropertyListener _listener;

  /**
   * Constructs the grid renderer.
   */
  public Grid3dRenderer() {
    super("");
    //_store.setDefault(HorizonPreferencePage.DECIMATE_ENABLED_KEY, true);
    //_store.setDefault(HorizonPreferencePage.DECIMATE_TYPE_KEY, "automatic");
    //_store.setDefault(HorizonPreferencePage.DECIMATE_FACTOR_KEY, 2);
    //setDecimate(_store.getBoolean(HorizonPreferencePage.DECIMATE_ENABLED_KEY), _store.getString(
    //    HorizonPreferencePage.DECIMATE_TYPE_KEY).equals("manual"), _store
    //    .getInt(HorizonPreferencePage.DECIMATE_FACTOR_KEY));
    setDecimate(false, false, 1);
    _model = new Grid3dRendererModel();
    setSmoothing(_model.getSmoothingMethod());
  }

  @Override
  protected void addSpatials() {
    final Vector3[][] points = new Vector3[_numRows][_numCols];
    final int numPoints = computeTriangulatedSurfacePoints(points);

    if (numPoints == 0) {
      _viewer.setMessageText("No valid data to render.");
      return;
    }

    final FloatBuffer vertices = BufferUtils.createVector3Buffer(numPoints);
    final FloatBuffer normals = BufferUtils.createVector3Buffer(numPoints);
    final FloatBuffer textureCoords = BufferUtils.createVector2Buffer(numPoints);
    final List<Integer> indexes = new ArrayList<Integer>();

    // Compute normals.
    final double[] xy01 = _gridGeometry.transformRowColToXY(0, 1);
    final double[] xy00 = _gridGeometry.transformRowColToXY(0, 0);
    double spacing = Math.abs(xy01[0] - xy00[0]);
    if (_decimationFactor > 1) {
      spacing = (2 * _decimationFactor - 1) * spacing;
    }

    final Orientation orientation = _gridGeometry.getClockwise();

    final Vector3[][] norms = computeNormals(_surfaceGrid, (float) spacing);
    final int[][] visited = initializeIndexArray();
    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1; // really or is this just for dec = 3?
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;

    for (int row = rowStart; row < rowEnd; row += _decimationFactor) {
      _viewer.setMessageText("Loading row: " + row);
      for (int col = colStart; col < colEnd; col += _decimationFactor) {
        final Vector3 p0 = points[row][col];
        final Vector3 p1 = points[row + _decimationFactor][col];
        final Vector3 p2 = points[row][col + _decimationFactor];
        final Vector3 p3 = points[row + _decimationFactor][col + _decimationFactor];
        final boolean isNull = p0 == null || p1 == null || p2 == null || p3 == null;

        if (!isNull) {
          int p0Index = visited[row][col];
          if (p0Index < 0) {
            p0Index = addVertex(p0, vertices, normals, norms[row][col], textureCoords, row, col, visited);
          }

          int p1Index = visited[row + _decimationFactor][col];
          if (p1Index < 0) {
            p1Index = addVertex(p1, vertices, normals, norms[row + _decimationFactor][col], textureCoords, row
                + _decimationFactor, col, visited);
          }

          int p2Index = visited[row][col + _decimationFactor];
          if (p2Index < 0) {
            p2Index = addVertex(p2, vertices, normals, norms[row][col + _decimationFactor], textureCoords, row, col
                + _decimationFactor, visited);
          }

          int p3Index = visited[row + _decimationFactor][col + _decimationFactor];
          if (p3Index < 0) {
            p3Index = addVertex(p3, vertices, normals, norms[row + _decimationFactor][col + _decimationFactor],
                textureCoords, row + _decimationFactor, col + _decimationFactor, visited);
          }

          // Hook up the points so that they represent each cell as two triangles.
          if (orientation == Orientation.COLUMN) {
            indexes.add(p0Index);
            indexes.add(p2Index);
            indexes.add(p1Index);
            indexes.add(p1Index);
            indexes.add(p2Index);
            indexes.add(p3Index);
          } else {
            indexes.add(p0Index);
            indexes.add(p1Index);
            indexes.add(p2Index);
            indexes.add(p1Index);
            indexes.add(p3Index);
            indexes.add(p2Index);
          }
        }
      }
    }
    _viewer.setMessageText("Flipping buffers.");
    vertices.flip();
    normals.flip();
    textureCoords.flip();

    _viewer.setMessageText("Computing texture");
    _meshSpatial = new Mesh(_surfaceGrid.getDisplayName() + "  " + _rgbGrid.getDisplayName());

    final TexCoords acoords = new TexCoords(textureCoords);
    final IntBuffer aindex = BufferUtils.createIntBuffer(indexes.size());
    for (final int val : indexes) {
      aindex.put(val);
    }
    _meshSpatial.reconstruct(vertices, normals, null, acoords);
    _meshSpatial.getMeshData().setIndexBuffer(aindex);
    _meshSpatial.setModelBound(new BoundingBox());
    _meshSpatial.updateModelBound();

    _viewer.setMessageText("Computing color");
    recomputeColorImage();

    // Setup horizon to blend.
    _meshSpatial.setRenderBucketType(RenderBucketType.Transparent);
    _meshSpatial.setNormalsMode(NormalsMode.NormalizeIfScaled);
    final BlendState blend = new BlendState();
    blend.setEnabled(true);
    blend.setBlendEnabled(true);
    blend.setSourceFunction(SourceFunction.SourceAlpha);
    blend.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    _meshSpatial.setRenderState(blend);

    // Setup material data.
    final MaterialState ms = new MaterialState();
    ms.setEnabled(true);
    ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
    ms.setMaterialFace(MaterialFace.FrontAndBack);
    _meshSpatial.setRenderState(ms);
    _viewer.setMessageText("Horizon rendering complete...");
    if (_model.getShowMesh()) {
      _viewer.showWireover(_meshSpatial);
    } else {
      _viewer.removeWireover(_meshSpatial);
    }

    // Force construction of the pick tree.
    CollisionTreeManager.getInstance().updateCollisionTree(_meshSpatial);
    _viewer.setMessageText("");

    if (_meshSpatial != null) {
      _viewer.mapSpatial(_meshSpatial, this);
      _viewer.addToScene(_surfaceGrid.getDataDomain(), _meshSpatial);
    }

    return;
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final Grid3dRendererDialog dialog = new Grid3dRendererDialog(shell, _surfaceGrid.getDisplayName(), this,
        _surfaceGrid);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected final void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, null, IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  public void redraw() {
    updateRendererModel(_model);
  }

  @Override
  public void refresh() {
    updateRendererModel(_model);
  }

  @Override
  protected final void setNameAndImage() {
    setName(_surfaceGrid);
    setImage(ModelUI.getSharedImages().getImage(_surfaceGrid));
  }

  @Override
  protected final void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!Grid3d.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }

    _surfaceGrid = (Grid3d) objects[0];

    // Default the hue grid of the model to be the surface grid.
    _model.setRGBGrid(_surfaceGrid);

    //_listener = new HorizonPropertyListener(_registry, this);
    //_registry.addPropertyChangeListener(_listener);
    _rgbGrid = _surfaceGrid;
    _gridGeometry = _rgbGrid.getGeometry();

    if (_surfaceGrid.isTimeGrid() || _surfaceGrid.isDepthGrid()) {
      _surfaceGrid = _rgbGrid;
    } else {
      _viewer.setMessageText("Grid " + _gridGeometry.getDisplayName() + " is not a z grid.");
    }
    setDomain(_rgbGrid.getZDomain());

    _numRows = _gridGeometry.getNumRows();
    _numCols = _gridGeometry.getNumColumns();

    if (_decimateEnabled && !_decimateManual) {
      _decimationFactor = (int) Math.sqrt(_numRows * _numCols) / 500 + 1;
    }
    if (_decimationFactor > 1) {
      _viewer.setMessageText("A triangle decimate factor of " + _decimationFactor + " was applied on "
          + _surfaceGrid.getDisplayName());
    }

    _imageWidth = MathUtils.nearestPowerOfTwo(_numCols / _decimationFactor);
    _imageHeight = MathUtils.nearestPowerOfTwo(_numRows / _decimationFactor);

    final double minValue = _rgbGrid.getMinValue();
    final double maxValue = _rgbGrid.getMaxValue();
    _model.getColorBar().setRange(minValue, maxValue, 1);
  }

  public final Object[] getRenderedObjects() {
    return new Object[] { _surfaceGrid };
  }

  public Grid3dRendererModel getSettingsModel() {
    return _model;
  }

  public SpatialExtent getExtent() {
    final double[] xs = new double[2];
    final double[] ys = new double[2];
    final double[] zs = new double[2];
    double xmin = Double.MAX_VALUE;
    double xmax = -Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;
    double zmin = Double.MAX_VALUE;
    double zmax = -Double.MAX_VALUE;
    for (final Point3d point : _surfaceGrid.getCornerPoints().getPointsDirect()) {
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      xmin = Math.min(xmin, x);
      xmax = Math.max(xmax, x);
      ymin = Math.min(ymin, y);
      ymax = Math.max(ymax, y);
      zmin = Math.min(zmin, z);
      zmax = Math.max(zmax, z);
    }
    xs[0] = xmin;
    xs[1] = xmax;
    ys[0] = ymin;
    ys[1] = ymax;
    zs[0] = zmin;
    zs[1] = zmax;
    final SpatialExtent extent = new SpatialExtent(xs, ys, zs, _surfaceGrid.getDataDomain());
    return extent;
  }

  /**
   * Loop through the surface's rows and columns and unpack the xyz points to use
   * to create the triangulated surface.
   *
   * @return an array of triangulated surface points.
   */
  public int computeTriangulatedSurfacePoints(final Vector3[][] points) {
    int numPoints = 0;
    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1; // really or is this just for dec = 3?
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;

    for (int row = rowStart; row < rowEnd + _decimationFactor; row += _decimationFactor) {
      _viewer.setMessageText("Processing row: " + row);
      for (int col = colStart; col < colEnd + _decimationFactor; col += _decimationFactor) {
        if (!_surfaceGrid.isNull(row, col)) {
          final double[] xy = _gridGeometry.transformRowColToXY(row, col);
          final float z = _surfaceGrid.getValueAtRowCol(row, col);
          points[row][col] = new Vector3((float) xy[0], (float) xy[1], z);
          numPoints++;
        }
      }
    }

    return numPoints;
  }

  public int computeNonTriangulatedSurfacePoints(final Vector3[][] points) {
    int numPoints = 0;
    for (int row = 0; row < _numRows; row++) {
      _viewer.setMessageText("Processing row: " + row);
      for (int col = 0; col < _numCols; col++) {
        if (!_surfaceGrid.isNull(row, col)) {
          final double[] xy = _gridGeometry.transformRowColToXY(row, col);
          final float z = _surfaceGrid.getValueAtRowCol(row, col);
          points[row][col] = new Vector3((float) xy[0], (float) xy[1], z);
          numPoints++;
        }
      }
    }
    return numPoints;
  }

  /**
   * Build the buffered image for the texture.
   * 
   * @return the buffered image.
   */
  private BufferedImage getBufferedImage() {
    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1;
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;

    // I guessed the image size. It could be computed more precisely based on decFactor and filter widths ?
    final BufferedImage image = new BufferedImage(_imageWidth, _imageHeight, BufferedImage.TYPE_INT_ARGB);

    int rowDec = 0; // this doesn't feel right either
    for (int row = rowStart; row < rowEnd; row += _decimationFactor) {
      int colDec = 0;
      for (int col = colStart; col < colEnd; col += _decimationFactor) {
        final ColorRGBA color = computeHorizonVertexColor(row, col);
        if (color != null) {
          image.setRGB(colDec, rowDec, color.asIntARGB());
        }
        colDec++;
      }
      rowDec++;
    }

    return image;
  }

  /**
   * Add the given vertex to the data stores.
   */
  private int addVertex(final Vector3 vertex, final FloatBuffer vertices, final FloatBuffer normals,
      final Vector3 norm, final FloatBuffer textureCoords, final int row, final int col, final int[][] index) {

    final int vertexIndex = vertices.position() / 3;
    final float[] temp = new float[3];
    vertices.put(vertex.toFloatArray(temp));
    normals.put(norm.toFloatArray(temp));
    // Add texture coords, first s, then t
    textureCoords.put((float) (col + 1) / _decimationFactor / _imageWidth);
    textureCoords.put((float) (row + 1) / _decimationFactor / _imageHeight);
    index[row][col] = vertexIndex;
    return vertexIndex;
  }

  /**
   * Initialize an array of the same size as the horizon.
   * Each value[x][y] will contain -1 if the point at x,y was not already chosen 
   * for an existing vertex or the vertex index if it has already been chosen.
   * Using this approach is the fastest way to find the index in the vertices array of a certain point.
   * 
   * @return the initial array with default values
   */
  private int[][] initializeIndexArray() {
    final int[][] indexes = new int[_numRows][_numCols];
    for (int i = 0; i < _numRows; i++) {
      for (int k = 0; k < _numCols; k++) {
        indexes[i][k] = NOT_CHOSEN_POINT;
      }
    }
    return indexes;
  }

  public void recomputeColorImage() {
    final TextureState old = (TextureState) _meshSpatial.getLocalRenderState(StateType.Texture);
    if (old != null) {
      final Texture t = old.getTexture(0);
      if (t != null) {
        // Release old texture. 
        _viewer.cleanupTexture(t);
      }
    }

    final Texture texture = AWTTextureUtil.loadTexture(getBufferedImage(), getMinificationFilter(),
        Format.GuessNoCompression, false);
    texture.setMagnificationFilter(getMagnificationFilter());
    texture.setAnisotropicFilterPercent(getAnisoLevel());
    final TextureState ts = new TextureState();
    ts.setTexture(texture);
    _meshSpatial.setRenderState(ts);
    _meshSpatial.updateRenderState();
    CollisionTreeManager.getInstance().updateCollisionTree(_meshSpatial);
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3 pickLoc) {
    final ReadoutInfo main = new ReadoutInfo(_surfaceGrid.getDisplayName());

    final List<String> keys = new ArrayList<String>();
    final List<String> vals = new ArrayList<String>();

    keys.add(_surfaceGrid.getDisplayName());
    vals.add(_surfaceGrid.getValueAtXY(pickLoc.getX(), pickLoc.getY()) + "");
    if (_rgbGrid != _surfaceGrid) {
      keys.add(_rgbGrid.getDisplayName());
      vals.add(_rgbGrid.getValueAtXY(pickLoc.getX(), pickLoc.getY()) + "");
    }

    final ReadoutInfo horz = new ReadoutInfo("Nearest point attributes", keys, vals);

    return new ReadoutInfo[] { main, horz };
  }

  @Override
  public String getShortMessage() {
    final StringBuffer message = new StringBuffer(super.getShortMessage());
    final Vector3 pickLoc = _viewer.getPickLocation();
    message.append("  " + _surfaceGrid.getDisplayName() + " : "
        + _surfaceGrid.getValueAtXY(pickLoc.getX(), pickLoc.getY()));
    if (_rgbGrid != _surfaceGrid) {
      message.append("  " + _rgbGrid.getDisplayName() + " : " + _rgbGrid.getValueAtXY(pickLoc.getX(), pickLoc.getY()));
    }
    return message.toString();
  }

  public void dispose() {
    //_registry.removePropertyChangeListener(_listener);
    _colorCache.clear();
    _model.getColorBar().dispose();
  }

  @Override
  public void clearOutline() {
    // Does nothing for now.
  }

  @Override
  public boolean renderOutline() {
    return false;
  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    if (domain == _surfaceGrid.getDataDomain()) {
      return new Spatial[] { _meshSpatial };
    }
    return new Spatial[0];
  }

  /**
   * Updates the colors of the color bar used to rendered the volume slices.
   * 
   * @param colorMap the color map of new colors.
   */
  public void updateColors(final ColorMapModel colorMap) {
    final ColorBar colorBarOld = _model.getColorBar();
    if (colorMap.getNumColors() != colorBarOld.getNumColors()) {
      final ColorBar colorBarNew = new ColorBar(colorMap, colorBarOld.getStartValue(), colorBarOld.getEndValue(),
          colorBarOld.getStepValue());
      _model.setColorBar(colorBarNew);
      colorBarOld.dispose();
      recomputeColorImage();
      return;
    }
    _model.getColorBar().setColors(colorMap.getColors());
    recomputeColorImage();
  }

  /**
   * Updates the model of rendering properties by copying from another model.
   * 
   * @param model the model from which to copy properties.
   */
  public void updateRendererModel(final Grid3dRendererModel model) {
    // Update the model.
    _model.updateFrom(model);

    // Update the rendering.
    setSmoothing(_model.getSmoothingMethod());
    final Grid3d rgbGrid = _model.getRGBGrid();
    if (rgbGrid != null && rgbGrid.getGeometry().matchesGeometry(_surfaceGrid.getGeometry())) {
      _rgbGrid = rgbGrid;
    }
    if (_model.getShowMesh()) {
      _viewer.showWireover(_meshSpatial);
    } else {
      _viewer.removeWireover(_meshSpatial);
    }
    recomputeColorImage();
  }

  /**
   * Compute the color for the given grid location.
   * 
   * @param row the grid row.
   * @param col the grid column.
   * @return the vertex RGBA color.
   */
  private ColorRGBA computeHorizonVertexColor(final int row, final int col) {
    ColorRGBA color = null;
    if (_surfaceGrid.isNull(row, col) || _rgbGrid.isNull(row, col)) {
      // If either the surface or RGB grid are null, then return a transparent.
      color = ColorRGBA.BLACK_NO_ALPHA;
    } else {
      // Otherwise, lookup the color based on the value of the RGB grid.
      final ColorBar colorBar = _model.getColorBar();
      final Color newColor = new Color(null, colorBar.getColor(_rgbGrid.getValueAtRowCol(row, col), true));
      color = getColorFromCache(newColor);
      final float alpha = 1f - _model.getTransparency() / 100f;
      color.setAlpha(alpha);
    }
    return color;
  }

  /**
   * Gets an RGBA color from the cache.
   * 
   * @param color the AWT color.
   * @return the RGBA color.
   */
  private ColorRGBA getColorFromCache(final Color color) {
    if (!_colorCache.containsKey(color)) {
      final float alpha = 1f - _model.getTransparency() / 100f;
      _colorCache.put(color, VolumeViewerHelper.swtColorToColorRGBA(color, alpha));
    }
    return _colorCache.get(color);
  }

  private float[][] computeDx(final float[][] grid) {

    _viewer.setMessageText("Computing dx");
    final float[][] dx = new float[_numRows][_numCols];
    final float zNull = _surfaceGrid.getNullValue();

    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1; // really or is this just for dec = 3?
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;
    // Null out around the edges where there is not enough data to compute gradient.
    for (int row = rowStart; row < rowEnd + _decimationFactor; row += _decimationFactor) {
      for (int col = colStart; col < colEnd + _decimationFactor; col += _decimationFactor) {
        dx[row][col] = zNull;
      }
    }

    for (int row = rowStart + _decimationFactor; row < rowEnd; row += _decimationFactor) {
      for (int col = colStart + _decimationFactor; col < colEnd; col += _decimationFactor) {
        if (_surfaceGrid.isNull(row, col + _decimationFactor) || _surfaceGrid.isNull(row, col)
            || _surfaceGrid.isNull(row, col - _decimationFactor)) {
          dx[row][col] = zNull;
        } else {
          dx[row][col] = -1 * (grid[row][col + _decimationFactor] - grid[row][col - _decimationFactor]) / 2.0f;
        }
      }
    }

    return dx;
  }

  protected float[][] computeDy(final float[][] grid) {
    _viewer.setMessageText("Computing dy");
    final float[][] dy = new float[_numRows][_numCols];
    final float zNull = _surfaceGrid.getNullValue();

    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1; // really or is this just for dec = 3?
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;
    // Null out around the edges where there is not enough data to compute gradient.
    for (int row = rowStart; row < rowEnd + _decimationFactor; row += _decimationFactor) {
      for (int col = colStart; col < colEnd + _decimationFactor; col += _decimationFactor) {
        dy[row][col] = zNull;
      }
    }

    // This might be slow?
    for (int row = rowStart + _decimationFactor; row < rowEnd; row += _decimationFactor) {
      for (int col = colStart + _decimationFactor; col < colEnd; col += _decimationFactor) {

        if (_surfaceGrid.isNull(row + _decimationFactor, col) || _surfaceGrid.isNull(row, col)
            || _surfaceGrid.isNull(row - _decimationFactor, col)) {
          dy[row][col] = zNull;
        } else {
          dy[row][col] = -1 * (grid[row + _decimationFactor][col] - grid[row - _decimationFactor][col]) / 2.0f;
        }
      }
    }

    return dy;
  }

  /**
   * The vector for the normal to the surface is [dx, dy, 1] and we
   * want avoid having to compute ||u|| later on. So we normalize
   * now so that when we move the sun we know that ||u|| = 1.
   *
   * compute vector norm to the surface using the cross product
   * along x axis we have A = 0,0,0 --> 1, 0, dx
   * along y axis we have B = 0,0,0 --> 0, 1, dy
   * cross product is A x B = Ay*Bz - Az*By, Az*Bx - Ax*Bz, Ax*By - Ay*Bx
   * 
   * @param surfaceGrid the surface grid.
   * @param spacing the desired spacing.
   */
  private Vector3[][] computeNormals(final Grid3d surfaceGrid, final float spacing) {

    _viewer.setMessageText("Computing normals");
    final float[][] zVals = surfaceGrid.getValues();
    final float[][] dx = computeDx(zVals);
    final float[][] dy = computeDy(zVals);
    final float zNull = surfaceGrid.getNullValue();

    final Vector3[][] normals = new Vector3[_numRows][_numCols];
    final Vector3 nullVector = new Vector3(0, 0, 1);

    // The dx and dy gradients are computed along the inline and cross lines and not the real x and y coordinates. 
    final double rot = _gridGeometry.getRotation();

    // Pre-compute the sine and cosine we will need to compute the real world dx and dy.
    final float cos = (float) Math.cos(Math.toRadians(rot));
    final float sin = (float) Math.sin(Math.toRadians(rot));

    final int rowStart = _decimationFactor - 1;
    final int rowEnd = _numRows - 2 * _decimationFactor + 1; // really or is this just for dec = 3?
    final int colStart = rowStart;
    final int colEnd = _numCols - 2 * _decimationFactor + 1;
    for (int row = rowStart; row < rowEnd + _decimationFactor; row += _decimationFactor) {
      for (int col = colStart; col < colEnd + _decimationFactor; col += _decimationFactor) {

        if (MathUtil.isEqual(dx[row][col], zNull) || MathUtil.isEqual(dy[row][col], zNull)) {
          normals[row][col] = nullVector;
        } else {
          final float x = cos * dx[row][col] - sin * dy[row][col];
          final float y = sin * dx[row][col] + cos * dy[row][col];
          normals[row][col] = new Vector3(x, y, spacing).normalizeLocal();
        }
      }
    }
    return normals;
  }

  /**
   * Sets the decimation parameters of the renderer.
   * 
   * @param decimateEnabled <i>true</i> to enable decimation; otherwise <i>false</i>.
   * @param decimateManual <i>true</i> for manual decimation; <i>false</i> for automatic.
   * @param decimateFactor the decimation factor.
   */
  private void setDecimate(final boolean decimateEnabled, final boolean decimateManual, final int decimateFactor) {
    _decimateEnabled = decimateEnabled;
    _decimateManual = decimateManual;
    if (_decimateEnabled) {
      _decimationFactor = decimateFactor;
    }
  }
}
