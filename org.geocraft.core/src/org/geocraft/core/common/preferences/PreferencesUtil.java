/*
 * Copyright (C) ConocoPhillips 2007 - 2008 All Rights Reserved.
 */
package org.geocraft.core.common.preferences;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.osgi.service.prefs.BackingStoreException;


/**
 * The preferences registry.
 */
public class PreferencesUtil {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(PreferencesUtil.class);

  private PreferencesUtil() {
    // left blank
  }

  //use the preference service adapter
  public static GeocraftPreferenceService getService(final String bundleId) {
    return GeocraftPreferenceServiceFactory.getInstance(bundleId);
  }

  //returns the IEclipsePreference associated with the bundle ID. If not, then it creates one using the scope
  //provided
  public static IEclipsePreferences getPreferencesStore(final String bundleId, final String scope) {
    IPreferencesService service = Platform.getPreferencesService();
    IEclipsePreferences rootNode = service.getRootNode();
    try {
      if (!rootNode.node(scope).nodeExists(bundleId)) {
        IEclipsePreferences preferences = PreferenceScopeFactory.getScope(scope).getNode(bundleId);
        service.applyPreferences(preferences, null);
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    } catch (CoreException e) {
      e.printStackTrace();
    }

    return (IEclipsePreferences) rootNode.node(scope).node(bundleId);
  }

  /**
   * Returns the preferences store for the specified bundle.
   * @param bundleId the bundle id
   * @return the preferences store
   */
  public static IEclipsePreferences getPreferencesStore(final String bundleId) {
    return getPreferencesStore(bundleId, InstanceScope.SCOPE);
  }

  public static void saveScopePreferences(final String bundleId, final String scope) {
    try {
      getPreferencesStore(bundleId, scope).flush();
    } catch (BackingStoreException ex) {
      LOGGER.warn("Could not save preferences for bundle " + bundleId, ex);
    }
  }

  /**
   * Save the preferences store corresponding to the specifies bundle.
   * @param bundleId the bundle id
   */
  public static void saveInstanceScopePreferences(final String bundleId) {
    saveScopePreferences(bundleId, InstanceScope.SCOPE);
  }

  /**
   * Save the preferences store corresponding to the specifies bundle.
   * @param bundleId the bundle id
   */
  public static void saveConfigurationScopePreferences(final String bundleId) {
    saveScopePreferences(bundleId, ConfigurationScope.SCOPE);
  }

  /**
   * Save the preferences store corresponding to the specifies bundle.
   * @param bundleId the bundle id
   */
  public static void saveDefaultScopePreferences(final String bundleId) {
    saveScopePreferences(bundleId, DefaultScope.SCOPE);
  }

}