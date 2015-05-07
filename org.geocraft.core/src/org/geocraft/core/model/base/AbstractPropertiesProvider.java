/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.base;


import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.aoi.ZRange;
import org.geocraft.core.model.aoi.ZRangeConstant;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.well.WellCheckShot;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.service.ServiceProvider;


public abstract class AbstractPropertiesProvider implements IPropertiesProvider {

  private static List<Class<?>> _candidateTypes = new ArrayList<Class<?>>();
  static {
    _candidateTypes.add(int.class);
    _candidateTypes.add(long.class);
    _candidateTypes.add(float.class);
    _candidateTypes.add(double.class);
    _candidateTypes.add(boolean.class);
    _candidateTypes.add(String.class);
    // _candidateTypes.add(String[].class);
    _candidateTypes.add(Timestamp.class);
    _candidateTypes.add(Color.class);
    _candidateTypes.add(Coordinate.class);
    _candidateTypes.add(FloatRange.class);
    _candidateTypes.add(ZRange.class);
    _candidateTypes.add(ZRangeConstant.class);

    // _candidateTypes.add(FloatMeasurement.class);

    //_candidateTypes.add(Coordinate.class);

    _candidateTypes.add(Unit.class);
    _candidateTypes.add(Domain.class);
    _candidateTypes.add(OnsetType.class);
    //_candidateTypes.add(Object[].class);
    //_candidateTypes.add(GeologicFeature.FeatureType.class);
    //_candidateTypes.add(FeatureRepresentationProperty[].class);
    //_candidateTypes.add(FloatMeasurementSeries.class);
    //_candidateTypes.add(Properties.class);
    _candidateTypes.add(StorageOrder.class);
    _candidateTypes.add(StorageFormat.class);
    _candidateTypes.add(StorageOrganization.class);
    //    _candidateTypes.add(CoordinateSeries.class);
    //    _candidateTypes.add(SpatialExtent.class);
    //    _candidateTypes.add(PostStack3d[].class);
    //    _candidateTypes.add(WellLogTrace[].class);
    //    _candidateTypes.add(WellPick[].class);
    _candidateTypes.add(WellDomain.class);
    _candidateTypes.add(WellCheckShot.class);
  }

  public AbstractPropertiesProvider() {
    // The no-argument constructor.
  }

  public Object[][] getDisplayableProperties() {
    List<String> names = new ArrayList<String>();
    List<Object> values = new ArrayList<Object>();
    Object[] arguments = new Object[] {};
    for (Method method : this.getClass().getMethods()) {
      if (isCandidate(method)) {
        try {
          Object result = method.invoke(this, arguments);
          names.add(method.getName().replaceAll("get", ""));
          values.add(result);
        } catch (InvocationTargetException e) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(
              "Problem dynamically getting property: " + method + " " + e.getTargetException().toString());
          e.printStackTrace();
        } catch (Exception e) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(
              "Problem dynamically getting property: " + method + " " + e.toString());
          e.printStackTrace();
        }
      }
    }
    Object[][] array = new Object[names.size()][2];
    for (int i = 0; i < names.size(); i++) {
      array[i][0] = names.get(i);
      array[i][1] = values.get(i);
    }
    return array;
  }

  /**
   * Returns a flag indicating if the method is a candidate for a
   * property that can be displayed.
   * @param method the method to check.
   * @return <i>true</i> if is candidate method; <i>false</i> if not.
   */
  private boolean isCandidate(final Method method) {

    // Ignore any methods that require arguments.
    if (method.getParameterTypes().length > 0) {
      return false;
    }

    // Don't confuse the users with mappers.
    if (method.getName().equals("getMapper")) {
      return false;
    }

    // Don't confuse the users with mappers.
    if (method.getName().equals("getType")) {
      return false;
    }

    // Ignore methods related to the load status.
    if (method.getName().equals("isLoaded") || method.getName().endsWith("isLoading")
        || method.getName().equals("isGhost")) {
      return false;
    }

    // Does it look like an accessor method?
    if (!(method.getName().startsWith("get") || method.getName().startsWith("is"))) {
      return false;
    }

    // Does it return an entity or a value object?
    if (AbstractPropertiesProvider.class.isAssignableFrom(method.getReturnType())) {
      return true;
    }

    // Does it return a primitive type we are interested in?
    Class<?> returnType = method.getReturnType();
    return _candidateTypes.contains(returnType);
  }

  /**
   * Get properties descriptor of this property provider. If it hasn't been generated 
   * before, create it from the properties descriptor file (if there is one).
   * @return null if there is no properties descriptor file or the file is ill-formed;
   * otherwise, the properties descriptor.
   */
  public PropertiesDescriptor getPropertiesDescriptor() {
    String entityType = getType();
    PropertiesDescriptor propertiesDescriptor = PropertyDescriptors.getInstance().getPropertiesDescriptor(entityType);

    if (propertiesDescriptor != null) {
      return propertiesDescriptor;
    }

    // Create the properties descriptor 
    propertiesDescriptor = new PropertiesDescriptor(entityType);
    // read the entity's property descriptor file and populate the descriptor
    boolean created = propertiesDescriptor.readEntityPropertyDesc();
    if (created) {
      PropertyDescriptors.getInstance().addPropertiesDescriptor(entityType, propertiesDescriptor);
      //propertiesDescriptor.dumpDescriptor();
      return propertiesDescriptor;
    }

    return null;
  }
}
