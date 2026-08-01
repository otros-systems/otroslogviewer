package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.filter.LogFilter;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterConfigView extends AbstractConfigView implements InMainConfig {
  private final JPanel panel;

  private final Map<String, JCheckBox> filterClassNameToCheckBoxMap = new HashMap<>();

  public FilterConfigView() {
    super("filter", "Filter", "Configure Filters");
    panel = new JPanel(new MigLayout("left"));
//    addLabel("Check for new version on startup", 'c', checkCombo, panel);
  }

  @Override
  public JComponent getView() {
    return panel;
  }

  @Override
  public ValidationResult validate() {
    return new ValidationResult();
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    panel.removeAll();
    filterClassNameToCheckBoxMap.clear();

    for (LogFilter logFilter : AllPluginables.getInstance().getLogFiltersContainer().getElements()) {
      JCheckBox checkCombo = new JCheckBox();
      checkCombo.setSelected(true);
      checkCombo.setToolTipText(logFilter.getDescription());
      JLabel jLabel = addLabel(logFilter.getName(), checkCombo, panel);
      jLabel.setToolTipText(logFilter.getDescription());
      filterClassNameToCheckBoxMap.put(logFilter.getClass().getName(), checkCombo);
    }
    List<String> hiddenFilters = configuration.getList(String.class, ConfKeys.LOG_TABLE_COLUMN_HIDDEN_FILTERS, List.of());
    for (String hiddenFilter : hiddenFilters) {
      if (filterClassNameToCheckBoxMap.containsKey(hiddenFilter)) {
        filterClassNameToCheckBoxMap.get(hiddenFilter).setSelected(false);
      }
    }
  }

  @Override
  public void saveConfiguration(Configuration c) {
    List<String> hiddenFilters = filterClassNameToCheckBoxMap.entrySet().stream()
      .filter(entry -> !entry.getValue().isSelected())
      .map(Map.Entry::getKey)
      .toList();
    c.setProperty(ConfKeys.LOG_TABLE_COLUMN_HIDDEN_FILTERS, hiddenFilters);
  }
}
