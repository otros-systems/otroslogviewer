package pl.otros.logview.gui.markers.editor;

import org.apache.commons.configuration2.BaseConfiguration;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.theme.ThemeConfig;
import pl.otros.logview.loader.LvDynamicLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MarkersEditorMain {
  public static void main(String[] args) throws IOException, InitializationException {
    LvDynamicLoader.getInstance().loadAll(new ThemeConfig(new BaseConfiguration()));

    JFrame frame = new JFrame("Markers editor");
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(new MarkersEditor(frame));
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }
}
