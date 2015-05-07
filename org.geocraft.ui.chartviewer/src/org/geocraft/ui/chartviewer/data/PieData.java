package org.geocraft.ui.chartviewer.data;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;


public class PieData extends AbstractChartData {

  private final Map<String, Float> _entryMap;

  private final Map<String, RGB> _rgbMap;

  private float _summationOfValues;

  public PieData(final String name) {
    this(name, new String[0], new float[0], new RGB[0]);
  }

  public PieData(final String name, final String[] entryNames, final float[] entryValues, final RGB[] rgbs) {
    super(name);

    if (entryNames == null || entryValues == null || rgbs == null) {
      throw new IllegalArgumentException("The entries cannot be null.");
    }
    if (entryNames.length != entryValues.length && entryNames.length != rgbs.length) {
      throw new IllegalArgumentException("The # of entry names (" + entryNames.length
          + ") does not equal the # of entry values (" + entryValues.length + ").");
    }
    _summationOfValues = 0;
    _entryMap = Collections.synchronizedMap(new HashMap<String, Float>());
    _rgbMap = Collections.synchronizedMap(new HashMap<String, RGB>());
    for (int i = 0; i < entryNames.length; i++) {
      addEntry(entryNames[i], entryValues[i], rgbs[i], false);
    }
  }

  public void addEntry(final String name, final float value, final RGB rgb, final boolean override) {
    if (_entryMap.containsKey(name) && !override) {
      return;
    }
    if (value < 0) {
      throw new IllegalArgumentException("The " + name + " entry value is negative (" + value + ").");
    }
    _entryMap.put(name, new Float(value));
    _rgbMap.put(name, rgb);
    updateSummation();
  }

  public void removeEntry(final String name) {
    _entryMap.remove(name);
    _rgbMap.remove(name);
    updateSummation();
  }

  public String[] getEntryNames() {
    return _entryMap.keySet().toArray(new String[0]);
  }

  public float getWeight(final String name) {
    if (!_entryMap.containsKey(name)) {
      return 0;
    }
    return _entryMap.get(name).floatValue() / _summationOfValues;
  }

  public RGB getRGB(final String name) {
    if (!_rgbMap.containsKey(name)) {
      return new RGB(0, 0, 0);
    }
    return _rgbMap.get(name);
  }

  private void updateSummation() {
    float summation = 0;
    for (Float f : _entryMap.values()) {
      summation += f.floatValue();
    }
    _summationOfValues = summation;
  }

}
