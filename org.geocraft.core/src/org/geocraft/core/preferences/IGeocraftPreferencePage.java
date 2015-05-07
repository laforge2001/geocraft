package org.geocraft.core.preferences;


import java.util.Map;


/**
 * Common methods for a Preference Page
 * @author hansegj
 *
 */
public interface IGeocraftPreferencePage {

  /**
   * Get the local state of a preference page. key=preference, value=state
   * <p>
   * Note: Only save the state of the local preference store for the page,
   * not the state of the global preference store. The latter is saved by
   * the workspace.
   * @return A map of preferences and their state values
   */
  public Map<String, String> getPreferenceState();

  /**
   * Set the local state of a preference page.
   * <p>
   * Note: Only restore the state of the local preference store for the page,
   * not the state of the global preference store. The latter is restored by
   * the workspace.
   * @param prefs A map of preferences and their state values
   */
  public void setPreferenceState(Map<String, String> prefs);
}
