/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo;


import org.geocraft.abavo.crossplot.IABavoCrossplot;


public interface IABavoCrossplotService {

  String[] getCrossplotKeys();

  IABavoCrossplot getCrossplot(String key);

  IABavoCrossplot[] getCrossplots();

  void addCrossplot(String key, final IABavoCrossplot crossplot);

  boolean containsKey(final String key);
}
