/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.weightedsmooth;


import java.util.List;

import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.geomath.algorithm.AlgorithmScript;
import org.geocraft.core.shell.IScriptExecutor;


public class WeightedSmoothScript extends AlgorithmScript implements IScriptExecutor {

  public WeightedSmoothScript() {
    // empty for now
  }

  @Override
  public void executeScript(final List<String> parms) {

    // get parameters
    String inputHorizonStr = parms.get(0);
    int colFilterSize = Integer.parseInt(parms.get(1));
    int rowFilterSize = Integer.parseInt(parms.get(2));
    float edgeWeight = Float.parseFloat(parms.get(3));
    boolean applyBlending = Boolean.parseBoolean(parms.get(4));
    String maskStr = parms.get(5);
    String outputNameStr = parms.get(6);

    // Determine the repository
    IRepository repository = ServiceProvider.getRepository();

    WeightedSmooth weightedSmooth = new WeightedSmooth();
    weightedSmooth._inputGrid.set((Grid3d) repository.get(inputHorizonStr));
    weightedSmooth._maskGrid.set((Grid3d) repository.get(maskStr));

    // determine if the user is using a mask Grid
    if (weightedSmooth._maskGrid.get() != null) {
      weightedSmooth._useMaskGrid.set(true);
    } else {
      weightedSmooth._useMaskGrid.set(false);
    }

    // Set the other parameters
    weightedSmooth._colFilterSize.set(colFilterSize);
    weightedSmooth._rowFilterSize.set(rowFilterSize);
    weightedSmooth._edgeWeight.set(edgeWeight);
    weightedSmooth._applyBlendingGrid.set(applyBlending);
    weightedSmooth._outputGridName.set(outputNameStr);

    // run the algorithm
    runAlgorithm(weightedSmooth, "WeightedSmooth");
  }
}
