package pl.otros.logview.api;

import org.jdesktop.swingx.JXTable;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;

import javax.swing.*;
import java.util.Collection;

public abstract class LogViewPanelI extends JPanel implements LogDataCollector {
  public abstract JTextPane getLogDetailTextArea();

  public abstract void add(LogData[] autoResizeSubsequent);

  public abstract void add(LogData logData);

  public abstract LogData[] getLogData();

  public abstract int[] getSelectedRowsInModel();

  public abstract void updateMarkerMenu(Collection<AutomaticMarker> markers);

  public abstract JXTable getTable();

  public abstract LogDataTableModel getDataTableModel();

  public abstract JPanel getLogsMarkersPanel();

  @Override
  public abstract int clear();

  public abstract LogData getDisplayedLogData();

  public abstract void setDisplayedLogData(LogData displayedLogData);

  public abstract OtrosJTextWithRulerScrollPane<JTextPane> getLogDetailWithRulerScrollPane();

  public abstract PluginableElementsContainer<MessageColorizer> getSelectedMessageColorizersContainer();

  public abstract PluginableElementsContainer<MessageFormatter> getSelectedMessageFormattersContainer();

  public abstract  JToolBar getMessageDetailToolbar();
}
