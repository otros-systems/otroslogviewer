package pl.otros.logview.gui.renderers;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ClassWrapper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClassWrapperRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
  private SortedMap<String, String> replacements;


  public ClassWrapperRenderer() {
    replacements = new TreeMap<String, String>(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        int result = o2.length() - o1.length();
        if (result == 0) {
          result = o1.compareTo(o2);
        }
        return result;
      }
    });

  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    final ClassWrapper classWrapper = (ClassWrapper) value;
    final String className = classWrapper.getClassName();
    final String abbreviatePackage = abbreviatePackage(className, replacements);
    return super.getTableCellRendererComponent(table, abbreviatePackage, isSelected, hasFocus, row, column);
  }

  Map<String, String> toMap(String configuration) {
    configuration = StringUtils.defaultString(configuration);
    Properties p = new Properties();
    Map<String, String> result = new HashMap<String, String>();
    try {
      p.load(new StringReader(configuration));
      for (Object o : p.keySet()) {
        final String key = o.toString();
        final String value = p.getProperty(key);
        result.put(key, value);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  String abbreviatePackage(String clazz, SortedMap<String, String> abbreviations) {
    for (String s : abbreviations.keySet()) {
      if (clazz.startsWith(s)) {
        return StringUtils.replaceOnce(clazz, s, abbreviations.get(s));
      }
    }
    return clazz;
  }

  public void reloadConfiguration(String propertyConfiguration) {
    replacements.clear();
    replacements.putAll(toMap(propertyConfiguration));

  }
}
