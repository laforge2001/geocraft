package org.geocraft.ui.chartviewer.data;



public class AbstractChartData implements IChartData {

  private String _name;

  public AbstractChartData(String name) {
    _name = name;
  }

  public String getDisplayName() {
    return _name;
  }

}
