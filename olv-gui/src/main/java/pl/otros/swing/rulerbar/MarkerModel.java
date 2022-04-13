package pl.otros.swing.rulerbar;

import java.util.List;

public interface MarkerModel {

  void addMarker(Marker... marker);

  void clear();

  List<Marker> getMarkers();

  void addMarkerModelListener(MarkerModelListener listener);

  void removeMarkerModelListener(MarkerModelListener listener);

  void clearMarkerModelListener();

}
