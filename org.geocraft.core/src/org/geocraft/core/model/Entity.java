/*
 * Copyright (C) ConocoPhillips 2006 - 2007 All Rights Reserved.
 */
package org.geocraft.core.model;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.progress.SafeTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.base.AbstractPropertiesProvider;
import org.geocraft.core.model.datatypes.LoadStatus;
import org.geocraft.core.model.mapper.IMapper;
//import org.geocraft.core.service.ServiceProvider;
//import org.geocraft.core.service.message.Topic;


/**
 * This is the abstract base class for all geoscience domain objects that follow
 * the <i>Entity</i> pattern.
 * <p>
 * Objects derived from Entity are expected to have their own independent
 * lifecycle. This usually means that they will exist in a persistent datastore
 * (e.g. SEGY file on disk).
 * <p>
 * Simple objects that do not have an independent lifecycle are called <i>Value
 * Objects</i> and are located in the datatypes package. Examples include Colors
 * and raw data arrays. Unlike entities, value objects do not have a managed
 * lifecycle.
 */
public abstract class Entity extends AbstractPropertiesProvider {

  /** The timestamp of last modification (e.g. "1998-10-22 20:15:50") */
  private Timestamp _lastModifiedDate = new Timestamp(0);

  /** The name of the entity. */
  private String _displayName;

  /** The default display color of the entity. */
  private RGB _displayColor;

  /** The flag to indicate if the entity has been updated.. */
  private boolean _isDirty;

  /** The name of the project that contains this entity. */
  private String _projectName = "";

  /** The comments associated with the entity. */
  private String _comments = "";

  /** The implementation specified mapper to use for persistence. */
  protected IMapper _mapper;

  /** The flag indicating the load status of the entity. */
  protected LoadStatus _status = LoadStatus.GHOST;

  protected Map<String, Date> _timestamps;

  /**
   * The default constructor for an entity. Entities are lazy-loaded, and thus
   * the only necessary information to create one is a display name and a
   * mapper. When information is requested from an entity for the 1st time, it
   * will load its attributes from the datastore via the mapper. After that, the
   * entity is considered loaded and will not have to re-read its attributes
   * again, unless it is specifically instructed to refresh itself from the
   * datastore.
   *
   * @param displayName the display name of the entity.
   * @param mapper the mapper used for persistence.
   */
  public Entity(final String displayName, final IMapper mapper) {
    if (mapper == null) {
      //      ServiceProvider.getLoggingService().getLogger(getClass())
      //          .warn("Null mapper passed to entity: " + displayName + " in " + getClass().getName());
    }
    _displayName = displayName;
    _mapper = mapper;
    _status = LoadStatus.GHOST;
    _lastModifiedDate = new Timestamp(System.currentTimeMillis());
    _timestamps = Collections.synchronizedMap(new HashMap<String, Date>());
  }

  /**
   * Checks if an object is "equals" to this entity. At present, entities are
   * considered equal if their mappers are considered equal.
   *
   * @param object the object to test for equality.
   * @return <i>true</i> if the entities are equal; <i>false</i> if not.
   */

  @Override
  public final boolean equals(final Object object) {
    if (object == null) {
      return false;
    }

    // First check if the target object is even an entity.
    // If not, return false.
    if (!(object instanceof Entity)) {
      return false;
    }
    Entity target = (Entity) object;
    // If the entity mapper is not null, compare it
    // to the mapper of the target object.
    if (getMapper() != null) {
      return getMapper().equals(target.getMapper());
    }
    return super.equals(target.getMapper());
  }

  /**
   * Returns a hash code value for the object. Because the equals() method was
   * overridden, it is necessary to do the same for hashCode(). At present, the
   * hash code of an entity is equal to the hash code of its mapper.
   *
   * @return the hash code.
   */

  @Override
  public final int hashCode() {
    // If the entity mapper is not null, return its hash code.
    if (getMapper() != null) {
      return getMapper().hashCode();
    }
    return super.hashCode();
  }

  /**
   * Returns the display name of the entity.
   * <p>
   * This name does not have any requirement of uniqueness. It is possible for
   * one or more entities to have the same name across different datastores,
   * and it is also possible for one or more entities to have the same name in
   * the same datastore.
   *
   * @return the display name.
   */
  public final String getDisplayName() {
    if (_displayName == null || _displayName.length() == 0) {
      return "Undefined";
    }
    return _displayName;
  }

  /**
   * Sets the display name of the entity.
   * <p>
   * This name does not have any requirement of uniqueness. It is possible for
   * one or more entities to have the same name across different datastores,
   * and it is also possible for one or more entities to have the same name in
   * the same datastore.
   *
   * @param displayName the display name.
   */
  public final void setDisplayName(final String displayName) {
    _displayName = displayName;
    setDirty(true);
  }

  /**
   * Create an output display name from an input displayName and a name suffix.
   * @param displayName
   * @param nameSuffix
   * @return
   */
  public final String createOutputDisplayName(final String displayName, final String nameSuffix) {
    if (getMapper() != null) {
      return getMapper().createOutputDisplayName(displayName, nameSuffix);
    }
    return displayName + nameSuffix;
  }

