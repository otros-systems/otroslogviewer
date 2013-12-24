package pl.otros.swing.rulerbar;

import java.util.List;

public interface MarkerModel {

  public void addMarker(Marker... marker);

  public void clear();

  public List<Marker> getMarkers();

  public void addMarkerModelListener(MarkerModelListener listener);

  public void removeMarkerModelListener(MarkerModelListener listener);

  public void clearMarkerModelListener();

}
