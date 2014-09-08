package pl.otros.logview.gui.renderers;

import pl.otros.logview.gui.ClassWrapper;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by krzyh on 9/7/14.
 */
public class ClassWrapperRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    private OtrosApplication otrosApplication;

    Properties p = new Properties();

    public ClassWrapperRenderer(OtrosApplication otrosApplication) {
        this.otrosApplication = otrosApplication;
        p.setProperty("pl.otros.logviewer","${OLV}");

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final ClassWrapper classWrapper = (ClassWrapper) value;
//        otrosApplication.getConfiguration().getList(String.class,"a",new ArrayList<String>);
        final String className = classWrapper.getClassName();
        for(Object s: p.keySet()){
            String key = (String)s;
        }
        Component c = super.getTableCellRendererComponent(table, "CL:" + classWrapper.getClassName(), isSelected, hasFocus, row, column);
    return c;
    }

    TreeMap toMap(String key){
        final StringReader stringReader = new StringReader(key);
        Properties p = new Properties();
        TreeMap<String, String> result = new TreeMap<String,String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        try {
            p.load(stringReader);
            //TODO load all
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
