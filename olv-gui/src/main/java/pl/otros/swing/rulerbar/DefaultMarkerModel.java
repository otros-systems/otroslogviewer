package pl.otros.swing.rulerbar;

import java.util.*;

public class DefaultMarkerModel implements MarkerModel {

  private final List<Marker> list = new ArrayList<>();
  private final Set<MarkerModelListener> listeners = new HashSet<>();


  @Override
  public void addMarker(Marker... markers) {
    Collections.addAll(list, markers);
    fireChangeEvent();
  }

  @Override
  public void clear() {
    list.clear();
    fireChangeEvent();

  }

  @Override
  public List<Marker> getMarkers() {
    return new ArrayList<>(list);
  }

  @Override
  public final void addMarkerModelListener(MarkerModelListener listener) {
    listeners.add(listener);

  }

  @Override
  public final void removeMarkerModelListener(MarkerModelListener listener) {
    listeners.remove(listener);

  }

  @Override
  public final void clearMarkerModelListener() {
    listeners.clear();
  }

  protected final void fireChangeEvent() {
    listeners.forEach(MarkerModelListener::markerChanged);
  }

}
