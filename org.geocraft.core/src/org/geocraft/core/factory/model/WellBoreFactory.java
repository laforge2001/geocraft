/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


/**
 * The only way to create a WellBore object is via this Factory.
 */
public class WellBoreFactory {//implements IEntityFactory {

  //  /** The logger. */
  //  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(WellBoreFactory.class);
  //
  //  private static WellBoreFactory _factory;
  //
  //  private WellBoreFactory() {
  //    // singleton
  //  }
  //
  //  public static synchronized WellBoreFactory getInstance() {
  //    if (_factory == null) {
  //      _factory = new WellBoreFactory();
  //    }
  //    return _factory;
  //  }
  //
  //  public Entity create(final Entity prototype, final String name) {
  //
  //    WellBore prototypeWellBore = (WellBore) prototype;
  //
  //    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
  //    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
  //
  //    WellBore wellBore = new WellBore(name, mapper);
  //    wellBore.setWell(prototypeWellBore.getWell());
  //
  //    updateBore(wellBore, prototypeWellBore, false);
  //
  //    return wellBore;
  //  }
  //
  //  public Entity create(final Entity prototype, final IMapper mapper, final String name) {
  //    WellBore prototypeWellBore = (WellBore) prototype;
  //    WellBore wellBore = new WellBore(name, mapper);
  //    wellBore.setWell(prototypeWellBore.getWell());
  //
  //    updateBore(wellBore, prototypeWellBore, false);
  //
  //    // If the Identifier parameter is a custom property of the property panel, use that one
  //    // else if no value is supplied by the property panel, rely on the prototype 
  //    // TODO: String identifier = mapper.getProperties().getProperty("Custom-Identifier", prototypeWellBore.getIdentifier());
  //    //wellBore.setIdentifier(identifier);
  //
  //    // Hmmmm  
  //    wellBore.setProjectName(prototypeWellBore.getProjectName());
  //
  //    return wellBore;
  //  }
  //
  //  public static WellBore create(final IRepository repository, final WellBore prototype, final String name,
  //      final Map<String, FloatMeasurementSeries> traceMap) {
  //    // Find the datastore accessor service. This will be used to replicate
  //    // the prototype mapper.
  //    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
  //    if (factory == null) {
  //      throw new RuntimeException("Datastore service not available.");
  //    }
  //
  //    // Create a new mapper based on the prototype mapper.
  //    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
  //    if (mapper == null) {
  //      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
  //    }
  //
  //    // Get all the grids in the repository.
  //    Map<String, Object> map = repository.get(new TypeSpecification(WellBore.class));
  //
  //    // Loop thru each one comparing its unique ID to the one to be created.
  //    for (Object object : map.values()) {
  //      WellBore bore = (WellBore) object;
  //      // Returns the existing grid if its unique ID matches.
  //      if (bore.getUniqueID().equals(mapper.getUniqueID())) {
  //        updateBore(bore, prototype, true);
  //        return bore;
  //      }
  //    }
  //
  //    // If no existing bore found, create and return a new bore backed by the new mapper.
  //    return create(repository, prototype, name, mapper, traceMap);
  //  }
  //
  //  /**
  //   * @param repository
  //   * @param name
  //   * @param mapper
  //   * @return
  //   */
  //  public static WellBore create(final IRepository repository, final WellBore boreProto, final String name,
  //      final IMapper mapper, final Map<String, FloatMeasurementSeries> traceMap) {
  //    try {
  //      WellBore wellBore = new WellBore(name, mapper);
  //      updateBore(wellBore, boreProto, false);
  //
  //      if (traceMap != null) {
  //        for (Entry<String, FloatMeasurementSeries> s : traceMap.entrySet()) {
  //          addWellLogTrace(repository, s.getKey(), s.getValue(), wellBore);
  //        }
  //      }
  //
  //      // Check if the grid exists in the datastore.
  //      if (!wellBore.getMapper().existsInStore()) {
  //        // If not, create it first.
  //        wellBore.getMapper().create(wellBore);
  //      } else {
  //        wellBore.update();
  //      }
  //
  //      repository.add(wellBore.getWell());
  //      // add the well bore to the repository
  //      repository.add(wellBore);
  //      // Now add the traces
  //      repository.add(wellBore.getWellLogTraces());
  //      return wellBore;
  //    } catch (Exception e) {
  //      LOGGER.error(e.toString(), e);
  //    }
  //    return null;
  //  }
  //
  //  //TODO will this work with new model properties
  //  private static WellBore updateBore(final WellBore wellBore, final WellBore prototype, final boolean includeTraces) {
  //
  //    // Save off the (potentially) new name
  //    String wellBoreName = wellBore.getDisplayName();
  //
  //    //mark well bore as loaded because the next copy triggers a needless premature load 
  //    wellBore.markLoaded();
  //    String wellBoreDatastore = wellBore.getDatastore();
  //
  //    // Copy over all the attributes
  //    FactoryHelper.copyFromEntity(prototype, wellBore, false);
  //
  //    wellBore.setDepthsAndTimes(prototype.getMeasuredDepths(), prototype.getTrueVerticalDepths(), prototype
  //        .getTwoWayTimes());
  //
  //    if (includeTraces) {
  //      //updates the values in the well log traces
  //      for (WellLogTrace t : prototype.getWellLogTraces()) {
  //        wellBore.addWellLogTrace(t);
  //      }
  //    }
  //
  //    // Reset the (potentially) new name 
  //    wellBore.setDisplayName(wellBoreName);
  //
  //    wellBore.setDirty(true);
  //    wellBore.markGhost();
  //    wellBore.markLoaded();
  //    return wellBore;
  //  }
  //
  //  public static void addWellLogTrace(final IRepository repo, final String name, final FloatMeasurementSeries ftemp,
  //      final WellBore bore, final IMapper mapper) {
  //    WellLogTrace trace = new WellLogTrace(name, mapper, bore);
  //    trace.setDisplayName(name);
  //    trace.setTraceData(ftemp.getValues(), ftemp.getUnit(), ftemp.getNullValue());
  //    trace.setZValues(bore.getZValues(WellDomain.TRUE_VERTICAL_DEPTH), WellDomain.TRUE_VERTICAL_DEPTH);
  //    trace.setTraceType(name);
  //    trace.setTraceName(name);
  //    trace.setDirty(false);
  //    bore.addWellLogTrace(trace);
  //
  //    //TODO should traces be entities or value objects?
  //    repo.add(trace);
  //
  //  }
  //
  //  /**
  //   * @param string
  //   * @param ftemp
  //   * @param bore
  //   * @param nullValue
  //   */
  //  public static void addWellLogTrace(final IRepository repo, final String name, final FloatMeasurementSeries ftemp,
  //      final WellBore bore) {
  //    addWellLogTrace(repo, name, ftemp, bore, new InMemoryMapper(WellLogTrace.class));
  //  }
  //
  //  /**
  //   * Checks if an entry with the proposed name exists in the underlying
  //   * datastore of a specified bore.
  //   * 
  //   * @param bore
  //   *          the bore whose underlying datastore to check.
  //   * @param proposedName
  //   *          the name of the entry to search for.
  //   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
  //   */
  //  public static boolean existsInStore(final WellBore bore, final String proposedName) {
  //    return DataSource.existsInStore(bore, proposedName);
  //  }
}
