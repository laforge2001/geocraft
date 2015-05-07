/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A named collection of geologic features representations and associated properties representing an internally consistent opinion
 * of the sub-surface.
 * <p>
 * An EarthModel's primary domain (either Time or Depth) represents the domain in which the bulk of interpretation was done. An
 * Earth Model may, however contain feature representations from both the time and depth domains.
 * <p>
 * A seismic interpretation project that picked faults and horizons on time migrated seismic data would be an example of an Earth
 * Model with a primary domain of time (even though one may also have depth converted faults and horizons in the same project).
 */
public class EarthModel extends Entity {

  /** Comments associated with the earth model. */
  private String _description;

  /** The primary domain of this earth model. */
  private final Domain _primaryZDomain;

  public EarthModel(final String name, final Domain primaryZDomain) {
    this(name, primaryZDomain, new InMemoryMapper(EarthModel.class));
  }

  /**
   * Parameterized constructor
   * 
   * @param name the name of the earth model.
   * @param mapper the mapper
   */
  public EarthModel(final String name, final Domain primaryZDomain, final IMapper mapper) {
    super(name, mapper);
    _primaryZDomain = primaryZDomain;
  }

  /**
   * Gets the primary domain of this earth model.
   * 
   * @return the primary domain of the earth model.
   */
  public Domain getPrimaryZDomain() {
    load();
    return _primaryZDomain;
  }

  /**
   * Gets the description of the earth model.
   * 
   * @return the description.
   */
  public String getDescription() {
    load();
    return _description;
  }

  /**
   * Sets the description of the earth model.
   * 
   * @param description a description to set.
   */
  public void setDescription(final String description) {
    _description = description;
    setDirty(true);
  }

}
