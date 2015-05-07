/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import java.awt.Toolkit;
import java.util.Map;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.listener.IModelSpaceListener;
import org.geocraft.ui.plot.model.IModelSpace;


/**
 * The model of section viewer display properties.
 * @author hansegj
 *
 */
public class SectionViewerModel extends Model implements IModelSpaceListener {

  /** The property key for the screen resolution */
  public static final String SCREEN_RESOLUTION = "Screen Resolution";

  /** The property key for the horizontal scale */
  public static final String HORIZONTAL_SCALE = "Horizontal Scale";

  /** The property key for the vertical scale */
  public static final String VERTICAL_SCALE = "Vertical Scale";

  /** The property key for the reference dataset */
  public static final String REFERENCE_DATASET = "Reference Dataset";

  /** The property key for the X axis range of the viewable bounds */
  public static final String XAXIS_RANGE = "X Axis Range";

  /** The property key for the Y axis range of the viewable bounds */
  public static final String YAXIS_RANGE = "Y Axis Range";

  /** The property key for the X axis default range of the viewable bounds */
  public static final String XAXIS_DEFAULT_RANGE = "X Axis Default Range";

  /** The property key for the Y axis default range of the viewable bounds */
  public static final String YAXIS_DEFAULT_RANGE = "Y Axis Default Range";

  public static final String SELECTED_Z_RANGE = "Selected Z Range";

  /** The property key for the viewer's TraceSection */
  public static final String TRACE_SECTION = "Trace Section";

  /** The screen resolution in dots-per-inch. */
  private final FloatProperty _screenResolution;

  /** The horizontal scale in traces-per-inch. */
  private final FloatProperty _tracesPerInch;

  /** The vertical scale in inches-per-second. */
  private final FloatProperty _inchesPerSec;

  /** The reference seismic dataset, which controls the navigation. */
  private final EntityProperty _referenceDataset;

  /** The X axis range of the viewable bounds */
  private final StringProperty _xaxisRange;

  private AxisRange _xAxisRange;

  /** The Y axis range of the vieweable bounds */
  private final StringProperty _yaxisRange;

  private AxisRange _yAxisRange;

  /** The default X axis range of the viewable bounds */
  private final StringProperty _xaxisDefaultRange;

  private AxisRange _xAxisDefaultRange;

  /** The default Y axis range of the vieweable bounds */
  private final StringProperty _yaxisDefaultRange;

  private AxisRange _yAxisDefaultRange;

  /** The selected z-range to display. */
  private final StringProperty _zRangeSelected;

  private FloatRange _selectedZRange;

  /** The viewer's TraceSection, a set of trace display properties*/
  private final StringProperty _traceSection;

  private TraceSection _tSection;

  IModelSpace _modelSpace;

  public SectionViewerModel(final IModelSpace mspace) {
    _screenResolution = addFloatProperty(SCREEN_RESOLUTION, Toolkit.getDefaultToolkit().getScreenResolution());
    _tracesPerInch = addFloatProperty(HORIZONTAL_SCALE, 100);
    _inchesPerSec = addFloatProperty(VERTICAL_SCALE, 2);
    _referenceDataset = addEntityProperty(REFERENCE_DATASET, SeismicDataset.class);
    _xaxisRange = addStringProperty(XAXIS_RANGE, "");
    _xAxisRange = new AxisRange(1.0, 1.0);
    _yaxisRange = addStringProperty(YAXIS_RANGE, "");
    _yAxisRange = new AxisRange(1.0, 1.0);
    _xaxisDefaultRange = addStringProperty(XAXIS_DEFAULT_RANGE, "");
    _xAxisDefaultRange = new AxisRange(1.0, 1.0);
    _yaxisDefaultRange = addStringProperty(YAXIS_DEFAULT_RANGE, "");
    _yAxisDefaultRange = new AxisRange(1.0, 1.0);
    _zRangeSelected = addStringProperty(SELECTED_Z_RANGE, "");
    _selectedZRange = new FloatRange(1.0f, 1.0f, 1.0f);
    _modelSpace = mspace;
    _modelSpace.addListener(this);
    _traceSection = addStringProperty(TRACE_SECTION, "");
    _tSection = null;
  }

  /**
   * Get the screen resolution in dots-per-inch
   * @return Screen resolution
   */
  public float getScreenResolution() {
    return _screenResolution.get();
  }

  /**
   * Set the screen resolution
   * @param resolution Screen resolution in dots-per-inch
   */
  public void setScreenResolution(final float resolution) {
    _screenResolution.set(resolution);
  }

  /**
   * Get the horizontal scale in traces-per-inch
   * @return The horizontal scale
   */
  public float getHorizontalScale() {
    return _tracesPerInch.get();
  }

  /**
   * Set the horizontal scale in traces-per-inch
   * @param hscale The horizontal scale
   */
  public void setHorizontalScale(final float hscale) {
    _tracesPerInch.set(hscale);
  }

  /**
   * Get the vertical scale in inches-per-second
   * @return The vertical scale
   */
  public float getVerticalScale() {
    return _inchesPerSec.get();
  }

