package org.geocraft.io.las;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.factory.model.WellLogTraceFactory;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;


public class WellLoadTask extends ImportTask {

  private WellMapperModel _mapperModel;

  public WellLoadTask() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    if (!mapperModel.getClass().equals(WellMapperModel.class)) {
      throw new IllegalArgumentException("Invalid model class: " + mapperModel.getClass() + ".");
    }
    _mapperModel = (WellMapperModel) mapperModel;

  }

  private boolean isStringInArray(final String findMe, final String[] arrayOfStrings) {
    //    String findMeWithoutUnits = findMe.substring(0, findMe.lastIndexOf('(')).trim();
    String findMeWithoutUnits = findMe;
    for (String arrayOfString : arrayOfStrings) {
      if (findMeWithoutUnits.equals(arrayOfString.toString()/*.substring(0, arrayOfString.lastIndexOf('('))*/.trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @throws CoreException
   */
  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, final IRepository repository) throws CoreException {
    if (_mapperModel == null) {
      throw new RuntimeException("The task for loading the LAS well has not been initialized.");
    }

    // Begin the task.
    String boreName = _mapperModel.getFileName();
    String fileName = boreName;
    monitor.beginTask("Loading LAS well: " + fileName, 4);

    // Create a master backup of the file in case of problems
    try {
      FileUtil.copy(_mapperModel.getDirectory() + File.separatorChar + _mapperModel.getFileName(),
          _mapperModel.getDirectory() + File.separatorChar + _mapperModel.getFileName() + "_orig");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Create the well mapper.
    WellMapper wellMapper = new WellMapper(_mapperModel);

    // Create the well entity.
    Well well = new Well(_mapperModel.getWellName(), wellMapper);

    // Check if the well entity already exists in the repository...
    ISpecification filter = new TypeSpecification(Well.class);
    Map<String, Object> map = repository.get(filter);
    Collection<Object> objects = map.values();
    boolean foundWell = false;
    for (Object object : objects) {
      Well temp = (Well) object;
      if (temp.getUniqueID().equals(well.getUniqueID())) {
        foundWell = true;
        well = temp;
      }
    }
    // If so, then log a warning.
    // If not, then add it.
    if (foundWell) {
      logger.warn(well.getDisplayName() + " already exists in the repository.");
    } else {
      repository.add(well);
    }
    monitor.worked(1);

    // Create the well bore.
    WellBore wellBore = well.getWellBore();

    monitor.worked(1);

    // Add the well log traces.
    List<WellLogTrace> wellLogsToAdd = new ArrayList<WellLogTrace>();

    String[] logs = _mapperModel.getColumnNames();
    String[] selectedLogs = _mapperModel.getSelectedColumnNames();

    for (String log : logs) {
      System.out.println("LOG NAME: " + log);
      //      String nameWithoutUnits = log.substring(0, log.lastIndexOf('(')).trim();
      String nameWithoutUnits = log;
      WellLogTraceMapperModel traceMapperModel = new WellLogTraceMapperModel();
      traceMapperModel.setValueObject(WellLogTraceMapperModel.TRACE_DISPLAY_NAME, nameWithoutUnits);
      traceMapperModel.setValueObject(WellLogTraceMapperModel.WELL_MAPPER_MODEL, _mapperModel);
      WellLogTraceMapper wellLogMapper = new WellLogTraceMapper(traceMapperModel);
      WellLogTrace wellLog = WellLogTraceFactory.create(nameWithoutUnits, wellLogMapper, well);
      if (!isStringInArray(log, selectedLogs)) {
        wellMapper.hideUnselectedLog(wellLog);

        //if the log trace was not selected do nothing further with it
        continue;
      }

      // Check if the well log entity already exists in the repository...
      filter = new TypeSpecification(WellLogTrace.class);
      map = repository.get(filter);
      objects = map.values();
      boolean foundWellLog = false;
      for (Object object : objects) {
        WellLogTrace temp = (WellLogTrace) object;
        if (temp.getUniqueID().equals(wellLog.getUniqueID())) {
          foundWellLog = true;
        }
      }
      // If so, then log a warning.
      // If not, then add it.
      if (foundWellLog) {
        logger.warn(wellLog.getDisplayName() + " already exists in the repository.");
      } else {
        wellLogsToAdd.add(wellLog);
      }
    }
    String[] names = repository.add(wellLogsToAdd.toArray(new WellLogTrace[0]));

    //    // Add the well picks.
    //    List<WellPick> wellPicksToAdd = new ArrayList<WellPick>();
    //    WellPickElement[] selectedPicks = _mapperModel.getPickElements();
    //    for (WellPickElement selectedPick : selectedPicks) {
    //      WellPickMapperModel pickModel = new WellPickMapperModel();
    //
    //      pickModel.setPickName(selectedPick.getName());
    //      pickModel.setInterpreter(selectedPick.getInterpreter());
    //      pickModel.setObservationNo(selectedPick.getObservationNo());
    //
    //      pickModel.setProjectName(_mapperModel.getProjectName());
    //      pickModel.setWellID(_mapperModel.getWellId());
    //      pickModel.setUniqueWellID(_mapperModel.getUniqueWellId());
    //      pickModel.setWellName(_mapperModel.getWellName());
    //      WellPickMapper wellPickMapper = new WellPickMapper(pickModel);
    //
    //      WellPick wellPick = new WellPick(selectedPick.getDisplayName(), wellPickMapper, wellBore);
    //      wellBore.addWellPick(wellPick);
    //
    //      // Check if the well pick entity already exists in the repository...
    //      filter = new TypeSpecification(WellPick.class);
    //      map = repository.get(filter);
    //      objects = map.values();
    //      boolean foundWellPick = false;
    //      for (Object object : objects) {
    //        WellPick temp = (WellPick) object;
    //        if (temp.getUniqueID().equals(wellPick.getUniqueID())) {
    //          foundWellPick = true;
    //        }
    //      }
    //      // If so, then log a warning.
    //      // If not, then add it.
    //      if (foundWellPick) {
    //        logger.warn(wellPick.getDisplayName() + " already exists in the repository.");
    //      } else {
    //        wellPicksToAdd.add(wellPick);
    //      }
    //    }
    //    repository.add(wellPicksToAdd.toArray(new WellPick[0]));
    //
    //    int[] prefID = { 0 };
    //    try {
    //      OpenWorks.devkit().getPreferredTimeDepthTable(_mapperModel.getWellId(),
    //          OpenWorksProject.getInstance().getInterpreter(), prefID);
    //    } catch (LandmarkException e) {
    //      // TODO Auto-generated catch block
    //      e.printStackTrace();
    //    }
    //
    //    // Add the well check shots.
    //    List<WellCheckShot> wellCheckShotsToAdd = new ArrayList<WellCheckShot>();
    //    WellCheckShotElement[] selectedCheckShots = _mapperModel.getCheckShotElements();
    //    for (WellCheckShotElement selectedCheckShot : selectedCheckShots) {
    //      WellCheckShotMapperModel wellCheckShotModel = new WellCheckShotMapperModel();
    //
    //      wellCheckShotModel.setTableName(selectedCheckShot.getName());
    //      wellCheckShotModel.setTableID(selectedCheckShot.getId());
    //
    //      wellCheckShotModel.setProjectName(_mapperModel.getProjectName());
    //      wellCheckShotModel.setWellID(_mapperModel.getWellId());
    //      wellCheckShotModel.setUniqueWellID(_mapperModel.getUniqueWellId());
    //      wellCheckShotModel.setWellName(_mapperModel.getWellName());
    //      WellCheckShotMapper wellTDTableMapper = new WellCheckShotMapper(wellCheckShotModel);
    //
    //      WellCheckShot wellCheckShot = new WellCheckShot(selectedCheckShot.getName(), wellTDTableMapper, wellBore);
    //      wellBore.addWellCheckShot(wellCheckShot);
    //
    //      if (prefID[0] > 0 && prefID[0] == selectedCheckShot.getId()) {
    //        wellBore.setDefaultCheckShot(wellCheckShot);
    //      }
    //
    //      // Check if the well TD table entity already exists in the repository...
    //      filter = new TypeSpecification(WellCheckShot.class);
    //      map = repository.get(filter);
    //      objects = map.values();
    //      boolean foundWellCheckShot = false;
    //      for (Object object : objects) {
    //        WellCheckShot temp = (WellCheckShot) object;
    //        if (temp.getUniqueID().equals(wellCheckShot.getUniqueID())) {
    //          foundWellCheckShot = true;
    //        }
    //      }
    //      // If so, then log a warning.
    //      // If not, then add it.
    //      if (foundWellCheckShot) {
    //        logger.warn(wellCheckShot.getDisplayName() + " already exists in the repository.");
    //      } else {
    //        wellCheckShotsToAdd.add(wellCheckShot);
    //      }
    //    }
    //    repository.add(wellCheckShotsToAdd.toArray(new WellCheckShot[0]));
    //
    //    wellBore.setDirty(false);
    //
    //    monitor.worked(1);
    //    monitor.done();

    //
    //    // Create the ModSpec grid mapper.
    //    monitor.subTask("Creating datastore mapper");
    //    IMapper lasMapper = new WellMapper(_mapperModel);
    //    monitor.worked(1);
    //
    //    // Create the grid entity.
    //    monitor.subTask("Creating Well Bore entity");
    //    String boreID = lasMapper.getUniqueID();
    //    WellBore bore = new WellBore(boreName, lasMapper);
    //    monitor.worked(1);
    //
    //    // Check if grid already exists in repository.
    //    monitor.subTask("Checking repository");
    //    ISpecification filter = new TypeSpecification(WellBore.class);
    //    Map<String, Object> map = repository.get(filter);
    //    for (Object object : map.values()) {
    //      WellBore temp = (WellBore) object;
    //      if (boreID.equals(temp.getUniqueID())) {
    //        monitor.done();
    //        throw new CoreException(new Status(IStatus.ERROR, "org.geocraft.io.las", getAlreadyExistsErrorMessage(bore)));
    //      }
    //    }
    //    monitor.worked(1);
    //
    //    // Add the grid to the repository.
    //    monitor.subTask("Adding well bore to repository");
    //    repository.add(bore);
    //    monitor.worked(1);
    //
    // Task is done.
    monitor.done();

  }
}
