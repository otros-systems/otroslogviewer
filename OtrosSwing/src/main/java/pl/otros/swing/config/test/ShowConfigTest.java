package pl.otros.swing.config.test;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import pl.otros.swing.config.ConfigComponent;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.provider.ConfigurationProvider;
import pl.otros.swing.config.provider.ConfigurationProviderImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ShowConfigTest {
  public static void main(String[] args) throws UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
//    OtrosApplication otrosApplication = new OtrosApplication();
//    otrosApplication.setConfiguration(new DataConfiguration(new BaseConfiguration()));
//    DataConfiguration configuration = otrosApplication.getConfiguration();
    DataConfiguration configuration = new DataConfiguration(new BaseConfiguration());
    ConfigurationProvider configurationProvider = new ConfigurationProviderImpl(configuration, new File(System.getProperty("java.io.tmpdir")));
    configuration.setProperty("view1.text", "ASD ASD");
    configuration.setProperty("view2.text", "sdf\nd\ndf\ns");
    JFrame f = new JFrame("CV");
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().setLayout(new BorderLayout());
    ConfigView[] configViews = new ConfigView[]{
        new View1(), // 
        new View2(), //
        new ViewTable(),// 
        new DateFormatView(),// 
        new ValidationView(),
        new ValidationView2()//
    };
    f.getContentPane().add(new ConfigComponent(configurationProvider, configViews));
    f.pack();
    f.setVisible(true);
  }
}
