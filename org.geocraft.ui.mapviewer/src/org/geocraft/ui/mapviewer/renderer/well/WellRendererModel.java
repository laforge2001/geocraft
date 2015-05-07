/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class WellRendererModel extends Model implements IWellRendererConstants {

  private final EnumProperty<LineStyle> _boreLineStyle;

  private final IntegerProperty _boreLineWidth;

  private final ColorProperty _boreLineColor;

  private final EnumProperty<PointStyle> _topHoleSymbol;

  private final IntegerProperty _topHoleSymbolSize;

  private final ColorProperty _topHoleSymbolColor;

  private final EnumProperty<PointStyle> _bottomHoleSymbol;

  private final IntegerProperty _bottomHoleSymbolSize;

  private final ColorProperty _bottomHoleSymbolColor;

  public WellRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    String boreLineStyleStr = preferences.getString(BORE_LINE_STYLE);
    LineStyle boreLineStyle = LineStyle.lookup(boreLineStyleStr);
    int boreLineWidth = preferences.getInt(BORE_LINE_WIDTH);
    RGB boreLineColor = PreferenceConverter.getColor(preferences, BORE_LINE_COLOR);

    String topSymbolStr = preferences.getString(TOP_HOLE_SYMBOL);
    PointStyle topSymbol = PointStyle.lookup(topSymbolStr);
    int topSymbolSize = preferences.getInt(TOP_HOLE_SYMBOL_SIZE);
    RGB topSymbolColor = PreferenceConverter.getColor(preferences, TOP_HOLE_SYMBOL_COLOR);

    String bottomSymbolStr = preferences.getString(BOTTOM_HOLE_SYMBOL);
    PointStyle bottomSymbol = PointStyle.lookup(bottomSymbolStr);
    int bottomSymbolSize = preferences.getInt(BOTTOM_HOLE_SYMBOL_SIZE);
    RGB bottomSymbolColor = PreferenceConverter.getColor(preferences, BOTTOM_HOLE_SYMBOL_COLOR);

    _boreLineStyle = addEnumProperty(BORE_LINE_STYLE, LineStyle.class, boreLineStyle);
    _boreLineWidth = addIntegerProperty(BORE_LINE_WIDTH, boreLineWidth);
    _boreLineColor = addColorProperty(BORE_LINE_COLOR, boreLineColor);
    _topHoleSymbol = addEnumProperty(TOP_HOLE_SYMBOL, PointStyle.class, topSymbol);
    _topHoleSymbolSize = addIntegerProperty(TOP_HOLE_SYMBOL_SIZE, topSymbolSize);
    _topHoleSymbolColor = addColorProperty(TOP_HOLE_SYMBOL_COLOR, topSymbolColor);
    _bottomHoleSymbol = addEnumProperty(BOTTOM_HOLE_SYMBOL, PointStyle.class, bottomSymbol);
    _bottomHoleSymbolSize = addIntegerProperty(BOTTOM_HOLE_SYMBOL_SIZE, bottomSymbolSize);
    _bottomHoleSymbolColor = addColorProperty(BOTTOM_HOLE_SYMBOL_COLOR, bottomSymbolColor);
  }

  public WellRendererModel(final WellRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_boreLineStyle.isNull()) {
      results.error(BORE_LINE_STYLE, "Bore line style not specified.");
    }
    if (_boreLineWidth.get() < 0) {
      results.error(BORE_LINE_WIDTH, "Bore line width must be >= 0.");
    }
    if (_boreLineColor.isNull()) {
      results.error(BORE_LINE_COLOR, "Bore line color not specified.");
    }
    if (_topHoleSymbol.isNull()) {
      results.error(TOP_HOLE_SYMBOL, "Top hole symbol not specified.");
    }
    if (_topHoleSymbolSize.get() < 0) {
      results.error(TOP_HOLE_SYMBOL_SIZE, "Top hole symbol size must be >= 0.");
    }
    if (_topHoleSymbolColor.isNull()) {
      results.error(TOP_HOLE_SYMBOL_COLOR, "Top hole symbol color not specified.");
    }
    if (_bottomHoleSymbol.isNull()) {
      results.error(BOTTOM_HOLE_SYMBOL, "Bottom hole symbol not specified.");
    }
    if (_bottomHoleSymbolSize.get() < 0) {
      results.error(BOTTOM_HOLE_SYMBOL_SIZE, "Bottom hole symbol size must be >= 0.");
    }
    if (_bottomHoleSymbolColor.isNull()) {
      results.error(BOTTOM_HOLE_SYMBOL_COLOR, "Bottom hole symbol color not specified.");
    }
  }

  public LineStyle getBoreLineStyle() {
    return _boreLineStyle.get();
  }

  public int getBoreLineWidth() {
    return _boreLineWidth.get();
  }

  public RGB getBoreLineColor() {
    return _boreLineColor.get();
  }

  public PointStyle getTopHoleSymbol() {
    return _topHoleSymbol.get();
  }

  public int getTopHoleSymbolSize() {
    return _topHoleSymbolSize.get();
  }

  public RGB getTopHoleSymbolColor() {
    return _topHoleSymbolColor.get();
  }

  public PointStyle getBottomHoleSymbol() {
    return _bottomHoleSymbol.get();
  }

  public int getBottomHoleSymbolSize() {
    return _bottomHoleSymbolSize.get();
  }

  public RGB getBottomHoleSymbolColor() {
    return _bottomHoleSymbolColor.get();
  }

  public LineProperties getBoreLineProperties() {
    return new LineProperties(getBoreLineStyle(), getBoreLineColor(), getBoreLineWidth());
  }

}
