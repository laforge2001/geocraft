/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IWellMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating WellLogTrace entities.
 */
public class WellLogTraceFactory {

  /**
   * Creates a WellLogTrace log trace backed by the specified mapper. This
   * factory method is used primarily by load/export tasks.
   * 
   * @param name
   *          the log trace name.
   * @param mapper
   *          the log trace mapper.
   * @param wellBore
   *          the well bore to which the log trace is associated.
   * @return the created WellLogTrace log trace.
   */
  public static WellLogTrace create(final String name, final IMapper mapper, final Well well) {
    // Create and return the new log trace.
    return new WellLogTrace(name, mapper, well);
  }

  /** 
   * create a new well log curve in the specified well based on the prototype log curve and 
   * updated data.  This was created for the Horizontal Well Parser workflow -- need a more general
   * solution to this issue (maybe create an in-memory log curve to use as a kind of container for
   * what could be consider query parameters to an INSERT).
   * 
   * Wondering if I should throw an exception here if a matching log curve is found...
   */
  public static WellLogTrace create(final IRepository repository, final WellLogTrace prototype, final Well well,
      final double[] zValues, final WellDomain wellDomain, final float[] traceData, final String creationRoutine,
      final String comment) throws IOException {

    // Get the well mapper
    IWellMapper wellMapper = (IWellMapper) well.getMapper();

    // use the well mapper and prototype details to create a wellLogTrace Mapper
    IMapper wellLogTraceMapper = wellMapper.getWellLogTraceMapper(prototype.getTraceName(), prototype.getRunNumber(),
        prototype.getTraceVersion(), prototype.getServiceName(), prototype.getLogPass());

    // Get all the log traces in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(WellLogTrace.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      WellLogTrace logTrace = (WellLogTrace) object;
      // Returns the existing log trace if its unique ID matches.
      if (logTrace.getUniqueID().equals(wellLogTraceMapper.getUniqueID())) {
        updateLogTrace(logTrace, prototype.getDisplayName(), prototype.getTraceName(), prototype.getRunNumber(),
            prototype.getTraceVersion(), prototype.getServiceName(), prototype.getLogPass(), prototype.getDataSource(),
            prototype.getDataUnit(), prototype.getNullValue(), traceData, zValues, wellDomain, creationRoutine, comment);
        return logTrace;
      }
    }

