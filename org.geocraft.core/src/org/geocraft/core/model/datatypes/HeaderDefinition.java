package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.geocraft.core.model.datatypes.HeaderEntry.Format;


public class HeaderDefinition implements Serializable {

  /** The collection of header entries. */
  private final List<HeaderEntry> _headerEntries;

  /** The header indices for string values, mapped by the entry key. */
  private final HeaderIndexMap _stringMap;

  /** The header indices for byte values, mapped by the entry key. */
  private final HeaderIndexMap _byteMap;

  /** The header indices for short values, mapped by the entry key. */
  private final HeaderIndexMap _shortMap;

  /** The header indices for integer values, mapped by the entry key. */
  private final HeaderIndexMap _integerMap;

  /** The header indices for long values, mapped by the entry key. */
  private final HeaderIndexMap _longMap;

  /** The header indices for float values, mapped by the entry key. */
  private final HeaderIndexMap _floatMap;

  /** The header indices for double values, mapped by the entry key. */
  private final HeaderIndexMap _doubleMap;

  /** The number of string header entries. */
  private int _numStrings = 0;

  /** The number of byte header entries. */
  private int _numBytes = 0;

  /** The number of short header entries. */
  private int _numShorts = 0;

  /** The number of integer header entries. */
  private int _numIntegers = 0;

  /** The number of long header entries. */
  private int _numLongs = 0;

  /** The number of float header entries. */
  private int _numFloats = 0;

  /** The number of double header entries. */
  private int _numDoubles = 0;

  /**
   * Constructs an empty header definition with no entries.
   */
  public HeaderDefinition(final HeaderEntry[] headerEntries) {
    // Synchronized on the input array of header entries to prevent its
    // modification during construction.
    synchronized (headerEntries) {

      _headerEntries = Collections.synchronizedList(new ArrayList<HeaderEntry>());

      // Allocate the header index maps.
      _stringMap = new HeaderIndexMap();
      _byteMap = new HeaderIndexMap();
      _shortMap = new HeaderIndexMap();
      _integerMap = new HeaderIndexMap();
      _longMap = new HeaderIndexMap();
      _floatMap = new HeaderIndexMap();
      _doubleMap = new HeaderIndexMap();

      // Add the header entries one at a time, ignoring duplicates.
      for (HeaderEntry headerEntry : headerEntries) {
        if (!_headerEntries.contains(headerEntry)) {
          _headerEntries.add(headerEntry);
          Format format = headerEntry.getFormat();
          int numElements = headerEntry.getNumElements();
          getHeaderIndexMap(format).add(headerEntry.getKey(), headerEntry.getNumElements());
          switch (format) {
            case STRING:
              _numStrings += numElements;
              break;
            case BYTE:
              _numBytes += numElements;
              break;
            case SHORT:
              _numShorts += numElements;
              break;
            case INTEGER:
              _numIntegers += numElements;
              break;
            case LONG:
              _numLongs += numElements;
              break;
            case FLOAT:
              _numFloats += numElements;
              break;
            case DOUBLE:
              _numDoubles += numElements;
              break;
          }
        }
      }
    }
  }

  /**
   * Returns the header index map for the given header entry format.
   * 
   * @param format the header entry format.
   * @return the header index map for the given format.
   * @throws IllegalArgumentException if the format is invalid.
   */
  private HeaderIndexMap getHeaderIndexMap(final Format format) {
    switch (format) {
      case STRING:
        return _stringMap;
      case BYTE:
        return _byteMap;
      case SHORT:
        return _shortMap;
      case INTEGER:
        return _integerMap;
      case LONG:
        return _longMap;
      case FLOAT:
        return _floatMap;
      case DOUBLE:
        return _doubleMap;
    }
    throw new IllegalArgumentException("Invalid header entry format: " + format);
  }

  /**
   * Returns the header entry that corresponds to the given key.
   * 
   * @param key the key on which to search.
   * @return the header entry with the given key.
   * @throws NoSuchElementException if no match is found.
   */
  public HeaderEntry getHeaderEntry(final String key) {
    for (HeaderEntry headerEntry : _headerEntries.toArray(new HeaderEntry[0])) {
      if (headerEntry.getKey().equals(key)) {
        return headerEntry;
      }
    }
    throw new NoSuchElementException("No header entry found: " + key);
  }

  /**
   * Returns the number of entries in the header.
   * 
   * @return the number of header entries.
   */
  public int getNumEntries() {
    return _headerEntries.size();
  }

  /**
   * Returns the header entry key for the given index.
   * 
   * @param index the index of the header entry for which to return the key.
   * @return the header entry key.
   */
  public String getKey(final int index) {
    return _headerEntries.get(index).getKey();
  }

  /**
   * Returns the header entry for the given index.
   * 
   * @param index the index of the header entry to return.
   * @return the header entry.
   */
  public HeaderEntry getHeaderEntry(final int index) {
    return _headerEntries.get(index);
  }

  /**
   * Returns the required size of the string array.
   * 
   * @return the required string array size.
   */
  public int getStringArraySize() {
    return _numStrings;
  }

  /**
   * Returns the required size of the byte array.
   * 
   * @return the required byte array size.
   */
  public int getByteArraySize() {
    return _numBytes;
  }

  /**
   * Returns the required size of the short array.
   * 
   * @return the required short array size.
   */
  public int getShortArraySize() {
    return _numShorts;
  }

  /**
   * Returns the required size of the integer array.
   * 
   * @return the required integer array size.
   */
  public int getIntegerArraySize() {
    return _numIntegers;
  }

  /**
   * Returns the required size of the long array.
   * 
   * @return the required long array size.
   */
  public int getLongArraySize() {
    return _numLongs;
  }

  /**
   * Returns the required size of the float array.
   * 
   * @return the required float array size.
   */
  public int getFloatArraySize() {
    return _numFloats;
  }

  /**
   * Returns the required size of the double array.
   * 
   * @return the required double array size.
   */
  public int getDoubleArraySize() {
    return _numDoubles;
  }

  public int getStringIndex(final String key) {
    return _stringMap.getIndex(key);
  }

  public int getByteIndex(final String key) {
    return _byteMap.getIndex(key);
  }

  public int getShortIndex(final String key) {
    return _shortMap.getIndex(key);
  }

  public int getIntegerIndex(final String key) {
    return _integerMap.getIndex(key);
  }

  public int getLongIndex(final String key) {
    return _longMap.getIndex(key);
  }

  public int getFloatIndex(final String key) {
    return _floatMap.getIndex(key);
  }

  public int getDoubleIndex(final String key) {
    return _doubleMap.getIndex(key);
  }

  public boolean contains(final HeaderEntry headerEntry) {
    for (HeaderEntry e : _headerEntries) {
      if (e.getDescription().equals(headerEntry.getDescription()) && e.getFormat().equals(headerEntry.getFormat())
          && e.getKey().equals(headerEntry.getKey()) && e.getName().equals(headerEntry.getName())
          && e.getNumElements() == headerEntry.getNumElements())
        return true;
    }
    return false;
  }

  public HeaderEntry[] getEntries() {
    return _headerEntries.toArray(new HeaderEntry[0]);
  }
}
