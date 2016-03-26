/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.filter;

import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;
import pl.otros.logview.gui.PopupListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class ClassFilter extends AbstractLogFilter {

  private JTree tree;
  private DefaultMutableTreeNode rootNode;
  private HashMap<Clazz, DefaultMutableTreeNode> clazzNodeMap;
  private final HashSet<String> ignoreList = new HashSet<>();
  private final HashSet<String> focusList = new HashSet<>();
  private Mode mode = Mode.IGNORE_MODE;

  private enum Mode {
    IGNORE_MODE, FOCUS_MODE
  }

  public ClassFilter() {
    super("Class Filter", "Filtering events based on class/package. It supports \"ignore\" and \"focus on\" mode.");
  }

  @Override
  public boolean accept(LogData logData, int row) {
    String clazz = logData.getClazz();
    boolean result = true;
    if (mode == Mode.IGNORE_MODE) {
      result = true;
      for (String c : ignoreList) {
        if (clazz.startsWith(c)) {
          result = false;
          break;
        }
      }
    } else if (mode == Mode.FOCUS_MODE && focusList.size() > 0) {
      result = false;
      for (String c : focusList) {
        if (clazz.startsWith(c)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  @Override
  public Component getGUI() {
    return tree;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {
    this.collector = collector;

    initTree();

  }

  @Override
  public void setEnable(boolean enable) {
    super.setEnable(enable);
    if (enable) {
      reloadClasses();
    }
  }

  @Override
  public void setValueChangeListener(LogFilterValueChangeListener listener) {
    this.listener = listener;
  }

  public void ignoreClass(String... classes) {
    Collections.addAll(ignoreList, classes);
    mode = Mode.IGNORE_MODE;
    listener.valueChanged();
  }

  public void focusOn(String... classes) {
    Collections.addAll(focusList, classes);
    mode = Mode.FOCUS_MODE;
    listener.valueChanged();
  }

  private void initTree() {
    rootNode = new DefaultMutableTreeNode(new Clazz("root"));
    tree = new JTree(rootNode);
    tree.setCellRenderer(new TreeRenderer());

    clazzNodeMap = new HashMap<>();
    clazzNodeMap.put(new Clazz("root"), rootNode);
    initPopup();
  }

  private void initPopup() {
    JPopupMenu menu = new JPopupMenu("Focusing/Ignoring classes and packages");
    menu.add(new JLabel("Add package/class to \"Ignore\" list"));
    JMenuItem addIgnoreMenuItem = new JMenuItem("Add to ignore");
    addIgnoreMenuItem.addActionListener(new AddToListAction(ignoreList, Mode.IGNORE_MODE));
    JMenuItem removeIgnoreMenuItem = new JMenuItem("Remove from ignore");
    removeIgnoreMenuItem.addActionListener(new RemoveFromListAction(ignoreList));

    JMenuItem addToFocus = new JMenuItem("Focus on this class/package");
    addToFocus.addActionListener(new AddToListAction(focusList, Mode.FOCUS_MODE));
    JMenuItem removeFromFocus = new JMenuItem("Do not focus on this class/package");
    removeFromFocus.addActionListener(new RemoveFromListAction(focusList));

    Action clearSettingsAction = new ClearListAction(ignoreList, focusList);

    menu.add(addIgnoreMenuItem);
    menu.add(removeIgnoreMenuItem);
    menu.add(new JSeparator());
    menu.add(new JLabel("Add package/class to \"Focus on\" list"));
    menu.add(addToFocus);
    menu.add(removeFromFocus);
    menu.add(new JSeparator());
    menu.add(clearSettingsAction);
    tree.addMouseListener(new PopupListener(menu));

  }

  private void reloadClasses() {
    LogData[] ld = collector.getLogData();

    Set<String> classesSet = new TreeSet<>();
    for (LogData aLd : ld) {
      classesSet.add(aLd.getClazz());
    }

    // prepare packages nodes
    TreeSet<String> packages = new TreeSet<>();
    for (String string : classesSet) {
      Clazz clazz = new Clazz(string);
      String pack = clazz.pack;
      int start = 0;
      while (start < pack.length()) {
        int idx = pack.indexOf('.', start + 1);
        if (idx < 0) {
          idx = pack.length();
        }
        String upClazz = pack.substring(0, idx);
        packages.add(upClazz);
        start = idx;
      }
    }

    // Create packages tree
    for (String string : packages) {
      Clazz clazz = new Clazz(string);
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(clazz);
      DefaultMutableTreeNode parentNode;
      if (clazzNodeMap.containsKey(new Clazz(clazz.pack))) {
        parentNode = clazzNodeMap.get(new Clazz(clazz.pack));
      } else {
        parentNode = rootNode;
      }
      clazzNodeMap.put(clazz, newNode);
      parentNode.add(newNode);
    }

    // Create classes leafs
    for (String string : classesSet) {
      Clazz clazz = new Clazz(string);
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(clazz);
      DefaultMutableTreeNode parentNode;
      Clazz packetClazz = new Clazz(clazz.pack);
      if (clazzNodeMap.containsKey(packetClazz)) {
        parentNode = clazzNodeMap.get(packetClazz);
      } else {
        parentNode = new DefaultMutableTreeNode(packetClazz);
        rootNode.add(parentNode);
        clazzNodeMap.put(packetClazz, parentNode);
      }
      parentNode.add(newNode);
      clazzNodeMap.put(clazz, newNode);
    }

    tree.expandPath(new TreePath(rootNode));
    tree.repaint();
  }

  private static class Clazz implements Comparable<Clazz> {

    private final String pack;
    private final String clazz;

    public Clazz(String packageClazz) {
      if (packageClazz.length() == 0) {
        pack = "";
        clazz = "";
        return;
      }

      int idx = packageClazz.lastIndexOf('.');

      if (idx > 0) {
        pack = packageClazz.substring(0, idx);
        clazz = packageClazz.substring(idx + 1);
      } else {
        pack = "";
        clazz = packageClazz;
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Clazz) {
        Clazz clazz2 = (Clazz) obj;
        return pack.equals(clazz2.pack) && this.clazz.equals(clazz2.clazz);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return clazz.hashCode();
    }

    @Override
    public String toString() {
      return clazz;
    }

    @Override
    public int compareTo(Clazz o) {
      if (pack.equalsIgnoreCase(o.pack)) {
        return clazz.compareTo(o.clazz);
      } else {
        return pack.compareTo(o.pack);
      }
    }

    public String toFullString() {
      if (pack != null && pack.length() > 0) {
        return pack + '.' + clazz;
      } else {
        return clazz;
      }
    }
  }

  private class AddToListAction implements ActionListener {

    private final HashSet<String> list;
    private final Mode mode;

    public AddToListAction(HashSet<String> list, Mode mode) {
      this.list = list;
      this.mode = mode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TreePath[] selectedPaths = tree.getSelectionModel().getSelectionPaths();
      for (TreePath treePath : selectedPaths) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        Clazz userObject = (Clazz) node.getUserObject();
        list.add(userObject.toFullString());
      }
      ClassFilter.this.mode = mode;
      listener.valueChanged();
    }

  }

  public class RemoveFromListAction implements ActionListener {

    private final HashSet<String> list;

    public RemoveFromListAction(HashSet<String> list) {
      super();
      this.list = list;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TreePath[] selectedPaths = tree.getSelectionModel().getSelectionPaths();
      for (TreePath treePath : selectedPaths) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        Clazz userObject = (Clazz) node.getUserObject();
        list.remove(userObject.toFullString());
      }
      listener.valueChanged();
    }

  }

  public class ClearListAction extends AbstractAction {

    private final HashSet<String>[] lists;

    public ClearListAction(HashSet<String>... lists) {
      this.lists = lists;
      this.putValue(NAME, "Clear settings");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      for (HashSet<String> l : lists) {
        l.clear();
      }
      listener.valueChanged();
      tree.repaint();
    }

  }

  private class TreeRenderer extends DefaultTreeCellRenderer {

    private final ImageIcon clazzIcon;
    private final ImageIcon clazzIgnoredIcon;
    private final ImageIcon packageOpenIcon;
    private final ImageIcon packageOpenIgnoreIcon;
    private final ImageIcon packageClosedIcon;
    private final ImageIcon packageClosedIgnoredIcon;
    private final Color background;

    public TreeRenderer() {
      clazzIcon = Icons.CLASS;
      clazzIgnoredIcon = Icons.CLASS_IGNORED;
      packageOpenIcon = Icons.PACKAGE_OPEN;
      packageOpenIgnoreIcon = Icons.PACKAGE_OPEN_IGNORED;
      packageClosedIcon = Icons.PACKAGE_CLOSE;
      packageClosedIgnoredIcon = Icons.PACKAGE_OPEN_IGNORED;
      background = getBackground();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
      Component parent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
      Clazz clazz = (Clazz) ((DefaultMutableTreeNode) value).getUserObject();
      Font font = getFont();
      JLabel l = (JLabel) parent;
      Icon icon = l.getIcon();
      int style = Font.PLAIN;
      boolean ignore = mode.equals(Mode.IGNORE_MODE) && ignoreList.contains(clazz.toFullString());
      boolean focused = mode.equals(Mode.FOCUS_MODE) && focusList.contains(clazz.toFullString());

      l.setBackground(background);
      l.setOpaque(false);

      if (ignore) {
        style = Font.ITALIC;
      } else if (focused) {
        style = Font.BOLD;
      }

      if (!focus && focused) {
        l.setBackground(Color.YELLOW);
        l.setOpaque(true);
      }

      if (leaf && ignore) {
        icon = clazzIgnoredIcon;
      } else if (leaf) {
        icon = clazzIcon;
      } else if (expanded && ignore) {
        icon = packageOpenIgnoreIcon;
      } else if (expanded) {
        icon = packageOpenIcon;
      } else if (ignore) {
        icon = packageClosedIgnoredIcon;
      } else {
        icon = packageClosedIcon;
      }
      Font newFont = font.deriveFont(style);
      parent.setFont(newFont);
      l.setIcon(icon);
      return parent;
    }
  }
}