  /**
   * Gets the default display color of the entity.
   *
   * @return the default display color.
   */
  public final RGB getDisplayColor() {
    // If no display color has been set, then default to red.
    if (_displayColor == null) {
      _displayColor = new RGB(255, 0, 0);
    }
    return _displayColor;
  }

  /**
   * Sets the default display color of the entity.
   *
   * @param displayColor the default display color.
   */
  public final void setDisplayColor(final RGB displayColor) {
    _displayColor = displayColor;
    setDirty(true);
  }

  /**
   * Returns the name of the project containing the entity.
   * <p>
   * Upon creation, the default project name is an empty.
   *
   * @return the project name.
   */
  public final String getProjectName() {
    load();
    return _projectName;
  }

  /**
   * Sets the name of the project containing the entity.
   * <p>
   * The project name is generally set by the mapper when the entity loads
   * itself from the datastore.
   *
   * @param projectName the project name.
   */
  public final void setProjectName(final String projectName) {
    _projectName = projectName;
    setDirty(true);
  }

  /**
   * Gets the comments associated with the entity.
   *
   * @return the comments.
   */
  public final String getComment() {
    load();
    return _comments;
  }

  /**
   * Sets the comments associated with the entity.
   *
   * @param comment the comments.
   */
  public final void setComment(final String comments) {
    _comments = comments;
    setDirty(true);
  }

  /**
   * Gets the timestamp of last modification for the entity.
   * <p>
   * This timestamp is generally set by the mapper when the entity is loaded from
   * or updated in the datastore. If the time stamp is not explicitly set it will
   * default to the start of Unix time and should never be null.
   *
   * @return the timestamp of the last modification.
   */
  public final Timestamp getLastModifiedDate() {
    load();
    return new Timestamp(_lastModifiedDate.getTime());
  }

  /**
   * Sets the timestamp of last modification for the entity.
   * <p>
   * This timestamp is generally set by the mapper when the entity is loaded from
   * or updated in the datastore. If the time stamp is not explicitly set it will
   * default to the start of Unix time and should never be null.
   *
   * @param lastModifiedDate the timestamp of last modification.
   */
  public final void setLastModifiedDate(final Timestamp lastModifiedDate) {
    if (lastModifiedDate != null) {
      _lastModifiedDate = lastModifiedDate;
    }
  }

  /**
   * Returns the type of the entity.
   * <p>
   * This is just the simple version of the class name (e.g. "PostStack3d").
   *
   * @return the type of of the entity.
   */
  public String getType() {
    return getClass().getSimpleName();
  }

  /**
   * Returns a string representation of the entity. This string is a combination
   * of the display name and the entity class name. For example, a
   * <code>PostStack3d</code> named "foo" would appear as "foo (PostStack3d)".
   * Similarly, a <code>Grid3d</code> named "bar" would appear as
   * "bar (Grid3d)".
   *
   * @return the string representation of the entity.
   */

  @Override
  public final String toString() {
    return _displayName + " (" + getType() + ")";
  }

  /**
   * Returns the "dirty" flag indicating if the entity has been edited.
   *
   * @return <i>true</i> if it needs to be saved; otherwise <i>false</i>.
   */
  public final boolean isDirty() {
    return _isDirty;
  }

  /**
   * Sets the "dirty" flag indicating if the entity has been edited. The "dirty"
   * flag is generally handled internally. Calling a <code>set</code> method on
   * an entity will set the "dirty" flag <i>true</i>, and updating an entity
   * to/from its underlying datastore will set the "dirty" flag <i>false</i>.
   * However, it is possible that users may need to manage the "dirty" flag
   * directly themselves.
   */
  public final void setDirty(final boolean isDirty) {
    _isDirty = isDirty;
  }

  /**
   * Returns the datastore mapper associated with the entity. The mapper is
   * passed into the default entity constructor and should never be null. This
   * method is used extensively within the system by factory methods and I/O
   * methods, but is not intended to be used directly by users.
   *
   * @return the datastore mapper associated with the entity.
   */
  public final IMapper getMapper() {
    return _mapper;
  }

  /**
   * Returns the name of the datastore from which this entity was loaded.
   * <p>
   * This is for informational purposes only.
   *
   * @return the name of the datastore.
   */
  public final String getDatastore() {
    load();
    return _mapper.getDatastore();
  }

  /**
   * Returns the storage directory for the entity in the underlying datastore.
   * <p>
   * This main applies to file-based datastores, and for databases this string
   * will likely be empty.
   *
   * @return the storage directory.
   */
  public final String getStorageDirectory() {
    load();
    return getMapper().getStorageDirectory();
  }

  /**
   * Loads the entity from the underlying datastore if it is not already loaded.
   * <p>
   * The persistence mapper will handle the actual reading, and will set all the
   * entity attributes it can.
   */
  public void load() {
    load(null);
  }