    // If no existing log trace found, create and return a new log trace backed by the new mapper.
    return create(repository, prototype.getDisplayName(), prototype.getTraceName(), wellLogTraceMapper, well,
        prototype.getRunNumber(), prototype.getTraceVersion(), prototype.getServiceName(), prototype.getLogPass(),
        prototype.getDataSource(), prototype.getDataUnit(), prototype.getNullValue(), traceData, zValues, wellDomain,
        creationRoutine, comment);

  }

  /**
   * create a new well log curve in the same well as the prototype log curve
   * @param repository
   * @param prototype
   * @param traceData
   * @param name
   * @return
   */
  public static WellLogTrace create(final IRepository repository, final WellLogTrace prototype,
      final float[] traceData, final String traceName) throws IOException {
    return create(repository, prototype, traceData, traceName, traceName);
  }

  public static WellLogTrace create(final IRepository repository, final WellLogTrace prototype,
      final float[] traceData, final String displayName, final String traceName) throws IOException {

    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper mapper = factory.createMapper(prototype.getMapper(), traceName);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the log traces in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(WellLogTrace.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      WellLogTrace logTrace = (WellLogTrace) object;
      // Returns the existing log trace if its unique ID matches.
      if (logTrace.getUniqueID().equals(mapper.getUniqueID())) {
        updateLogTrace(logTrace, displayName, traceName, prototype.getRunNumber(), prototype.getTraceVersion(),
            prototype.getServiceName(), prototype.getLogPass(), prototype.getDataSource(), prototype.getDataUnit(),
            prototype.getNullValue(), traceData, prototype.getZValues(WellDomain.MEASURED_DEPTH),
            WellDomain.MEASURED_DEPTH, prototype.getCreationRoutine(), prototype.getComment());
        return logTrace;
      }
    }

    // If no existing log trace found, create and return a new log trace backed by the new mapper.
    return create(repository, displayName, traceName, mapper, prototype.getWell(), prototype.getRunNumber(),
        prototype.getTraceVersion(), prototype.getServiceName(), prototype.getLogPass(), prototype.getDataSource(),
        prototype.getDataUnit(), prototype.getNullValue(), traceData, prototype.getZValues(WellDomain.MEASURED_DEPTH),
        WellDomain.MEASURED_DEPTH, prototype.getCreationRoutine(), prototype.getComment());
  }

  /**
   * The common factory method for creating a new log trace entity. This allows
   * for specifying the data unit, data values and depth values.
   * 
   * @param repository
   *          the repository in which to add the created log trace.
   * @param name
   *          the name of the new log trace.
   * @param mapper
   *          the mapper of the new log trace.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @param dataValues
   *          the array of data values.
   * @return the created log trace.
   * @throws IOException 
   */
  private static WellLogTrace create(final IRepository repository, final String displayName, final String traceName,
      final IMapper mapper, final Well well, final int logRunNo, final int traceVersion, final String serviceName,
      final String logPass, final String dataSource, final Unit dataUnit, final float nullValue,
      final float[] dataValues, final double[] zValues, final WellDomain zDomain, final String creationRoutine,
      final String comment) throws IOException {

    // Create the new log trace.
    WellLogTrace logTrace = new WellLogTrace(displayName, mapper, well);

    updateLogTrace(logTrace, displayName, traceName, logRunNo, traceVersion, serviceName, logPass, dataSource,
        dataUnit, nullValue, dataValues, zValues, zDomain, creationRoutine, comment);

    // Check if the log trace exists in the datastore.
    if (!mapper.existsInStore()) {
      // If not, create it first.
      logTrace.setCreationUserID(logTrace.getUpdateUserID());
      logTrace.setCreationDate(logTrace.getLastModifiedDate());
      mapper.create(logTrace);
    } else {
      logTrace.update();
    }

    // Add the log trace to the repository.
    repository.add(logTrace);
    return logTrace;
  }

  private static void updateLogTrace(final WellLogTrace logTrace, final String displayName, final String traceName,
      final int logRunNo, final int traceVersion, final String serviceName, final String logPass,
      final String dataSource, final Unit dataUnit, final float nullValue, final float[] dataValues,
      final double[] zValues, final WellDomain zDomain, final String creationRoutine, final String comment) {
    logTrace.setDisplayName(displayName);
    logTrace.setTraceName(traceName);
    logTrace.setRunNumber(logRunNo);
    logTrace.setTraceVersion(traceVersion);
    logTrace.setServiceName(serviceName);
    logTrace.setLogPass(logPass);
    logTrace.setDataSource(dataSource);
    logTrace.setTraceData(dataValues, dataUnit, nullValue);
    logTrace.setZValues(zValues, zDomain);
    logTrace.setUpdateUserID(System.getProperty("user.name"));
    logTrace.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
    logTrace.setCreationRoutine(creationRoutine);
    logTrace.setComment(comment);
    logTrace.markGhost();
    logTrace.markLoaded();
  }

  /**
   * Creates a well log with an in-memory mapper.
   * 
   * @param name
   *          The name of the new well log.
   * @param logPrototype
   *          Prototype well log to get attributes from
   * @param traceData
   *          Well log values
   * @return The created well log
   */
  public static WellLogTrace createInMemory(final String name, final WellLogTrace logPrototype, final float[] traceData) {
    try {
      // Create the new well log.
      WellLogTrace wellLog = new WellLogTrace(name, new InMemoryMapper(WellLogTrace.class), logPrototype.getWell());

      updateLogTrace(wellLog, name, name, logPrototype.getRunNumber(), logPrototype.getTraceVersion(),
          logPrototype.getServiceName(), logPrototype.getLogPass(), logPrototype.getDataSource(),
          logPrototype.getDataUnit(), logPrototype.getNullValue(), traceData,
          logPrototype.getZValues(WellDomain.MEASURED_DEPTH), WellDomain.MEASURED_DEPTH, "GeoCraft", "");

      wellLog.markGhost();
      wellLog.markLoaded();
      return wellLog;
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  /**
   * Checks if an entry with the proposed name exists in the underlying
   * datastore of a specified well log trace.
   * 
   * @param logTrace
   *          the log trace whose underlying datastore to check.
   * @param proposedName
   *          the name of the entry to search for.
   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
   */
  public static boolean existsInStore(final WellLogTrace logTrace, final String proposedName) {
    return DataSource.existsInStore(logTrace, proposedName);
  }
}
