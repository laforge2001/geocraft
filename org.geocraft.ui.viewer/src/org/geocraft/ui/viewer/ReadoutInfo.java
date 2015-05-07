/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import java.util.Arrays;
import java.util.List;

import org.geocraft.core.common.util.Generics;


/**
 * Defines a class for storing the cursor readout information for a view layer.
 * The readout information is a 2d array of key-value data, associated with
 * an identifier (e.g. an entity name). View layers will return instances of
 * this class, which will be passed on to the ReadoutPanel class.
 */
public class ReadoutInfo {

  /** The title of this readout section eg Mouse Location or PointSet: foo.vs */
  private String _title;

  /** An ordered array of the key names eg x, y, z. */
  private String[] _keys;

  /** An ordered array of the values to display. */
  private String[] _values;

  /**
   * Use this constructor when the class does not yet provide a
   * correct ReadoutInfo. 
   * 
   * @param id
   */
  public ReadoutInfo(final String id) {
    _title = id;
    _keys = Generics.asArray("not");
    _values = Generics.asArray("implemented");
  }

  /**
   * Constructs a readout info object with a set of key-value data.
   * 
   * @param id the id of the key-value data entry (e.g. an entity name).
   * @param keys the array of readout keys (e.g. "X", "Y", "Inline", "Xline").
   * @param values the array of readout values (e.g. x-coordinate, y-coordinate, inline #, xline #).
   */
  public ReadoutInfo(final String id, final String[] keys, final String[] values) {
    _title = id;
    _keys = keys;
    _values = values;
    check();
  }

  /**
   * Constructs a readout info object with a set of key-value data.
   * 
   * @param id the id of the key-value data entry (e.g. an entity name).
   * @param keys list of readout keys (e.g. "X", "Y", "Inline", "Xline").
   * @param values list of readout values (e.g. x-coordinate, y-coordinate, inline #, xline #).
   */
  public ReadoutInfo(final String id, final List<String> keys, final List<String> values) {
    _title = id;
    _keys = keys.toArray(new String[keys.size()]);
    _values = values.toArray(new String[keys.size()]);
    check();
  }

  private void check() {
    if (_keys.length != _values.length) {
      throw new IllegalArgumentException(_title + " keys and values must be same length " + _keys.length + " "
          + _values.length);
    }
  }

  public String getTitle() {
    return _title;
  }

  public String[] getKeys() {
    return _keys;
  }

  public String[] getValues() {
    return _values;
  }

  public String getKey(int i) {
    return _keys[i];
  }

  public String getValue(int i) {
    return _values[i];
  }

  public int getNumRecords() {
    return _keys.length;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[ReadoutInfo] Title:");
    builder.append(getTitle());
    builder.append(" Keys:");
    builder.append(Arrays.toString(getKeys()));
    builder.append(" Values:");
    builder.append(Arrays.toString(getValues()));
    return builder.toString();
  }

}
