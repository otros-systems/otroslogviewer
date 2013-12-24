package pl.otros.swing.rulerbar;


public class ScrollToSelectedMarker implements MarkerClickListener {


  public ScrollToSelectedMarker() {
  }

  @Override
  public void markerClicked(Marker marker) {
    marker.markerClicked();

  }

}
