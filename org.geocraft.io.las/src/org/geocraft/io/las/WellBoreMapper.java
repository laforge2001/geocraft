/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;




public class WellBoreMapper { // extends AbstractMapper {

  //  /** The logger. */
  //  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(WellBoreMapper.class);
  //
  //  private final WellMapperModel _model;
  //
  //  private final List<WellLogTrace> _hiddenLogTraces = new ArrayList<WellLogTrace>();
  //
  //  public WellBoreMapper(final WellMapperModel model) {
  //    _model = model;
  //  }
  //
  //  public MapperModel getModel() {
  //    return new WellMapperModel(_model);
  //  }
  //
  //  @Override
  //  protected MapperModel getInternalModel() {
  //    return _model;
  //  }
  //
  //  public IMapper factory(final MapperModel mapperModel) {
  //    return new WellBoreMapper((WellMapperModel) mapperModel);
  //  }
  //
  //  @Override
  //  public String getDatastoreEntryDescription() {
  //    return "LAS Well Bore";
  //  }
  //
  //  public String getDatastore() {
  //    return "LAS";
  //  }
  //
  //  @Override
  //  protected void createInStore(final Entity entity) throws IOException {
  //    // Validate the entity is a well bore.
  //    validateEntity(entity);
  //
  //    String filePath = _model.getFilePath();
  //    File file = new File(filePath);
  //    if (!file.createNewFile()) {
  //      LOGGER.info("Overwriting file: " + filePath);
  //    }
  //  }
  //
  //  @Override
  //  protected void readFromStore(final Entity entity) throws IOException {
  //    // Validate the entity is a well bore.
  //    WellBore wellBore = validateEntity(entity);
  //
  //    // Call the read method on the parent well's mapper.
  //    Well well = wellBore.getWell();
  //    well.getMapper().read(wellBore, null);
  //  }
  //
  //  @Override
  //  protected void updateInStore(final Entity entity) throws IOException {
  //    // Validate the entity is a well bore.
  //    WellBore wellBore = validateEntity(entity);
  //
  //    // Check the format to write (default is ASCII).
  //    File file = new File(_model.getFilePath());
  //
  //    // Create a backup copy of the original file.
  //    createBackup(file);
  //
  //    // Check if the specified file already exists.
  //    if (file.exists()) {
  //      // Check if the specified file is actually a directory.
  //      if (file.isDirectory()) {
  //        throw new IOException("The path represents a directory, not a las file.");
  //      }
  //      // Attempt to delete the existing file.
  //      if (!file.delete()) {
  //        throw new IOException("Could not delete the existing las file.");
  //      }
  //    }
  //
  //    WellMapperModel wellMapperModel = (WellMapperModel) wellBore.getMapper().getModel();
  //
  //    //we need to write out all the columns - not just the selected ones
  //    //so add back in the hidden log traces to the bore
  //    for (WellLogTrace trace : _hiddenLogTraces) {
  //      wellBore.addWellLogTrace(trace);
  //    }
  //
  //    //write out the file - any exception will restore the file and rethrow 
  //    try {
  //      LasWriter.write(wellBore, wellMapperModel);
  //    } catch (Exception ex) {
  //      // An error occurred, so restore the backup copy of the original file.
  //      LOGGER.error("Error detected writing LAS file. Restoring to previous state...");
  //      restoreBackup(file);
  //      throw new IOException(ex);
  //    }
  //
  //    //hide the unselected curves again
  //    for (WellLogTrace trace : _hiddenLogTraces) {
  //      wellBore.removeWellLogTrace(trace);
  //    }
  //
  //    WellMapper wellMapper = new WellMapper(wellMapperModel);
  //
  //    // Create the well entity.
  //    Well well = new Well(wellBore.getDisplayName(), wellMapper);
  //
  //    // Check if the well entity already exists in the repository...
  //    ISpecification filter = new TypeSpecification(Well.class);
  //    Map<String, Object> map = ServiceProvider.getRepository().get(filter);
  //    Collection<Object> objects = map.values();
  //    for (Object object : objects) {
  //      Well temp = (Well) object;
  //      if (temp.getUniqueID().equals(well.getUniqueID())) {
  //        well = temp;
  //      }
  //    }
  //
  //    wellBore.setWell(well);
  //    wellBore.setDirty(false);
  //    well.removeAllWellBores();
  //    well.addWellBore(wellBore);
  //
  //    String[] newSelectedColumns = new String[wellBore.getWellLogTraces().length];
  //
  //    WellLogTrace[] oldWellLogTraces = wellBore.getWellLogTraces();
  //    for (int i = 0; i < oldWellLogTraces.length; ++i) {
  //      WellLogTraceMapperModel traceMapperModel = new WellLogTraceMapperModel();
  //      traceMapperModel.setValueObject(WellLogTraceMapperModel.WELL_MAPPER_MODEL, wellMapperModel);
  //      traceMapperModel.setValueObject(WellLogTraceMapperModel.TRACE_DISPLAY_NAME, oldWellLogTraces[i].getDisplayName());
  //      wellBore.addWellLogTrace(WellLogTraceFactory.create(oldWellLogTraces[i].getDisplayName(), new WellLogTraceMapper(
  //          traceMapperModel), wellBore));
  //      newSelectedColumns[i] = oldWellLogTraces[i].getDisplayName() + " (" + oldWellLogTraces[i].getDataUnit() + ")";
  //    }
  //
  //    //TODO LAS load task will look at the selected logs property to determine which logs to load
  //    //so the property in the model must be updated - is this the right place to do this?
  //    ((WellBoreMapper) wellBore.getMapper()).updateSelectedLogs(newSelectedColumns);
  //
  //    //TODO LAS load task will look at the entire list of logs property to determine which logs to load
  //    //so the property in the model must be updated - is this the right place to do this?
  //    List<String> allColumnNames = new ArrayList<String>();
  //    for (WellLogTrace trace : _hiddenLogTraces) {
  //      allColumnNames.add(trace.getDisplayName() + " (" + trace.getDataUnit() + ")");
  //    }
  //    allColumnNames.addAll(Arrays.asList(newSelectedColumns));
  //    ((WellBoreMapper) wellBore.getMapper()).updateAllLogs(allColumnNames.toArray(new String[0]));
  //
  //    //TODO LAS load task uses the well name property in the mapper model to determine the name of the well
  //    //so the property in the model must be updated - is this the right place to do this?
  //    ((WellBoreMapper) wellBore.getMapper()).updateWellName(wellBore.getDisplayName());
  //
  //  }
  //
  //  @Override
  //  protected void deleteFromStore(final Entity entity) throws IOException {
  //    // Validate the entity is a well bore.
  //    WellBore bore = validateEntity(entity);
  //
  //    // Call the delete method on the parent well's mapper.
  //    Well well = bore.getWell();
  //    well.getMapper().delete(bore);
  //  }
  //
  //  public void hideUnselectedLog(final WellLogTrace trace) {
  //    _hiddenLogTraces.add(trace);
  //    trace.getWellBore().removeWellLogTrace(trace);
  //  }
  //
  //  /**
  //   * Restores a backup copy of the LAS file.
  //   * 
  //   * @param file the LAS file.
  //   * @throws IOException thrown on copy error.
  //   */
  //  private void restoreBackup(final File file) throws IOException {
  //    FileUtil.copy(file.getAbsolutePath() + "_bak", file.getAbsolutePath());
  //  }
  //
  //  /**
  //   * Creates a backup copy of the LAS file.
  //   * 
  //   * @param file the LAS file.
  //   * @throws IOException thrown on copy error.
  //   */
  //  private void createBackup(final File file) throws IOException {
  //    FileUtil.copy(file.getAbsolutePath(), file.getAbsolutePath() + "_bak");
  //  }
  //
  //  /**
  //   * Validates the given entity is a well bore.
  //   * 
  //   * @param entity the entity to check.
  //   * @return the entity as a well bore.
  //   * @throws IllegalArgumentException if the entity is not a well bore.
  //   */
  //  private WellBore validateEntity(final Entity entity) {
  //    if (WellBore.class.isAssignableFrom(entity.getClass())) {
  //      return (WellBore) entity;
  //    }
  //    throw new IllegalArgumentException("Invalid entity type: " + entity.getClass());
  //  }
  //
  //  public void updateWellName(final String name) {
  //    _model.setValueObject(WellMapperModel.WELL_NAME, name);
  //  }
  //
  //  public void updateSelectedLogs(final String[] selectedLogs) {
  //    _model.setValueObject(WellMapperModel.SELECTED_COLUMN_NAMES, selectedLogs);
  //  }
  //
  //  public void updateAllLogs(final String[] selectedLogs) {
  //    _model.setValueObject(WellMapperModel.COLUMN_NAMES, selectedLogs);
  //  }

}
