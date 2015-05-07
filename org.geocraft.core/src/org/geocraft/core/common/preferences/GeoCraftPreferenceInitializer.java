/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;


public class GeoCraftPreferenceInitializer extends AbstractPreferenceInitializer {

  public GeoCraftPreferenceInitializer() {
    // do nothing
  }

  @Override
  public void initializeDefaultPreferences() {
    IEclipsePreferences node = new DefaultScope().getNode("org.geocraft.product");

    // Set the workspace selection dialog to open by default
    node.putBoolean(IPreferenceConstants.P_SHOW_WORKSPACE_SELECTION_DIALOG, true);
  }

}
