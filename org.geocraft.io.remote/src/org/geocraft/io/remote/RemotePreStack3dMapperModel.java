package org.geocraft.io.remote;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.validation.IValidation;


public class RemotePreStack3dMapperModel extends MapperModel {

  private String _uniqueID;

  private EnumProperty<Domain> _domain;

  private EnumProperty<Unit> _dataUnit;

  private EnumProperty<Unit> _zUnit;

  private FloatProperty _zStart;

  private FloatProperty _zEnd;

  private FloatProperty _zDelta;

  private FloatProperty _inlineStart;

  private FloatProperty _inlineEnd;

  private FloatProperty _inlineDelta;

  private FloatProperty _xlineStart;

  private FloatProperty _xlineEnd;

  private FloatProperty _xlineDelta;

  private FloatProperty _offsetStart;

  private FloatProperty _offsetEnd;

  private FloatProperty _offsetDelta;

  private DoubleProperty _x0;

  private DoubleProperty _y0;

  private DoubleProperty _x1;

  private DoubleProperty _y1;

  private DoubleProperty _x2;

  private DoubleProperty _y2;

  private ObjectProperty<HeaderDefinition> _traceHeaderDef;

  public RemotePreStack3dMapperModel() {
    _domain = addEnumProperty("Domain", Domain.class, Domain.TIME);
    _dataUnit = addEnumProperty("Data Unit", Unit.class, Unit.UNDEFINED);
    _zUnit = addEnumProperty("Z Unit", Unit.class, Unit.UNDEFINED);
    _zStart = addFloatProperty("Z Start", 0);
    _zEnd = addFloatProperty("Z End", 0);
    _zDelta = addFloatProperty("Z Delta", 0);
    _inlineStart = addFloatProperty("Inline Start", 0);
    _inlineEnd = addFloatProperty("Inline End", 0);
    _inlineDelta = addFloatProperty("Inline Delta", 0);
    _xlineStart = addFloatProperty("Xline Start", 0);
    _xlineEnd = addFloatProperty("Xline End", 0);
    _xlineDelta = addFloatProperty("Xline Delta", 0);
    _offsetStart = addFloatProperty("Offset Start", 0);
    _offsetEnd = addFloatProperty("Offset End", 0);
    _offsetDelta = addFloatProperty("Offset Delta", 0);
    _x0 = addDoubleProperty("X0", 0);
    _y0 = addDoubleProperty("Y0", 0);
    _x1 = addDoubleProperty("X1", 0);
    _y1 = addDoubleProperty("Y1", 0);
    _x2 = addDoubleProperty("X2", 0);
    _y2 = addDoubleProperty("Y2", 0);

    _traceHeaderDef = addObjectProperty("", HeaderDefinition.class);
    HeaderDefinition headerDef = new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.INLINE_NO,
        TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.OFFSET });
    _traceHeaderDef.set(headerDef);
  }

  public RemotePreStack3dMapperModel(final RemotePreStack3dMapperModel model) {
    this();
    updateFrom(model);
    setTraceHeaderDef(model.getTraceHeaderDef());
    updateUniqueId(model.getUniqueId());
  }

  @Override
  public String getUniqueId() {
    return _uniqueID;
  }

  @Override
  public void updateUniqueId(final String name) {
    _uniqueID = name;
  }

  public void setDomain(Domain domain) {
    _domain.set(domain);
  }

  public void setDataUnit(Unit dataUnit) {
    _dataUnit.set(dataUnit);
  }

  public void setUnitOfZ(Unit zUnit) {
    _zUnit.set(zUnit);
  }

  public void setInlineStart(float inlineStart) {
    _inlineStart.set(inlineStart);
  }

  public void setInlineEnd(float inlineEnd) {
    _inlineEnd.set(inlineEnd);
  }

  public void setInlineDelta(float inlineDelta) {
    _inlineDelta.set(inlineDelta);
  }

  public void setXlineStart(float xlineStart) {
    _xlineStart.set(xlineStart);
  }

  public void setXlineEnd(float xlineEnd) {
    _xlineEnd.set(xlineEnd);
  }

  public void setXlineDelta(float xlineDelta) {
    _xlineDelta.set(xlineDelta);
  }

  public void setOffsetStart(float offsetStart) {
    _offsetStart.set(offsetStart);
  }

  public void setOffsetEnd(float offsetEnd) {
    _offsetEnd.set(offsetEnd);
  }

  public void setOffsetDelta(float offsetDelta) {
    _offsetDelta.set(offsetDelta);
  }

  public void setStartZ(float zStart) {
    _zStart.set(zStart);
  }

  public void setEndZ(float zEnd) {
    _zEnd.set(zEnd);
  }

  public void setDeltaZ(float zDelta) {
    _zDelta.set(zDelta);
  }

  public void setX0(double x0) {
    _x0.set(x0);
  }

  public void setY0(double y0) {
    _y0.set(y0);
  }

  public void setX1(double x1) {
    _x1.set(x1);
  }

  public void setY1(double y1) {
    _y1.set(y1);
  }

  public void setX2(double x2) {
    _x2.set(x2);
  }

  public void setY2(double y2) {
    _y2.set(y2);
  }

  public Domain getDomain() {
    return _domain.get();
  }

  public Unit getDataUnit() {
    return _dataUnit.get();
  }

  public Unit getUnitOfZ() {
    return _zUnit.get();
  }

  public float getInlineStart() {
    return _inlineStart.get();
  }

  public float getInlineEnd() {
    return _inlineEnd.get();
  }

  public float getInlineDelta() {
    return _inlineDelta.get();
  }

  public float getXlineStart() {
    return _xlineStart.get();
  }

  public float getXlineEnd() {
    return _xlineEnd.get();
  }

  public float getXlineDelta() {
    return _xlineDelta.get();
  }

  public float getOffsetStart() {
    return _offsetStart.get();
  }

  public float getOffsetEnd() {
    return _offsetEnd.get();
  }

  public float getOffsetDelta() {
    return _offsetDelta.get();
  }

  public float getStartZ() {
    return _zStart.get();
  }

  public float getEndZ() {
    return _zEnd.get();
  }

  public float getDeltaZ() {
    return _zDelta.get();
  }

  public double getX0() {
    return _x0.get();
  }

  public double getY0() {
    return _y0.get();
  }

  public double getX1() {
    return _x1.get();
  }

  public double getY1() {
    return _y1.get();
  }

  public double getX2() {
    return _x2.get();
  }

  public double getY2() {
    return _y2.get();
  }

  public HeaderDefinition getTraceHeaderDef() {
    return _traceHeaderDef.get();
  }

  public void setTraceHeaderDef(final HeaderDefinition traceHeaderDef) {
    _traceHeaderDef.set(traceHeaderDef);
  }

  @Override
  public boolean existsInStore() {
    return true;
  }

  @Override
  public boolean existsInStore(final String name) {
    return true;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.common.model2.IModel#validate(org.geocraft.core.common.model2.validation.IValidation)
   */
  @Override
  public void validate(IValidation validation) {
    // TODO Auto-generated method stub

  }

}
