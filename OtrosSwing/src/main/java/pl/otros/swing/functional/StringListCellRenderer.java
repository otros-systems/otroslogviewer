package pl.otros.swing.functional;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class StringListCellRenderer<E> implements ListCellRenderer<E> {

  private final Function<E,String> mapping;
  private final DefaultListCellRenderer defaultListCellRenderer;

  public StringListCellRenderer(Function<E, String> mapping) {
    this.mapping = mapping;
    defaultListCellRenderer = new DefaultListCellRenderer();
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    final String string = mapping.apply(value);
    return defaultListCellRenderer.getListCellRendererComponent(list, string, index, isSelected, cellHasFocus);
  }

}