  /**
   * Set the vertical scale in inches-per-second
   * @param vscale The vertical scale
   */
  public void setVerticalScale(final float vscale) {
    _inchesPerSec.set(vscale);
  }

  /**
   * Get the reference dataset
   * @return The reference dataset
   */
  public SeismicDataset getReferenceDataset() {
    return (SeismicDataset) _referenceDataset.get();
  }

  /**
   * Set the reference dataset
   * @param entity The seismic reference dataset
   */
  public void setReferenceDataset(final Entity entity) {
    _referenceDataset.set(entity);
  }

  public AxisRange getXAxisRange() {
    return _xAxisRange;
  }

  /**
   * Set the X axis range of the viewable bounds
   * @param xrange The X axis range
   */
  public void setXAxisRange(final AxisRange xrange) {
    _xAxisRange = xrange;
    _xaxisRange.set(xrange.toString());
  }

  public AxisRange getYAxisRange() {
    return _yAxisRange;
  }

  /**
   * Set the Y axis range of the viewable bounds
   * @param yrange The Y axis range
   */
  public void setYAxisRange(final AxisRange yrange) {
    _yAxisRange = yrange;
    _yaxisRange.set(yrange.toString());
  }

  public AxisRange getXAxisDefaultRange() {
    return _xAxisDefaultRange;
  }

  /**
   * Set the X axis default range of the viewable bounds
   * @param xrange The X axis default range
   */
  public void setXAxisDefaultRange(final AxisRange xrange) {
    _xAxisDefaultRange = xrange;
    _xaxisDefaultRange.set(xrange.toString());
  }

  public AxisRange getYAxisDefaultRange() {
    return _yAxisDefaultRange;
  }

  /**
   * Set the Y axis default range of the viewable bounds
   * @param yrange The Y axis default range
   */
  public void setYAxisDefaultRange(final AxisRange yrange) {
    _yAxisDefaultRange = yrange;
    _yaxisDefaultRange.set(yrange.toString());
  }

  public FloatRange getSelectedZRange() {
    return _selectedZRange;
  }

  public void setSelectedZRange(final FloatRange zrange) {
    _selectedZRange = zrange;
    _zRangeSelected.set(zrange.toString());
  }

  public TraceSection getTraceSection() {
    return _tSection;
  }

  public void setTraceSection(final TraceSection tSection) {
    _tSection = tSection;
    if (_tSection != null) {
      _traceSection.set(tSection.toString());
    }
  }

  public Domain getReferenceZDomain() {
    return getReferenceDataset().getZDomain();
  }

  public SeismicSurvey2d getReferenceSurvey2d() {
    SeismicDataset dataset = getReferenceDataset();
    return dataset instanceof PostStack2dLine ? ((PostStack2dLine) dataset).getSurvey() : null;
  }

  public SeismicSurvey3d getReferenceSurvey3d() {
    SeismicDataset dataset = getReferenceDataset();
    if (dataset instanceof PostStack3d) {
      return ((PostStack3d) dataset).getSurvey();
    }
    if (dataset instanceof PreStack3d) {
      return ((PreStack3d) dataset).getSurvey();
    }
    return null;
  }

  /**
   * Parse an axis range of the form "start, end"
   * @param range String representation of the axis range
   * @return An AxisRange
   */
  private AxisRange parseAxisRange(final String range) {
    float start = 1.0f, end = 1.0f;
    if (range.equals("")) {
      return new AxisRange(start, end);
    }
    String[] bounds = range.split(",");
    try {
      start = Float.parseFloat(bounds[0]);
      end = Float.parseFloat(bounds[1]);
    } catch (NumberFormatException nfe) {
      //
    }
    return new AxisRange(start, end);
  }

  private float parseFloatBound(final String bound) {
    float val = 0.0f;
    try {
      val = Float.parseFloat(bound);
    } catch (NumberFormatException nfe) {
      val = 1.0f;
    }
    return val;
  }

  /**
   * Parse a trace axis range of the form [start, end, step]
   * @param range String representation of the trace axis range
   * @return A FloatRange
   */
  private FloatRange parseFloatRange(final String range) {
    if (range.equals("")) {
      return new FloatRange(1.0f, 1.0f, 1.0f);
    }
    FloatRange val = null;
    try {
      String list = range.substring(range.indexOf('[') + 1, range.indexOf(']'));
      String[] axisRange = list.split(",");
      val = new FloatRange(Float.parseFloat(axisRange[0]), Float.parseFloat(axisRange[1]),
          Float.parseFloat(axisRange[2]));
    } catch (NumberFormatException nfe) {
      val = new FloatRange(1.0f, 1.0f, 1.0f);
    }
    return val;
  }

