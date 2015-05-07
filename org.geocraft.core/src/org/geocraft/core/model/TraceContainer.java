package org.geocraft.core.model;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class TraceContainer extends UnicastRemoteObject implements ITraceContainer {

  public final static String PLUGIN_ID = "org.geocraft.core";

  private static final ILogger _logger = ServiceProvider.getLoggingService().getLogger(PLUGIN_ID);

  private final LinkedBlockingQueue<TraceData> _traceDataList = new LinkedBlockingQueue<TraceData>();

  private int _cnt;

  //State memory
  protected boolean _writeToSharedMemory = false;

  // The range of inlines and crosslines for the output data volume.
  protected int _minCrossline;

  private int _startCrossline;

  protected int _maxCrossline;

  protected int _incCrossline;

  protected int _minInline;

  private int _startInline;

  protected int _maxInline;

  protected int _incInline;

  protected boolean _xIsInline;

  // Apply automatic scaling? Good question
  protected boolean _applyScaling = true;

  // The sample information
  protected float _sampleStart;

  protected float _sampleInc;

  protected float _sampleEnd;

  protected int _nSamples;

  // Identifiers for the output volume file
  protected String _datasetName;

  protected String _datasetPath;

  private String _datasetType;

  private String _attributeName;

  protected double _origCoordX;

  protected double _origCoordY;

  protected double _deltaCoordX;

  protected double _deltaCoordY;

  private String _scalingType;

  private double _scalingClipLow;

  private double _scalingClipHigh;

  private boolean _canCreate = false;

  private Unit _dataUnit;

  private Unit _xyUnit;

  private double[] _worldCoordsX;

  private double[] _worldCoordsY;

  private double[] _worldSurveyCoordsX;

  private double[] _worldSurveyCoordsY;

  private String[] _axisLabels;

  private float _minValue;

  private float _maxValue;

  public TraceContainer() throws RemoteException {
    super();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#addTraceData(org.geocraft.core.model.datatypes.TraceData)
   */
  @Override
  public synchronized void addTraceData(final TraceData traceData) {
    _logger.info(_cnt++ + ": GOT TRACE DATA" + " with " + traceData.getNumTraces() + " traces, each with "
        + traceData.getNumSamples() + " samples in units of " + traceData.getUnitOfZ());
    _traceDataList.add(traceData);
    notifyAll();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getTraceData()
   */
  @Override
  public AbstractQueue<TraceData> getTraceData() {
    return _traceDataList;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#isWriteToSharedMemory()
   */
  @Override
  public boolean isWriteToSharedMemory() {
    return _writeToSharedMemory;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setWriteToSharedMemory(boolean)
   */
  @Override
  public void setWriteToSharedMemory(final boolean writeToSharedMemory) {
    this._writeToSharedMemory = writeToSharedMemory;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getMinCrossline()
   */
  @Override
  public int getMinCrossline() {
    return _minCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setMinCrossline(int)
   */
  @Override
  public void setMinCrossline(final int minCrossline) {
    _logger.info("Min Xline: " + minCrossline);
    this._minCrossline = minCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getStartCrossline()
   */
  @Override
  public int getStartCrossline() {
    return _startCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setStartCrossline(int)
   */
  @Override
  public void setStartCrossline(final int startCrossline) {
    this._startCrossline = startCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getMaxCrossline()
   */
  @Override
  public int getMaxCrossline() {
    return _maxCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setMaxCrossline(int)
   */
  @Override
  public void setMaxCrossline(final int maxCrossline) {
    _logger.info("Max Xline: " + maxCrossline);
    this._maxCrossline = maxCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getIncCrossline()
   */
  @Override
  public int getIncCrossline() {
    return _incCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setIncCrossline(int)
   */
  @Override
  public void setIncCrossline(final int incCrossline) {
    _logger.info("Xline inc: " + incCrossline);
    this._incCrossline = incCrossline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getMinInline()
   */
  @Override
  public int getMinInline() {
    return _minInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setMinInline(int)
   */
  @Override
  public void setMinInline(final int minInline) {
    _logger.info("Min Inline: " + minInline);
    this._minInline = minInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getStartInline()
   */
  @Override
  public int getStartInline() {
    return _startInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setStartInline(int)
   */
  @Override
  public void setStartInline(final int startInline) {
    this._startInline = startInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getMaxInline()
   */
  @Override
  public int getMaxInline() {
    return _maxInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setMaxInline(int)
   */
  @Override
  public void setMaxInline(final int maxInline) {
    _logger.info("Max Inline: " + maxInline);
    this._maxInline = maxInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getIncInline()
   */
  @Override
  public int getIncInline() {
    return _incInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setIncInline(int)
   */
  @Override
  public void setIncInline(final int incInline) {
    _logger.info("Inline inc: " + incInline);
    this._incInline = incInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#isxIsInline()
   */
  @Override
  public boolean isxIsInline() {
    return _xIsInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setxIsInline(boolean)
   */
  @Override
  public void setxIsInline(final boolean xIsInline) {
    this._xIsInline = xIsInline;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#isApplyScaling()
   */
  @Override
  public boolean isApplyScaling() {
    return _applyScaling;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setApplyScaling(boolean)
   */
  @Override
  public void setApplyScaling(final boolean applyScaling) {
    this._applyScaling = applyScaling;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getSampleStart()
   */
  @Override
  public float getSampleStart() {
    return _sampleStart;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setSampleStart(float)
   */
  @Override
  public void setSampleStart(final float sampleStart) {
    this._sampleStart = sampleStart;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getSampleInc()
   */
  @Override
  public float getSampleInc() {
    return _sampleInc;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setSampleInc(float)
   */
  @Override
  public void setSampleInc(final float sampleInc) {
    this._sampleInc = sampleInc;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getnSamples()
   */
  @Override
  public int getnSamples() {
    return _nSamples;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setnSamples(int)
   */
  @Override
  public void setnSamples(final int nSamples) {
    _logger.info("num samples: " + nSamples);
    this._nSamples = nSamples;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getDatasetName()
   */
  @Override
  public String getDatasetName() {
    return _datasetName;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setDatasetName(java.lang.String)
   */
  @Override
  public void setDatasetName(final String datasetName) {
    this._datasetName = datasetName;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getDatasetPath()
   */
  @Override
  public String getDatasetPath() {
    return _datasetPath;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setDatasetPath(java.lang.String)
   */
  @Override
  public void setDatasetPath(final String datasetPath) {
    this._datasetPath = datasetPath;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getDatasetType()
   */
  @Override
  public String getDatasetType() {
    return _datasetType;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setDatasetType(java.lang.String)
   */
  @Override
  public void setDatasetType(final String datasetType) {
    this._datasetType = datasetType;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getAttributeName()
   */
  @Override
  public String getAttributeName() {
    return _attributeName;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setAttributeName(java.lang.String)
   */
  @Override
  public void setAttributeName(final String attributeName) {
    this._attributeName = attributeName;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getOrigCoordX()
   */
  @Override
  public double getOrigCoordX() {
    return _origCoordX;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setOrigCoordX(double)
   */
  @Override
  public void setOrigCoordX(final double origCoordX) {
    _logger.info("origin coord X: " + origCoordX);
    this._origCoordX = origCoordX;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getOrigCoordY()
   */
  @Override
  public double getOrigCoordY() {
    return _origCoordY;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setOrigCoordY(double)
   */
  @Override
  public void setOrigCoordY(final double origCoordY) {
    _logger.info("origin coord Y: " + origCoordY);
    this._origCoordY = origCoordY;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getDeltaCoordX()
   */
  @Override
  public double getDeltaCoordX() {
    return _deltaCoordX;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setDeltaCoordX(double)
   */
  @Override
  public void setDeltaCoordX(final double deltaCoordX) {
    _logger.info("Delta X: " + deltaCoordX);
    this._deltaCoordX = deltaCoordX;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getDeltaCoordY()
   */
  @Override
  public double getDeltaCoordY() {
    return _deltaCoordY;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setDeltaCoordY(double)
   */
  @Override
  public void setDeltaCoordY(final double deltaCoordY) {
    _logger.info("Delta Y: " + deltaCoordY);
    this._deltaCoordY = deltaCoordY;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getScalingType()
   */
  @Override
  public String getScalingType() {
    return _scalingType;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setScalingType(java.lang.String)
   */
  @Override
  public void setScalingType(final String scalingType) {
    this._scalingType = scalingType;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getScalingClipLow()
   */
  @Override
  public double getScalingClipLow() {
    return _scalingClipLow;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setScalingClipLow(double)
   */
  @Override
  public void setScalingClipLow(final double scalingClipLow) {
    this._scalingClipLow = scalingClipLow;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#getScalingClipHigh()
   */
  @Override
  public double getScalingClipHigh() {
    return _scalingClipHigh;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.ITraceContainer#setScalingClipHigh(double)
   */
  @Override
  public void setScalingClipHigh(final double scalingClipHigh) {
    this._scalingClipHigh = scalingClipHigh;
  }

  /**
   * Called by the master node to signal the completion of sending over all the data
   * pieces required to create a mapper model
   */
  public void setCanCreateMapperModel(final boolean flag) {
    synchronized (this) {
      _canCreate = flag;
      notifyAll();
    }
  }

  public boolean canCreateMapperModel() {
    return _canCreate;
  }

  /**
   * @return the dataUnit
   */
  public Unit getDataUnit() {
    return _dataUnit;
  }

  /**
   * @param dataUnit the dataUnit to set
   */
  public void setDataUnit(final Unit dataUnit) {
    this._dataUnit = dataUnit;
  }

  @Override
  public void setWorldCoordsX(final double[] worldCoordsX) {
    _worldCoordsX = worldCoordsX;

  }

  @Override
  /**
   * returns a REFERENCE to the world coords array
   */
  public double[] getWorldCoordsX() {
    return _worldCoordsX;
  }

  @Override
  public void setWorldCoordsY(final double[] worldCoordsY) {
    _worldCoordsY = worldCoordsY;
  }

  @Override
  /**
   * returns a REFERENCE to the world coords array
   */
  public double[] getWorldCoordsY() {
    return _worldCoordsY;
  }

  @Override
  public void setSurveyWorldCoordsX(final double[] worldCoordsX) {
    _worldSurveyCoordsX = worldCoordsX;
  }

  @Override
  /**
   * returns a REFERENCE to the world coords array
   */
  public double[] getSurveyWorldCoordsX() {
    return _worldSurveyCoordsX;
  }

  @Override
  public void setSurveyWorldCoordsY(final double[] worldCoordsY) {
    _worldSurveyCoordsY = worldCoordsY;
  }

  @Override
  /**
   * returns a REFERENCE to the world coords array
   */
  public double[] getSurveyWorldCoordsY() {
    return _worldSurveyCoordsY;
  }

  @Override
  public void setAxisLabels(final String[] axisLabels) {
    _axisLabels = axisLabels;
  }

  @Override
  public String[] getAxisLabels() {
    return _axisLabels;
  }

  @Override
  public float getSampleEnd() {
    return _sampleEnd;
  }

  @Override
  public void setSampleEnd(final float sampleEnd) {
    _sampleEnd = sampleEnd;
  }

  @Override
  public void setXyUnit(final Unit xyUnit) {
    _xyUnit = xyUnit;

  }

  @Override
  public Unit getXyUnit() {
    return _xyUnit;
  }

  /**
   * @return the minValue
   */
  public float getMinValue() {
    return _minValue;
  }

  /**
   * @param minValue the minValue to set
   */
  public void setMinValue(final float minValue) {
    _logger.info("min value: " + minValue);
    this._minValue = minValue;
  }

  /**
   * @return the maxValue
   */
  public float getMaxValue() {
    return _maxValue;
  }

  /**
   * @param maxValue the maxValue to set
   */
  public void setMaxValue(final float maxValue) {
    _logger.info("min value: " + maxValue);
    this._maxValue = maxValue;
  }

  public static void main(final String[] args) {
    try {
      LocateRegistry.createRegistry(1099);
      // All DB transactions are handled via the server. So, the model in the
      // registry contains a reference to this DAO object on the server
      TraceContainer container = new TraceContainer();

      // Bind this object instance to the server address name
      Naming.rebind("//hololw52/TraceTest", container);
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
