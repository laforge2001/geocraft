package org.geocraft.core.model.datatypes;


import java.io.Serializable;


/**
 * This class defines an entry in a <code>HeaderDefinition</code>.
 * <p>
 * An entry is a description of data contained in a <code>Header</code>. It
 * consists of a unique key, a short name, a lengthier description, the format
 * of the data (e.g. integer, floating-point, string), and the number of
 * elements.
 * <p>
 * This class is immutable, and thus thread-safe.
 */
public final class HeaderEntry implements Serializable {

  private static final long serialVersionUID = 42L;

  /** Enumeration of the various header entry formats. */
  public static enum Format implements Serializable {
    BYTE("byte"),
    SHORT("short"),
    INTEGER("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    STRING("string");

    private static final long serialVersionUID = 43L;

    /** The string representation of the header format. */
    private String _text;

    Format(final String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  /** The unique identifier of the header entry. */
  private final String _key;

  /** The common (display) name of the header entry. */
  private final String _name;

  /** The informational description of the header entry. */
  private final String _description;

  /** The format of elements of the header entry. */
  private final Format _format;

  /** The number of elements in the header entry. */
  private final int _numElements;

  public HeaderEntry(final String key, final String name, final String description, final Format format, final int numElements) {
    // Validate all the properties of the header entry, throwing an exception if
    // any of them are invalid.
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Invalid header entry key: " + key);
    }
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Invalid header entry name: " + name);
    }
    if (description == null) {
      throw new IllegalArgumentException("Invalid header entry description: " + description);
    }
    if (format == null) {
      throw new IllegalArgumentException("Invalid header entry format: " + format);
    }
    if (numElements < 1) {
      throw new IllegalArgumentException("Invalid header entry count: " + numElements);
    }

    // Store the header entry properties.
    _key = key;
    _name = name;
    _description = description;
    _format = format;
    _numElements = numElements;
  }

  /**
   * Returns the key of the header entry.
   * <p>
   * This is the unique identifier used to distinguish one header entry from
   * another. It must be unique for each entry within a header definition.
   * 
   * @return the header entry key.
   */
  public String getKey() {
    return _key;
  }

  /**
   * Returns the name of the header entry.
   * <p>
   * This is the common display name of the header entry.
   * 
   * @return the header entry (display) name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the description of the header entry.
   * <p>
   * This can be anything and is for information purposes only.
   * 
   * @return header entry description.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Returns the format of the header entry.
   * <p>
   * Options include <code>BOOLEAN</code>, <code>INTEGER</code>,
   * <code>FLOAT</code>, etc. This, along with the number of elements,
   * determines the byte size of the entry.
   * 
   * @return the header entry format.
   */
  public Format getFormat() {
    return _format;
  }

  /**
   * Returns the mumber of elements in the header entry.
   * <p>
   * This must be a positive number. This, along with the format, determines the
   * byte size of the entry.
   * 
   * @return the header entry element count.
   */
  public int getNumElements() {
    return _numElements;
  }

}
