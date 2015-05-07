/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.common.image;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 * Interface to declare the shared images across bundles keys. 
 */
public interface ISharedImages {

  String IMG_FRAMES = "Frames16";

  String IMG_Z_RANGE = "zrange";

  String IMG_DIRTY = "dirty3";

  String IMG_PRINT = "Print16";

  String IMG_FIND = "Find16";

  String IMG_REMOVE = "Remove16";

  String IMG_REFRESH = "Refresh16";

  String IMG_EXPORT = "export";

  String IMG_SECTION_VIEW = "sectionview-16";

  String IMG_HISTOGRAM = "histogram-16";

  String IMG_TABLE = "table-16";

  String IMG_FORM_CLOSE = "close-16";

  String IMG_FORM_AUTOPILOTS = "form-autopilots-16";

  String IMG_TOOL_24 = "autopilot-24";

  String IMG_TOOLS_32 = "autopilot-32";

  String IMG_PLOT_BOUNDS = "PlotBounds16";

  String IMG_BLANK = "blank-16";

  String IMG_MAIN_LOGO = "GeoCraft_LOGO_S_new";

  String IMG_SMALL_LOGO = "GC-mark-16";

  String IMG_BACK = "Back16";

  String IMG_FORWARD = "Forward16";

  String IMG_PREVIOUS = "previous";

  String IMG_NEXT = "next";

  String IMG_NAVIGATOR = "navigator-16";

  String IMG_NAVIGATE_PREVIOUS = "navigate-prev-16";

  String IMG_NAVIGATE_NEXT = "navigate-next-16";

  String IMG_ZOOM_IN = "zoom-in-16";

  String IMG_ZOOM_OUT = "zoom-out-16";

  String IMG_ZOOM_OBJECT = "zoom-object-16";

  String IMG_PREFERENCES = "Preferences16";

  String IMG_SAVE_AS = "save_as-16";

  String IMG_IMPORT = "Import16";

  String IMG_DELETE = "Delete16";

  String IMG_RESET = "reset";

  String IMG_TABLE_VARIABLE = "table-variable-16";

  String IMG_HOME = "home-16";

  String IMG_SEND_CURSOR = "SendCursor16";

  String IMG_RECEIVE_CURSOR = "ReceiveCursor16";

  String IMG_LIGHT = "3d-light-16";

  String IMG_VOLUME = "Volume-16";

  String IMG_ORTHOGRAPHIC = "orhtographic";

  String IMG_BROADCAST = "Volume16";

  String IMG_POS_SIZE = "position-size-16";

  String IMG_RELOAD = "reload-16";

  String IMG_DRAG_MODE = "drag-mode-16";

  String IMG_ANCHOR = "anchor-16";

  String IMG_STOCK_HELP_AGENT = "stock_help-agent-16";

  String IMG_MODIFY_LAYOUT = "modify-layout-16";

  String IMG_COLORBAR = "ColorBar16";

  String IMG_POLYGON = "stock_draw-polygon-filled-16";

  String IMG_RUN = "run_exc";

  String IMG_HELP = "help";

  String IMG_RECTANGLE_16 = "rectangle16";

  String IMG_RECTANGLE_24 = "rectangle24";

  String IMG_ROUNDED_RECTANGLE_16 = "rounded-rectangle-16";

  String IMG_ROUNDED_RECTANGLE_24 = "rounded-rectangle-24";

  String IMG_CONN_16 = "conn16";

  String IMG_CONN_24 = "conn24";

  String IMG_OPEN = "open-16";

  String IMG_EDIT = "edit-16";

  String IMG_VERB = "select-verb";

  String IMG_DRAW_LINE_CONNECTOR = "draw-line-connector-16";

  String IMG_SORT = "sort-criteria-16";

  ImageDescriptor getImageDescriptor(final String key);

  Image getImage(String key);

}
