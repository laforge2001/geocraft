package org.geocraft.core.model.datatypes;


import java.io.Serializable;

import org.geocraft.core.model.datatypes.HeaderEntry.Format;


/**
 * This class contains some standard trace header entries.
 * <p>
 * Each of the entries contained in this catalog is an immutable, thread-safe
 * object.
 */
public final class TraceHeaderCatalog implements Serializable {

  /**
   * The header entry for the trace inline #.
   * <p>
   * Consists of a single integer value.
   */
  public static final HeaderEntry INLINE_NO = new HeaderEntry("INLINE_NO", "Inline #", "Inline number", Format.INTEGER,
      1);

  /**
   * The header entry for the trace crossline #.
   * <p>
   * Consists of a single integer value.
   */
  public static final HeaderEntry XLINE_NO = new HeaderEntry("XLINE_NO", "Xline #", "Crossline number", Format.INTEGER,
      1);

  /**
   * The header entry for the trace CDP #.
   * <p>
   * Consists of a single integer value.
   */
  public static final HeaderEntry CDP_NO = new HeaderEntry("CDP_NO", "CDP #", "Common depth point number",
      Format.INTEGER, 1);

  /**
   * The header entry for the trace shotpoint #.
   * <p>
   * Consists of a single float value.
   */
  public static final HeaderEntry SHOTPOINT_NO = new HeaderEntry("SHOTPOINT_NO", "Shotpoint #", "Shotpoint number",
      Format.FLOAT, 1);

  /**
   * The header entry for the trace offset.
   * <p>
   * Consists of a single float value.
   */
  public static final HeaderEntry OFFSET = new HeaderEntry("OFFSET", "Offset", "Offset or source-receiver distance",
      Format.FLOAT, 1);

  /**
   * The header entry for the trace x-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry X = new HeaderEntry("X", "X Location", "The x location in world coordinates",
      Format.DOUBLE, 1);

  /**
   * The header entry for the trace y-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry Y = new HeaderEntry("Y", "Y Location", "The y location in world coordinates",
      Format.DOUBLE, 1);

  /**
   * The header entry for the source x-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry SOURCE_X = new HeaderEntry("Source X", "Source X Location",
      "The source x location in world coordinates", Format.DOUBLE, 1);

  /**
   * The header entry for the source y-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry SOURCE_Y = new HeaderEntry("Source Y", "Source Y Location",
      "The source y location in world coordinates", Format.DOUBLE, 1);

  /**
   * The header entry for the receiver x-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry RECEIVER_X = new HeaderEntry("Receiver X", "Receiver X Location",
      "The receiver x location in world coordinates", Format.DOUBLE, 1);

  /**
   * The header entry for the receiver y-coordinate.
   * <p>
   * Consists of a single double value.
   */
  public static final HeaderEntry RECEIVER_Y = new HeaderEntry("Receiver Y", "Receiver Y Location",
      "The receiver y location in world coordinates", Format.DOUBLE, 1);
}
