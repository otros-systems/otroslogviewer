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

package pl.otros.logview.gui.actions;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.filter.CallHierarchyLogFilter;
import pl.otros.logview.uml.Message;
import pl.otros.logview.uml.Message.MessageType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.Collectors;

public class ShowCallHierarchyAction extends FocusOnThisAbstractAction<CallHierarchyLogFilter> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShowCallHierarchyAction.class.getName());

  public ShowCallHierarchyAction(CallHierarchyLogFilter filter, JCheckBox filterEnableCheckBox, OtrosApplication otrosApplication) {
    super(filter, filterEnableCheckBox, otrosApplication);
    this.putValue(NAME, "Show call hierarchy");
    this.putValue(SHORT_DESCRIPTION, "Filter events to call hierarchy. Logging of method entry/return have to be present.");
  }

  @Override
  public void action(ActionEvent e, CallHierarchyLogFilter filter, LogData... selectedLogData) throws Exception {
    JXTable jTable = getOtrosApplication().getSelectPaneJXTable();
    int selected = jTable.getSelectedRow();
    selected = jTable.convertRowIndexToModel(selected);
    ArrayList<Integer> listOfEvents2 = new ArrayList<>();
    HashSet<Integer> listEntryEvents = new HashSet<>();

    LogDataTableModel dataTableModel = getOtrosApplication().getSelectedPaneLogDataTableModel();
    try {
      findCallHierarchyEvents(selected, dataTableModel, listEntryEvents, listOfEvents2);
    } catch (NoSuchElementException e1) {
      LOGGER.error("Log file do not have consistent Entry/Return in logs");
      throw new Exception("Log file do not have consistent Entry/Return in logs", e1);
    }
    filter.setListId(listEntryEvents, listOfEvents2);
    filterEnableCheckBox.setSelected(true);
    filter.setEnable(true);
  }

  protected void findCallHierarchyEvents(int selected, LogDataTableModel model, Collection<Integer> listEntryEvents, Collection<Integer> listOfEvents2) {
    LogData ld = model.getLogData(selected);
    String thread = ld.getThread();
    LinkedList<LogData> stack = new LinkedList<>();
    HashMap<Integer, ArrayList<Integer>> allEventsInCallHierarchyMap = new HashMap<>();

    int rowCount = model.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      LogData logData = model.getLogData(i);
      if (!logData.getThread().equals(thread)) {
        continue;
      }
      Message m = new Message(logData.getMessage());
      Integer stackSize = stack.size();
      if (!allEventsInCallHierarchyMap.containsKey(stackSize)) {
        allEventsInCallHierarchyMap.put(stackSize, new ArrayList<>());
      }
      ArrayList<Integer> tempListOfEvents = allEventsInCallHierarchyMap.get(stackSize);
      if (m.getType().equals(MessageType.TYPE_ENTRY)) {
        stack.addLast(logData);
      } else if (m.getType().equals(MessageType.TYPE_EXIT) && theSameLogMethod(stack.getLast(), logData)) {
        stack.removeLast();
        tempListOfEvents.clear();
      } else {
        tempListOfEvents.add(logData.getId());
      }
      if (logData.getId() == ld.getId()) {
        break;
      }
    }

    allEventsInCallHierarchyMap.values().forEach(listOfEvents2::addAll);

    listEntryEvents.addAll(stack.stream().map(LogData::getId).collect(Collectors.toList()));
  }

  protected boolean theSameLogMethod(LogData ld1, LogData ld2) {
    return StringUtils.equals(ld1.getClazz(), ld2.getClazz()) && StringUtils.equals(ld1.getMethod(), ld2.getMethod());
  }

}
