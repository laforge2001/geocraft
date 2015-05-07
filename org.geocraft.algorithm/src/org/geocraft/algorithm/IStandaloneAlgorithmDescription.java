/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;


public interface IStandaloneAlgorithmDescription {

  /**
   * Returns the name of the algorithm.
   */
  public abstract String getName();

  /**
   * Returns the help ID of the algorithm.
   */
  public abstract String getHelpId();

  /**
   * Returns the visibility of the algorithm in the tree.
   */
  public abstract boolean isVisible();

  /**
   * Returns the category of the algorithm, a slash-separated list of strings.
   * This list of strings represents the location of the algorithm in the tree.
   */
  public abstract String getCategory();

  /**
   * Returns the full path of the algorithm.
   * This is a composite of the category and name.
   */
  public abstract String getFullPath();

  /**
   * Returns the name of the class that represents the algorithm implementation.
   */
  public abstract String getClassName();

  /**
   * Returns the algorithm-tip that will appear over the algorithm in the tree.
   */
  public abstract String getToolTip();

  /**
   * Returns the version that will appear with the algorithm in the tree.
   */
  public abstract String getVersion();

  /**
   * Creates an implementation of the algorithm.
   */
  public abstract StandaloneAlgorithm createAlgorithm();

  /**
   * Returns the image to use as an icon for the algorithm.
   * Returns </i>null</i> if none specified.
   */
  public abstract ImageDescriptor getIcon();

  /**
   * Creates a page for editing the algorithm parameters.
   */
  public abstract StandaloneAlgorithmEditorPage createEditorPage(final SharedHeaderFormEditor sharedHeaderFormEditor);

  /**
   * Creates a page for editing the algorithm parameters.
   */
  public abstract StandaloneAlgorithmEditorPage createEditorPage(final SharedHeaderFormEditor sharedHeaderFormEditor,
      StandaloneAlgorithm algorithm);

  /**
   * Returns the name to record in usage tracking.
   */
  public abstract String getUsageName();

  public abstract String dumpDescription();

}