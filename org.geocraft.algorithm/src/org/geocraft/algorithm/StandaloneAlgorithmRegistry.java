/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.algorithm;


import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;


/**
 * The collection of algorithms that are registered via the following OSGI extension point: "org.geocraft.algorithm".
 * This registry is used to build the tree of available algorithms to present to the user.
 */
public class StandaloneAlgorithmRegistry {

  public static final String ALGORITHM_REGISTRY_UPDATED = "AlgorithmRegistryUpdated";

  // TODO for now just have a singleton.  This should be replaced with a service
  private static StandaloneAlgorithmRegistry AlgorithmRegistry = new StandaloneAlgorithmRegistry();

  public static StandaloneAlgorithmRegistry getInstance() {
    return AlgorithmRegistry;
  }

  /**
   * Returns an array of algorithm descriptions for the algorithms currently registered via the OSGI extension point.
   */
  public IStandaloneAlgorithmDescription[] getAlgorithmDescriptions() {
    ArrayList<IStandaloneAlgorithmDescription> result = new ArrayList<IStandaloneAlgorithmDescription>();

    IExtensionRegistry registry = Platform.getExtensionRegistry();

    // Get the algorithms registered via the standard extension point.
    IConfigurationElement[] configElements = registry.getConfigurationElementsFor("org.geocraft.algorithm");
    for (IConfigurationElement configElement : configElements) {
      result.add(new StandaloneAlgorithmDescription(configElement));
    }

    // Get the algorithms registered via the programmatic provider extension point.
    configElements = registry.getConfigurationElementsFor("org.geocraft.algorithm.programmatic");
    for (IConfigurationElement configElement : configElements) {
      try {
        IStandaloneAlgorithmProvider provider = (IStandaloneAlgorithmProvider) configElement
            .createExecutableExtension("class");
        for (IStandaloneAlgorithmDescription algorithmDesc : provider.getAlgorithmDescriptions()) {
          result.add(algorithmDesc);
        }
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return result.toArray(new IStandaloneAlgorithmDescription[0]);
  }

  /**
   * Returns the editor input for a algorithm, given its algorithm class name.
   * @param algorithmClassName the algorithm class name.
   * @return the editor input for the algorithm.
   */
  public StandaloneAlgorithmEditorInput getEditorInput(final String algorithmClassName) {
    if (algorithmClassName == null) {
      return null;
    }
    return getEditorInput(lookupAlgorithm(algorithmClassName));
  }

  /**
   * Returns the editor input for a algorithm, given its algorithm description.
   * @param algorithmDescription the algorithm description.
   * @return the editor input for the algorithm.
   */
  public StandaloneAlgorithmEditorInput getEditorInput(final IStandaloneAlgorithmDescription algorithmDescription) {
    if (algorithmDescription == null) {
      return null;
    }
    return new StandaloneAlgorithmEditorInput(algorithmDescription);
  }

  /**
   * Finds a algorithm description based on the class name of the algorithm.
   * If no match is found, then <i>null</i> is returned.
   * @param algorithmClassName the class name of the algorithm.
   * @return the matching algorithm description.
   */
  public IStandaloneAlgorithmDescription lookupAlgorithm(final String algorithmClassName) {
    for (IStandaloneAlgorithmDescription algorithmDescription : getAlgorithmDescriptions()) {
      if (algorithmClassName.equals(algorithmDescription.getClassName())) {
        return algorithmDescription;
      }
    }
    return null;
  }
}
