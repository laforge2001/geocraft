package org.geocraft.core.model;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractQueue;

import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;


public interface ITraceContainer extends Remote {

  public abstract void addTraceData(final TraceData traceData) throws RemoteException;

  public abstract AbstractQueue<TraceData> getTraceData() throws RemoteException;

  /**
   * @return the writeToSharedMemory
   */
  public abstract boolean isWriteToSharedMemory() throws RemoteException;

  /**
   * @param writeToSharedMemory the writeToSharedMemory to set
   */
  public abstract void setWriteToSharedMemory(final boolean writeToSharedMemory) throws RemoteException;

  /**
   * @return the minCrossline
   */
  public abstract int getMinCrossline() throws RemoteException;

  /**
   * @param minCrossline the minCrossline to set
   */
  public abstract void setMinCrossline(final int minCrossline) throws RemoteException;

  /**
   * @return the startCrossline
   */
  public abstract int getStartCrossline() throws RemoteException;

  /**
   * @param startCrossline the startCrossline to set
   */
  public abstract void setStartCrossline(final int startCrossline) throws RemoteException;

  /**
   * @return the maxCrossline
   */
  public abstract int getMaxCrossline() throws RemoteException;

  /**
   * @param maxCrossline the maxCrossline to set
   */
  public abstract void setMaxCrossline(final int maxCrossline) throws RemoteException;

  /**
   * @return the incCrossline
   */
  public abstract int getIncCrossline() throws RemoteException;

  /**
   * @param incCrossline the incCrossline to set
   */
  public abstract void setIncCrossline(final int incCrossline) throws RemoteException;

  /**
   * @return the minInline
   */
  public abstract int getMinInline() throws RemoteException;

  /**
   * @param minInline the minInline to set
   */
  public abstract void setMinInline(final int minInline) throws RemoteException;

  /**
   * @return the startInline
   */
  public abstract int getStartInline() throws RemoteException;

  /**
   * @param startInline the startInline to set
   */
  public abstract void setStartInline(final int startInline) throws RemoteException;

  /**
   * @return the maxInline
   */
  public abstract int getMaxInline() throws RemoteException;

  /**
   * @param maxInline the maxInline to set
   */
  public abstract void setMaxInline(final int maxInline) throws RemoteException;

  /**
   * @return the incInline
   */
  public abstract int getIncInline() throws RemoteException;

  /**
   * @param incInline the incInline to set
   */
  public abstract void setIncInline(final int incInline) throws RemoteException;

  /**
   * @return the xIsInline
   */
  public abstract boolean isxIsInline() throws RemoteException;

  /**
   * @param xIsInline the xIsInline to set
   */
  public abstract void setxIsInline(final boolean xIsInline) throws RemoteException;

  /**
   * @return the applyScaling
   */
  public abstract boolean isApplyScaling() throws RemoteException;

  /**
   * @param applyScaling the applyScaling to set
   */
  public abstract void setApplyScaling(final boolean applyScaling) throws RemoteException;

  /**
   * @return the sampleStart
   */
  public abstract float getSampleStart() throws RemoteException;

  /**
   * @param sampleStart the sampleStart to set
   */
  public abstract void setSampleStart(final float sampleStart) throws RemoteException;

  /**
   * @return the sampleStart
   */
  public abstract float getSampleEnd() throws RemoteException;

  /**
   * @param sampleStart the sampleStart to set
   */
  public abstract void setSampleEnd(final float sampleStart) throws RemoteException;

  /**
   * @return the sampleInc
   */
  public abstract float getSampleInc() throws RemoteException;

  /**
   * @param sampleInc the sampleInc to set
   */
  public abstract void setSampleInc(final float sampleInc) throws RemoteException;

  /**
   * @return the nSamples
   */
  public abstract int getnSamples() throws RemoteException;

  /**
   * @param nSamples the nSamples to set
   */
  public abstract void setnSamples(final int nSamples) throws RemoteException;

  /**
   * @return the datasetName
   */
  public abstract String getDatasetName() throws RemoteException;

  /**
   * @param datasetName the datasetName to set
   */
  public abstract void setDatasetName(final String datasetName) throws RemoteException;

