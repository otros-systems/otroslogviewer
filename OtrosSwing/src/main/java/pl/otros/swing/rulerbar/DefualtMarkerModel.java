package pl.otros.swing.rulerbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefualtMarkerModel implements MarkerModel {

  private List<Marker> list = new ArrayList<Marker>();
  private Set<MarkerModelListener> listeners = new HashSet<MarkerModelListener>();


  @Override
  public void addMarker(Marker... markers) {
    for (Marker marker : markers) {
      list.add(marker);
    }
    fireChangeEvent();
  }

  @Override
  public void clear() {
    list.clear();
    fireChangeEvent();

  }

  @Override
  public List<Marker> getMarkers() {
    return new ArrayList<Marker>(list);
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
    for (MarkerModelListener listener : listeners) {
      listener.markerChanged();
    }
  }

}
