/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.mediansmooth;


import java.util.List;

import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.geomath.algorithm.AlgorithmScript;
import org.geocraft.geomath.algorithm.horizon.mediansmooth.MedianSmooth.FilterType;
import org.geocraft.geomath.algorithm.horizon.mediansmooth.MedianSmooth.InterpolateOption;
import org.geocraft.core.shell.IScriptExecutor;


public class MedianSmoothScript extends AlgorithmScript implements IScriptExecutor {

  public MedianSmoothScript() {
    // empty for now
  }

  @Override
  public void executeScript(final List<String> parms) {

    // get parameters
    String inputHorizonStr = parms.get(0);
    int colFilterSize = Integer.parseInt(parms.get(1));
    int rowFilterSize = Integer.parseInt(parms.get(2));
    int squareFilterSize = Integer.parseInt(parms.get(3));
    String interpolateOptionStr = parms.get(4);
    String maskStr = parms.get(5);
    String outputNameStr = parms.get(6);

    // Determine the repository
    IRepository repository = ServiceProvider.getRepository();

    // Set the horizon (Determine whether the horizon is 3D or 2D)
    Object object = repository.get(inputHorizonStr);
    if (object == null) {
      throw new RuntimeException("Invalid input Horizon: " + inputHorizonStr);

    } else if (object instanceof Grid3d) {
      MedianSmooth3d medianSmooth = new MedianSmooth3d();
      medianSmooth._inputGrid.set((Grid3d) repository.get(inputHorizonStr));
      medianSmooth._maskGrid.set((Grid3d) repository.get(maskStr));

      // determine if the user is using a mask Grid
      if (medianSmooth._maskGrid.get() != null) {
        medianSmooth._useMaskGrid.set(true);
      } else {
        medianSmooth._useMaskGrid.set(false);
      }

      // Set the other parameters
      medianSmooth._filterType.set(FilterType.COLS_ROWS);
      medianSmooth._colFilterSize.set(colFilterSize);
      medianSmooth._rowFilterSize.set(rowFilterSize);
      medianSmooth._interpolationOption.set((InterpolateOption) restoreEnum(interpolateOptionStr,
          InterpolateOption.class));
      medianSmooth._outputGridName.set(outputNameStr);

      // determine if the user wants to use a square filter
      if (squareFilterSize > 0) {
        medianSmooth._filterType.set(FilterType.SQUARE_FILTER);
        medianSmooth._squareFilterSize.set(squareFilterSize);
      }

      runAlgorithm(medianSmooth, "MedianSmooth");

    } else if (object instanceof Grid2d) {
      MedianSmooth2d medianSmooth = new MedianSmooth2d();
      medianSmooth._inputGrid.set((Grid2d) repository.get(inputHorizonStr));
      medianSmooth._maskGrid.set((Grid2d) repository.get(maskStr));

      // determine if the user is using a mask Grid
      if (medianSmooth._maskGrid.get() != null) {
        medianSmooth._useMaskGrid.set(true);
      } else {
        medianSmooth._useMaskGrid.set(false);
      }

      // Set the other parameters
      medianSmooth._cdpFilterSize.set(squareFilterSize);
      medianSmooth._interpolationOption.set((InterpolateOption) restoreEnum(interpolateOptionStr,
          InterpolateOption.class));
      medianSmooth._outputGridName.set(outputNameStr);

      runAlgorithm(medianSmooth, "MedianSmooth");
    }
  }
}
