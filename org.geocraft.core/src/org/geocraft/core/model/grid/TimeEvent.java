package org.geocraft.core.model.grid;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


public class TimeEvent {

  private final String _eventName;

  private final int _id;

  private final int _lithologyId;

  private final Map<OverlayDefinition, AbstractCubeGridProperty> _overlayToElements = new TreeMap<OverlayDefinition, AbstractCubeGridProperty>();

  public TimeEvent(final int id, final String name, final int lithologyId, final Map<OverlayDefinition, AbstractCubeGridProperty> overlays) {
    _eventName = name;
    _id = id;
    _lithologyId = lithologyId;
    for (Entry<OverlayDefinition, AbstractCubeGridProperty> e : overlays.entrySet()) {
      AbstractCubeGridProperty property = e.getValue();
      property.setTimeEventId(_id);
      _overlayToElements.put(e.getKey(), property);
    }
  }

  public String getName() {
    return _eventName;
  }

  public int getId() {
    return _id;
  }

  public int getLithologyId() {
    return _lithologyId;
  }

  public AbstractCubeGridProperty getElements(final int overlayId) {
    OverlayDefinition od = getOverlay(overlayId);
    if (od != null) {
      return _overlayToElements.get(od);
    }
    return null;
  }

  public Set<OverlayDefinition> getOverlayDefinitions() {
    return _overlayToElements.keySet();
  }

  public AbstractCubeGridProperty getElements(final OverlayDefinition key) {
    return _overlayToElements.get(key);
  }

  public AbstractCubeGridProperty getElements(final String overlayName) {
    for (OverlayDefinition od : _overlayToElements.keySet()) {
      if (od.getName().equals(overlayName)) {
        return _overlayToElements.get(od);
      }
    }
    return null;
  }

  public OverlayDefinition getOverlay(final int overlayId) {
    for (OverlayDefinition od : _overlayToElements.keySet()) {
      if (od.getId() == overlayId) {
        return od;
      }
    }
    return null;
  }

}
