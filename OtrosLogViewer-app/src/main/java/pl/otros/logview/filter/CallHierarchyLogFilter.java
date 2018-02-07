/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.filter;

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CallHierarchyLogFilter extends AbstractLogFilter {

  private final Set<Integer> entryIds = new HashSet<>();
  private final Set<Integer> ids = new HashSet<>();
  private final JCheckBox showOnlyEntryExits;

  public CallHierarchyLogFilter() {
    super("Call hierarchy", "Call hierarchy log filter");
    showOnlyEntryExits = new JCheckBox("Show only entry/exits");
    showOnlyEntryExits.addChangeListener(e -> CallHierarchyLogFilter.this.listener.ifPresent(LogFilterValueChangeListener::valueChanged));
  }

  @Override
  public boolean accept(LogData logData, int row) {
    Integer logDataId = Integer.valueOf(logData.getId());
    if (showOnlyEntryExits.isSelected()) {
      return entryIds.contains(logDataId);
    } else {
      return entryIds.contains(logDataId) || ids.contains(logDataId);
    }
  }

  @Override
  public Component getGUI() {
    return showOnlyEntryExits;
  }

  public void setListId(Collection<Integer> listOfEntryIds, Collection<Integer> listOfIds) {
    entryIds.clear();
    entryIds.addAll(listOfEntryIds);
    ids.clear();
    ids.addAll(listOfIds);
    listener.ifPresent(LogFilterValueChangeListener::valueChanged);
  }

}
