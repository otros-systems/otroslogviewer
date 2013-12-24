package pl.otros.swing.rulerbar.demo;

import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;
import pl.otros.swing.rulerbar.RulerBarHelper.TooltipMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class RulerBarDemo {


  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    JFrame f = new JFrame("RulerBar demo");
    JToolBar bar = new JToolBar();
    Color[] colors = new Color[]{
        Color.MAGENTA, Color.YELLOW, Color.BLACK, Color.CYAN, Color.GRAY, Color.BLUE, Color.WHITE, Color.ORANGE, Color.PINK, Color.RED
    };
    final JComboBox box = new JComboBox(colors);

    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    final String text = readText();
    JTextArea jTextArea = new JTextArea(text);
    final OtrosJTextWithRulerScrollPane<JTextArea> wrapTextComponent = RulerBarHelper.wrapTextComponent(jTextArea);
    final Random random = new Random();
    bar.add(new AbstractAction("Add") {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        int nextInt = random.nextInt(text.length());
        RulerBarHelper.addTextMarkerToPosition(wrapTextComponent, nextInt, "Position " + nextInt, (Color) box.getSelectedItem(), TooltipMode.LINE_NUMBER_PREFIX);
      }
    });
    bar.add(new AbstractAction("Clear") {

      @Override
      public void actionPerformed(ActionEvent e) {
        RulerBarHelper.clearMarkers(wrapTextComponent);

      }
    });
    bar.add(box);


    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(wrapTextComponent);
    f.getContentPane().add(bar, BorderLayout.NORTH);
    f.setSize(400, 400);
    f.setVisible(true);

  }

  private static String readText() throws IOException {
    BufferedReader bin = new BufferedReader(new FileReader("OtrosSwing/src/main/java/pl/otros/swing/rulerbar/RulerBar.java"));
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = bin.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString().trim();
  }

}
