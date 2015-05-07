package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.LinkedHashMap;


public class HeaderIndexMap implements Serializable {

  protected final LinkedHashMap<String, Integer> _indices;

  private int _numEntries;

  private int _currentIndex;

  public HeaderIndexMap() {
    _indices = new LinkedHashMap<String, Integer>();
    _numEntries = 0;
    _currentIndex = 0;
  }

  public synchronized int getNumEntries() {
    return _numEntries;
  }

  public synchronized boolean containsKey(final String key) {
    return _indices.containsKey(key);
  }

  public void add(final String key, final int numElements) {
    _indices.put(key, _currentIndex);
    _currentIndex += numElements;
    _numEntries++;
  }

  public int getIndex(final String key) {
    Integer index = _indices.get(key);
    if (index == null) {
      throw new RuntimeException("Header entry not found: " + key);
    }
    return index.intValue();
  }

}