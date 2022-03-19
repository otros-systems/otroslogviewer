package pl.otros.logview.exceptionshandler.errrorreport;

import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class DesktopERDC implements ErrorReportDataCollector {

  @Override
  public Map<String, String> collect(ErrorReportCollectingContext context) {
    HashMap<String, String> r = new HashMap<>();
    NumberFormat nf = NumberFormat.getIntegerInstance();
    GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = g.getScreenDevices();
    for (int i = 0; i < devices.length; i++) {
      r.put("DESKTOP:screen." + i + ".width", nf.format(devices[i].getDisplayMode().getWidth()));
      r.put("DESKTOP:screen." + i + ".height", nf.format(devices[i].getDisplayMode().getHeight()));
    }
    r.put("DESKTOP:count", nf.format(devices.length));
    return r;
  }

}
