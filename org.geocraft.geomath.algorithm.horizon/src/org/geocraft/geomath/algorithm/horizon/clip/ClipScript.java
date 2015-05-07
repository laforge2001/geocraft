/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.clip;


import java.util.List;

import org.geocraft.core.common.math.Clip.ClipType;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.geomath.algorithm.AlgorithmScript;
import org.geocraft.core.shell.IScriptExecutor;


public class ClipScript extends AlgorithmScript implements IScriptExecutor {

  public ClipScript() {
    // empty for now
  }

  @Override
  public void executeScript(final List<String> parms) {

    // get parameters
    String inputHorizonStr = parms.get(0);
    float minVal = Float.parseFloat(parms.get(1));
    float maxVal = Float.parseFloat(parms.get(2));
    String clipTypeStr = parms.get(3);
    String areaOfInterestStr = parms.get(4);
    String outputHorizonName = parms.get(5);
    float clipConstant = Float.parseFloat(parms.get(6));

    // Determine the clip algorithm
    GridClip clip = new GridClip();

    // Determine the repository
    IRepository repository = ServiceProvider.getRepository();

    // Set the parameters
    clip._inputGrid.set((Grid3d) repository.get(inputHorizonStr));
    clip._aoi.set((AreaOfInterest) repository.get(areaOfInterestStr));
    clip._clipMin.set(minVal);
    clip._clipMax.set(maxVal);
    clip._clipType.set((ClipType) restoreEnum(clipTypeStr, ClipType.class));
    clip._outputGridName.set(outputHorizonName);
    clip._clipConstant.set(clipConstant);

    runAlgorithm(clip, "Clip");
  }

}
