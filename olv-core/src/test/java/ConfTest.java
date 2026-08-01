import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.DataConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import pl.otros.swing.config.OtrosConfiguration;

import java.io.IOException;

public class ConfTest {

  public static void main(String[] args) throws IOException {

    System.getProperties().store(System.out, "");
    BaseConfiguration bc = new BaseConfiguration();
    DataConfiguration dc = new DataConfiguration(bc);
    DataConfiguration oc = new OtrosConfiguration(bc);
    bc.addEventListener(ConfigurationEvent.ANY, new ConfigurationLogListener("Base"));
    dc.addEventListener(ConfigurationEvent.ANY, new ConfigurationLogListener("Data"));
    oc.addEventListener(ConfigurationEvent.ANY, new ConfigurationLogListener("Otros"));

    dc.setProperty("A", "B");

    System.out.println("Base size: " + bc.getEventListeners(ConfigurationEvent.ANY).size());
    System.out.println("Data size: " + dc.getEventListeners(ConfigurationEvent.ANY).size());
    System.out.println("Otros size: " + oc.getEventListeners(ConfigurationEvent.ANY).size());

  }

  public static class ConfigurationLogListener implements EventListener<ConfigurationEvent> {

    private final String name;

    public ConfigurationLogListener(String name) {
      this.name = name;
    }

    @Override
    public void onEvent(ConfigurationEvent event) {
      if (!event.isBeforeUpdate()) {
        // only display events after the modification was done
        System.out.println(name + " Received event!");
        System.out.println(name + " ThemeType = " + event.getEventType());
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
