/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.internal.core.factory;


import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

import org.geocraft.core.model.Entity;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class FactoryHelper {

  /**
   * Copies the setable post-creation state of the source entity to this entity. This is a shallow
   * copy -- does not copy states of the parent nor any children. The derived entities that have
   * bulk data will need to override this method to copy the bulk data. The simple attributes will
   * be copied generically in the Entity base class.
   * @param source
   * @param copyBulkData
   */

  public static void copyFromEntity(final Entity source, final Entity destination, final boolean copyBulkData) {
    if (!destination.getClass().equals(source.getClass())) {
      ServiceProvider.getLoggingService().getLogger(FactoryHelper.class).error(
          "Source object and destination object are not of the same type.");
      return;
    }

    Method[] destinationMethods = destination.getClass().getMethods();
    Method[] sourceMethods = source.getClass().getMethods();
    Hashtable<String, Method> destinationSetMethods = new Hashtable<String, Method>();
    Hashtable<String, Method> sourceGetMethods = new Hashtable<String, Method>();
    for (Method meth : destinationMethods) {
      if (meth.getName().startsWith("set")) {
        destinationSetMethods.put(meth.getName().substring(3), meth);
      }
    }
    for (Method meth : sourceMethods) {
      if (meth.getName().startsWith("get")) {
        sourceGetMethods.put(meth.getName().substring(3), meth);
      }
    }

    Object[] destinationSetArgs = new Object[1];
    String methodRootName = "";
    ILogger logger = ServiceProvider.getLoggingService().getLogger(FactoryHelper.class);
    try {
      for (Enumeration<String> e = destinationSetMethods.keys(); e.hasMoreElements();) {
        methodRootName = e.nextElement();
        Method sourceGetMethod = sourceGetMethods.get(methodRootName);
        Method destinationSetMethod = destinationSetMethods.get(methodRootName);
        if (sourceGetMethod == null || destinationSetMethod == null) {
          continue;
        }

        // Embedding tight try/catch because if just one of these sets fails it needs to continue
        try {
          Object res = sourceGetMethod.invoke(source, (Object[]) null);
          destinationSetArgs[0] = res;
          destinationSetMethod.invoke(destination, destinationSetArgs);
        } catch (Exception ie) {
          logger.info("Problem dynamically getting and/or setting properties (" + methodRootName + "): ");
          logger.info(ie.getLocalizedMessage());
          //ServiceProvider.getLoggingService().getLogger(getClass()).info(ie.getMessage());
        }
      }
    } catch (Exception e) {
      logger.info("Problem dynamically getting and/or setting properties (" + methodRootName + "): ", e);
      logger.info(e.getLocalizedMessage());
      logger.info(e.getMessage(), e);
    }
  }

}
