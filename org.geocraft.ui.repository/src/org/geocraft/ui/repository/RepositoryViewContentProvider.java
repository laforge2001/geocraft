/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.PolylineSet;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.RegionOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.aoi.SimpleROI;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.culture.Feature;
import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.culture.PointFeature;
import org.geocraft.core.model.culture.PolygonFeature;
import org.geocraft.core.model.culture.PolylineFeature;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.model.geologicfeature.GeologicFeature;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.AbstractCubeGridProperty;
import org.geocraft.core.model.grid.CubeGrid;
import org.geocraft.core.model.grid.CubeGridCellProperty;
import org.geocraft.core.model.grid.CubeGridCornerPointProperty;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellCheckShot;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.common.tree.TreeLeaf;
import org.geocraft.ui.common.tree.TreeRoot;


/**
 * Provides the content displayed in the repository view. The provider listeners for repository 
 * change events via the event bus and then updates the repository view accordingly.
 */
public class RepositoryViewContentProvider implements IStructuredContentProvider, ITreeContentProvider,
    IMessageSubscriber, ChangeListener {

  /** The root node in the repository view tree. */
  private TreeBranch _invisibleRoot;

  /** The "Seismic" node in the repository view tree. */
  private TreeBranch _seismicNode;

  /** The "Grids" node in the repository view tree. */
  private TreeBranch _gridNode;

  /** The "Wells" node in the repository view tree. */
  private TreeBranch _wellNode;

  /** The "Faults" node in the repository view tree. */
  private TreeBranch _faultNode;

  /** The "Culture" node in the repository view tree. */
  private TreeBranch _cultureNode;

  /** The "Areas of Interest" node in the repository view tree. */
  private TreeBranch _aoiNode;

  /** The "Wavelet" node in the repository view tree. */
  private TreeBranch _waveletNode;

  /** The "Geologic Feature" node in the repository view tree. */
  private TreeBranch _geologicFeatureNode;

  /** The "PointSet: node in the repository view tree. */
  private TreeBranch _pointSetNode;

  /** The reference to the viewer for which content is provided. */
  private TreeViewer _viewer;

  public RepositoryViewContentProvider() {
    this(true);
  }

  public RepositoryViewContentProvider(final boolean subscribeToRepositoryChanges) {
    if (subscribeToRepositoryChanges) {
      // Register the content provider to subscribe only for repository change events.
      IMessageService messageService = ServiceProvider.getMessageService();
      messageService.subscribe(Topic.REPOSITORY_OBJECTS_ADDED, this);
      messageService.subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
      messageService.subscribe(Topic.REPOSITORY_OBJECT_UPDATED, this);
    }
  }

  @SuppressWarnings("unused")
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    _viewer = (TreeViewer) viewer;
  }

  public void dispose() {
    // Unregister from receiving messages from the repository.
    IMessageService messageService = ServiceProvider.getMessageService();
    messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_ADDED, this);
    messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
    messageService.unsubscribe(Topic.REPOSITORY_OBJECT_UPDATED, this);
  }

  public Object[] getElements(final Object parent) {
    if (parent instanceof TreeRoot) {
      if (_invisibleRoot == null) {
        _invisibleRoot = (TreeRoot) parent;
        initialize();
      }
      return _invisibleRoot.getChildren();
    }
    return getChildren(parent);
  }

  public Object getParent(final Object child) {
    if (child instanceof TreeLeaf) {
      return ((TreeLeaf) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(final Object parent) {
    if (parent instanceof TreeBranch) {
      return ((TreeBranch) parent).getChildren();
    }
    return new Object[0];
  }

  public boolean hasChildren(final Object parent) {
    if (parent instanceof TreeBranch) {
      return ((TreeBranch) parent).hasChildren();
    }
    return false;
  }

  /**
   * Initializes the default content for the repository view. This includes the invisible root node and the nodes for seismic,
   * grids, wells, faults, culture and AOI data.
   */
  private void initialize() {
    TreeBranch root = _invisibleRoot;
    _seismicNode = new TreeBranch("Seismic", "Contains seismic geometries, volumes", this, PostStack3d.class,
        PreStack3d.class, PostStack2d.class, PostStack2dLine.class);
    //_seismic2dNode = new TreeBranch("2D Seismic", "Contains 2D seismic geometries, volumes", this, PostStack2d.class,
    //    PostStack2dLine.class);
    _gridNode = new TreeBranch("Grids", "Contains grids and grid properties", this, Grid2d.class, Grid3d.class,
        CubeGrid.class, CubeGridCellProperty.class, CubeGridCornerPointProperty.class);
    _wellNode = new TreeBranch("Wells", "Contains wells, bores, logs, etc.", this, Well.class, WellLogTrace.class);
    _faultNode = new TreeBranch("Faults", "Contains faults.", this, FaultInterpretation.class);
    _cultureNode = new TreeBranch("Culture", "Contains culture data (lease blocks, etc.)", this);
    _aoiNode = new TreeBranch("Areas/Regions of Interest", "Contains areas and regions of interest", this,
        MapPolygonAOI.class, SeismicSurvey3dAOI.class, SeismicSurvey2dAOI.class, AreaOfInterest.class,
        AreaOfInterest.class, SimpleROI.class, RegionOfInterest.class);
    _waveletNode = new TreeBranch("Wavelets", "Contains wavelets", this, Wavelet.class);
    _geologicFeatureNode = new TreeBranch("Geologic Features", "Contains geologic features", this);
    _pointSetNode = new TreeBranch("Point Sets", "Contains point sets", this, PointSet.class);
    root.addChild(_seismicNode);
    //root.addChild(_seismicNode);
    root.addChild(_gridNode);
    root.addChild(_wellNode);
    root.addChild(_faultNode);
    root.addChild(_cultureNode);
    root.addChild(_aoiNode);
    root.addChild(_waveletNode);
    root.addChild(_geologicFeatureNode);
    root.addChild(_pointSetNode);

    refreshContent(ServiceProvider.getRepository().getAll().values());
  }

  /**
   * Refresh the content to display in the repository view. This works by clearing out all the existing data content and
   * rebuilding from scratch.
   */
  public synchronized void refreshContent(final Collection<Object> objects) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        ISelection selectedNodes = _viewer.getSelection();
        // Clear the nodes....
        for (TreeLeaf object : _seismicNode.getChildren()) {
          _seismicNode.removeChild(object);
        }
        //for (TreeLeaf object : _seismic2dNode.getChildren()) {
        //  _seismic2dNode.removeChild(object);
        //}
        for (TreeLeaf object : _gridNode.getChildren()) {
          _gridNode.removeChild(object);
        }
        for (TreeLeaf child : _wellNode.getChildren()) {
          _wellNode.removeChild(child);
        }
        for (TreeLeaf object : _faultNode.getChildren()) {
          _faultNode.removeChild(object);
        }
        for (TreeLeaf child : _cultureNode.getChildren()) {
          _cultureNode.removeChild(child);
        }
        for (TreeLeaf child : _aoiNode.getChildren()) {
          _aoiNode.removeChild(child);
        }
        for (TreeLeaf child : _waveletNode.getChildren()) {
          _waveletNode.removeChild(child);
        }
        for (TreeLeaf child : _pointSetNode.getChildren()) {
          _pointSetNode.removeChild(child);
        }
        for (TreeLeaf child : _geologicFeatureNode.getChildren()) {
          _geologicFeatureNode.removeChild(child);
        }

        // Add the objects under the correct data nodes.
        for (Object object : objects.toArray(new Object[0])) {
          if (object instanceof Grid3d) {
            Grid3d grid = (Grid3d) object;
            addGrid3d(grid);
          } else if (object instanceof Grid2d) {
            Grid2d lineInterp = (Grid2d) object;
            addGrid2d(lineInterp);
          } else if (object instanceof AbstractCubeGridProperty) {
            addCubeGridProperty((AbstractCubeGridProperty) object);
          } else if (object instanceof PostStack2d) {
            addPostStack2d((PostStack2d) object);
          } else if (object instanceof PostStack3d) {
            addPostStack3d((PostStack3d) object);
          } else if (object instanceof PreStack3d) {
            addPreStack3d((PreStack3d) object);
          } else if (object instanceof Well) {
            addWell((Well) object);
          } else if (object instanceof PolylineSet) {
            addFault((PolylineSet) object);
          } else if (object instanceof FaultInterpretation) {
            addFaultInterpretation((FaultInterpretation) object);
          } else if (object instanceof MapPolygonAOI || object instanceof SeismicSurvey2dAOI
              || object instanceof SeismicSurvey3dAOI) {
            addAOI((AreaOfInterest) object);
          } else if (object instanceof SimpleROI) {
            addROI((RegionOfInterest) object);
          } else if (object instanceof PolygonFeature || object instanceof PolylineFeature
              || object instanceof PointFeature) {
            addFeature((Feature) object);
          } else if (object instanceof Wavelet) {
            addWavelet((Wavelet) object);
          } else if (object instanceof Grid3d) {
            addGrid3d((Grid3d) object);
          } else if (object instanceof GeologicFeature) {
            addGeologicFeature((GeologicFeature) object);
          } else if (object instanceof PointSet) {
            addPointSet((PointSet) object);
          }
        }
        if (selectedNodes != null && !selectedNodes.isEmpty()) {
          _viewer.setSelection(selectedNodes, true);
        }
      }

    });
  }

  /**
   * Adds a pretack3d volume under the seismic node.
   * 
   * @param prestack volume to add.
   */
  private void addPreStack3d(final PreStack3d prestack) {
    PropertiesProviderTreeObject surveyObject = null;
    for (TreeLeaf child : _seismicNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      IPropertiesProvider propsProvider = tempObject.getPropertiesProvider();
      if (SeismicSurvey3d.class.isAssignableFrom(propsProvider.getClass())) {
        SeismicSurvey3d survey = (SeismicSurvey3d) tempObject.getPropertiesProvider();
        if (prestack.getSurvey().equals(survey)) {
          surveyObject = tempObject;
          break;
        }
      }
    }
    if (surveyObject == null) {
      surveyObject = new PropertiesProviderTreeObject(prestack.getSurvey());
      _seismicNode.addChild(surveyObject);
    }
    PropertiesProviderTreeObject prestackObject = new PropertiesProviderTreeObject(prestack);
    surveyObject.addChild(prestackObject);
    addSeismic3dOverlays(prestackObject, prestack);
  }

  /**
   * Adds a PostStack3d under the "3D Seismic" folder node.
   * 
   * @param poststack the PostStack3d to add.
   */
  private void addPostStack3d(final PostStack3d poststack) {
    TreeBranch parentNode = null;

    SeismicSurvey3d survey = poststack.getSurvey();
    if (survey != null) {

      // If the survey is non-null, then search the children of the "3D Seismic" node
      // to see if the survey is already contained.
      for (TreeLeaf child : _seismicNode.getChildren()) {
        if (child instanceof PropertiesProviderTreeObject) {
          PropertiesProviderTreeObject childNode = (PropertiesProviderTreeObject) child;
          IPropertiesProvider propsProvider = childNode.getPropertiesProvider();
          if (SeismicSurvey3d.class.isAssignableFrom(propsProvider.getClass())) {
            SeismicSurvey3d tempSurvey = (SeismicSurvey3d) propsProvider;
            if (survey.equals(tempSurvey)) {
              parentNode = childNode;
              break;
            }
          }
        }
      }

      // If no existing survey node found, then create a new node
      // for the survey and make it the parent of the poststack node.
      if (parentNode == null) {
        parentNode = new PropertiesProviderTreeObject(survey);
        _seismicNode.addChild(parentNode);
      }
    } else {
      // If the survey is null, the default the parent node to be the "3D Seismic" node.
      parentNode = _seismicNode;
    }

    // Create a new node for the poststack and add it under the parent node.
    PropertiesProviderTreeObject poststackNode = new PropertiesProviderTreeObject(poststack);
    parentNode.addChild(poststackNode);
    addSeismic3dOverlays(poststackNode, poststack);
  }

  /**
   * Add a well to the well node.
   * @param well the well to add
   */
  private void addWell(final Well well) {
    PropertiesProviderTreeObject wellObject = null;
    for (TreeLeaf child : _wellNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      Well tempWell = (Well) tempObject.getPropertiesProvider();
      if (tempWell.equals(well)) {
        return;
      }
    }
    wellObject = new PropertiesProviderTreeObject(well);
    _wellNode.addChild(wellObject);
    addWellPicksLogsAndCheckshots(well);
    //    for (WellBore bore : well.getWellBores()) {
    //      PropertiesProviderTreeObject boreObject = new PropertiesProviderTreeObject(bore);
    //      wellObject.addChild(boreObject);
    //      for (WellLogTrace log : bore.getWellLogTraces()) {
    //        boreObject.addChild(new PropertiesProviderTreeObject(log));
    //      }
    //      for (WellPick pick : bore.getWellPicks()) {
    //        boreObject.addChild(new PropertiesProviderTreeObject(pick));
    //      }
    //    }
  }

  @SuppressWarnings("unused")
  protected void addSeismic3dOverlays(final PropertiesProviderTreeObject poststackObject, final SeismicDataset volume) {
    // to be implemented in subclasses
  }

  /**
   * Adds a seismic survey2d under the seismic node.
   * 
   * @param poststack volume to add.
   */
  private PropertiesProviderTreeObject addSeismicSurvey2d(final SeismicSurvey2d seismicSurvey) {
    PropertiesProviderTreeObject surveyTreeBranch = null;
    for (TreeLeaf child : _seismicNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      IPropertiesProvider propsProvider = tempObject.getPropertiesProvider();
      if (SeismicSurvey2d.class.isAssignableFrom(propsProvider.getClass())) {
        SeismicSurvey2d entity = (SeismicSurvey2d) propsProvider;
        if (seismicSurvey != null && seismicSurvey.equals(entity)) {
          surveyTreeBranch = tempObject;
          break;
        }
      }
    }
    if (surveyTreeBranch == null) {
      surveyTreeBranch = new PropertiesProviderTreeObject(seismicSurvey);
      _seismicNode.addChild(surveyTreeBranch);
    }
    return surveyTreeBranch;
  }

  /**
   * Adds a poststack3d volume under the seismic node.
   * 
   * @param postStack volume to add.
   */
  private void addPostStack2d(final PostStack2d poststack) {
    TreeBranch parentNode = null;

    SeismicSurvey2d survey = poststack.getSurvey();
    if (survey != null) {

      // If the survey is non-null, then search the children of the "3D Seismic" node
      // to see if the survey is already contained.
      for (TreeLeaf child : _seismicNode.getChildren()) {
        if (child instanceof PropertiesProviderTreeObject) {
          PropertiesProviderTreeObject childNode = (PropertiesProviderTreeObject) child;
          IPropertiesProvider propsProvider = childNode.getPropertiesProvider();
          if (SeismicSurvey2d.class.isAssignableFrom(propsProvider.getClass())) {
            SeismicSurvey2d tempSurvey = (SeismicSurvey2d) propsProvider;
            if (survey.equals(tempSurvey)) {
              parentNode = childNode;
              break;
            }
          }
        }
      }

      // If no existing survey node found, then create a new node
      // for the survey and make it the parent of the poststack node.
      if (parentNode == null) {
        parentNode = new PropertiesProviderTreeObject(survey);
        _seismicNode.addChild(parentNode);
      }
    } else {
      // If the survey is null, the default the parent node to be the "3D Seismic" node.
      parentNode = _seismicNode;
    }

    // Create a new node for the poststack and add it under the parent node.
    PropertiesProviderTreeObject poststackNode = new PropertiesProviderTreeObject(poststack);
    parentNode.addChild(poststackNode);
    for (PostStack2dLine line : poststack.getPostStack2dLines(true)) {
      poststackNode.addChild(new PropertiesProviderTreeObject(line));
    }
  }

  /**
   * Adds a feature under the culture node.
   * 
   * @param feature to add.
   */
  private void addFeature(final Feature feature) {
    PropertiesProviderTreeObject layerObject = null;
    for (TreeLeaf child : _cultureNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      Layer layer = (Layer) tempObject.getPropertiesProvider();
      if (feature.getLayer().equals(layer)) {
        layerObject = tempObject;
        break;
      }
    }
    if (layerObject == null) {
      layerObject = new PropertiesProviderTreeObject(feature.getLayer());
      _cultureNode.addChild(layerObject);
    }
    layerObject.addChild(new PropertiesProviderTreeObject(feature));
  }

  //  private void addCornerPointGrid(CornerPointGrid grid) {
  //    PropertiesProviderTreeObject gridObject = null;
  //    for (TreeLeaf child : _gridNode.getChildren()) {
  //      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
  //      if (CornerPointGrid.class.isAssignableFrom(tempObject.getPropertiesProvider().getClass())) {
  //        CornerPointGrid tempGrid = (CornerPointGrid) tempObject.getPropertiesProvider();
  //        if (tempGrid.equals(grid)) {
  //          return;
  //        }
  //      }
  //    }
  //    gridObject = new PropertiesProviderTreeObject(grid);
  //    _gridNode.addChild(gridObject);
  //  }

  /**
   * Adds an area-of-interest (AOI).
   */
  private void addAOI(final AreaOfInterest aoi) {
    PropertiesProviderTreeObject aoiObject = null;
    for (TreeLeaf child : _aoiNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      if (AreaOfInterest.class.isAssignableFrom(tempObject.getPropertiesProvider().getClass())) {
        AreaOfInterest tempAOI = (AreaOfInterest) tempObject.getPropertiesProvider();
        if (tempAOI.equals(aoi)) {
          return;
        }
      }
    }
    aoiObject = new PropertiesProviderTreeObject(aoi);
    _aoiNode.addChild(aoiObject);
  }

  /**
  * Adds a region-of-interest (ROI).
  */
  private void addROI(final RegionOfInterest roi) {
    PropertiesProviderTreeObject roiObject = null;
    for (TreeLeaf child : _aoiNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      if (RegionOfInterest.class.isAssignableFrom(tempObject.getPropertiesProvider().getClass())) {
        RegionOfInterest tempROI = (RegionOfInterest) tempObject.getPropertiesProvider();
        if (tempROI.equals(roi)) {
          return;
        }
      }
    }
    roiObject = new PropertiesProviderTreeObject(roi);
    _aoiNode.addChild(roiObject);
  }

  /**
   * Adds a grid under the grids node.
   * 
   * @param grid the grid to add.
   */
  private void addGrid2d(final Grid2d grid) {
    PropertiesProviderTreeObject gridObject = null;
    PropertiesProviderTreeObject geometryObject = null;
    GridGeometry2d geometry = grid.getGridGeometry();
    for (TreeLeaf child : _gridNode.getChildren()) {
      if (child instanceof PropertiesProviderTreeObject) {
        PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
        IPropertiesProvider provider = tempObject.getPropertiesProvider();
        if (provider instanceof GridGeometry2d) {
          GridGeometry2d tempGeometry = (GridGeometry2d) provider;
          if (tempGeometry.equals(geometry) && tempGeometry.getDisplayName().equals(geometry.getDisplayName())) {
            geometryObject = tempObject;
            break;
          }
        }
      }
    }
    if (geometryObject == null) {
      geometryObject = new PropertiesProviderTreeObject(geometry);
      _gridNode.addChild(geometryObject);
    }
    gridObject = new PropertiesProviderTreeObject(grid);
    geometryObject.addChild(gridObject);
  }

  /**
   * Adds a Grid3d under the "Grids" folder node.
   * 
   * @param grid the Grid3d to add.
   */
  private void addGrid3d(final Grid3d grid) {
    TreeBranch parentNode = null;

    GridGeometry3d geometry = grid.getGeometry();
    if (geometry != null) {

      // If the geometry is non-null, then search the children of the "Grids" node
      // to see if the geometry is already contained.
      for (TreeLeaf child : _gridNode.getChildren()) {
        if (child instanceof PropertiesProviderTreeObject) {
          PropertiesProviderTreeObject childNode = (PropertiesProviderTreeObject) child;
          IPropertiesProvider propsProvider = childNode.getPropertiesProvider();
          if (propsProvider instanceof GridGeometry3d) {
            GridGeometry3d tempGeometry = (GridGeometry3d) propsProvider;
            if (tempGeometry.equals(geometry) && tempGeometry.getDisplayName().equals(geometry.getDisplayName())) {
              parentNode = childNode;
              break;
            }
          }
        }
      }

      // If no existing geometry node found, the create a new node for the geometry
      // and make it the parent of the grid node.
      if (parentNode == null) {
        parentNode = new PropertiesProviderTreeObject(geometry);
        _gridNode.addChild(parentNode);
      }
    } else {
      // If the geometry is null, then default the parent node to be the "Grid" node.
      parentNode = _gridNode;
    }

    // Create a new node for the grid and add it under the parent node.
    PropertiesProviderTreeObject gridNode = new PropertiesProviderTreeObject(grid);
    parentNode.addChild(gridNode);
  }

  private void addCubeGridProperty(final AbstractCubeGridProperty cubeGridProperty) {
    TreeBranch parentNode = null;

    CubeGrid cubeGrid = cubeGridProperty.getGrid();

    if (cubeGrid != null) {
      // Check if cube grid  is already in the tree.
      for (TreeLeaf child : _gridNode.getChildren()) {
        if (child instanceof PropertiesProviderTreeObject) {
          PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
          IPropertiesProvider provider = tempObject.getPropertiesProvider();
          if (provider instanceof CubeGrid) {
            CubeGrid tempGrid = (CubeGrid) provider;
            if (tempGrid.equals(cubeGrid) && tempGrid.getDisplayName().equals(cubeGrid.getDisplayName())) {
              parentNode = tempObject;
              break;
            }
          }
        }
      }
      // and make it the parent of the grid node.
      if (parentNode == null) {
        parentNode = new PropertiesProviderTreeObject(cubeGrid);
        _gridNode.addChild(parentNode);
      }
    } else {
      // If the geometry is null, then default the parent node to be the "Grid" node.
      parentNode = _gridNode;
    }
    // Create a new node for the grid and add it under the parent node.
    PropertiesProviderTreeObject gridNode = new PropertiesProviderTreeObject(cubeGridProperty);
    parentNode.addChild(gridNode);

  }

  /**
   * Add a well to the well node.
   * @param wellBore the well to add
   */
  private void addWellPicksLogsAndCheckshots(final Well well) {
    PropertiesProviderTreeObject wellObject = null;

    // Check if well is already in the tree.
    for (TreeLeaf child : _wellNode.getChildren()) {
      if (child instanceof PropertiesProviderTreeObject) {
        PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
        IPropertiesProvider provider = tempObject.getPropertiesProvider();
        if (provider instanceof Well) {
          Well tempWell = (Well) provider;
          if (tempWell.equals(well) && tempWell.getDisplayName().equals(well.getDisplayName())) {
            wellObject = tempObject;
            break;
          }
        }
      }
    }

    // If well isn't in the tree already, add it.
    if (wellObject == null) {
      wellObject = new PropertiesProviderTreeObject(well);
      _wellNode.addChild(wellObject);
    }

    TreeBranch logsNode = new TreeBranch("Logs", "Logs associated with the well");
    wellObject.addChild(logsNode);

    // Add the well log traces to well bore node.
    for (WellLogTrace trace : well.getWellLogTraces()) {
      PropertiesProviderTreeObject traceObject = new PropertiesProviderTreeObject(trace);
      logsNode.addChild(traceObject);
    }

    TreeBranch picksNode = new TreeBranch("Picks", "Picks associated with the well");
    wellObject.addChild(picksNode);

    // Add the well picks to well bore node.
    for (WellPick pick : well.getWellPicks()) {
      PropertiesProviderTreeObject pickObject = new PropertiesProviderTreeObject(pick);
      picksNode.addChild(pickObject);
    }

    TreeBranch checkShotNode = new TreeBranch("Check Shots", "Check shots associated with the well");
    wellObject.addChild(checkShotNode);

    // Add the well check shots to well bore node.
    for (WellCheckShot checkShot : well.getWellCheckshots()) {
      PropertiesProviderTreeObject checkShotObject = new PropertiesProviderTreeObject(checkShot);
      checkShotNode.addChild(checkShotObject);
    }
  }

  /**
   * Add a fault to the faults node.
   * @param fault the fault to add
   */
  private void addFault(final PolylineSet fault) {
    PropertiesProviderTreeObject faultObject = null;
    for (TreeLeaf child : _faultNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      PolylineSet tempFault = (PolylineSet) tempObject.getPropertiesProvider();
      if (tempFault.equals(fault)) {
        return;
      }
    }
    faultObject = new PropertiesProviderTreeObject(fault);
    _faultNode.addChild(faultObject);
  }

  /**
   * Add a fault to the faults node.
   * @param fault the fault to add
   */
  private void addFaultInterpretation(final FaultInterpretation fault) {
    PropertiesProviderTreeObject faultObject = null;
    for (TreeLeaf child : _faultNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      FaultInterpretation tempFault = (FaultInterpretation) tempObject.getPropertiesProvider();
      if (tempFault.equals(fault)) {
        return;
      }
    }
    faultObject = new PropertiesProviderTreeObject(fault);
    _faultNode.addChild(faultObject);
  }

  private void addWavelet(final Wavelet wavelet) {
    PropertiesProviderTreeObject waveletObject = null;
    for (TreeLeaf child : _waveletNode.getChildren()) {
      PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
      Wavelet tempWavelet = (Wavelet) tempObject.getPropertiesProvider();
      if (tempWavelet.equals(wavelet)) {
        return;
      }
    }
    waveletObject = new PropertiesProviderTreeObject(wavelet);
    _waveletNode.addChild(waveletObject);
  }

  /**
   * Adds a geologic feature under the geologic feature node.
   * 
   * @param feature the geologic feature to add.
   */
  private void addGeologicFeature(final GeologicFeature grid) {
    PropertiesProviderTreeObject gridObject = null;
    for (TreeLeaf child : _geologicFeatureNode.getChildren()) {
      if (child instanceof PropertiesProviderTreeObject) {
        PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
        if (tempObject.getPropertiesProvider() instanceof GeologicFeature) {
          GeologicFeature tempGeologicFeature = (GeologicFeature) tempObject.getPropertiesProvider();
          if (tempGeologicFeature.equals(grid)) {
            return;
          }
        }
      }
    }
    gridObject = new PropertiesProviderTreeObject(grid);
    _geologicFeatureNode.addChild(gridObject);
  }

  /**
   * Adds a PointSet under the Point Set feature node.
   * 
   * @param pointSet the feature to add.
   */
  private void addPointSet(final PointSet pointSet) {
    PropertiesProviderTreeObject pointSetObject = null;
    for (TreeLeaf child : _pointSetNode.getChildren()) {
      if (child instanceof PropertiesProviderTreeObject) {
        PropertiesProviderTreeObject tempObject = (PropertiesProviderTreeObject) child;
        if (tempObject.getPropertiesProvider() instanceof PointSet) {
          PointSet temp = (PointSet) tempObject.getPropertiesProvider();
          if (temp.equals(pointSet)) {
            return;
          }
        }
      }
    }
    pointSetObject = new PropertiesProviderTreeObject(pointSet);
    _pointSetNode.addChild(pointSetObject);
  }

  /**
   * Invoked when the event bus publishes an event. Currently the content provider only subscribes for repository change events.
   * 
   * @param topic of the published event.
   * @param value of the published event.
   */
  public synchronized void messageReceived(final String topic, final Object value) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_ADDED) | topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)
        || topic.equals(Topic.REPOSITORY_OBJECT_UPDATED)) {
      // Get the repository reference.
      IRepository repository = ServiceProvider.getRepository();
      // Get all objects from the repository.
      Collection<Object> objects = repository.getAll().values();

      refreshContent(objects);
    }
  }

  /**
   * Invoked when one of the tree objects is updated.
   * 
   * @param event the change event.
   */
  public void stateChanged(final ChangeEvent event) {
    refreshViewer();
  }

  /**
   * Refreshes the viewer.
   * 
   * @param viewer the viewer to refresh.
   */
  private void refreshViewer() {
    if (!_viewer.getTree().isDisposed()) {
      _viewer.getControl().getDisplay().asyncExec(new Runnable() {

        public void run() {
          if (_viewer != null && !_viewer.getTree().isDisposed()) {
            try {
              _viewer.refresh(false);
            } catch (SWTException e) {
              ServiceProvider.getLoggingService().getLogger(getClass()).warn("Could not refresh viewer");
            }
          }
        }
      });
    }
  }
}
