/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.common;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.image.ISharedImages;


public class SharedImages implements ISharedImages {

  /** The logger. */

  public SharedImages() {
    _keyToPath.put(IMG_FRAMES, "icons/misc/Frames16.png");
    _keyToPath.put(IMG_Z_RANGE, "icons/misc/zrange.png");
    _keyToPath.put(IMG_DIRTY, "icons/misc/dirty3.png");
    _keyToPath.put(IMG_PRINT, "icons/sun/Print16.gif");
    _keyToPath.put(IMG_FIND, "icons/sun/Find16.gif");
    _keyToPath.put(IMG_REFRESH, "icons/sun/Refresh16.gif");
    _keyToPath.put(IMG_REMOVE, "icons/sun/Remove16.gif");
    _keyToPath.put(IMG_EXPORT, "icons/oo/export.png");
    _keyToPath.put(IMG_SECTION_VIEW, "icons/misc/sectionview-16.png");
    _keyToPath.put(IMG_HISTOGRAM, "icons/misc/histogram-16.png");
    _keyToPath.put(IMG_TABLE, "icons/misc/table-16.png");
    _keyToPath.put(IMG_FORM_CLOSE, "icons/oo/close-16.png");
    _keyToPath.put(IMG_FORM_AUTOPILOTS, "icons/oo/form-autopilots-16.png");
    _keyToPath.put(IMG_TOOL_24, "icons/oo/autopilot-24.png");
    _keyToPath.put(IMG_TOOLS_32, "icons/oo/autopilot-32.png");
    _keyToPath.put(IMG_PLOT_BOUNDS, "icons/misc/PlotBounds16.png");
    _keyToPath.put(IMG_BLANK, "icons/misc/blank-16.png");
    _keyToPath.put(IMG_MAIN_LOGO, "icons/misc/GeoCraft_LOGO_S_new.png");
    _keyToPath.put(IMG_SMALL_LOGO, "icons/misc/GC-mark-16.png");
    _keyToPath.put(IMG_BACK, "icons/sun/Back16.gif");
    _keyToPath.put(IMG_FORWARD, "icons/sun/Forward16.gif");
    _keyToPath.put(IMG_PREVIOUS, "icons/misc/prev.png");
    _keyToPath.put(IMG_NEXT, "icons/misc/next.png");
    _keyToPath.put(IMG_NAVIGATOR, "icons/oo/navigator-16.png");
    _keyToPath.put(IMG_NAVIGATE_PREVIOUS, "icons/oo/navigate-prev-16.png");
    _keyToPath.put(IMG_NAVIGATE_NEXT, "icons/oo/navigate-next-16.png");
    _keyToPath.put(IMG_ZOOM_IN, "icons/oo/zoom-in-16.png");
    _keyToPath.put(IMG_ZOOM_OUT, "icons/oo/zoom-out-16.png");
    _keyToPath.put(IMG_PREFERENCES, "icons/sun/Preferences16.gif");
    _keyToPath.put(IMG_SAVE_AS, "icons/oo/save_as-16.png");
    _keyToPath.put(IMG_IMPORT, "icons/sun/Import16.gif");
    _keyToPath.put(IMG_DELETE, "icons/sun/Delete16.gif");
    _keyToPath.put(IMG_RESET, "icons/misc/reset.png");
    _keyToPath.put(IMG_TABLE_VARIABLE, "icons/oo/table-variable-16.png");
    _keyToPath.put(IMG_HOME, "icons/oo/home-16.png");
    _keyToPath.put(IMG_SEND_CURSOR, "icons/misc/SendCursor16.gif");
    _keyToPath.put(IMG_RECEIVE_CURSOR, "icons/misc/ReceiveCursor16.gif");
    _keyToPath.put(IMG_LIGHT, "icons/oo/3d-light-16.png");
    _keyToPath.put(IMG_VOLUME, "icons/misc/Volume-16.png");
    _keyToPath.put(IMG_ORTHOGRAPHIC, "icons/oo/draw-cube-16.png");
    _keyToPath.put(IMG_POS_SIZE, "icons/oo/position-size-16.png");
    _keyToPath.put(IMG_BROADCAST, "icons/sun/Volume16.gif");
    _keyToPath.put(IMG_RELOAD, "icons/oo/reload-16.png");
    _keyToPath.put(IMG_ZOOM_OBJECT, "icons/oo/zoom-object-16.png");
    _keyToPath.put(IMG_DRAG_MODE, "icons/oo/drag-mode-16.png");
    _keyToPath.put(IMG_ANCHOR, "icons/oo/anchor-16.png");
    _keyToPath.put(IMG_STOCK_HELP_AGENT, "icons/misc/stock_help-agent-16.png");
    _keyToPath.put(IMG_MODIFY_LAYOUT, "icons/oo/modify-layout-16.png");
    _keyToPath.put(IMG_COLORBAR, "icons/misc/ColorBar16.png");
    _keyToPath.put(IMG_POLYGON, "icons/misc/stock_draw-polygon-filled-16.png");
    _keyToPath.put(IMG_RUN, "icons/obj16/run_exc.gif");
    _keyToPath.put(IMG_HELP, "icons/obj16/help.gif");
    _keyToPath.put(IMG_RECTANGLE_16, "icons/oo/draw-rectangle-16.png");
    _keyToPath.put(IMG_RECTANGLE_24, "icons/oo/draw-rectangle.png");
    _keyToPath.put(IMG_ROUNDED_RECTANGLE_16, "icons/oo/draw-rounded-rectangle-16.png");
    _keyToPath.put(IMG_ROUNDED_RECTANGLE_24, "icons/oo/draw-rounded-rectangle.png");
    _keyToPath.put(IMG_CONN_16, "icons/oo/draw-connector-ends-with-arrow-16.png");
    _keyToPath.put(IMG_CONN_24, "icons/oo/draw-connector-ends-with-arrow.png");
    _keyToPath.put(IMG_OPEN, "icons/oo/open-16.png");
    _keyToPath.put(IMG_EDIT, "icons/oo/edit-16.png");
    _keyToPath.put(IMG_VERB, "icons/oo/format-object-16.png");
    _keyToPath.put(IMG_DRAW_LINE_CONNECTOR, "icons/oo/draw-line-connector-16.png");
    _keyToPath.put(IMG_SORT, "icons/oo/sort-criteria-16.png");

  }

  private final Map<String, String> _keyToPath = new HashMap<String, String>();

  public Image getImage(final String key) {
    ImageDescriptor desc = getImageDescriptor(key);
    if (desc == null) {
      return null;
    }
    return desc.createImage();
  }

  public ImageDescriptor getImageDescriptor(final String key) {
    if (_keyToPath.get(key) == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("Unknown image key " + key);
      return null;
    }
    ImageRegistry imageRegistry = ServiceComponent.getImageRegistry();
    ImageDescriptor image = imageRegistry.getDescriptor(key);
    if (image == null) {
      URL url = FileLocator.find(Platform.getBundle(ServiceComponent.PLUGIN_ID), new Path(_keyToPath.get(key)), null);
      if (url != null) {
        imageRegistry.put(key, ImageDescriptor.createFromURL(url));
      } else {
        ServiceProvider.getLoggingService().getLogger(getClass()).warn("Image not found for key " + key);
      }
    }
    return imageRegistry.getDescriptor(key);
  }

}