  /**
   * @return the datasetPath
   */
  public abstract String getDatasetPath() throws RemoteException;

  /**
   * @param datasetPath the datasetPath to set
   */
  public abstract void setDatasetPath(final String datasetPath) throws RemoteException;

  /**
   * @return the datasetType
   */
  public abstract String getDatasetType() throws RemoteException;

  /**
   * @param datasetType the datasetType to set
   */
  public abstract void setDatasetType(final String datasetType) throws RemoteException;

  /**
   * @return the attributeName
   */
  public abstract String getAttributeName() throws RemoteException;

  /**
   * @param attributeName the attributeName to set
   */
  public abstract void setAttributeName(final String attributeName) throws RemoteException;

  /**
   * @return the origCoordX
   */
  public abstract double getOrigCoordX() throws RemoteException;

  /**
   * @param origCoordX the origCoordX to set
   */
  public abstract void setOrigCoordX(final double origCoordX) throws RemoteException;

  /**
   * @return the origCoordY
   */
  public abstract double getOrigCoordY() throws RemoteException;

  /**
   * @param origCoordY the origCoordY to set
   */
  public abstract void setOrigCoordY(final double origCoordY) throws RemoteException;

  /**
   * @return the deltaCoordX
   */
  public abstract double getDeltaCoordX() throws RemoteException;

  /**
   * @param deltaCoordX the deltaCoordX to set
   */
  public abstract void setDeltaCoordX(final double deltaCoordX) throws RemoteException;

  /**
   * @return the deltaCoordY
   */
  public abstract double getDeltaCoordY() throws RemoteException;

  /**
   * @param deltaCoordY the deltaCoordY to set
   */
  public abstract void setDeltaCoordY(final double deltaCoordY) throws RemoteException;

  /**
   * @return the scalingType
   */
  public abstract String getScalingType() throws RemoteException;

  /**
   * @param scalingType the scalingType to set
   */
  public abstract void setScalingType(final String scalingType) throws RemoteException;

  /**
   * @return the scalingClipLow
   */
  public abstract double getScalingClipLow() throws RemoteException;

  /**
   * @param scalingClipLow the scalingClipLow to set
   */
  public abstract void setScalingClipLow(final double scalingClipLow) throws RemoteException;

  /**
   * @return the scalingClipHigh
   */
  public abstract double getScalingClipHigh() throws RemoteException;

  /**
   * @param scalingClipHigh the scalingClipHigh to set
   */
  public abstract void setScalingClipHigh(final double scalingClipHigh) throws RemoteException;

  public abstract void setDataUnit(final Unit dataUnit) throws RemoteException;

  public abstract Unit getDataUnit() throws RemoteException;

  public abstract void setXyUnit(final Unit xyUnit) throws RemoteException;

  public abstract Unit getXyUnit() throws RemoteException;

  public abstract void setWorldCoordsX(double[] worldCoordsX) throws RemoteException;

  public abstract double[] getWorldCoordsX() throws RemoteException;

  public abstract void setWorldCoordsY(double[] worldCoordsY) throws RemoteException;

  public abstract double[] getWorldCoordsY() throws RemoteException;

  public abstract void setSurveyWorldCoordsX(double[] worldCoordsX) throws RemoteException;

  public abstract double[] getSurveyWorldCoordsX() throws RemoteException;

  public abstract void setSurveyWorldCoordsY(double[] worldCoordsY) throws RemoteException;

  public abstract double[] getSurveyWorldCoordsY() throws RemoteException;

  public abstract void setAxisLabels(String[] axisLabels) throws RemoteException;

  public abstract String[] getAxisLabels() throws RemoteException;

  public abstract void setCanCreateMapperModel(boolean flag) throws RemoteException;

  public abstract boolean canCreateMapperModel() throws RemoteException;

  public abstract float getMinValue() throws RemoteException;

  public abstract void setMinValue(float minValue) throws RemoteException;

  public abstract float getMaxValue() throws RemoteException;

  public abstract void setMaxValue(float maxValue) throws RemoteException;

}