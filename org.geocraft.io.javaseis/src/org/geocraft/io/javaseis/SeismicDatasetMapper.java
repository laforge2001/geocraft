package org.geocraft.io.javaseis;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.javaseis.grid.GridDefinition;
import org.javaseis.io.Seisio;
import org.javaseis.properties.DataFormat;
import org.javaseis.util.SeisException;

import edu.mines.jtk.util.Parameter;
import edu.mines.jtk.util.ParameterSet;


public abstract class SeismicDatasetMapper<E extends SeismicDataset> extends AbstractMapper<E> {

  protected static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  protected VolumeMapperModel _model;

  protected Seisio _seisio;

  protected HeaderDefinition _headerDef;

  protected Domain _zDomain;

  protected GridDefinition _gridDef;

  /** Open status of JavaSeis file: null, "r", "rw" */
  protected String _status;

  public SeismicDatasetMapper(final VolumeMapperModel model) {
    _model = model;

    try {
      _seisio = new Seisio(_model.getDirectory() + File.separator + _model.getFileName());
      _gridDef = _seisio.getGridDefinition();
    } catch (SeisException e) {
      getLogger().error(e.toString(), e);
    }

    Unit zUnit = _model.getUnitOfZ();
    Domain zDomain = zUnit.getDomain();
    if (zDomain == Domain.TIME) {
      _zDomain = Domain.TIME;
    } else if (zDomain == Domain.DISTANCE) {
      _zDomain = Domain.DISTANCE;
    } else {
      throw new IllegalArgumentException("Invalid z unit." + zUnit);
    }

  }

  @Override
  protected void createInStore(final E seismicDataset) throws IOException {
    String filePath = getFilePath();
    File file = new File(filePath);
    if (!file.mkdir()) {
      throw new IOException("The file \'" + filePath + "\' already exists.");
    }
  }

  @Override
  protected void deleteFromStore(final E seismicDataset) {
    close();
    _seisio.delete();
  }

  @Override
  protected VolumeMapperModel getInternalModel() {
    return _model;
  }

  public void checkTraceIndex() {
    // No action.
  }

  public void close() {
    if (_status != null) {
      try {
        _seisio.close();
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
      _status = null;
    }
  }

  public StorageOrganization getStorageOrganization() {
    // TODO: Does JavaSeis support a BRICK or SLICE organization?
    // No BRICK but SLICE organization can be accommodated via transposition
    return StorageOrganization.TRACE;
  }

  public StorageFormat getStorageFormat() {
    String dataFormat = _model.getDataFormat();
    if (dataFormat.equals(DataFormat.INT08.toString())) {
      return StorageFormat.INTEGER_08;
    } else if (dataFormat.equals(DataFormat.INT16.toString())) {
      return StorageFormat.INTEGER_16;
    } else if (dataFormat.equals(DataFormat.COMPRESSED_INT08.toString())) {
      return StorageFormat.INTEGER_08;
    } else if (dataFormat.equals(DataFormat.COMPRESSED_INT16.toString())) {
      return StorageFormat.INTEGER_16;
    } else if (dataFormat.equals(DataFormat.FLOAT.toString())) {
      return StorageFormat.FLOAT_32;
    }
    throw new RuntimeException("Invalid data format: " + dataFormat);
  }

  public void setStorageFormat(final StorageFormat storageFormat) {
    String dataFormat = DataFormat.FLOAT.toString();
    if (storageFormat.equals(StorageFormat.INTEGER_08)) {
      dataFormat = DataFormat.INT08.toString();
    } else if (storageFormat.equals(StorageFormat.INTEGER_16)) {
      dataFormat = DataFormat.INT16.toString();
    } else {
      dataFormat = DataFormat.FLOAT.toString();
    }
    _model.setDataFormat(dataFormat);
    // Need to re-initialize to make sure everything is updated.
    // TODO initialize(getInternalModel(), true);
  }

  public VolumeMapperModel getModel() {
    return new VolumeMapperModel(_model);
  }

  public Domain getDomain() {
    return _zDomain;
  }

  public void setDomain(final Domain domain) {
    if (!_zDomain.equals(domain)) {
      _zDomain = domain;
      if (_zDomain.equals(Domain.TIME)) {
        _model.setUnitOfZ(Unit.MILLISECONDS);
      } else if (_zDomain.equals(Domain.DISTANCE)) {
        _model.setUnitOfZ(UNIT_PREFS.getVerticalDistanceUnit());
      }
    }
  }

  public HeaderDefinition getHeaderDefinition() {
    return _headerDef;
  }

  protected String getFilePath() {
    return _model.getDirectory() + File.separator + _model.getFileName();
  }

  public Map<String, Object> getMetaData() {
    ParameterSet customParms = _seisio.getCustomProperties();

    Iterator<ParameterSet> paramSetIter = customParms.getParameterSets();

    Map<String, Object> results = new HashMap<String, Object>();
    while (paramSetIter.hasNext()) {
      ParameterSet paramSet = paramSetIter.next();
      Iterator<Parameter> paramIter = paramSet.getParameters();
      while (paramIter.hasNext()) {
        Parameter parm = paramIter.next();
        int paramType = parm.getType();
        switch (paramType) {
          case Parameter.BOOLEAN:
            results.put(parm.getName(), parm.getBoolean());
            break;
          case Parameter.DOUBLE:
            results.put(parm.getName(), parm.getDouble());
            break;
          case Parameter.FLOAT:
            results.put(parm.getName(), parm.getFloat());
            break;
          case Parameter.INT:
            results.put(parm.getName(), parm.getInt());
            break;
          case Parameter.LONG:
            results.put(parm.getName(), parm.getLong());
            break;
          case Parameter.STRING:
            results.put(parm.getName(), parm.getString());
            break;
        }
      }
    }

    return results;
  }
}