  @Override
  public void unpickle(final Map<String, String> parms) {
    super.unpickle(parms);

    for (String key : parms.keySet()) {
      String pval = parms.get(key);
      if (key.equals(XAXIS_RANGE)) {
        _xAxisRange = parseAxisRange(pval);
      } else if (key.equals(YAXIS_RANGE)) {
        _yAxisRange = parseAxisRange(pval);
      } else if (key.equals(XAXIS_DEFAULT_RANGE)) {
        _xAxisDefaultRange = parseAxisRange(pval);
      } else if (key.equals(YAXIS_DEFAULT_RANGE)) {
        _yAxisDefaultRange = parseAxisRange(pval);
      } else if (key.equals(SELECTED_Z_RANGE)) {
        _selectedZRange = parseFloatRange(pval);
      } else if (key.equals(TRACE_SECTION) && !pval.equals("")) {
        String[] props = pval.split(";");

        SectionType tsType = SectionType.INLINE_SECTION;
        boolean is2D = false;
        Domain domain = Domain.TIME;
        float startZ = 0.0f, endZ = 1.0f;
        TraceAxisKey[] traceAxisKeys = null;
        FloatRange[] traceAxisKeyRanges = null;
        SeismicLine2d seismicLine = null;
        for (String prop : props) {
          int idx = prop.indexOf(' ');
          String key2 = prop.substring(0, idx);
          String val = prop.substring(idx + 1);
          if (key2.equals(TraceSection.TYPE)) {
            tsType = SectionType.fromString(val);
          } else if (key2.equals(TraceSection.DOMAIN)) {
            domain = Domain.fromString(val);
          } else if (key2.equals(TraceSection.START_Z)) {
            startZ = parseFloatBound(val);
          } else if (key2.equals(TraceSection.END_Z)) {
            endZ = parseFloatBound(val);
          } else if (key2.equals(TraceSection.TRACE_AXIS_KEYS)) {
            String list = val.substring(val.indexOf('[') + 1, val.indexOf(']'));
            String[] keys = list.split(",");
            traceAxisKeys = new TraceAxisKey[keys.length];
            for (int i = 0; i < keys.length; i++) {
              traceAxisKeys[i] = TraceAxisKey.fromString(keys[i]);
            }
          } else if (key2.equals(TraceSection.TRACE_AXIS_KEY_RANGES)) {
            String list = val.substring(val.indexOf('[') + 1, val.lastIndexOf(']'));
            String[] keyRanges = list.split(":");
            traceAxisKeyRanges = new FloatRange[keyRanges.length];
            for (int i = 0; i < keyRanges.length; i++) {
              traceAxisKeyRanges[i] = parseFloatRange(keyRanges[i]);
            }
          } else if (key2.equals(TraceSection.IS_2D)) {
            is2D = val.equals("true") ? true : false;
          } else if (key2.equals(TraceSection.SEISMIC_LINE)) {
            if (!val.equals("null")) {
              //Note: val is the name of the SeismicLine2d's
              //seismicLine = (SeismicLine2d) ServiceProvider.getRepository().get(val);
              //Get the SeismicLine2d from the reference dataset
              Entity entity = (Entity) _referenceDataset.get();
              seismicLine = ((PostStack2dLine) entity).getSurvey().getLineByName(val);
            }
          }
        }
        if (tsType != SectionType.IRREGULAR) {
          if (is2D) {
            _tSection = new TraceSection(tsType, seismicLine, traceAxisKeys, traceAxisKeyRanges, domain, startZ, endZ);
          } else {
            //Note: Assume SeismicSurvey3d obtainable from reference dataset (PostStack3d, PreStack3d)
            SeismicDataset dataset3d = (SeismicDataset) _referenceDataset.get();
            SeismicSurvey3d geometry = null;
            if (dataset3d instanceof PostStack3d) {
              geometry = ((PostStack3d) dataset3d).getSurvey();
            }
            if (dataset3d instanceof PreStack3d) {
              geometry = ((PreStack3d) dataset3d).getSurvey();
            }
            if (geometry != null) {
              _tSection = new TraceSection(tsType, geometry, traceAxisKeys, traceAxisKeyRanges, domain, startZ, endZ);
            }
          }
        } else {
          //TODO handle irregular trace section
        }
      }
    }
    //reset the model space
    //Note: Do not set viewable and default bounds separately
    _modelSpace.setDefaultAndViewableBounds(_xAxisDefaultRange.getStart(), _xAxisDefaultRange.getEnd(),
        _yAxisDefaultRange.getStart(), _yAxisDefaultRange.getEnd(), _xAxisRange.getStart(), _xAxisRange.getEnd(),
        _yAxisRange.getStart(), _yAxisRange.getEnd());
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModel#validate(org.geocraft.core.model.validation.IValidation)
   */
  @Override
  public void validate(final IValidation results) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.plot.listener.IModelSpaceListener#modelSpaceUpdated(org.geocraft.ui.plot.event.ModelSpaceEvent)
   */
  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent modelEvent) {
    setXAxisRange(modelEvent.getModelSpace().getViewableBounds().getRangeX());
    setYAxisRange(modelEvent.getModelSpace().getViewableBounds().getRangeY());
    setXAxisDefaultRange(modelEvent.getModelSpace().getDefaultBounds().getRangeX());
    setYAxisDefaultRange(modelEvent.getModelSpace().getDefaultBounds().getRangeY());
  }

}
