/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.culture;


import java.util.Properties;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;


/**
 * Abstract superclass used by GIS Features. 
 */

public abstract class Feature extends Entity {

  /** True if the feature points have z-values. */
  private boolean _is3D;

  /** True if the feature points have measure values. */
  private boolean _isMeasured;

  /** The Layer that this Feature belongs to. */
  private Layer _layer;

  /** TODO 360 */
  private String[] _attributeValues;

  /** TODO 360 what are they? */
  private Properties _attributes;

  /**
   * Parameterized constructor.
   *
   * @param name of the entity.
   * @param mapper
   * @param layer this feature belongs in.
   */
  protected Feature(String name, IMapper mapper, Layer layer) {

    super(name, mapper);
    _layer = layer;

    _attributes = new Properties();
    for (String attrName : layer.getAttributeNames()) {
      _attributes.setProperty(attrName, "");
    }
  }

  /**
   * True if the feature points have z-values.
   *
   * @return is3D
   */
  public boolean is3D() {
    return _is3D;
  }

  /**
   * True if the feature points have measure values.
   *
   * @return isMeasured
   */
  public boolean isMeasured() {
    return _isMeasured;
  }

  /**
   * Reference to the Layer that this Feature belongs to.
   *
   * @return layer
   */
  public Layer getLayer() {
    return _layer;
  }

  /**
   * True if the feature points have z-values.
   *
   * @param is3D
   */
  public void setIs3D(boolean is3D) {
    _is3D = is3D;
    setDirty(true);
  }

  /**
   * True if the feature points have measure values.
   *
   * @param isMeasured
   */
  public void setIsMeasured(boolean isMeasured) {
    _isMeasured = isMeasured;
    setDirty(true);
  }

  /**
   * Reference to the Layer that this Feature belongs to.
   *
   * @param layer
   */
  public void setLayer(Layer layer) {
    _layer = layer;
    setDirty(true);
  }

  /**
   * @return The extended Attributes that features in this layer have. Refer to
   *  Layer.getAttributeValues() to get the attribute types
   */
  public Properties getAttributes() {
    return _attributes;
  }

  /**
   * @return The extended AttributeValues that features in this layer have. Refer to
   * Layer.getAttributeNames() and Layer.getAttributeValues() to get the attribute names and types
   */
  public String[] getAttributeValues() {
    return _attributeValues;
  }

  /**
   * TODO 360 really an exception? finish docs.
   * 
   * @param name    The atribute name
   * @param value  The value to assign to this attribute name
   */
  public void setAttributeValue(String name, String value) throws Exception {
    if (!_attributes.containsKey(name)) {
      throw new Exception(name + " is not a valid attribute name for features in the" + _layer.getDisplayName() + " layer");
    }
    _attributes.setProperty(name, value);
  }
}
