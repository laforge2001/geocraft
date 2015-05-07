/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.Workbench;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.internal.viewer.ServiceComponent;


public class ViewerHelper {

  private static int _currentId;

  private static boolean _inPlotWindow;

  private static IWorkbenchWindow _plotWindow;

  public static IWorkbenchWindow getPlotWindow() {
    if (_plotWindow == null || _plotWindow.getShell() == null) {
      return null;
    }
    return _plotWindow;
  }

  public static void setPlotWindow(IWorkbenchWindow plotWindow) {
    _plotWindow = plotWindow;
  }

  public static int getNextViewerId() {
    _inPlotWindow = PreferencesUtil.getService(ServiceComponent.PLUGIN_ID).get("plotCreate", "").equals(
        "separateWindow");
    if (_inPlotWindow && (_plotWindow == null || _plotWindow.getShell() == null)) {
      initViewerPerspective();
    }
    return _currentId++;
  }

  public static IWorkbenchWindow getViewerWindow() {
    if (!_inPlotWindow || _plotWindow == null) {
      return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    return _plotWindow;
  }

  private static void initViewerPerspective() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
    try {
      _plotWindow = workbench.openWorkbenchWindow("Viewer.perspective", input);
    } catch (WorkbenchException e) {
      ServiceProvider.getLoggingService().getLogger(ViewerHelper.class).warn("Cannot open viewer perspective", e);
    }
  }

  public static void reset() {
    _currentId = 0;
  }

  /**
   * Test if a map is included into another map 
   * @param map1 the first map
   * @param map2 the second map
   * @return
   */
  public static boolean isIncluded(final Map<String, Object> map1, final Map<String, Object> map2) {
    for (String key : map1.keySet()) {
      if (!map2.keySet().contains(key)) {
        return false;
      }
    }
    return true;
  }

}
