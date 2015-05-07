package org.geocraft.ui.volumeviewer.renderer.fault;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolylinePick;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.model.fault.TriangleDefinition;
import org.geocraft.core.model.fault.TriangulatedSurface;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.NormalsMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;


public class FaultRenderer extends VolumeViewRenderer {

  /** The fault interpretation being rendered. */
  private FaultInterpretation _fault;

  /** The model of renderer properties. */
  private final FaultRendererModel _model;

  /** The node containing all the fault spatials. */
  private Node _faultNode;

  /** The node containing the pick segment spatials. */
  private Node _segmentsNode;

  /** The node containing the triangulation spatials. */
  private Node _triangulationNode;

  /** The line spatials representing the pick segments. */
  private final List<Line> _segmentLines;

  /** The mesh spatial representing the triangulation. */
  private Mesh _triangleMesh;

  /**
   * Constructs the fault renderer.
   */
  public FaultRenderer() {
    super("Fault Renderer");
    _model = new FaultRendererModel();
    _segmentLines = Collections.synchronizedList(new ArrayList<Line>());
  }

  @Override
  protected void addSpatials() {
    _segmentLines.clear();
    _faultNode = new Node(_fault.getDisplayName());
    _segmentsNode = new Node("Segments");
    _triangulationNode = new Node("Triangulation");
    //    final Polyline[] polylines = _fault.getPolylines();
    //    if (polylines.length != _fault.getNumPolylines()) {
    //      LOGGER.debug(_fault.getDisplayName() + ": nPolylines should be=" + _fault.getNumPolylines() + " but "
    //          + polylines.length + "found");
    //    }
    //    if (polylines.length == 0) {
    //      _registry.setMessage("This polyline set contains no polylines");
    //    }

    for (final PolylinePick polyline : _fault.getSegments()) {
      final Vector3[] vert = VolumeViewerHelper.points3dToVector3(polyline.getPoints());
      for (final Vector3 v : vert) {
        //v.setZ(-v.getZ());
        //System.out.println("segment point: " + v.getXf() + " " + v.getYf() + " " + v.getZf());
      }
      final Line line = new Line(_fault.getDisplayName() + " segment", vert, null, null, null);
      line.setLineWidth(_model.getSegmentWidth());
      line.getMeshData().setIndexMode(IndexMode.LineStrip);
      line.setRenderBucketType(RenderBucketType.Opaque);
      line.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the polylines
      line.setLineWidth(1f);
      final RGB rgb = _model.getSegmentColor();
      line.setDefaultColor(VolumeViewerHelper.colorToColorRGBA(rgb, 1));
      line.setAntialiased(true);
      line.setModelBound(new OrientedBoundingBox());
      line.updateModelBound();

      final BlendState alphaState = new BlendState();
      alphaState.setEnabled(true);
      alphaState.setBlendEnabled(true);
      alphaState.setSourceFunction(SourceFunction.SourceAlpha);
      alphaState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
      line.setRenderState(alphaState);
      _segmentsNode.attachChild(line);

      _segmentLines.add(line);
    }

    if (_fault.isTriangulated()) {
      final TriangulatedSurface surface = _fault.getTriangulatedSurface();
      final int numVertices = surface.getNumVertices();
      final int numTriangles = surface.getNumTriangles();
      final Mesh mesh = new Mesh(_fault.getDisplayName() + " triangulation");
      final FloatBuffer vertices = BufferUtils.createVector3Buffer(numVertices);
      final FloatBuffer normals = BufferUtils.createVector3Buffer(numVertices);
      //final FloatBuffer textureCoords = BufferUtils.createVector2Buffer(numVertices);
      final List<Integer> indices = new ArrayList<Integer>();
      final Point3d[] vertexArray = surface.getVertices();

      // Compute normals for the vertices.  For each triangle, compute the normal
      // and update the normals for the indicated vertices
      final Vector3[] normalArray = new Vector3[numVertices];
      final TriangleDefinition[] triangleArray = surface.getTriangles();
      final int[] t = new int[3];
      for (int i = 0; i < numTriangles; i++) {
        t[0] = triangleArray[i].getVertex1() - 1;
        t[1] = triangleArray[i].getVertex2() - 1;
        t[2] = triangleArray[i].getVertex3() - 1;
        final Point3d p1 = vertexArray[t[0]];
        final Point3d p2 = vertexArray[t[1]];
        final Point3d p3 = vertexArray[t[2]];
        final Vector3 v1 = new Vector3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ());
        final Vector3 v2 = new Vector3(p3.getX() - p1.getX(), p3.getY() - p1.getY(), p3.getZ() - p1.getZ());
        for (int tv = 0; tv < 3; tv++) {
          if (normalArray[t[tv]] == null) {
            normalArray[t[tv]] = v1.cross(v2, null);
          } else {
            normalArray[t[tv]].addLocal(v1.cross(v2, null));
          }
        }
      }

      for (int tv = 0; tv < numVertices; tv++) {
        normalArray[tv].normalizeLocal();
      }

      for (int i = 0; i < numVertices; i++) {
        final Point3d p1 = vertexArray[i];
        final Vector3 vertex = new Vector3(p1.getX(), p1.getY(), p1.getZ());
        addVertex(vertex, vertices, normalArray[i], normals, null);
        indices.add(i);
      }
      final IntBuffer aIndices = BufferUtils.createIntBuffer(numTriangles * 3);
      //for (final int index : indices) {
      //  aIndices.put(index);
      //}

      final TriangleDefinition[] triangleDefs = surface.getTriangles();
      for (int i = 0; i < numTriangles; i++) {
        final TriangleDefinition triangleDef = triangleDefs[i];
        aIndices.put(triangleDef.getVertex1() - 1);
        aIndices.put(triangleDef.getVertex2() - 1);
        aIndices.put(triangleDef.getVertex3() - 1);
      }

      //      for (final TriangleDefinition triangleDef : surface.getTriangles()) {
      //        final Point3d vertex1 = vertices[triangleDef.getVertex1() - 1];
      //        final Point3d vertex2 = vertices[triangleDef.getVertex2() - 1];
      //        final Point3d vertex3 = vertices[triangleDef.getVertex3() - 1];
      //        final Vector3 point1 = new Vector3(vertex1.getX(), vertex1.getX(), vertex1.getZ());
      //        final Vector3 point2 = new Vector3(vertex2.getX(), vertex2.getX(), vertex2.getZ());
      //        final Vector3 point3 = new Vector3(vertex3.getX(), vertex3.getX(), vertex3.getZ());
      //        final Triangle triangle = new Triangle(point1, point2, point3);
      //      }
      vertices.flip();
      mesh.reconstruct(vertices, normals, null, null);
      mesh.getMeshData().setIndexBuffer(aIndices);
      mesh.setModelBound(new BoundingBox());
      mesh.updateModelBound();
      final RGB rgb = _model.getTriangleColor();
      mesh.setSolidColor(new ColorRGBA(rgb.red / 255.0f, rgb.green / 255.0f, rgb.blue / 255.0f, 1.0f));

      // Setup to blend.
      mesh.setRenderBucketType(RenderBucketType.Opaque);
      mesh.setNormalsMode(NormalsMode.NormalizeIfScaled);
      //      final BlendState blend = new BlendState();
      //      blend.setEnabled(true);
      //      blend.setBlendEnabled(true);
      //      blend.setSourceFunction(SourceFunction.SourceAlpha);
      //      blend.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
      //      mesh.setRenderState(blend);

      // Setup material data.
      final MaterialState ms = new MaterialState();
      ms.setEnabled(true);
      ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
      ms.setMaterialFace(MaterialFace.FrontAndBack);
      ms.setShininess(2);

      //      ms.setDiffuse(new ColorRGBA(_model.getTriangleColor().red / 255.0f, _model.getTriangleColor().green / 255.0f,
      //          _model.getTriangleColor().blue / 255.0f, 1.0f));
      ms.setDiffuse(ColorRGBA.RED);
      ms.setAmbient(ColorRGBA.GREEN);
      mesh.setRenderState(ms);
      _triangulationNode.attachChild(mesh);
      _triangleMesh = mesh;
    }
    _faultNode.attachChild(_segmentsNode);
    _faultNode.attachChild(_triangulationNode);
    _viewer.mapSpatial(_faultNode, this);
    _viewer.addToScene(_fault.getZDomain(), _faultNode);
    _viewer.makeDirty();
  }

  private int addVertex(final Vector3 vertex, final FloatBuffer vertices, final Vector3 normal,
      final FloatBuffer normals, final FloatBuffer textureCoords) {
    final int vertexIndex = vertices.position() / 3;
    final float[] temp = new float[3];
    vertices.put(vertex.toFloatArray(temp));
    normals.put(normal.toFloatArray(temp));
    return vertexIndex;
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final FaultRendererDialog dialog = new FaultRendererDialog(shell, _fault.getDisplayName(), this, _fault);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, null, IViewer.FAULT_FOLDER, autoUpdate);
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
  protected void setNameAndImage() {
    setName(_fault);
    setImage(ModelUI.getSharedImages().getImage(_fault));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!FaultInterpretation.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }
    _fault = (FaultInterpretation) objects[0];

    _model.setSegmentColor(_fault.getDisplayColor());
    _model.setTriangleColor(_fault.getDisplayColor());
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _fault };
  }

  public FaultRendererModel getSettingsModel() {
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
    for (final PolylinePick pick : _fault.getSegments()) {
      for (final Point3d point : pick.getPoints()) {
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
    }
    if (_fault.isTriangulated()) {
      final TriangulatedSurface surface = _fault.getTriangulatedSurface();
      for (final Point3d point : surface.getVertices()) {
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
    }
    xs[0] = xmin;
    xs[1] = xmax;
    ys[0] = ymin;
    ys[1] = ymax;
    zs[0] = zmin;
    zs[1] = zmax;
    final SpatialExtent extent = new SpatialExtent(xs, ys, zs, _fault.getZDomain());
    return extent;
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3 pickLoc) {
    return new ReadoutInfo[] { new ReadoutInfo(_fault.getDisplayName()) };
  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    if (domain.equals(_fault.getZDomain())) {
      return new Spatial[] { _faultNode };
    }
    return new Spatial[0];
  }

  @Override
  public void clearOutline() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean renderOutline() {
    // TODO Auto-generated method stub
    return false;
  }

  public void updateRendererModel(final FaultRendererModel model) {
    _model.updateFrom(model);
    if (model.getShowSegments()) {
      _faultNode.attachChild(_segmentsNode);
    } else {
      _faultNode.detachChild(_segmentsNode);
    }
    if (model.getShowTriangulation()) {
      _faultNode.attachChild(_triangulationNode);
    } else {
      _faultNode.detachChild(_triangulationNode);
    }
    final RGB segmentColor = _model.getSegmentColor();
    for (final Line segmentLine : _segmentLines) {
      segmentLine.setDefaultColor(VolumeViewerHelper.colorToColorRGBA(segmentColor, 1));
      segmentLine.setLineWidth(_model.getSegmentWidth());
    }
    _triangleMesh.setSolidColor(VolumeViewerHelper.colorToColorRGBA(_model.getTriangleColor(), 1));
    _viewer.makeDirty();
  }

}
