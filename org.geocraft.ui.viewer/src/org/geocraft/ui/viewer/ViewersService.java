/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved.
 */
package org.geocraft.ui.viewer;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.viewer.IViewersService;
import org.geocraft.core.session.ComponentState;
import org.geocraft.core.session.SavesetDescriptor;
import org.geocraft.core.session.SessionManager;
import org.geocraft.core.session.SavesetDescriptor.Viewer;
import org.geocraft.core.session.SavesetDescriptor.ViewerPart;
import org.geocraft.core.session.SavesetDescriptor.Viewer.ViewerLayer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


/** Access methods to the registry of active viewers */
public class ViewersService implements IViewersService {

  /** The logger. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(ViewersService.class);

  /** List of viewers whose renderer states were restored */
  ArrayList<IViewer> _restoredViewers = new ArrayList<IViewer>();

  /** List of the layered model content providers for all the restored viewers */
  List _contentProviders = Collections.synchronizedList(new ArrayList<IContentProvider>());

  public ViewersService() {
    LOGGER.debug("Viewers service created.");
  }

  public Map<Integer, Object> getRegisteredViewerParts() {
    return ActiveViewerRegistry.getInstance().getRegisteredViewerParts();
  }

  public Map<String, String> getViewerParms(Object viewer) {
    //Object viewer = ActiveViewerRegistry.getInstance().getRegisteredViewer(key);
    Model model = ((IViewer) viewer).getViewerModel();
    return model != null ? model.pickle() : new HashMap<String, String>();
  }

  public String getViewerName(Object viewer) {
    //Object viewer = ActiveViewerRegistry.getInstance().getRegisteredViewer(key);
    Class klass = viewer.getClass();
    //use reflection to get the viewer's title (i.e., call getTitle)
    try {
      Method m = klass.getMethod("getTitle", null);
      return (String) m.invoke(viewer, null);
    } catch (NoSuchMethodException nsme) {
      return "Untitled Viewer";
    } catch (IllegalAccessException iae) {
      return "Untitled Viewer";
    } catch (IllegalArgumentException iae) {
      return "Untitled Viewer";
    } catch (InvocationTargetException ite) {
      return "Untitled Viewer";
    }
  }

  public String getViewerClassName(Object viewer) {
    //Object viewer = ActiveViewerRegistry.getInstance().getRegisteredViewer(key);
    return viewer.getClass().getName();
  }

  public String getViewerPartClassName(int key) {
    Object part = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(key);
    return part.getClass().getName();
  }

