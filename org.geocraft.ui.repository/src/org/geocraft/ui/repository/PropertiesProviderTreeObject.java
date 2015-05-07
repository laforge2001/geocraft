/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.ui.common.tree.TreeBranch;


/**
 * Wraps an abstract properties provider (entity or value object) for insertion into the repository view tree.
 */
public class PropertiesProviderTreeObject extends TreeBranch implements IPropertiesProviderContainer, IAdaptable,
    IActionFilter {

  /** The wrapped properties provider. */
  private final IPropertiesProvider _propProvider;

  private IPropertiesProvider _parentProvider;

  /**
   * The default constructor.
   * @param propProvider to wrap
   */
  public PropertiesProviderTreeObject(final IPropertiesProvider propProvider) {
    super(propProvider.getDisplayName(), propProvider.getClass().getSimpleName());
    _propProvider = propProvider;
  }

  IPropertiesProvider getParentProvider() {
    return _parentProvider;
  }

  public void setParentProvider(final IPropertiesProvider provider) {
    _parentProvider = provider;
  }

  /**
   * Returns the properties provider wrapped by this tree object.
   * 
   * @return the properties provider wrapped by this tree object
   */
  public IPropertiesProvider getPropertiesProvider() {
    return _propProvider;
  }

  @Override
  public Object getAdapter(final Class key) {
    if (key.equals(IPropertySource.class)) {
      return new PropertiesProviderFieldPropertySource(_propProvider);
    }
    return null;
  }

  public boolean testAttribute(final Object object, final String filterName, final String filterValue) {
    if (!(object instanceof PropertiesProviderTreeObject)) {
      return false;
    }
    PropertiesProviderTreeObject entityObject = (PropertiesProviderTreeObject) object;
    if (filterName.equals("entityTypes")) {
      String entityType = entityObject.getPropertiesProvider().getClass().getName();
      List<String> supportedTypes = Arrays.asList(filterValue.split(",( )+"));
      return supportedTypes.contains(entityType);
    }

    if (filterName.equals("isDirty")) {
      if (entityObject.getPropertiesProvider() instanceof Entity) {
        Entity testMe = (Entity) entityObject.getPropertiesProvider();
        if (testMe.isDirty()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (_propProvider == null || !(o instanceof PropertiesProviderTreeObject)
        || ((PropertiesProviderTreeObject) o).getPropertiesProvider() == null) {
      return false;
    }
    PropertiesProviderTreeObject otherTreeObject = (PropertiesProviderTreeObject) o;
    if (_parentProvider == null && otherTreeObject.getParentProvider() != null || _parentProvider != null
        && otherTreeObject.getParentProvider() == null) {
      return false;
    }
    return _propProvider.equals(((PropertiesProviderTreeObject) o).getPropertiesProvider())
        && (_parentProvider == null && otherTreeObject.getParentProvider() == null || _parentProvider
            .equals(otherTreeObject.getParentProvider()));
  }

  @Override
  public int hashCode() {
    if (_propProvider == null) {
      return 0;
    }
    return _propProvider.hashCode();
  }

}
