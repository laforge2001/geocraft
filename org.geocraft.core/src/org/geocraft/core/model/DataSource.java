/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.mapper.IMapper;


public class DataSource {

  /**
   * Validates the proposed name for an entity based on the underlying datastore of another entity.
   * 
   * @param prototypeEntity the entity whose datastore to check for validity.
   * @param proposedName the proposed name of the new entity.
   * @return the validation status of the proposed name.
   */
  public static IStatus validateName(final Entity prototypeEntity, final String proposedName) {
    IMapper mapper = prototypeEntity.getMapper();
    IStatus status = mapper.validateName(proposedName);
    return status;
  }

  public static boolean existsInStore(final Entity entity) {
    IMapper mapper = entity.getMapper();
    return mapper.existsInStore();
  }

  public static boolean existsInStore(final Entity prototypeEntity, final String proposedName) {
    IMapper mapper = prototypeEntity.getMapper();
    return mapper.existsInStore(proposedName);
  }

  //  public static QueryResultSet select(final String dataStore, final String projectName, final String[] selectColumnsGC,
  //      final String tableNameGC, final String whereClauseGC) {
  //  }
  //
  //  public static int delete(final String dataStore, final String projectName, final String[] tableNameGC,
  //      final String whereClauseGC) {
  //  }
}
