package pl.otros.swing.rulerbar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulerBar extends JComponent implements MarkerModelListener {

  /**
   *
   */
  private static final long serialVersionUID = -8366414516731078241L;

  private final MarkerModel markerModel;
  private final Map<Range, Marker> tooltips;
  private final List<MarkerClickListener> listenersList;
  private Marker itemsWithFocus;

  public RulerBar(MarkerModel model) {
    super();
    this.markerModel = model;
    listenersList = new ArrayList<>();
    tooltips = new HashMap<>();
    model.addMarkerModelListener(this);
    Dimension dimension = new Dimension(8, 16);
    setMinimumSize(dimension);
    setSize(dimension);
    setPreferredSize(dimension);
    addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseMoved(MouseEvent e) {
        calculateTooltipAndCursor(e.getPoint());
      }

      @Override
      public void mouseDragged(MouseEvent e) {

      }
    });

    this.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (itemsWithFocus != null) {
          notifyClickListeners(itemsWithFocus);
        }
      }

    });
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    tooltips.clear();
    g.setColor(getBackground());
    int height = getHeight();
    int width = getWidth();
    List<Marker> markers = markerModel.getMarkers();
    int markerSize = 3;
    for (Marker marker : markers) {
      int markerStart = (int) (height * marker.getPercentValue());
      g.setColor(marker.getColor().brighter().brighter().brighter());
      g.fillRect(0, markerStart, width - 2, markerSize);
      g.setColor(marker.getColor());
      g.drawRect(0, markerStart, width - 2, markerSize);

      Range range = new Range(markerStart, markerStart + markerSize);
      tooltips.put(range, marker);

    }
  }


  @Override
  public void markerChanged() {
    repaint();
  }

  protected MarkerModel getMarkerModel() {
    return markerModel;
  }

  public void calculateTooltipAndCursor(Point point) {
    itemsWithFocus = null;
    String tooltip = null;
    Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    for (Range r : tooltips.keySet()) {
      if (r.isInRange(point.y)) {
        Marker marker = tooltips.get(r);
        tooltip = marker.getMessage();
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        itemsWithFocus = marker;
        break;
      }
    }
    setToolTipText(tooltip);
    setCursor(cursor);

  }

  public void addMarkerClickListener(MarkerClickListener markerClickListener) {
    listenersList.add(markerClickListener);
  }

  public void removeMarkerClickListener(
      MarkerClickListener markerClickListener) {
    listenersList.remove(markerClickListener);
  }

  public void clearListeners() {
    listenersList.clear();
  }

  protected void notifyClickListeners(Marker marker) {
    for (MarkerClickListener markerClickListener : listenersList) {
      markerClickListener.markerClicked(marker);
    }
  }

}
