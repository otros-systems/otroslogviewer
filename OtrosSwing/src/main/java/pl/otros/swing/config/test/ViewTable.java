package pl.otros.swing.config.test;

import org.apache.commons.configuration.Configuration;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class ViewTable extends AbstractConfigView {
  private JTable table;

  public ViewTable() {
    super("view4", "View 4", "VSfs fas fsf \nadsf saf ");
    table = new JTable(new Object[][]{new String[]{"SDF", "fsdf", "sdgsg"}, new String[]{"sdfsdf", "sfsf", "sdfsf"},
        new String[]{"sdfsdf", "s32f", "sdfsf"}, new String[]{"sdfsdf", "sf3f", "sdfsf"}, new String[]{"sdfbf", "sfbf", "sdfsf"},
        new String[]{"sdsdfsdf", "sfsf", "sdfsf"},}, new Object[]{"A", "B", "C"});
  }

  @Override
  public JComponent getView() {
    return table;
  }

  @Override
  public void loadConfguration(Configuration configuration) {
  }

  @Override
  public void saveConfiguration(final Configuration c) {
  }

  public ValidationResult validate() {
    return new ValidationResult();
  }
}
