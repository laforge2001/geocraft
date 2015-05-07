package org.geocraft.core.model.mapper;

import org.geocraft.core.model.well.Well;


public interface IWellMapper extends IMapper<Well> {

  void setWellName(String wellName);

  void setWellIdentifier(String wellIdentifier);

  void setWellIdentifierType(String wellIdentifierType);

  // Pass natural keys from GeoCraft well log trace model
  IMapper getWellLogTraceMapper(String traceName, int logRunNumber, int traceVersion, String loggingService,
      String logPassId);

  IMapper getWellPickMapper();

  IMapper getWellCheckShotMapper();
}
