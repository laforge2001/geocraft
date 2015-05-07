package org.geocraft.io.javaseis;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.HeaderEntry.Format;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.javaseis.grid.GridDefinition;
import org.javaseis.grid.IBinGrid;
import org.javaseis.properties.AxisLabel;
import org.javaseis.properties.DataDomain;
import org.javaseis.properties.PropertyDescription;
import org.javaseis.properties.TraceProperties;


public class JavaSeisUtil {

  public static final String JS_INLINE_NO_LABEL = "ILINE_NO";

  public static final String JS_XLINE_NO_LABEL = "XLINE_NO";

  public static final String JS_INLINE_NO_DESCR = "3D inline number";

  public static final String JS_XLINE_NO_DESCR = "3D crossline number";

  public static final String JS_CDP_X_LABEL = "CDP_X";

  public static final String JS_CDP_X_DESCR = "X coordinate of CDP";

  public static final String JS_CDP_Y_LABEL = "CDP_Y";

  public static final String JS_CDP_Y_DESCR = "Y coordinate of CDP";

  public static final String JS_CDP_XD_LABEL = "CDP_XD";

  public static final String JS_CDP_XD_DESCR = "X coordinate of CDP (double)";

  public static final String JS_CDP_YD_LABEL = "CDP_YD";

  public static final String JS_CDP_YD_DESCR = "Y coordinate of CDP (double)";

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(JavaSeisUtil.class);

  /** The application preferences */
  //  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /**
   * Returns <i>true</i> if the JavaSeis file exists on disk; <i>false</i> if not.
   * 
   * @param filePath the full file path of the JavaSeis file to check.
   * @return <i>true</i> if the JavaSeis file exists on disk; <i>false</i> if not.
   */
  public static boolean existsInStore(final String filePath) {
    File file = new File(filePath);
    return file.exists();
  }

  public static int logicalToIndex(final GridDefinition gridDef, final int whichAxis, final float logicalCoordinate) {
    float delta = findLogicalDelta(gridDef, whichAxis);
    float result = (logicalCoordinate - gridDef.getAxisLogicalOrigin(whichAxis) + delta / 2) / delta;
    int retval = (int) result;
    return retval;
  }

  public static long findLogicalDelta(final GridDefinition gridDef, final int whichAxis) {
    return gridDef.getAxisLogicalDelta(whichAxis);
  }

  public static int getOffsetAxis(final PreStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_OFFSET_Z:
      case XLINE_INLINE_OFFSET_Z:
        retval = 1;
        break;
      case INLINE_OFFSET_XLINE_Z:
      case XLINE_OFFSET_INLINE_Z:
        retval = 2;
        break;
      case OFFSET_INLINE_XLINE_Z:
      case OFFSET_XLINE_INLINE_Z:
        retval = 3;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getCrosslineAxis(final PreStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case INLINE_OFFSET_XLINE_Z:
      case OFFSET_INLINE_XLINE_Z:
        retval = 1;
        break;
      case INLINE_XLINE_OFFSET_Z:
      case OFFSET_XLINE_INLINE_Z:
        retval = 2;
        break;
      case XLINE_INLINE_OFFSET_Z:
      case XLINE_OFFSET_INLINE_Z:
        retval = 3;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getInlineAxis(final PreStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case XLINE_OFFSET_INLINE_Z:
      case OFFSET_XLINE_INLINE_Z:
        retval = 1;
        break;
      case INLINE_OFFSET_XLINE_Z:
      case INLINE_XLINE_OFFSET_Z:
        retval = 3;
        break;
      case XLINE_INLINE_OFFSET_Z:
      case OFFSET_INLINE_XLINE_Z:
        retval = 2;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getZAxis(final PreStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case XLINE_OFFSET_INLINE_Z:
      case OFFSET_XLINE_INLINE_Z:
      case INLINE_OFFSET_XLINE_Z:
      case INLINE_XLINE_OFFSET_Z:
      case XLINE_INLINE_OFFSET_Z:
      case OFFSET_INLINE_XLINE_Z:
        retval = 0;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getCrosslineAxis(final PostStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_Z:
      case Z_XLINE_INLINE:
        retval = 1;
        break;
      case XLINE_INLINE_Z:
        retval = 2;
        break;
      case Z_INLINE_XLINE:
        retval = 0;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getInlineAxis(final PostStack3d.StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_Z:
        retval = 2;
        break;
      case XLINE_INLINE_Z:
      case Z_INLINE_XLINE:
        retval = 1;
        break;
      case Z_XLINE_INLINE:
        retval = 0;
        break;
      default:
        assert false : "Invalid storage order.";
    }
    return retval;
  }

  public static int getZAxis(final StorageOrder storageOrder) {
    int retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_Z:
      case XLINE_INLINE_Z:
        retval = 0;
        break;
      case Z_INLINE_XLINE:
      case Z_XLINE_INLINE:
        retval = 2;
        break;
      default:
        assert false : "Invalid storage order";
    }
    return retval;
  }

  public static double findPhysicalDelta(final GridDefinition gridDef, final int whichAxis) {
    return gridDef.getAxisPhysicalDelta(whichAxis);
  }

  public static long findLogicalCoordinate(final GridDefinition gridDef, final int whichAxis, final int index) {
    // translate index to logicalCoordinate coordinate
    long retval = index * gridDef.getAxisLogicalDelta(whichAxis) + gridDef.getAxisLogicalOrigin(whichAxis);
    return retval;
  }

  public static long findCrosslineLogicalCoordinate(final GridDefinition gridDef, final int crosslineIndex,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalCoordinate(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineIndex);
  }

  public static long findInlineLogicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalCoordinate(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineIndex);
  }

  public static long findCrosslineLogicalCoordinate(final GridDefinition gridDef, final int crosslineIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalCoordinate(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineIndex);
  }

  public static long findInlineLogicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalCoordinate(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineIndex);
  }

  public static long findOffsetLogicalCoordinate(final GridDefinition gridDef, final int offsetIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalCoordinate(gridDef, JavaSeisUtil.getOffsetAxis(storageOrder), offsetIndex);
  }

  public static int findCrosslineIndex(final GridDefinition gridDef, final float crosslineLogicalCoordinate,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil
        .logicalToIndex(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineLogicalCoordinate);
  }

  public static int findInlineIndex(final GridDefinition gridDef, final float inlineLogicalCoordinate,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.logicalToIndex(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineLogicalCoordinate);
  }

  public static int findCrosslineIndex(final GridDefinition gridDef, final float crosslineLogicalCoordinate,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil
        .logicalToIndex(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineLogicalCoordinate);
  }

  public static int findInlineIndex(final GridDefinition gridDef, final float inlineLogicalCoordinate,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.logicalToIndex(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineLogicalCoordinate);
  }

  public static int findOffsetIndex(final GridDefinition gridDef, final float offsetPhysicalCoordinate,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.physicalToIndex(gridDef, JavaSeisUtil.getOffsetAxis(storageOrder), offsetPhysicalCoordinate);
  }

  public static int findZIndex(final GridDefinition gridDef, final double zPhysicalCoordinate,
      final PostStack3d.StorageOrder storageOrder) {
    return physicalToIndex(gridDef, JavaSeisUtil.getZAxis(storageOrder), zPhysicalCoordinate);
  }

  public static int findZIndex(final GridDefinition gridDef, final double zPhysicalCoordinate,
      final PreStack3d.StorageOrder storageOrder) {
    return physicalToIndex(gridDef, JavaSeisUtil.getZAxis(storageOrder), zPhysicalCoordinate);
  }

  public static double findPhysicalCoordinate(final GridDefinition gridDef, final int whichAxis, final int index) {
    // translate index to logicalCoordinate coordinate
    double retval = index * gridDef.getAxisPhysicalDelta(whichAxis) + gridDef.getAxisPhysicalOrigin(whichAxis);
    return retval;
  }

  public static double findZPhysicalCoordinate(final GridDefinition gridDef, final int zIndex,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getZAxis(storageOrder), zIndex);
  }

