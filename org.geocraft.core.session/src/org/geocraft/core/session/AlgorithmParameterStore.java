package org.geocraft.core.session;


import org.eclipse.core.runtime.preferences.InstanceScope;
import org.geocraft.core.model.Model;
import org.geocraft.core.service.ServiceProvider;
import org.osgi.service.prefs.Preferences;


/**
 * Restores the parameters that were set in any visible algorithm panels. 
 */
public class AlgorithmParameterStore extends AbstractStore {

  /** The name for the preferences node under which to store algorithm parameters. */
  private static final String NODE_NAME = "algorithm.parameter";

  /** The preferences node under which to store algorithm parameters. */
  private static Preferences ALGORITHM_PARAMETER_NODE = new InstanceScope().getNode(NODE_NAME);

  /**
   * Saves the properties of the given model under the algorithm parameters node.
   * 
   * @param model the model to save.
   * @throws Exception thrown if any error occurs saving the model.
   */
  public static void save(final Model model) throws Exception {
    Preferences modelName = ALGORITHM_PARAMETER_NODE.node(model.getClass().getSimpleName());
    RepositoryIdStore.save(model, modelName);
    ALGORITHM_PARAMETER_NODE.flush();
  }

  /**
   * Restores the properties of the given model from the algorithm parameters node.
   * 
   * @param model the model to restore.
   * @throws Exception thrown if any error occurs restoring the model.
   */
  public static void restore(final Model model) throws Exception {
    if (ALGORITHM_PARAMETER_NODE.nodeExists(model.getClass().getSimpleName())) {
      Preferences modelName = ALGORITHM_PARAMETER_NODE.node(model.getClass().getSimpleName());
      RepositoryIdStore.restore(model, modelName);
    }
  }

  public static void dumpPreferences() {
    StringBuffer txt = new StringBuffer("Algorithm Parameter Session Contents\n");
    txt = dumpPreferences(ALGORITHM_PARAMETER_NODE, "  ", txt);
    ServiceProvider.getLoggingService().getLogger(AlgorithmParameterStore.class).debug(txt.toString());
  }

}
