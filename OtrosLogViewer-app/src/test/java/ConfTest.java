import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import pl.otros.swing.config.OtrosConfiguration;

import java.io.IOException;

public class ConfTest {

  public static void main(String[] args) throws IOException {

    System.getProperties().store(System.out,"");
    BaseConfiguration bc = new BaseConfiguration();
    DataConfiguration dc = new DataConfiguration(bc);
    DataConfiguration oc = new OtrosConfiguration(bc);
    bc.addConfigurationListener(new ConfigurationLogListener("Base"));
    dc.addConfigurationListener(new ConfigurationLogListener("Data"));
    oc.addConfigurationListener(new ConfigurationLogListener("Otros"));

    dc.setProperty("A", "B");

    System.out.println("Base size: " + bc.getConfigurationListeners().size());
    System.out.println("Data size: " + dc.getConfigurationListeners().size());
    System.out.println("Otros size: " + oc.getConfigurationListeners().size());

  }

  public static class ConfigurationLogListener implements ConfigurationListener {

    private String name;

    public ConfigurationLogListener(String name) {
      this.name = name;
    }

    public void configurationChanged(ConfigurationEvent event) {
      if (!event.isBeforeUpdate()) {
        // only display events after the modification was done
        System.out.println(name + " Received event!");
        System.out.println(name + " Type = " + event.getType());
        if (event.getPropertyName() != null) {
          System.out.println(name + " Property name = " + event.getPropertyName());
        }
        if (event.getPropertyValue() != null) {
          System.out.println(name + " Property value = " + event.getPropertyValue());
        }
      }
    }
  }
}
