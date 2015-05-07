package org.geocraft.core.service;


import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.algorithm.IAlgorithmsService;
import org.geocraft.core.service.color.IColorFormatService;
import org.geocraft.core.service.color.IColorMapService;
import org.geocraft.core.service.logging.ILoggingService;
import org.geocraft.core.service.logging.NullLoggingService;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.viewer.IViewersService;


/**
 * This class provides static methods for accessing the <i>CORE</i> services.
 * <p>
 * <i>CORE</i> services are those services used not only by the application
 * level components, but within the core components themselves. For example, the
 * logging and messaging services are used by classes within the
 * <code>core.model</code> bundle to record exceptions and broadcast
 * notifications when entities are updated, added to the repository, etc.
 */
public class ServiceProvider {

  /** The repository service. */
  private static IRepository _repository;

  /** The logging service. */
  private static ILoggingService _loggingService = new NullLoggingService();

  /** The message service. */
  private static IMessageService _messageService;

  /** The active algorithms service. */
  private static IAlgorithmsService _algorithmService;

  /** The active viewers service. */
  private static IViewersService _viewerService;

  /** The datastore accessor service. */
  private static IDatastoreAccessorService _datastoreAccessorService;

  /** The color format service. */
  private static IColorFormatService _colorFormatService;

  /** The color map service. */
  private static IColorMapService _colorMapService;

  /**
   * Returns the repository service.
   * 
   * @return the repository service.
   */
  public static IRepository getRepository() {
    return _repository;
  }

  /**
   * Sets the repository service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param repository
   *          the repository service.
   */
  public synchronized void setRepository(final IRepository repository) {
    _repository = repository;
  }

  /**
   * Unsets the repository service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetRepsitory() {
    _repository = null;
  }

  /**
   * Returns the logging service.
   * 
   * @return the logging service.
   */
  public static ILoggingService getLoggingService() {
    return _loggingService;
  }

  /**
   * Sets the logging service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param loggingService
   *          the logging service.
   */
  public synchronized void setLoggingService(final ILoggingService loggingService) {
    _loggingService = loggingService;
  }

  /**
   * Unsets the logging service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetLoggingService() {
    _loggingService = null;
  }

  /**
   * Returns the message service.
   * 
   * @return the message service.
   */
  public static IMessageService getMessageService() {
    return _messageService;
  }

  /**
   * Sets the message service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param messageService
   *          the message service.
   */
  public synchronized void setMessageService(final IMessageService messageService) {
    _messageService = messageService;
  }

  /**
   * Unsets the message service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetMessageService() {
    _messageService = null;
  }

  /**
   * Get the active algorithm service.
   * 
   * @return the algorithm service.
   */
  public static IAlgorithmsService getAlgorithmsService() {
    return _algorithmService;
  }

  /**
   * Sets the algorithm service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param algorithmService
   *          the algorithm service.
   */
  public synchronized void setAlgorithmsService(final IAlgorithmsService algorithmService) {
    _algorithmService = algorithmService;
  }

  /**
   * Unsets the algorithm service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetAlgorithmsService() {
    _algorithmService = null;
  }

  /**
   * Get the active viewer service.
   * 
   * @return the viewer service.
   */
  public static IViewersService getViewersService() {
    return _viewerService;
  }

  /**
   * Sets the viewer service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param viewerService
   *          the viewer service.
   */
  public synchronized void setViewersService(final IViewersService viewerService) {
    _viewerService = viewerService;
  }

  /**
   * Unsets the viewer service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetViewersService() {
    _viewerService = null;
  }

  /**
   * Get the active datastore accessor service.
   * 
   * @return the datastore accessor service.
   */
  public static IDatastoreAccessorService getDatastoreAccessorService() {
    return _datastoreAccessorService;
  }

  /**
   * Sets the datastore accessor service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param datastoreAccessorService
   *          the datastore accessor service.
   */
  public synchronized void setDatastoreAccessorService(final IDatastoreAccessorService datastoreAccessorService) {
    _datastoreAccessorService = datastoreAccessorService;
  }

  /**
   * Unsets the datastore accessor service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unsetDatastoreAccessorService() {
    _datastoreAccessorService = null;
  }

  /**
   * Get the active color format service.
   * 
   * @return the color format service.
   */
  public static IColorFormatService getColorFormatService() {
    return _colorFormatService;
  }

  /**
   * Binds the color format service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param colorFormatService
   *          the color format service.
   */
  public synchronized void bindColorFormatService(final IColorFormatService colorFormatService) {
    _colorFormatService = colorFormatService;
  }

  /**
   * Unbinds the color format service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unbindColorFormatService() {
    _colorFormatService = null;
  }

  /**
   * Get the active color map service.
   * 
   * @return the color map service.
   */
  public static IColorMapService getColorMapService() {
    return _colorMapService;
  }

  /**
   * Binds the color map service.
   * <p>
   * This is called automatically by OSGI.
   * 
   * @param colorMapService
   *          the color map service.
   */
  public synchronized void bindColorMapService(final IColorMapService colorMapService) {
    _colorMapService = colorMapService;
  }

  /**
   * Unbinds the color map service.
   * <p>
   * This is called automatically by OSGI.
   */
  public synchronized void unbindColorMapService() {
    _colorMapService = null;
  }
}