  public String getViewerPartId(int key) {
    Object part = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(key);
    return ((IViewerPart) part).getPartId();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#getViewerPartId(java.lang.Object)
   */
  @Override
  public String getViewerPartID(Object viewerPart) {
    return ((IViewerPart) viewerPart).getPartId();
  }

  public int getViewerPartUniqueId(int key) {
    Object part = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(key);
    return part.hashCode();
  }

  public String getViewerPartName(int key) {
    Object part = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(key);
    return ((IViewerPart) part).getPartName();
  }

  public String getViewerPartId(IViewPart viewPart) {
    //ignore views that are not an IViewerPart
    if (viewPart instanceof IViewerPart) {
      //use reflection to get the ID of a viewer part
      //Note: not all views are a IViewerPart
      try {
        Method m = viewPart.getClass().getMethod("getPartId", null);
        String partID = (String) m.invoke(viewPart, null);
        return partID;
      } catch (NoSuchMethodException nsme) {
        System.out.println("EXCEPTION: cannot obtain viewer part's getPartId method: " + nsme.getMessage());
      } catch (IllegalAccessException iae) {
        System.out.println("EXCEPTION: cannot access viewer part's getPartId method: " + iae.getMessage());
      } catch (IllegalArgumentException iae) {
        System.out.println("EXCEPTION: improper arguments to viewer part's getPartId method: " + iae.getMessage());
      } catch (InvocationTargetException ite) {
        System.out.println("EXCEPTION: failed to invoke viewer part's getPartId method: " + ite.getMessage());
      }
    }
    return "";
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#getViewers(org.eclipse.ui.IViewPart)
   */
  @Override
  public IViewer getViewer(IViewPart viewPart) {
    //use reflection to get the viewer
    try {
      Method m = viewPart.getClass().getMethod("getViewer", null);
      IViewer viewer = (IViewer) m.invoke(viewPart, null);
      //Note: The viewer cannot be redrawn until the renderers have been added
      return viewer;
    } catch (NoSuchMethodException nsme) {
      System.out.println("EXCEPTION: cannot obtain viewer part's getViewer method: " + nsme.getMessage());
    } catch (IllegalAccessException iae) {
      System.out.println("EXCEPTION: cannot access viewer part's getViewer method: " + iae.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println("EXCEPTION: improper arguments to viewer part's getViewer method: " + iae.getMessage());
    } catch (InvocationTargetException ite) {
      System.out.println("EXCEPTION: failed to invoke viewer part's getViewer method: " + ite.getMessage());
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#getViewers(org.eclipse.ui.IViewPart)
   */
  @Override
  public Object[] getViewers(IViewPart viewPart) {
    //use reflection to get the viewers
    try {
      Method m = viewPart.getClass().getMethod("getViewers", null);
      IViewer[] viewers = (IViewer[]) m.invoke(viewPart, null);
      //Note: The viewers cannot be redrawn until the renderers have been added
      return viewers;
    } catch (NoSuchMethodException nsme) {
      System.out.println("EXCEPTION: cannot obtain viewer part's getViewers method: " + nsme.getMessage());
    } catch (IllegalAccessException iae) {
      System.out.println("EXCEPTION: cannot access viewer part's getViewers method: " + iae.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println("EXCEPTION: improper arguments to viewer part's getViewers method: " + iae.getMessage());
    } catch (InvocationTargetException ite) {
      System.out.println("EXCEPTION: failed to invoke viewer part's getViewers method: " + ite.getMessage());
    }
    return null;
  }

  public Object getDescription(int key) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#getWindowID(int)
   */
  @Override
  public String getViewerPartWindowID(int key) {
    return ActiveViewerRegistry.getInstance().getWindowID(key);
  }

  public void add(final Object viewerPart) {
    ActiveViewerRegistry.getInstance().registerViewerPart(viewerPart);
  }

  public void remove(final int key) {
    ActiveViewerRegistry.getInstance().unregisterViewerPart(key);
  }

  public void removeAll() {
    ActiveViewerRegistry.getInstance().clearRegistry();
  }

  public void deactivate(final int key) {
    Object viewerPart = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(key);
    ((ViewPart) viewerPart).dispose();
  }

  public Object activateViewer(String klass, Map<String, String> parms, String partName) {
    try {
      // Create a new viewer
      String id = Integer.toString(ViewerHelper.getNextViewerId());
      IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage().showView(klass, id,
          IWorkbenchPage.VIEW_ACTIVATE);
      ((AbstractViewerPart) viewPart).setViewerPartName(partName);

      //use reflection to get the viewer
      try {
        Method m = viewPart.getClass().getMethod("getViewer", null);
        IViewer viewer = (IViewer) m.invoke(viewPart, null);
        // Update viewer's model
        updateViewerModel(viewer, parms);
        //Note: The viewer cannot be redrawn until the renderers have been added
        return viewer;
      } catch (NoSuchMethodException nsme) {
        System.out.println("EXCEPTION: cannot obtain viewer part's getViewer method: " + nsme.getMessage());
      } catch (IllegalAccessException iae) {
        System.out.println("EXCEPTION: cannot access viewer part's getViewer method: " + iae.getMessage());
      } catch (IllegalArgumentException iae) {
        System.out.println("EXCEPTION: improper arguments to viewer part's getViewer method: " + iae.getMessage());
      } catch (InvocationTargetException ite) {
        System.out.println("EXCEPTION: failed to invoke viewer part's getViewer method: " + ite.getMessage());
      }
    } catch (PartInitException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
    return null;
  }

  public Object[] activateViewers(String klass, ArrayList<Map<String, String>> vprops) {
    try {
      // Create a new viewer
      String id = Integer.toString(ViewerHelper.getNextViewerId());
      IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage().showView(klass, id,
          IWorkbenchPage.VIEW_ACTIVATE);

      //use reflection to get the viewers
      try {
        Method m = viewPart.getClass().getMethod("getViewers", null);
        IViewer[] viewers = (IViewer[]) m.invoke(viewPart, null);
        // Note: Model must be updated AFTER renderers added
        int i = 0;
        for (IViewer viewer : viewers) {
          updateViewerModel(viewer, vprops.get(i));
          i++;
        }
        //Note: The viewer cannot be redrawn until the renderers have been added
        return viewers;
      } catch (NoSuchMethodException nsme) {
        System.out.println("EXCEPTION: cannot obtain viewer's unpickle method: " + nsme.getMessage());
      } catch (IllegalAccessException iae) {
        System.out.println("EXCEPTION: cannot access viewer's unpickle method: " + iae.getMessage());
      } catch (IllegalArgumentException iae) {
        System.out.println("EXCEPTION: improper arguments to viewer's unpickle method: " + iae.getMessage());
      } catch (InvocationTargetException ite) {
        System.out.println("EXCEPTION: failed to invoke viewer's unpickle method: " + ite.getMessage());
      }
    } catch (PartInitException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
    return null;
  }

  public void addRenderer(Object viewer, String klass, Map<String, String> props, String uniqueId) {
    ((IViewer) viewer).addRenderer(klass, props, uniqueId);
  }

  /**
   * Get the viewers associated with a registered viewer part
   * @param vkey Unique ID of the viewer part instance
   * @return
   */
  private IViewer[] getViewers(int vkey) {
    Object part = ActiveViewerRegistry.getInstance().getRegisteredViewerPart(vkey);
    return ((IViewerPart) part).getViewers();
  }

  public ArrayList<String> getSessionState(int vkey) {
    //Note: vkey is for a registered viewer part that contains all its 
    //      associated viewers
    ArrayList<String> xml = new ArrayList<String>();

    //get the viewers associated with the viewer part
    IViewer[] viewers = getViewers(vkey);

    for (IViewer viewer : viewers) {
      //      TreeViewer treeViewer = viewer.getLayerViewer();
      //capture active viewers and their model parameters
      String name = getViewerName(viewer);
      String className = getViewerClassName(viewer);
      xml.add(ComponentState.openViewerElement(name, className));

      ILayeredModel layeredModel = viewer.getLayeredModel();
      if (layeredModel != null) {
        IViewLayer[] layers = layeredModel.getLayers();
        for (IViewLayer layer : layers) {
          xml.add(ComponentState.viewerLayerElement(layer.getUniqueID(), layer.isChecked()));
        }
      }

      //capture viewer's parameters
      Map<String, String> parms = getViewerParms(viewer);
      Set<String> pkeys = parms.keySet();
      Iterator<String> parmIter = pkeys.iterator();
      while (parmIter.hasNext()) {
        String key = parmIter.next();
        String val = parms.get(key);
        val = val.replace("\r\n", "&#xD;&#xA;"); //Windows (CR+LF)
        val = val.replace("\n", "&#xA;"); //Unix (CR)
        if (!key.equals("")) {
          xml.add(ComponentState.viewerParameterElement(key, val));
        }
      }

      //capture associated renderers and their properties
      IRenderer[] renderers = viewer.getRenderers();
      for (IRenderer renderer : renderers) {
        className = renderer.getClass().getName();
        xml.add(ComponentState.openRendererElement(className, renderer.hashCode()));

        //capture data entities rendered by the renderer
        Object[] objects = renderer.getRenderedObjects();
        for (Object object : objects) {
          if (object instanceof Entity) {
            Entity entity = (Entity) object;
            xml.add(ComponentState.renderedEntityElement(entity.getUniqueID()));
          }
        }

        //capture renderer's properties
        Model rmodel = renderer.getSettingsModel();
        //ignore renderer's that have no model
        if (rmodel != null) {
          //capture renderer's model properties
          Map<String, String> props = rmodel.pickle();
          Set<String> keys = props.keySet();
          Iterator<String> keyIter = keys.iterator();
          while (keyIter.hasNext()) {
            String key = keyIter.next();
            String val = props.get(key);
            xml.add(ComponentState.rendererModelPropertyElement(key, val));
          }
        }

        xml.add(ComponentState.closeRendererElement());
      }

      xml.add(ComponentState.closeViewerElement());
    }

    return xml;
  }

  public void dumpRegistry() {
    ActiveViewerRegistry.getInstance().dumpRegistry();
  }

  public void updateViewerModel(Object viewer, Map<String, String> vprops) {
    Model model = ((IViewer) viewer).getViewerModel();
    if (model != null) {
      model.unpickle(vprops);
      //Note: The viewer cannot be redrawn until the renderers have been added 
    }
  }

  public void updateFromModel(Object viewer) {
    ((IViewer) viewer).updateFromModel();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#updateAllViewerFolderLayers(org.geocraft.core.sessionSavesetDescriptor, java.util.ArrayList)
   */
  @Override
  public void updateAllViewerLayers(Object desc, Object newWindowIds) {
    SavesetDescriptor _desc = (SavesetDescriptor) desc;
    HashMap<String, String> _newWindowIds = (HashMap<String, String>) newWindowIds;

    IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
    ArrayList<String> windowIds = _desc.getWorkbenchWindows();
    for (String windowId : windowIds) {
      IWorkbenchWindow workbenchWindow = null;
      //find the new workbench window having the new window ID
      String newWindowId = _newWindowIds.get(windowId);
      for (IWorkbenchWindow window : workbenchWindows) {
        String id = SessionManager.getInstance().getWorkbenchWindowID(window);
        if (id.equals(newWindowId)) {
          workbenchWindow = window;
          break;
        }
      }
      //ignore updating if cannot find workbench window
      if (workbenchWindow == null) {
        continue;
      }
      ArrayList<ViewerPart> viewerParts = _desc.getViewerParts(windowId);
      for (ViewerPart viewerPart : viewerParts) {
        String partID = viewerPart.getViewerPartId();
        if (viewerPart.getNumViewers() == 1) {
          Viewer viewer = viewerPart.getViewers().get(0);
          Object viewer2 = SessionManager.getInstance().findViewerInWindow(workbenchWindow, partID);
          if (viewer2 != null) {
            updateViewerLayers((IViewer) viewer2, viewer);
          }
        } else { // multiple viewers in viewer part
          ArrayList<Viewer> viewers = viewerPart.getViewers();
          Object[] viewers2 = SessionManager.getInstance().findViewersInWindow(workbenchWindow, partID);
          if (viewers2 != null) {
            int idx = 0;
            for (Viewer viewer : viewers) {
              if (viewers2[idx] != null) {
                updateViewerLayers((IViewer) viewers2[idx], viewer);
              }
              idx++;
            }
          }
        }
      }
    }
  }

  private void updateViewerLayers(IViewer viewer, Viewer viewerState) {
    ArrayList<ViewerLayer> viewerLayers = viewerState.getViewerLayers();
    HashMap<String, Boolean> checked = new HashMap<String, Boolean>();
    for (ViewerLayer layer : viewerLayers) {
      checked.put(layer.getLayerName(), layer.isChecked());
    }
    ILayeredModel layeredModel = viewer.getLayeredModel();
    if (layeredModel != null) {
      layeredModel.setCheckedMap(checked);
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#initViewers()
   */
  @Override
  public void initViewers() {
    _restoredViewers.clear();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#addViewer(java.lang.Object)
   */
  @Override
  public void addViewer(Object viewer) {
    _restoredViewers.add((IViewer) viewer);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#initLayers()
   */
  @Override
  public void initLayers() {
    for (IViewer viewer : _restoredViewers) {
      _contentProviders.add(viewer.getLayerViewer().getContentProvider());
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#isPlotWindow(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public boolean isPlotWindow(IWorkbenchWindow win) {
    IWorkbenchWindow plotWin = ViewerHelper.getPlotWindow();
    return plotWin != null && win.equals(plotWin) ? true : false;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.viewer.IViewersService#setPloatWindow(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void setPlotWindow(IWorkbenchWindow win) {
    ViewerHelper.setPlotWindow(win);
  }
}
