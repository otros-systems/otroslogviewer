package pl.otros.logview.gui.renderers;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ClassWrapper;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClassWrapperRenderer implements TableCellRenderer {

  private final Comparator<String> stringLengthComparator = (o1, o2) -> {
    int result = o2.length() - o1.length();
    if (result == 0) {
      result = o1.compareTo(o2);
    }
    return result;
  };

  private SortedMap<String, String> replacements;
  private final JLabel label;


  public ClassWrapperRenderer() {
    replacements = toMap("");
    label = new JLabel();
    label.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String abbreviatePackage = "";
    if (value != null) {
      final ClassWrapper classWrapper = (ClassWrapper) value;
      final String className = classWrapper.getClassName();
      abbreviatePackage = abbreviatePackageUsingMappings(className, replacements);

      int availableWidth = table.getColumnModel().getColumn(column).getWidth();
      availableWidth -= table.getIntercellSpacing().getWidth();
      if (label.getBorder() != null) {
        Insets borderInsets = label.getBorder().getBorderInsets(label);
        availableWidth -= (borderInsets.left + borderInsets.right);
      }
      FontMetrics fm = label.getFontMetrics(label.getFont());

      abbreviatePackage = abbreviatePackagesToSingleLetter(abbreviatePackage, availableWidth, fm);

      while (fm.stringWidth(abbreviatePackage) > availableWidth && abbreviatePackage.length() > 0) {
        abbreviatePackage = abbreviatePackage.substring(1);
      }

    }
    label.setText(abbreviatePackage);
    return label;
  }

  public String abbreviatePackagesToSingleLetter(String abbreviatePackage, int availableWidth, FontMetrics fm) {
    String result = abbreviatePackage;
    if (fm.stringWidth(result) > availableWidth) {
      final java.util.List<String> split = Splitter.on('.').splitToList(result);
      int index = 0;
      while (fm.stringWidth(result) > availableWidth && index < split.size() - 1) {
        java.util.List<String> list = new ArrayList<>(split.size());
        for (int i = 0; i < split.size(); i++) {
          final String s = split.get(i);
          list.add(i <= index && s.length()>0 ? s.substring(0, 1) : s);
        }
        result = Joiner.on(".").join(list);
        index++;
      }
    }
    return result;
  }

  protected SortedMap<String, String> toMap(String configuration) {
    Properties p = new Properties();
    SortedMap<String, String> result = new TreeMap<>(stringLengthComparator);
    try {
      p.load(new StringReader(StringUtils.defaultString(configuration)));
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

  protected String abbreviatePackageUsingMappings(String clazz, SortedMap<String, String> abbreviations) {
    for (String s : abbreviations.keySet()) {
      if (clazz.startsWith(s)) {
        return StringUtils.replaceOnce(clazz, s, abbreviations.get(s));
      }
    }
    return clazz;
  }

  public void reloadConfiguration(String propertyConfiguration) {
    replacements = toMap(propertyConfiguration);
  }
}
