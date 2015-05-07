/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.ui.color;


import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorFormatDescription;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.color.format.IColorFormat;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.color.IColorMapService;
import org.geocraft.ui.color.action.ColorBarCircularShift;
import org.geocraft.ui.color.action.ColorBarMarkerHighlight;
import org.geocraft.ui.color.action.ColorBarNonEditable;
import org.geocraft.ui.color.action.ColorBarReverseColors;
import org.geocraft.ui.common.GridLayoutHelper;


/**
 * The basic color bar editor. Allows for loading a new color map, circularly adjusting
 * the color bar and specifying the desired start/end range.
 */
public class ColorBarEditor extends Composite implements MouseListener, MouseMoveListener, MouseTrackListener,
    IPropertyChangeListener, ColorMapListener {

  /** The color model being edited. */
  protected ColorBar _colorbar;

  /** The color model being edited. */
  protected ColorMapModel _colormap;

  /** The copy of the color model being edited. */
  protected ColorBar _colorbarBuf;

  /** The color to display a distinctive marker in. */
  protected RGB _colorMarker = new RGB(255, 255, 0);

  /** The upper index for the marker. */
  protected int _upperMarker;

  /** The lower index for the marker. */
  protected int _lowerMarker;

  /** The edit operation on the color bar (e.g. cycling through color scheme). */
  protected ColorBar.Mode _editMode = ColorBar.Mode.NONE;

  /** The operation on the the color marker (e.g. moving, resizing the marker). */
  protected ColorBar.MarkerMode _markerMode = ColorBar.MarkerMode.NONE;

  /** The pixel location of initial mouse click. */
  protected int _xStart = Integer.MAX_VALUE;

  /** The pixel location of current mouse location. */
  protected int _xMotion = Integer.MAX_VALUE;

  /** The pixel location of initial mouse click. */
  protected int _yStart = Integer.MAX_VALUE;

  /** The pixel location of current mouse location. */
  protected int _yMotion = Integer.MAX_VALUE;

  /** The menu of color I/O actions (opening pre-defined colormaps, loading/saving colormaps to file). */
  protected Menu _colorsMenu;

  /** The menu of editing actions (circular adjustment, color marker). */
  protected Menu _editMenu;

  /** The menu item for color map edit mode. */
  protected MenuItem _menuItemEditor;

  /** The color bar drawing canvas. */
  protected ColorBarCanvas _canvas;

  /** The cursor being displayed on the editor (e.g. a hand, up-down arrows). */
  protected Cursor _cursor;

  /** The marker color selector. */
  protected ColorSelector _optMarkerColor;

  /** The start value label. */
  protected Label _lblStart;

  /** The end value label. */
  protected Label _lblEnd;

  /** The start value text field. */
  protected Text _txtStart;

  /** The end value text field. */
  protected Text _txtEnd;

  /** set this when the user changes the color map */
  protected boolean _colorMapChanged = false;

  /**
   * Creates a ColorBar with specified colors and scale flags.
   * 
   * @param colorbar the colorbar to edit.
   */
  public ColorBarEditor(final Composite parent, final ColorBar colorbar) {
    this(parent, colorbar, 256);
  }

  /**
   * Creates a ColorBar with specified colors and scale flags.
   * 
   * @param colorbar the colorbar to edit.
   */
  public ColorBarEditor(final Composite parent, final ColorBar colorbar, final int height) {
    super(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    parent.setLayout(layout);

    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    layoutData.horizontalSpan = 1;
    layoutData.verticalSpan = 1;
    layoutData.heightHint = height + 200;
    setLayoutData(layoutData);

    GridLayout gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = false;
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    setLayout(gridLayout);

    _colorbar = colorbar;
    _colorbar.addColorMapListener(this);
    _colormap = new ColorMapModel(colorbar.getColors());

    build();
  }

  public void colorsChanged(final ColorMapEvent event) {
    double start = _colorbar.getStartValue();
    double end = _colorbar.getEndValue();
    _txtStart.setText("" + start);
    _txtEnd.setText("" + end);
    _canvas.update();
    _canvas.redraw();
  }

  /**
   * Builds the color bar panel and defines default parameters.
   */
  protected void build() {
    final ToolBar toolbar = new ToolBar(this, SWT.HORIZONTAL);
    toolbar.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.CENTER, 2, 1));

    initMarkers();
    int marginWidth = 100;
    int marginHeight = 25;
    _canvas = new ColorBarCanvas(this, _colorbar, marginWidth, marginHeight);
    _canvas.setVisible(true);
    _canvas.addMouseListener(this);
    _canvas.addMouseMoveListener(this);
    _canvas.addMouseTrackListener(this);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.verticalAlignment = SWT.FILL;
    gridData.horizontalSpan = 2;
    gridData.verticalSpan = 1;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.widthHint = 300;
    _canvas.setLayoutData(gridData);

    _lblStart = new Label(this, SWT.NONE);
    _lblStart.setText("Start Value");
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.widthHint = 150;
    _lblStart.setLayoutData(gridData);

    _lblEnd = new Label(this, SWT.NONE);
    _lblEnd.setText("End Value");
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.widthHint = 150;
    _lblEnd.setLayoutData(gridData);

    _txtStart = new Text(this, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 150;
    _txtStart.setLayoutData(gridData);

    _txtEnd = new Text(this, SWT.BORDER);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.widthHint = 100;
    _txtEnd.setLayoutData(gridData);

    _txtStart.setText(Double.toString(_colorbar.getStartValue()));
    _txtEnd.setText(Double.toString(_colorbar.getEndValue()));

    Listener startListener = new Listener() {

      @Override
      public void handleEvent(final Event event) {
        String text = _txtStart.getText();
        try {
          double value = Double.parseDouble(text.trim());
          _colorbar.setStartValue(value);
        } catch (NumberFormatException e) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error("Invalid start: " + text);
        }
      }
    };
    _txtStart.addListener(SWT.DefaultSelection, startListener);
    _txtStart.addListener(SWT.FocusOut, startListener);

    Listener endListener = new Listener() {

      @Override
      public void handleEvent(final Event event) {
        String text = _txtEnd.getText();
        try {
          double value = Double.parseDouble(text.trim());
          _colorbar.setEndValue(value);
        } catch (NumberFormatException e) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error("Invalid end: " + text);
        }
      }
    };
    _txtEnd.addListener(SWT.DefaultSelection, endListener);
    _txtEnd.addListener(SWT.FocusOut, endListener);

    _optMarkerColor = new ColorSelector(this);
    _optMarkerColor.getButton().setText("Marker Color");
    _optMarkerColor.setColorValue(_colorMarker);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 2;
    gridData.widthHint = 300;
    _optMarkerColor.getButton().setLayoutData(gridData);
    _optMarkerColor.addListener(this);

    final ToolItem fileToolItem = new ToolItem(toolbar, SWT.DROP_DOWN);
    fileToolItem.setText("Colors");
    _colorsMenu = new Menu(_canvas.getShell(), SWT.POP_UP);
    fileToolItem.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        Rectangle rect = fileToolItem.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = toolbar.toDisplay(pt);
        _colorsMenu.setLocation(pt.x, pt.y);
        _colorsMenu.setVisible(true);
      }
    });
    addColorsMenuItems(_colorsMenu);

    final ToolItem editToolItem = new ToolItem(toolbar, SWT.DROP_DOWN);
    editToolItem.setText("Edit");
    _editMenu = new Menu(_canvas.getShell(), SWT.POP_UP);
    editToolItem.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        Rectangle rect = editToolItem.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = toolbar.toDisplay(pt);
        _editMenu.setLocation(pt.x, pt.y);
        _editMenu.setVisible(true);
      }
    });
    addEditMenuItems(_editMenu);
    //_toolBar.pack();

    setEditMode(ColorBar.Mode.NONE);
    addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        _colorsMenu.dispose();
        _editMenu.dispose();
        _colorbar.removeColorMapListener(ColorBarEditor.this);
        _colorbar.dispose();
        if (_colorbarBuf != null) {
          _colorbarBuf.dispose();
        }
      }
    });
  }

  public void initMarkers() {
    _lowerMarker = _colorbar.getNumColors() / 2;
    _upperMarker = _lowerMarker + 1;
  }

  /**
   * Gets the color at the specified index. Will
   * return the marker color if in marker editing
   * mode and the index is between the markers.
   *
   * @param index the index of the color to get.
   * @return the color of the specified index.
   */
  protected RGB getColor(final int index) {

    RGB result = _colormap.getColor(index);

    if (_editMode.equals(ColorBar.Mode.MARKER)) {
      if (index >= _lowerMarker && index <= _upperMarker) {
        result = _colorMarker;
      }
    }

    return result;
  }

  /**
   * Sets the color at the specified index.
   * @param index the index of the color to set.
   * @param color the color to set in the specified index.
   */
  protected void setColor(final int index, final RGB color) {
    if (index > 0 && index < _colorbar.getNumColors()) {
      _colorbar.setColor(index, color);
    }
    updated();
  }

  /**
   * Sets the color array.
   * @param colors the colors to set in the color bar.
   */
  public void setColors(final RGB[] colors) {
    _colorbar.setColors(colors);
    _colormap.setColors(_colorbar.getColors());
    updated();
  }

  public void updated() {
    redraw();
    update();
    _colorbar.updated();
    _canvas.redraw();
    _canvas.update();
  }

  /**
   * Sets the edit mode of the colorbar.
   */
  public void setEditMode(final ColorBar.Mode editMode) {
    _editMode = editMode;
    updateCursor(SWT.CURSOR_ARROW);

    //    Icon iconChecked = ImageRegistryUtil.createImageIcon("icons/misc/checkbox-16.png");
    //    Icon iconNonAdj = null;
    //    Icon iconCircularAdj = null;
    //    Icon iconMarkerAdj = null;
    //
    //    if (editMode.equals(ColorBar.Mode.None)) {
    //      iconNonAdj = iconChecked;
    //    }
    //    if (editMode.equals(ColorBar.Mode.Circular)) {
    //      iconCircularAdj = iconChecked;
    //    }
    //    if (editMode.equals(ColorBar.Mode.Marker)) {
    //      iconMarkerAdj = iconChecked;
    //    }
    //
    //    _actionNonAdj.putValue(Action.SMALL_ICON, iconNonAdj);
    //    _actionCircularAdj.putValue(Action.SMALL_ICON, iconCircularAdj);
    //    _actionMarkerAdj.putValue(Action.SMALL_ICON, iconMarkerAdj);

    if (editMode.equals(ColorBar.Mode.MARKER)) {
      setMarkerMode(ColorBar.MarkerMode.MOTION);
    } else {
      setMarkerMode(ColorBar.MarkerMode.NONE);
    }
    for (int ndx = 0; ndx < _colorbar.getNumColors(); ndx++) {
      _colorbar.setColor(ndx, getColor(ndx), false);
    }
    _canvas.redraw();
    _canvas.update();
    redraw();
    update();
    _colorbar.updated();
  }

  /**
   * Updates the cursor with a new style.
   * @param cursorStyle the new cursor style to set.
   */
  private void updateCursor(final int cursorStyle) {
    Cursor cursorOld = _cursor;
    _cursor = new Cursor(getDisplay(), cursorStyle);
    setCursor(_cursor);
    if (cursorOld != null) {
      cursorOld.dispose();
    }
  }

  /**
   * Sets the marker mode of the colorbar.
   *
   * @param mode the new marker mode
   */
  public void setMarkerMode(final ColorBar.MarkerMode mode) {
    _markerMode = mode;
  }

  public ColorBar getColorBar() {
    return _colorbar;
  }

  public void setColorBar(final ColorBar colorbar) {
    _colorbar = colorbar;
    _canvas.setColorBar(colorbar);
    _txtStart.setText(Double.toString(colorbar.getStartValue()));
    _txtEnd.setText(Double.toString(colorbar.getEndValue()));
    updated();
  }

  public double getStartValue() throws NumberFormatException {
    return Double.parseDouble(_txtStart.getText().trim());
  }

  public double getEndValue() throws NumberFormatException {
    return Double.parseDouble(_txtEnd.getText().trim());
  }

  /**
   * Adds items to the "Colors" menu.
   * This includes options for opening pre-defined colormaps, loading new colormaps from file,
   * and saving the current colorbar to file.
   * 
   * @param menu the "Colors" menu.
   */
  protected void addColorsMenuItems(final Menu menu) {

    IColorMapService colorMapService = ServiceProvider.getColorMapService();
    // For now, exit if color map service not available
    if (colorMapService == null) {
      return;
    }
    ColorMapDescription[] colorMapDescriptions = colorMapService.getAll();

    // Add the 'Select...' menu item.
    MenuItem selectItem = new MenuItem(menu, SWT.CASCADE);
    selectItem.setText("Select...");
    Menu menuSelect = new Menu(menu.getShell(), SWT.DROP_DOWN);
    selectItem.setMenu(menuSelect);

    // Add the registered color maps.
    for (ColorMapDescription colorMapDescription : colorMapDescriptions) {
      MenuItem menuItemOpen = new MenuItem(menuSelect, SWT.PUSH);
      Image image = colorMapDescription.createImage(Display.getCurrent());
      menuItemOpen.setImage(image);
      menuItemOpen.setText("\'" + colorMapDescription.getName() + "\' Colormap");
      menuItemOpen.setData(colorMapDescription);
      menuItemOpen.addListener(SWT.Selection, new Listener() {

        @Override
        public void handleEvent(final Event event) {
          MenuItem widget = (MenuItem) event.widget;
          ColorMapDescription colorMapDesc = (ColorMapDescription) widget.getData();
          // Set this if the since the color map has changed
          _colorMapChanged = true;
          IColorMap colorMap = colorMapDesc.createMap();
          int numColors = _colorbar.getNumColors();
          setColors(colorMap.getRGBs(numColors));
        }

      });
    }

    // Add the 'Open...' menu item.
    MenuItem openItem = new MenuItem(menu, SWT.CASCADE);
    openItem.setText("Open...");
    Menu menuOpen = new Menu(menu.getShell(), SWT.DROP_DOWN);
    openItem.setMenu(menuOpen);

    ColorFormatDescription[] colorFormatDescs = ServiceProvider.getColorFormatService().getAll();

    for (ColorFormatDescription colorFormatDesc : colorFormatDescs) {
      final ColorFormatDescription temp = colorFormatDesc;
      if (temp.canRead()) {
        // Add the 'Load from File...' menu item.
        MenuItem menuItemOpen = new MenuItem(menuOpen, SWT.PUSH);
        menuItemOpen.setText(colorFormatDesc.getName() + "...");
        menuItemOpen.addListener(SWT.Selection, new Listener() {

          @Override
          public void handleEvent(final Event event) {
            IColorFormat colorFormat = temp.createFormat();
            try {
              RGB[] colors = colorFormat.loadColors(getShell());
              if (colors != null && colors.length > 0) {
                setColors(colors);
              }
            } catch (IOException ex) {
              MessageDialog.openError(getShell(), "Color Read Error", ex.toString());
            }
          }

        });
      }
    }

    // If no color formats are available, then simply return and do not
    // add the 'Save As' sub-menu.
    if (colorFormatDescs == null || colorFormatDescs.length == 0) {
      return;
    }

    // Add a separator.
    new MenuItem(menu, SWT.SEPARATOR);

    // Add the 'Save As...' menu item.
    MenuItem saveAsItem = new MenuItem(menu, SWT.CASCADE);
    saveAsItem.setText("Save As...");
    Menu menuSave = new Menu(menu.getShell(), SWT.DROP_DOWN);
    saveAsItem.setMenu(menuSave);

    for (ColorFormatDescription colorFormatDesc : colorFormatDescs) {
      final ColorFormatDescription temp = colorFormatDesc;
      if (temp.canWrite()) {
        // Add the 'Save As...' menu item.
        MenuItem menuItemSave = new MenuItem(menuSave, SWT.PUSH);
        menuItemSave.setText(colorFormatDesc.getName() + "...");
        menuItemSave.addListener(SWT.Selection, new Listener() {

          @Override
          public void handleEvent(final Event event) {
            try {
              IColorFormat colorFormat = temp.createFormat();
              colorFormat.saveColors(getShell(), _colorbar.getColors());
            } catch (IOException ex) {
              MessageDialog.openError(getShell(), "Color Write Error", ex.toString());
            }
          }

        });
      }
    }
  }

  /**
   * Adds items to the "Edit" menu.
   * This includes options for reversing colors, shifting colors and highlighing colors.
   * 
   * @param menu the "Edit" menu.
   */
  protected void addEditMenuItems(final Menu menu) {

    final Action actionReverseColors = new ColorBarReverseColors(this);
    final Action actionNonEditable = new ColorBarNonEditable(this);
    final Action actionCircularShift = new ColorBarCircularShift(this);
    final Action actionMarkerEdit = new ColorBarMarkerHighlight(this);

    MenuItem menuItemReverse = new MenuItem(menu, SWT.PUSH);
    menuItemReverse.setText("Reverse Colors");
    menuItemReverse.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        actionReverseColors.run();
      }
    });

    MenuItem menuItemNoEdit = new MenuItem(menu, SWT.RADIO);
    menuItemNoEdit.setText("Non-Editable");
    menuItemNoEdit.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        actionNonEditable.run();
      }
    });

    MenuItem menuItemCircular = new MenuItem(menu, SWT.RADIO);
    menuItemCircular.setText("Circular Shift");
    menuItemCircular.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        actionCircularShift.run();
      }
    });

    MenuItem menuItemMarker = new MenuItem(menu, SWT.RADIO);
    menuItemMarker.setText("Marker");
    menuItemMarker.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        actionMarkerEdit.run();
      }
    });
  }

  /**
   * Gets the color bar canvas component.
   * 
   * @return the color bar canvas component.
   */
  public Canvas getCanvas() {
    return _canvas;
  }

  @Override
  public void mouseDoubleClick(final MouseEvent event) {
    // No action.
  }

  public void mouseDown(final MouseEvent event) {
    _xStart = event.x;
    _yStart = event.y;

    // Create a copy of the color map to use later ...
    _colorbarBuf = new ColorBar(_colorbar);
  }

  public void mouseUp(final MouseEvent event) {
    // trash these variables so we don't use them accidentally ...
    _xStart = Integer.MAX_VALUE;
    _yStart = Integer.MAX_VALUE;
    _xMotion = Integer.MAX_VALUE;
    _yMotion = Integer.MAX_VALUE;

    // We have finished with the buffered color map so null it out so
    // we can't ever accidentally use it in invalid state.
    _colorbarBuf = null;
  }

  public void mouseMove(final MouseEvent event) {
    if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
      mouseDragged(event);
      return;
    }
    if (_editMode == ColorBar.Mode.MARKER) {
      int mcell = _canvas.getColorCell(event.y);
      if (mcell == _upperMarker) {
        updateCursor(SWT.CURSOR_SIZEN);
        setMarkerMode(ColorBar.MarkerMode.EXTEND_UP);
      } else if (mcell == _lowerMarker) {
        updateCursor(SWT.CURSOR_SIZES);
        setMarkerMode(ColorBar.MarkerMode.EXTEND_DOWN);
      } else if (mcell > _lowerMarker && mcell < _upperMarker) {
        updateCursor(SWT.CURSOR_HAND);
        setMarkerMode(ColorBar.MarkerMode.MOTION);
      } else {
        updateCursor(SWT.CURSOR_HAND);
        setMarkerMode(ColorBar.MarkerMode.MOTION);
      }
    }
  }

  /**
   * Change the colors depending on the edit and marker modes.
   *
   * @param event the mouse event to handle.
   */
  public void mouseDragged(final MouseEvent event) {

    if (_xStart == Integer.MAX_VALUE || _yStart == Integer.MAX_VALUE) {
      return;
    }

    _xMotion = event.x;
    _yMotion = event.y;

    // Map from pixel locations into color cells.
    final int scell = _canvas.getColorCell(_yStart);
    final int mcell = _canvas.getColorCell(_yMotion);
    int shift = mcell - scell;
    //if (_canvas.getDirection().equals(Direction.START_AT_TOP)) {
    //  shift *= -1;
    //}

    switch (_editMode) {
      case NONE:
        break;

      case CIRCULAR:

        for (int cell = 0; cell < _colorbar.getNumColors(); cell++) {
          int pcell = cell + shift;
          // Transform pcell into a usuable array index 0 < pcell < numColors.
          pcell = MathUtil.mod(pcell, _colorbar.getNumColors());
          _colorbar.setColor(pcell, _colorbarBuf.getColor(cell), false);
          _colormap.setColor(pcell, _colorbarBuf.getColor(cell), false);
        }
        break;

      case MARKER:

        int idiff = _upperMarker - _lowerMarker;
        assert idiff > 0 : "-ve idiff: " + idiff;
        switch (_markerMode) {
          case MOTION:
            if (mcell <= idiff) {
              _lowerMarker = 0;
              _upperMarker = idiff;
            } else {
              _upperMarker = mcell;
              _lowerMarker = _upperMarker - idiff;
            }
            break;
          case EXTEND_UP:
            _upperMarker = mcell;
            if (_upperMarker < _lowerMarker) {
              _upperMarker = _lowerMarker; // _colorbar.getNumColors() - 1;
            }
            break;
          case EXTEND_DOWN:
            _lowerMarker = mcell;
            if (_lowerMarker > _upperMarker) {
              _lowerMarker = _upperMarker;
            }
            break;
          default:
            throw new RuntimeException("Unrecognized marker mode: " + _markerMode);
        }
        for (int ndx = 0; ndx < _colorbar.getNumColors(); ndx++) {
          _colorbar.setColor(ndx, getColor(ndx), false);
        }
        break;

      default:
        throw new RuntimeException("Unrecognized edit mode: " + _editMode);
    }
    redraw();
    update();
    _canvas.redraw();
    _canvas.update();
    _colorbar.updated();
  }

  public void mouseEnter(final MouseEvent e) {
    // No action.
  }

  public void mouseExit(final MouseEvent e) {
    // No action.
  }

  public void mouseHover(final MouseEvent e) {
    // No action.
  }

  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getNewValue() instanceof RGB) {
      _colorMarker = (RGB) event.getNewValue();
      for (int ndx = 0; ndx < _colorbar.getNumColors(); ndx++) {
        _colorbar.setColor(ndx, getColor(ndx));
      }
      redraw();
      update();
      _colorbar.updated();
    }
  }

  public void adapt(final FormToolkit toolkit) {
    toolkit.adapt(this);
    toolkit.adapt(_canvas);
    toolkit.adapt(_canvas.getShell());
    toolkit.adapt(_colorsMenu.getParent());
    toolkit.adapt(_editMenu.getParent());
    toolkit.adapt(_lblStart, true, true);
    toolkit.adapt(_lblEnd, true, true);
    toolkit.adapt(_txtStart, true, true);
    toolkit.adapt(_txtEnd, true, true);
    toolkit.adapt(_optMarkerColor.getButton(), true, true);
    _canvas.setSize(400, 800);
  }

  /**
   * Reverses the array of colors in the colorbar being edited.
   */
  public void reverseColors() {
    RGB[] colors = _colorbar.getColors();
    int numColors = colors.length;
    for (int i = 0; i < numColors / 2; i++) {
      RGB rgb = colors[i];
      colors[i] = colors[numColors - 1 - i];
      colors[numColors - 1 - i] = rgb;
    }
    setColors(colors);
    // Set this if the since the color map has changed
    _colorMapChanged = true;
  }

  public boolean colorMapChanged() {
    return _colorMapChanged;
  }
}
