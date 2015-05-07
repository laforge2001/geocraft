/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


public interface ILasModelListViewer {

  /**
   * update the view to reflect that a model was added to 
   * the model list
   * @param model
   */
  public void addModel(LasMnemonicDescriptionModel model);

  /**
   * update the view to reflect that a model was removed from 
   * the model list
   * @param model
   */
  public void removeModel(LasMnemonicDescriptionModel model);

  /**
   * update the view to reflect that a model was updated in 
   * the model list
   * @param model
   */
  public void updateModel(LasMnemonicDescriptionModel model);

}
