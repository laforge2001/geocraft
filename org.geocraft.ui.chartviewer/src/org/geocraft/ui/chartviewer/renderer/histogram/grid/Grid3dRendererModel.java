/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.grid;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorUtil;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * The model of rendering properties for a <code>Grid3d</code> entity
 * in the histogram view.
 */
public class Grid3dRendererModel extends Model {

  /** Enumeration of the types of data bounds to use for 3D grid histograms. */
  public enum DataBounds {
    /** Uses the entire grid in the histogram. */
    USE_ALL_DATA("Use all data"),
    /** Uses only the data contained in an area-of-interest. */
    USE_AOI("Use area of interest");

    private String _text;

    DataBounds(String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  /** The key for the color property. */
  public static final String COLOR = "Color";

  /** The key for the # of cells property. */
  public static final String NUMBER_OF_CELLS = "# of Cells";

  /** The key for the data bounds property. */
  public static final String DATA_BOUNDS = "Data Bounds";

  /** The key for the area-of-interest property. */
  public static final String AREA_OF_INTEREST = "Area of Interest";

  /** The property for the histogram color. */
  private ColorProperty _color;

  /** The property for the # of histogram cells. */
  private IntegerProperty _numCells;

  /** The property for the bounds of the data to histogram. */
  private EnumProperty<DataBounds> _dataBounds;

  /** The property for the area-of-interest to histogram. */
  private EntityProperty<AreaOfInterest> _aoi;

  public Grid3dRendererModel() {
    super();
    _color = addColorProperty(COLOR, ColorUtil.getCommonRGB());
    _numCells = addIntegerProperty(NUMBER_OF_CELLS, 100);
    _dataBounds = addEnumProperty(DATA_BOUNDS, DataBounds.class, DataBounds.USE_ALL_DATA);
    _aoi = addEntityProperty(AREA_OF_INTEREST, AreaOfInterest.class);
  }

  public Grid3dRendererModel(Grid3dRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(IValidation results) {
    if (_numCells.get() < 2) {
      results.error(_numCells, "Minimum of 2 cells required for histogram.");
    }
    if (_dataBounds.isNull()) {
      results.error(_dataBounds, "No region specified.");
    } else {
      if (_dataBounds.get() == DataBounds.USE_AOI && _aoi.isNull()) {
        results.error(_aoi, "No area of interest specified.");
      }
    }
  }

  /**
   * Returns the histogram color to use.
   */
  public RGB getColor() {
    return _color.get();
  }

  /**
   * Sets the histogram color to use.
   */
  public void setColor(RGB color) {
    _color.set(color);
  }

  /**
   * Returns the # of histogram cells to use.
   */
  public int getNumCells() {
    return _numCells.get();
  }

  /**
   * Returns the type of data bounds to use for the histogram.
   */
  public DataBounds getDataBounds() {
    return _dataBounds.get();
  }

  /**
   * Returns the area-of-interest to use (optional).
   */
  public AreaOfInterest getAOI() {
    return _aoi.get();
  }
}