  public static double findZPhysicalCoordinate(final GridDefinition gridDef, final int zIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getZAxis(storageOrder), zIndex);
  }

  public static double findCrosslinePhysicalCoordinate(final GridDefinition gridDef, final int crosslineIndex,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineIndex);
  }

  public static double findInlinePhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineIndex);
  }

  public static double findCrosslinePhysicalCoordinate(final GridDefinition gridDef, final int crosslineIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder), crosslineIndex);
  }

  public static double findInlinePhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getInlineAxis(storageOrder), inlineIndex);
  }

  public static double findOffsetPhysicalCoordinate(final GridDefinition gridDef, final int offsetIndex,
      final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalCoordinate(gridDef, JavaSeisUtil.getOffsetAxis(storageOrder), offsetIndex);
  }

  public static long findCrosslineLogicalDelta(final GridDefinition gridDef, final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalDelta(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder));
  }

  public static long findInlineLogicalDelta(final GridDefinition gridDef, final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalDelta(gridDef, JavaSeisUtil.getInlineAxis(storageOrder));
  }

  public static long findCrosslineLogicalDelta(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalDelta(gridDef, JavaSeisUtil.getCrosslineAxis(storageOrder));
  }

  public static long findInlineLogicalDelta(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalDelta(gridDef, JavaSeisUtil.getInlineAxis(storageOrder));
  }

  public static long findOffsetLogicalDelta(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findLogicalDelta(gridDef, JavaSeisUtil.getOffsetAxis(storageOrder));
  }

  public static double findZPhysicalDelta(final GridDefinition gridDef, final PostStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalDelta(gridDef, JavaSeisUtil.getZAxis(storageOrder));
  }

  public static double findZPhysicalDelta(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalDelta(gridDef, JavaSeisUtil.getZAxis(storageOrder));
  }

  public static double findOffsetPhysicalDelta(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    return JavaSeisUtil.findPhysicalDelta(gridDef, JavaSeisUtil.getOffsetAxis(storageOrder));
  }

  public static int[] orderPosition(final int zPosition, final int crosslinePosition, final int inlinePosition,
      final PostStack3d.StorageOrder storageOrder) {
    int[] retval = new int[3];
    retval[JavaSeisUtil.getZAxis(storageOrder)] = zPosition;
    retval[JavaSeisUtil.getCrosslineAxis(storageOrder)] = crosslinePosition;
    retval[JavaSeisUtil.getInlineAxis(storageOrder)] = inlinePosition;
    return retval;
  }

  public static int[] orderPosition(final int zPosition, final int offsetPosition, final int crosslinePosition,
      final int inlinePosition, final PreStack3d.StorageOrder storageOrder) {
    int[] retval = new int[4];
    retval[JavaSeisUtil.getZAxis(storageOrder)] = zPosition;
    retval[JavaSeisUtil.getCrosslineAxis(storageOrder)] = offsetPosition;
    retval[JavaSeisUtil.getCrosslineAxis(storageOrder)] = crosslinePosition;
    retval[JavaSeisUtil.getInlineAxis(storageOrder)] = inlinePosition;
    return retval;
  }

  /**
   * Returns the domain of the z axis.
   * 
   * @return the domain of the z axis.
   */
  public static Domain getZAxisDomain(final GridDefinition gridDef, final PostStack3d.StorageOrder storageOrder) {
    Domain retval = Domain.TIME;
    DataDomain domain = gridDef.getAxisDomain(JavaSeisUtil.getZAxis(storageOrder));
    if (domain.equals(DataDomain.get("time"))) {
      retval = Domain.TIME;
    } else if (domain.equals(DataDomain.get("depth"))) {
      retval = Domain.DISTANCE;
    } else if (domain.equals(DataDomain.get("space"))) {
      retval = Domain.DISTANCE;
    } else {
      throw new RuntimeException("Invalid domain type: " + domain);
    }
    return retval;
  }

  /**
   * Returns the domain of the z axis.
   * 
   * @return the domain of the z axis.
   */
  public static Domain getZAxisDomain(final GridDefinition gridDef, final PreStack3d.StorageOrder storageOrder) {
    Domain retval = Domain.TIME;
    DataDomain domain = gridDef.getAxisDomain(JavaSeisUtil.getZAxis(storageOrder));
    if (domain.equals(DataDomain.get("time"))) {
      retval = Domain.TIME;
    } else if (domain.equals(DataDomain.get("depth"))) {
      retval = Domain.DISTANCE;
    } else {
      throw new RuntimeException("Invalid domain type: " + domain);
    }
    return retval;
  }

  /**
   * Returns the corner points of a poststack3d volume.
   * 
   * @return the corner points of a poststack3d volume.
   */
  public static Point3d[] getCornerPoints(final GridDefinition gridDef, final IBinGrid binGrid, final Unit inlineUnit,
      final Unit xlineUnit, final PostStack3d.StorageOrder storageOrder, final Domain zDomain) {
    // TODO: Find transformation in future based on the X, Y values located in the 4 corner trace headers
    // TODO: Make the transformation parameters member variables that can be used to compute the
    // TODO: X & Y physical coordinates
    int[] lengths = JavaSeisUtil.getAxisLengthsForPostStack3d(gridDef);
    Point3d[] points = new Point3d[4];
    int inlineAxis = JavaSeisUtil.getInlineAxis(storageOrder);
    int xlineAxis = JavaSeisUtil.getCrosslineAxis(storageOrder);
    int inlineIndexEnd = lengths[inlineAxis] - 1;
    int xlineIndexEnd = lengths[xlineAxis] - 1;
    int[] inlineIndices = { 0, inlineIndexEnd, inlineIndexEnd, 0 };
    int[] xlineIndices = { 0, 0, xlineIndexEnd, xlineIndexEnd };
    double x = 0;
    double y = 0;
    long inlineLogicalOrigin = gridDef.getAxisLogicalOrigin(JavaSeisUtil.getInlineAxis(storageOrder));
    long inlineLogicalDelta = gridDef.getAxisLogicalDelta(JavaSeisUtil.getInlineAxis(storageOrder));
    long xlineLogicalOrigin = gridDef.getAxisLogicalOrigin(JavaSeisUtil.getCrosslineAxis(storageOrder));
    long xlineLogicalDelta = gridDef.getAxisLogicalDelta(JavaSeisUtil.getCrosslineAxis(storageOrder));
    long inlineLogicalEnd = inlineLogicalOrigin + inlineLogicalDelta * (lengths[inlineAxis] - 1);
    long xlineLogicalEnd = xlineLogicalOrigin + xlineLogicalDelta * (lengths[xlineAxis] - 1);
    long[] logicalX = new long[0];
    long[] logicalY = new long[0];
    logicalX = new long[] { inlineLogicalOrigin, inlineLogicalEnd, inlineLogicalEnd, inlineLogicalOrigin };
    logicalY = new long[] { xlineLogicalOrigin, xlineLogicalOrigin, xlineLogicalEnd, xlineLogicalEnd };
    for (int j = 0; j < 4; j++) {
      if (binGrid != null) {
        double[] xy = binGrid.logicalToWorld(new long[] { logicalX[j], logicalY[j] });
        x = xy[0];
        y = xy[1];
      } else {
        // For now get the four corner points internally (this is logically circular)
        x = JavaSeisUtil.getXPhysicalCoordinate(gridDef, inlineIndices[j], xlineIndices[j], inlineUnit, xlineUnit,
            storageOrder);
        y = JavaSeisUtil.getYPhysicalCoordinate(gridDef, inlineIndices[j], xlineIndices[j], inlineUnit, xlineUnit,
            storageOrder);
      }
      points[j] = new Point3d(x, y, 0);
    }
    return points;
  }

  /**
   * Returns the corner points of a prestack3d volume.
   * 
   * @return the corner points of a prestack3d volume.
   */
  public static Point3d[] getCornerPoints(final GridDefinition gridDef, final IBinGrid binGrid, final Unit inlineUnit,
      final Unit xlineUnit, final PreStack3d.StorageOrder storageOrder, final Domain zDomain) {
    // TODO: Find transformation in future based on the X, Y values located in the 4 corner trace headers
    // TODO: Make the transformation parameters member variables that can be used to compute the
    // TODO: X & Y physical coordinates
    int[] lengths = JavaSeisUtil.getAxisLengthsForPreStack3d(gridDef);
    Point3d[] points = new Point3d[4];
    int inlineAxis = JavaSeisUtil.getInlineAxis(storageOrder);
    int xlineAxis = JavaSeisUtil.getCrosslineAxis(storageOrder);
    int ilineEnd = lengths[inlineAxis] - 1;
    int xlineEnd = lengths[xlineAxis] - 1;
    int[] ilines = { 0, ilineEnd, ilineEnd, 0 };
    int[] xlines = { 0, 0, xlineEnd, xlineEnd };
    double x = 0;
    double y = 0;
    long inlineLogicalOrigin = gridDef.getAxisLogicalOrigin(inlineAxis);
    long inlineLogicalDelta = gridDef.getAxisLogicalDelta(inlineAxis);
    long xlineLogicalOrigin = gridDef.getAxisLogicalOrigin(xlineAxis);
    long xlineLogicalDelta = gridDef.getAxisLogicalDelta(xlineAxis);
    long inlineLogicalEnd = inlineLogicalOrigin + inlineLogicalDelta * (lengths[inlineAxis] - 1);
    long xlineLogicalEnd = xlineLogicalOrigin + xlineLogicalDelta * (lengths[xlineAxis] - 1);
    long[] logicalX = new long[0];
    long[] logicalY = new long[0];
    logicalX = new long[] { inlineLogicalOrigin, inlineLogicalEnd, inlineLogicalEnd, inlineLogicalOrigin };
    logicalY = new long[] { xlineLogicalOrigin, xlineLogicalOrigin, xlineLogicalEnd, xlineLogicalEnd };
    for (int j = 0; j < 4; j++) {
      if (binGrid != null) {
        double[] xy = binGrid.logicalToWorld(new long[] { logicalX[j], logicalY[j] });
        x = xy[0];
        y = xy[1];
      } else {
        // For now get the four corner points internally (this is logically circular)
        x = JavaSeisUtil.getXPhysicalCoordinate(gridDef, ilines[j], xlines[j], inlineUnit, xlineUnit, storageOrder);
        y = JavaSeisUtil.getYPhysicalCoordinate(gridDef, ilines[j], xlines[j], inlineUnit, xlineUnit, storageOrder);
      }
      points[j] = new Point3d(x, y, 0);
    }
    return points;
  }

  public static int physicalToIndex(final GridDefinition gridDef, final int whichAxis, final double physicalCoordinate) {
    // translate physicalCoordinate to index
    double delta = JavaSeisUtil.findPhysicalDelta(gridDef, whichAxis);
    double result = (physicalCoordinate - gridDef.getAxisPhysicalOrigin(whichAxis) + delta / 2) / delta;
    int retval = (int) result;
    return retval;
  }

  public static PostStack3d.StorageOrder computePostStackStorageOrder(final GridDefinition gridDef) {
    PostStack3d.StorageOrder retval = PostStack3d.StorageOrder.INLINE_XLINE_Z;
    AxisLabel[] axisLabels = gridDef.getAxisLabels();
    if (isZ(axisLabels[0]) && isXline(axisLabels[1]) && isInline(axisLabels[2])) {
      retval = PostStack3d.StorageOrder.INLINE_XLINE_Z;
    } else if (isZ(axisLabels[0]) && isInline(axisLabels[1]) && isXline(axisLabels[2])) {
      retval = PostStack3d.StorageOrder.XLINE_INLINE_Z;
    } else if (isXline(axisLabels[0]) && isInline(axisLabels[1]) && isZ(axisLabels[2])) {
      retval = PostStack3d.StorageOrder.Z_INLINE_XLINE;
    } else if (isInline(axisLabels[0]) && isXline(axisLabels[1]) && isZ(axisLabels[2])) {
      retval = PostStack3d.StorageOrder.Z_XLINE_INLINE;
    } else {
      throw new IllegalArgumentException("Unsupported poststack storage order.");
    }
    return retval;
  }

  public static PreStack3d.StorageOrder computePreStackStorageOrder(final GridDefinition gridDef) {
    PreStack3d.StorageOrder retval = PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z;
    AxisLabel[] axisLabels = gridDef.getAxisLabels();
    if (isZ(axisLabels[0]) && isOffset(axisLabels[1]) && isXline(axisLabels[2]) && isInline(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z;
    } else if (isZ(axisLabels[0]) && isOffset(axisLabels[1]) && isInline(axisLabels[2]) && isXline(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.XLINE_INLINE_OFFSET_Z;
    } else if (isZ(axisLabels[0]) && isXline(axisLabels[1]) && isOffset(axisLabels[2]) && isInline(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.INLINE_OFFSET_XLINE_Z;
    } else if (isZ(axisLabels[0]) && isInline(axisLabels[1]) && isOffset(axisLabels[2]) && isXline(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.XLINE_OFFSET_INLINE_Z;
    } else if (isZ(axisLabels[0]) && isXline(axisLabels[1]) && isInline(axisLabels[2]) && isOffset(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.OFFSET_INLINE_XLINE_Z;
    } else if (isZ(axisLabels[0]) && isInline(axisLabels[1]) && isXline(axisLabels[2]) && isOffset(axisLabels[3])) {
      retval = PreStack3d.StorageOrder.OFFSET_XLINE_INLINE_Z;
    } else {
      throw new IllegalArgumentException("Unsupported prestack storage order. " + Arrays.toString(axisLabels));
    }
    return retval;
  }

  /**
   * Determine if the given axis label represents inline.
   */
  public static boolean isInline(final AxisLabel axisLabel) {
    String text = axisLabel.getName();
    return text.equalsIgnoreCase("INLINE") || text.equalsIgnoreCase("R_ILINE") || text.equalsIgnoreCase("INLINE_NO")
        || text.equalsIgnoreCase("ILINE") || text.equalsIgnoreCase("I-LINE") || text.equalsIgnoreCase("FRAME");
  }

  /**
   * Determine if the given axis label represents crossline.
   */
  public static boolean isXline(final AxisLabel axisLabel) {
    String text = axisLabel.getName();
    return text.equalsIgnoreCase("CROSSLINE") || text.equalsIgnoreCase("R_XLINE") || text.equalsIgnoreCase("XLINE_NO")
        || text.equalsIgnoreCase("XLINE") || text.equalsIgnoreCase("X-LINE") || text.equalsIgnoreCase("AZI_IDX");
  }

  /**
   * Determine if the given axis label represents "offset".
   * Note: "offset" simply means the 4th dimension.
   */
  public static boolean isOffset(final AxisLabel axisLabel) {
    String text = axisLabel.getName();
    return text.equalsIgnoreCase("OFFSET") || text.equalsIgnoreCase("SOURCE") || text.equalsIgnoreCase("OFFSET_BIN")
        || text.equalsIgnoreCase("SEQNO");
  }

  /**
   * Determine if the given axis label represents z (time or depth).
   */
  public static boolean isZ(final AxisLabel axisLabel) {
    String text = axisLabel.getName();
    return text.equalsIgnoreCase("TIME") || text.equalsIgnoreCase("DEPTH") || text.equalsIgnoreCase("OFF_IDX");
  }

  /**
   * Returns the unit of measurement for the specified axis.
   * 
   * @param axisIndex the axis index.
   * @return the unit of measurement for the specified axis.
   */
  public static Unit getAxisUnit(final GridDefinition gridDef, final int axisIndex) {
    String jsUnits = gridDef.getAxisUnitsString(axisIndex);
    // Javaseis recognizes units of measure in plural form and Geocraft in singular form
    // TODO: check for more common units.
    if (jsUnits.equalsIgnoreCase("seconds")) {
      return Unit.SECOND;
    } else if (jsUnits.equalsIgnoreCase("feet")) {
      return Unit.FOOT;
    } else if (jsUnits.equalsIgnoreCase("meters")) {
      return Unit.METER;
    } else if (jsUnits.equalsIgnoreCase("milliseconds")) {
      return Unit.MILLISECONDS;
    }
    return Unit.UNDEFINED;
  }

  /**
   * Returns the length of the axes.
   * 
   * @return the length of the axes.
   */
  public static int[] getAxisLengthsForPostStack3d(final GridDefinition gridDef) {
    int[] retval = new int[3];
    for (int i = 0; i < 3; i++) {
      retval[i] = (int) gridDef.getAxisLength(i);
    }
    return retval;
  }

  /**
   * Returns the length of the axes.
   * 
   * @return the length of the axes.
   */
  public static int[] getAxisLengthsForPreStack3d(final GridDefinition gridDef) {
    int[] retval = new int[4];
    for (int i = 0; i < 4; i++) {
      retval[i] = (int) gridDef.getAxisLength(i);
    }
    return retval;
  }

  /**
   * Returns the x physical coordinate based on the specified inline,xline indices.
   * 
   * @param inlineIndex the inline index.
   * @param xlineIndex the xline index.
   * @return the x physical coordinate.
   */
  public static double getXPhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final int xlineIndex, final Unit inlineUnit, final Unit xlineUnit, final PostStack3d.StorageOrder storageOrder) {
    // TODO: Do transformation in future based on the X, Y values located in the 4 corner traces
    // For now assume that inlines lie along the Y-dimension and crosslines lie along the X-dimension
    double retval = JavaSeisUtil.findCrosslinePhysicalCoordinate(gridDef, xlineIndex, storageOrder);
    try {
      // long[] logicalXY = _seisio.getBinGrid().indexToLogical(new long[] { inlineIndex, xlineIndex });
      // double[] worldXY = _seisio.getBinGrid().logicalToWorld(logicalXY);
      UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();
      retval = Unit.convert(retval, xlineUnit, UNIT_PREFS.getHorizontalDistanceUnit());
    } catch (Exception e) {
      LOGGER.error(e.toString(), e);
    }
    // inlineIndex = 0; // For now rotation not allowed
    return retval;
  }

  /**
   * Returns the y physical coordinate based on the specified inline,xline indices.
   * 
   * @param inlineIndex the inline index.
   * @param xlineIndex the xline index.
   * @return the y physical coordinate.
   */
  public static double getYPhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final int xlineIndex, final Unit inlineUnit, final Unit xlineUnit, final PostStack3d.StorageOrder storageOrder) {
    // TODO: Do transformation in future based on the X, Y values located in the 4 corner traces
    // For now assume that inlines lie along the Y-dimension and crosslines lie along the X-dimension
    double retval = JavaSeisUtil.findInlinePhysicalCoordinate(gridDef, inlineIndex, storageOrder);
    try {
      UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();
      retval = Unit.convert(retval, inlineUnit, UNIT_PREFS.getHorizontalDistanceUnit());
    } catch (Exception e) {
      LOGGER.error(e.toString(), e);
    }
    // xlineIndex = 0; // For now rotation not allowed
    return retval;
  }

  /**
   * Returns the x physical coordinate based on the specified inline,xline indices.
   * 
   * @param inlineIndex the inline index.
   * @param xlineIndex the xline index.
   * @return the x physical coordinate.
   */
  public static double getXPhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final int xlineIndex, final Unit inlineUnit, final Unit xlineUnit, final PreStack3d.StorageOrder storageOrder) {
    // TODO: Do transformation in future based on the X, Y values located in the 4 corner traces
    // For now assume that inlines lie along the Y-dimension and crosslines lie along the X-dimension
    double retval = JavaSeisUtil.findCrosslinePhysicalCoordinate(gridDef, xlineIndex, storageOrder);
    try {
      // long[] logicalXY = _seisio.getBinGrid().indexToLogical(new long[] { inlineIndex, xlineIndex });
      // double[] worldXY = _seisio.getBinGrid().logicalToWorld(logicalXY);
      UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();
      retval = Unit.convert(retval, xlineUnit, UNIT_PREFS.getHorizontalDistanceUnit());
    } catch (Exception e) {
      LOGGER.error(e.toString(), e);
    }
    // inlineIndex = 0; // For now rotation not allowed
    return retval;
  }

  /**
   * Returns the y physical coordinate based on the specified inline,xline indices.
   * 
   * @param inlineIndex the inline index.
   * @param xlineIndex the xline index.
   * @return the y physical coordinate.
   */
  public static double getYPhysicalCoordinate(final GridDefinition gridDef, final int inlineIndex,
      final int xlineIndex, final Unit inlineUnit, final Unit xlineUnit, final PreStack3d.StorageOrder storageOrder) {
    // TODO: Do transformation in future based on the X, Y values located in the 4 corner traces
    // For now assume that inlines lie along the Y-dimension and crosslines lie along the X-dimension
    double retval = JavaSeisUtil.findInlinePhysicalCoordinate(gridDef, inlineIndex, storageOrder);
    try {
      UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();
      retval = Unit.convert(retval, inlineUnit, UNIT_PREFS.getHorizontalDistanceUnit());
    } catch (Exception e) {
      LOGGER.error(e.toString(), e);
    }
    // xlineIndex = 0; // For now rotation not allowed
    return retval;
  }

  public static long findTraceIndex(final GridDefinition gridDef, final int inlineIndex, final int crosslineIndex,
      final PostStack3d.StorageOrder storageOrder) {
    int[] lengths = JavaSeisUtil.getAxisLengthsForPostStack3d(gridDef);
    int inlineAxis = JavaSeisUtil.getInlineAxis(storageOrder);
    int xlineAxis = JavaSeisUtil.getCrosslineAxis(storageOrder);
    long retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_Z:
        retval = inlineIndex * lengths[xlineAxis] + crosslineIndex;
        break;
      case XLINE_INLINE_Z:
        retval = crosslineIndex * lengths[inlineAxis] + inlineIndex;
        break;
      case Z_INLINE_XLINE:
      case Z_XLINE_INLINE:
      default:
        throw new IllegalArgumentException("Invalid storage order: " + storageOrder);
    }
    return retval;
  }

  public static long findTraceIndex(final GridDefinition gridDef, final int inlineIndex, final int xlineIndex,
      final int offsetIndex, final PreStack3d.StorageOrder storageOrder) {
    int[] lengths = JavaSeisUtil.getAxisLengthsForPreStack3d(gridDef);
    int inlineAxis = JavaSeisUtil.getInlineAxis(storageOrder);
    int xlineAxis = JavaSeisUtil.getCrosslineAxis(storageOrder);
    int offsetAxis = JavaSeisUtil.getOffsetAxis(storageOrder);
    long retval = -1;
    switch (storageOrder) {
      case INLINE_XLINE_OFFSET_Z:
        retval = (inlineIndex * lengths[xlineAxis] + xlineIndex) * lengths[offsetAxis] + offsetIndex;
        break;
      case INLINE_OFFSET_XLINE_Z:
        retval = (inlineIndex * lengths[offsetAxis] + offsetIndex) * lengths[xlineAxis] + xlineIndex;
        break;
      case XLINE_INLINE_OFFSET_Z:
        retval = (xlineIndex * lengths[inlineAxis] + inlineIndex) * lengths[offsetAxis] + offsetIndex;
        break;
      case XLINE_OFFSET_INLINE_Z:
        retval = (xlineIndex * lengths[offsetAxis] + offsetIndex) * lengths[inlineAxis] + inlineIndex;
        break;
      case OFFSET_INLINE_XLINE_Z:
        retval = (offsetIndex * lengths[inlineAxis] + inlineIndex) * lengths[xlineAxis] + xlineIndex;
        break;
      case OFFSET_XLINE_INLINE_Z:
        retval = (offsetIndex * lengths[xlineAxis] + xlineIndex) * lengths[inlineAxis] + inlineIndex;
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + storageOrder);
    }
    return retval;
  }

  /**
   * Builds a trace header definition based on the JavaSeis trace properties descriptions.
   * 
   * @param propDescs the JavaSeis trace properties descriptions.
   * @return the trace header.
   */
  public static HeaderDefinition buildHeaderDefinition(final PropertyDescription[] propDescs,
      final boolean includeOffset, final boolean includeInlineXlineXandY) {
    List<HeaderEntry> headerEntries = new ArrayList<HeaderEntry>();
    for (PropertyDescription propDesc : propDescs) {
      Format format = Format.BYTE;
      int count = propDesc.getCount();
      if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_BYTE) {
        format = Format.BYTE;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_SHORT) {
        format = Format.SHORT;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_INTEGER) {
        format = Format.INTEGER;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_LONG) {
        format = Format.LONG;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_FLOAT) {
        format = Format.FLOAT;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_DOUBLE) {
        format = Format.DOUBLE;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_COMPLEX) {
        format = Format.FLOAT;
        count *= 2;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_DCOMPLEX) {
        format = Format.DOUBLE;
        count *= 2;
      } else if (propDesc.getFormat() == PropertyDescription.HDR_FORMAT_STRING) {
        format = Format.STRING;
      }
      String label = propDesc.getLabel().toUpperCase();
      // TODO: This is a hack...
      // Convert JS trace headers for inline,xline to GeoCraft trace headers.
      if (format.equals(Format.INTEGER) && count == 1 && label.equals(JS_INLINE_NO_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.INLINE_NO)) {
          headerEntries.add(TraceHeaderCatalog.INLINE_NO);
        }
      } else if (format.equals(Format.INTEGER) && count == 1 && label.equalsIgnoreCase(JS_XLINE_NO_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.XLINE_NO)) {
          headerEntries.add(TraceHeaderCatalog.XLINE_NO);
        }
      } else if (format.equals(Format.FLOAT) && count == 1 && label.equals(JS_CDP_X_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.X)) {
          headerEntries.add(TraceHeaderCatalog.X);
        }
      } else if (format.equals(Format.FLOAT) && count == 1 && label.equalsIgnoreCase(JS_CDP_Y_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.Y)) {
          headerEntries.add(TraceHeaderCatalog.Y);
        }
      } else if (format.equals(Format.DOUBLE) && count == 1 && label.equals(JS_CDP_XD_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.X)) {
          headerEntries.add(TraceHeaderCatalog.X);
        }
      } else if (format.equals(Format.DOUBLE) && count == 1 && label.equalsIgnoreCase(JS_CDP_YD_LABEL)) {
        if (!headerEntries.contains(TraceHeaderCatalog.Y)) {
          headerEntries.add(TraceHeaderCatalog.Y);
        }
      } else if (includeOffset && format.equals(Format.INTEGER) && count == 1
          && label.equalsIgnoreCase(TraceHeaderCatalog.OFFSET.getName())) {
        headerEntries.add(TraceHeaderCatalog.OFFSET);
      } else {
        HeaderEntry headerEntry = new HeaderEntry(propDesc.getLabel().toUpperCase(), propDesc.getLabel(),
            propDesc.getDescription(), format, count);
        headerEntries.add(headerEntry);
      }
    }
    if (includeInlineXlineXandY) {
      if (!headerEntries.contains(TraceHeaderCatalog.INLINE_NO)) {
        headerEntries.add(TraceHeaderCatalog.INLINE_NO);
      }
      if (!headerEntries.contains(TraceHeaderCatalog.XLINE_NO)) {
        headerEntries.add(TraceHeaderCatalog.XLINE_NO);
      }
      if (!headerEntries.contains(TraceHeaderCatalog.X)) {
        headerEntries.add(TraceHeaderCatalog.X);
      }
      if (!headerEntries.contains(TraceHeaderCatalog.Y)) {
        headerEntries.add(TraceHeaderCatalog.Y);
      }
    }
    if (includeOffset) {
      if (!headerEntries.contains(TraceHeaderCatalog.OFFSET)) {
        headerEntries.add(TraceHeaderCatalog.OFFSET);
      }
    }
    return new HeaderDefinition(headerEntries.toArray(new HeaderEntry[0]));
  }

  /**
   * Builds JavaSeis trace properties from the specified header definition.
   * 
   * @param headerDef the trace header definition.
   * @return the JavaSeis trace properties.
   */
  public static TraceProperties buildTraceProperties(final HeaderDefinition headerDef) {
    TraceProperties traceProps = new TraceProperties();
    for (HeaderEntry headerEntry : headerDef.getEntries()) {
      int format = PropertyDescription.HDR_FORMAT_UNDEFINED;
      if (headerEntry.getFormat().equals(Format.BYTE)) {
        // Convert byte to 4-byte integer.
        format = PropertyDescription.HDR_FORMAT_INTEGER;
      } else if (headerEntry.getFormat().equals(Format.SHORT)) {
        // Convert short to 4-byte integer.
        format = PropertyDescription.HDR_FORMAT_INTEGER;
      } else if (headerEntry.getFormat().equals(Format.INTEGER)) {
        format = PropertyDescription.HDR_FORMAT_INTEGER;
      } else if (headerEntry.getFormat().equals(Format.LONG)) {
        format = PropertyDescription.HDR_FORMAT_LONG;
      } else if (headerEntry.getFormat().equals(Format.FLOAT)) {
        format = PropertyDescription.HDR_FORMAT_FLOAT;
      } else if (headerEntry.getFormat().equals(Format.DOUBLE)) {
        format = PropertyDescription.HDR_FORMAT_DOUBLE;
      } else if (headerEntry.getFormat().equals(Format.STRING)) {
        format = PropertyDescription.HDR_FORMAT_STRING;
      }
      // Convert GeoCraft trace headers for inline,xline to JS trace headers.
      if (headerEntry.equals(TraceHeaderCatalog.INLINE_NO)) {
        PropertyDescription propDesc = new PropertyDescription(JS_INLINE_NO_LABEL, JS_INLINE_NO_DESCR,
            PropertyDescription.HDR_FORMAT_INTEGER, 1);
        traceProps.addTraceProperty(propDesc);
      } else if (headerEntry.equals(TraceHeaderCatalog.XLINE_NO)) {
        PropertyDescription propDesc = new PropertyDescription(JS_XLINE_NO_LABEL, JS_XLINE_NO_DESCR,
            PropertyDescription.HDR_FORMAT_INTEGER, 1);
        traceProps.addTraceProperty(propDesc);
      } else if (headerEntry.equals(TraceHeaderCatalog.X)) {
        PropertyDescription propDesc = new PropertyDescription(JS_CDP_X_LABEL, JS_CDP_X_DESCR,
            PropertyDescription.HDR_FORMAT_FLOAT, 1);
        traceProps.addTraceProperty(propDesc);
        propDesc = new PropertyDescription(JS_CDP_XD_LABEL, JS_CDP_XD_DESCR, PropertyDescription.HDR_FORMAT_DOUBLE, 1);
        traceProps.addTraceProperty(propDesc);
      } else if (headerEntry.equals(TraceHeaderCatalog.Y)) {
        PropertyDescription propDesc = new PropertyDescription(JS_CDP_Y_LABEL, JS_CDP_Y_DESCR,
            PropertyDescription.HDR_FORMAT_FLOAT, 1);
        traceProps.addTraceProperty(propDesc);
        propDesc = new PropertyDescription(JS_CDP_YD_LABEL, JS_CDP_YD_DESCR, PropertyDescription.HDR_FORMAT_DOUBLE, 1);
        traceProps.addTraceProperty(propDesc);
      } else {
        PropertyDescription propDesc = new PropertyDescription(headerEntry.getName(), headerEntry.getDescription(),
            format, headerEntry.getNumElements());
        traceProps.addTraceProperty(propDesc);
      }
    }
    return traceProps;
  }

  /**
   * Builds a trace header from the specified JavaSeis trace properties.
   * 
   * @param headerDef the header definition.
   * @param traceProps the JavaSeis trace properties.
   * @param traceIndex the index of the trace in the trace properties for which to get header values.
   * @return the trace header.
   */
  public static Header buildHeader(final HeaderDefinition headerDef, final TraceProperties traceProps,
      final int traceIndex) {
    Header header = new Header(headerDef);
    traceProps.setTraceIndex(traceIndex);
    for (HeaderEntry headerEntry : headerDef.getEntries()) {
      // Handle inlines and xline in a custom way.
      if (headerEntry.equals(TraceHeaderCatalog.INLINE_NO)) {
        int inline = traceProps.getInt(JS_INLINE_NO_LABEL);
        header.putInteger(TraceHeaderCatalog.INLINE_NO, inline);
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.XLINE_NO)) {
        int xline = traceProps.getInt(JS_XLINE_NO_LABEL);
        header.putInteger(TraceHeaderCatalog.XLINE_NO, xline);
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.X)) {
        double x = 0;
        if (traceProps.exists(JS_CDP_X_LABEL)) {
          x = traceProps.getFloat(JS_CDP_X_LABEL);
        }
        if (traceProps.exists(JS_CDP_XD_LABEL)) {
          x = traceProps.getDouble(JS_CDP_XD_LABEL);
        }
        header.putDouble(TraceHeaderCatalog.X, x);
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.Y)) {
        double y = 0;
        if (traceProps.exists(JS_CDP_Y_LABEL)) {
          y = traceProps.getFloat(JS_CDP_Y_LABEL);
        }
        if (traceProps.exists(JS_CDP_YD_LABEL)) {
          y = traceProps.getDouble(JS_CDP_YD_LABEL);
        }
        header.putDouble(TraceHeaderCatalog.Y, y);
        continue;
      }
      String key = headerEntry.getKey();
      PropertyDescription propDesc = traceProps.getTraceProperty(headerEntry.getKey());

      int jsFormat = -999;
      int jsCount = -1;
      if (propDesc != null) {
        //System.out.println("HeaderElement=" + headerEntry.getName() + " " + headerEntry.getKey() + " "
        //    + propDesc.getLabel() + " " + propDesc.getFormat() + " " + propDesc.getCount());
        jsFormat = propDesc.getFormat();
        jsCount = propDesc.getCount();
      }
      if (propDesc != null) {
        Format format = headerEntry.getFormat();
        int count = headerEntry.getNumElements();
        if (format.equals(Format.BYTE) && jsFormat == PropertyDescription.HDR_FORMAT_BYTE) {
          // TODO: how to get bytes?
        } else if (format.equals(Format.SHORT) && jsFormat == PropertyDescription.HDR_FORMAT_SHORT) {
          if (count == 1) {
            short value = traceProps.getShort(key);
            header.putShort(headerEntry.getKey(), value);
          } else {
            short[] values = new short[count];
            traceProps.getShortArray(key, values);
            header.putShorts(headerEntry, values);
          }
        } else if (format.equals(Format.INTEGER) && jsFormat == PropertyDescription.HDR_FORMAT_INTEGER) {
          if (count == 1) {
            int value = traceProps.getInt(key);
            header.putInteger(headerEntry.getKey(), value);
          } else {
            int[] values = new int[count];
            traceProps.getIntArray(key, values);
            header.putIntegers(headerEntry, values);
          }
        } else if (format.equals(Format.LONG) && jsFormat == PropertyDescription.HDR_FORMAT_LONG) {
          if (count == 1) {
            long value = traceProps.getLong(key);
            header.putLong(headerEntry.getKey(), value);
          } else {
            long[] values = new long[count];
            traceProps.getLongArray(key, values);
            header.putLongs(headerEntry, values);
          }
        } else if (format.equals(Format.FLOAT) && jsFormat == PropertyDescription.HDR_FORMAT_FLOAT) {
          if (count == 1) {
            float value = traceProps.getFloat(key);
            header.putFloat(headerEntry.getKey(), value);
          } else {
            float[] values = new float[count];
            traceProps.getFloatArray(key, values);
            header.putFloats(headerEntry, values);
          }
        } else if (format.equals(Format.DOUBLE) && jsFormat == PropertyDescription.HDR_FORMAT_DOUBLE) {
          if (count == 1) {
            double value = traceProps.getDouble(key);
            header.putDouble(headerEntry.getKey(), value);
          } else {
            double[] values = new double[count];
            traceProps.getDoubleArray(key, values);
            header.putDoubles(headerEntry, values);
          }
        } else if (format.equals(Format.STRING) && jsFormat == PropertyDescription.HDR_FORMAT_STRING) {
          String value = traceProps.getValue(key);
          header.putString(headerEntry.getKey(), value);
        }
      }
    }
    return header;
  }

  /**
   * Copies header values to JavaSeis trace properties.
   * 
   * @param header the header containing the values to copy.
   * @param traceProps the JavaSeis trace properties.
   * @param traceIndex the index of the trace.
   */
  public static void copyHeaderValuesToJSTraceProperties(final Header header, final TraceProperties traceProps,
      final int traceIndex) {
    HeaderDefinition headerDef = header.getHeaderDefinition();
    traceProps.setTraceIndex(traceIndex);
    for (HeaderEntry headerEntry : headerDef.getEntries()) {
      // Handle inlines and xlines in a custom way.
      if (headerEntry.equals(TraceHeaderCatalog.INLINE_NO)) {
        int inline = header.getInteger(TraceHeaderCatalog.INLINE_NO);
        traceProps.putInt(JS_INLINE_NO_LABEL, inline);
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.XLINE_NO)) {
        int xline = header.getInteger(TraceHeaderCatalog.XLINE_NO);
        traceProps.putInt(JS_XLINE_NO_LABEL, xline);
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.X)) {
        double x = header.getDouble(TraceHeaderCatalog.X);
        if (traceProps.exists(JS_CDP_X_LABEL)) {
          traceProps.putFloat(JS_CDP_X_LABEL, (float) x);
        }
        if (traceProps.exists(JS_CDP_XD_LABEL)) {
          traceProps.putDouble(JS_CDP_XD_LABEL, x);
        }
        continue;
      } else if (headerEntry.equals(TraceHeaderCatalog.Y)) {
        double y = header.getDouble(TraceHeaderCatalog.Y);
        if (traceProps.exists(JS_CDP_Y_LABEL)) {
          traceProps.putFloat(JS_CDP_Y_LABEL, (float) y);
        }
        if (traceProps.exists(JS_CDP_YD_LABEL)) {
          traceProps.putDouble(JS_CDP_YD_LABEL, y);
        }
        continue;
      }
      String key = headerEntry.getKey();
      Format format = headerEntry.getFormat();
      int count = headerEntry.getNumElements();
      if (format.equals(Format.BYTE)) {
        // TODO: how to get bytes?
      } else if (format.equals(Format.SHORT)) {
        if (count == 1) {
          // Convert shorts to integers.
          short value = header.getShort(headerEntry);
          //traceProps.putShort(key, value);
          traceProps.putInt(key, value);
        } else {
          short[] values = header.getShorts(headerEntry);
          int[] ivalues = new int[values.length];
          for (int i = 0; i < values.length; i++) {
            ivalues[i] = values[i];
          }
          //traceProps.putShortArray(key, values);
          traceProps.putIntArray(key, ivalues);
        }
      } else if (format.equals(Format.INTEGER)) {
        if (count == 1) {
          int value = header.getInteger(headerEntry);
          traceProps.putInt(key, value);
        } else {
          int[] values = header.getIntegers(headerEntry);
          traceProps.putIntArray(key, values);
        }
      } else if (format.equals(Format.LONG)) {
        if (count == 1) {
          long value = header.getLong(headerEntry);
          traceProps.putLong(key, value);
        } else {
          long[] values = header.getLongs(headerEntry);
          traceProps.putLongArray(key, values);
        }
      } else if (format.equals(Format.FLOAT)) {
        if (count == 1) {
          float value = header.getFloat(headerEntry);
          traceProps.putFloat(key, value);
        } else {
          float[] values = header.getFloats(headerEntry);
          traceProps.putFloatArray(key, values);
        }
      } else if (format.equals(Format.DOUBLE)) {
        if (count == 1) {
          double value = header.getDouble(headerEntry);
          traceProps.putDouble(key, value);
        } else {
          double[] values = header.getDoubles(headerEntry);
          traceProps.putDoubleArray(key, values);
        }
      } else if (format.equals(Format.STRING)) {
        String value = header.getString(headerEntry);
        traceProps.putValue(key, value);
      } else {
        // TODO: how to handle other formats (complex, etc).
      }
    }
  }
}
