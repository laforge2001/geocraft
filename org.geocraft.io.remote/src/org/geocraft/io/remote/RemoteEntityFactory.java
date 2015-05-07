/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.remote;


import org.geocraft.core.factory.model.PreStack3dFactory;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.seismic.PreStack3d;


public class RemoteEntityFactory {

  public static PreStack3d createPreStack3d(final String uniqueID, final Domain domain, final FloatRange inlineRange,
      final FloatRange xlineRange, final FloatRange offsetRange, final FloatRange zRange, final Unit zUnit,
      final Unit dataUnit, final HeaderDefinition traceHeaderDef, final CornerPointsSeries cornerPoints) {

    // Create the prestack mapper model.
    RemotePreStack3dMapperModel model = new RemotePreStack3dMapperModel();
    model.setDomain(domain);
    model.setDataUnit(dataUnit);
    model.setUnitOfZ(zUnit);
    model.setInlineStart(inlineRange.getStart());
    model.setInlineEnd(inlineRange.getEnd());
    model.setInlineDelta(inlineRange.getDelta());
    model.setXlineStart(xlineRange.getStart());
    model.setXlineEnd(xlineRange.getEnd());
    model.setXlineDelta(xlineRange.getDelta());
    model.setOffsetStart(offsetRange.getStart());
    model.setOffsetEnd(offsetRange.getEnd());
    model.setOffsetDelta(offsetRange.getDelta());
    model.setStartZ(zRange.getStart());
    model.setEndZ(zRange.getEnd());
    model.setDeltaZ(zRange.getDelta());
    model.setTraceHeaderDef(traceHeaderDef);
    model.setX0(cornerPoints.getX(0));
    model.setY0(cornerPoints.getY(0));
    model.setX1(cornerPoints.getX(1));
    model.setY1(cornerPoints.getY(1));
    model.setX2(cornerPoints.getX(3));
    model.setY2(cornerPoints.getY(3));
    model.updateUniqueId(uniqueID);

    // Create the prestack mapper.
    RemotePreStack3dMapper mapper = new RemotePreStack3dMapper(model);

    // Create the prestack entity.
    return PreStack3dFactory.create(uniqueID, mapper);
  }
}
