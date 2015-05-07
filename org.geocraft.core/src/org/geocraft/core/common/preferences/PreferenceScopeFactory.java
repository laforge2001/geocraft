/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;


public class PreferenceScopeFactory {

  public static IScopeContext getScope(final String scope) {
    if (scope.equals(InstanceScope.SCOPE)) {
      return new InstanceScope();
    }
    if (scope.equals(ConfigurationScope.SCOPE)) {
      return new ConfigurationScope();
    }
    if (scope.equals(DefaultScope.SCOPE)) {
      return new DefaultScope();
    }
    return null;
  }

}