  /**
   * Loads the entity from the underlying datastore if it is not already loaded.
   * <p>
   * The persistence mapper will handle the actual reading, and will set all the
   * entity attributes it can.
   * <p>
   * @param listener the job change listener used when loading with a progress monitor.
   */
  public void load(final IJobChangeListener listener) {
    // If the entity is not a ghost (i.e. already loaded or loading), then simply return.
    if (!isGhost()) {
      if (listener != null) {
        listener.done(new IJobChangeEvent() {

          public long getDelay() {
            return 0;
          }

          public Job getJob() {
            return null;
          }

          public IStatus getResult() {
            return ValidationStatus.ok();
          }

        });
      }
      return;
    }
    try {
      markLoaded();
      if (listener == null) {
        read((IJobChangeListener[]) null);
      } else {
        read(new IJobChangeListener[] { listener });
      }
    } catch (IOException ex) {
      markGhost();
      // Log the exception.
      String message = "Error loading " + getType() + " \'" + toString() + "\' from datastore: " + ex.getMessage();
      //ServiceProvider.getLoggingService().getLogger(getClass()).error(message);
      // Throw a runtime exception.
      throw new RuntimeException(message);
    }
  }

  /**
   * Read the entity from the underlying datastore.
   * <p>
   * The persistence mapper will handle the actual reading, and will set all the
   * entity attributes it can. Afterwards, the status flag is set to <i>LOADED</i>
   * and the dirty flag is set to <i>false</i>.
   */
  protected void read(final IJobChangeListener... listeners) throws IOException {
    if (_mapper == null) {
      throw new IOException("The entity \'" + toString() + " does not contain a mapper.");
    }

    String taskName = "Loading: " + getDisplayName();
    if (listeners != null) {
      EntityLoadTask task = new EntityLoadTask(this);
      SafeTask job = (SafeTask) TaskRunner.runTask(task, taskName, TaskRunner.INTERACTIVE);
      for (IJobChangeListener listener : listeners) {
        job.addJobChangeListener(listener);
      }
    } else {
      IMapper mapper = getMapper();
      if (mapper != null) {
        getMapper().read(this, new NullProgressMonitor());
      }
      setDirty(false);
      //TaskRunner.runTask(task, taskName, TaskRunner.JOIN);
    }
  }

  /**
   * Updates (writes) the current state of the entity in the underlying
   * datastore.
   * <p>
   * The persistence mapper will handle the actual writing. Afterwards, the status flag
   * is set to <i>true</i> and the "dirty" flag is set to <i>false</i>.
   */
  public void update() throws IOException {
    if (!isLoaded()) {
      throw new IOException("The entity " + toString()
          + " is a ghost and cannot be updated in the datastore until is has been loaded.");
    }

    // Update the modification data just before writing to datastore.
    Timestamp lastModifiedDate = new Timestamp(System.currentTimeMillis());
    setLastModifiedDate(lastModifiedDate);
    _mapper.update(this);
    _isDirty = false;

    // Notify any listeners (e.g. viewers, etc) that the object in the repository has been updated.
    //    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECT_UPDATED, this);
  }

  /**************************************************************************/

  /**
   * Returns the ghost status of the entity.
   * <p>
   * If the entity is in a <code>GHOST</code> state, then this method returns <i>true</i>.
   * Otherwise it returns <i>false</i>.
   *
   * @return <i>true</i> if the entity is a ghost, <i>false</i> if not.
   */
  public final boolean isGhost() {
    return _status == LoadStatus.GHOST;
  }

  /**
   * Returns the loaded status of the entity.
   * <p>
   * If the entity is <code>LOADED</code> state, then this method returns <i>true</i>.
   * Otherwise it returns <i>false</i>.
   *
   * @return <i>true</i> if the entity is loaded, <i>false</i> if not.
   */
  public final boolean isLoaded() {
    return _status == LoadStatus.LOADED;
  }

  /**
   * Marks the entity as being in the <code>GHOST</code> state.
   */
  public final void markGhost() {
    _status = LoadStatus.GHOST;
  }

  /**
   * Marks the entity as being in the <code>LOADED</code> state.
   * <p>
   * The entity status must currently be loading, or else an exception is thrown.
   */
  public final void markLoaded() {
    if (!isGhost()) {
      throw new RuntimeException("The entity must be in " + LoadStatus.GHOST + " status before it can marked as "
          + LoadStatus.LOADED + ".");
    }
    _status = LoadStatus.LOADED;
  }

  /**
   * Returns the unique ID for this entity.
   *
   * @return the entity unique ID.
   */
  public final String getUniqueID() {
    // Get the unique ID from the mapper.
    IMapper mapper = getMapper();
    if (mapper == null) {
      return "";
    }
    return mapper.getUniqueID();
  }

  /**
   * Adds a date to the entity.
   *
   * @param key the date key.
   * @param date the date.
   */
  public void addDate(final String key, final Date date) {
    _timestamps.put(key, date);
  }

  /**
   * Removes a date from the entity.
   *
   * @param key the date key.
   */
  public void removeDate(final String key) {
    _timestamps.remove(key);
  }

  /**
   * Gets a date from the entity.
   *
   * @param key the date key.
   * @return the date; or <i>null</i> if none.
   */
  public Date getDate(final String key) {
    return _timestamps.get(key);
  }
}
