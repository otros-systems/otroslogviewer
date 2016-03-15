/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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
package pl.otros.logview.accept;

import org.testng.annotations.BeforeMethod;
import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;

import javax.swing.*;

public class SelectionAwareAcceptConditionTestBase {

  protected JTable table;
  protected LogDataTableModel dataTableModel;
  protected String[] classes = {"a.a.A", "a.a.B", "a.b.A"};

  @BeforeMethod
  public void setUp() {
    dataTableModel = new LogDataTableModel();
    table = new JTable(dataTableModel);
    for (int i = 0; i < 5; i++) {
      LogData data = new LogData();
      data.setId(i);
      data.setThread(Integer.toString(i % 3));
      data.setClazz(classes[i % classes.length]);
      dataTableModel.add(data);

    }
  }

}
