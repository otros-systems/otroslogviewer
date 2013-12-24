/*******************************************************************************
 * Copyright 2011 krzyh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.accept;

import org.junit.Test;
import pl.otros.logview.LogData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectedThreadAcceptConditionTest extends SelectionAwareAcceptConditionTestBase {

  @Test
  public void testAccept() {
    SelectedThreadAcceptCondition acceptCondition = new SelectedThreadAcceptCondition(table, dataTableModel);
    table.getSelectionModel().addListSelectionListener(acceptCondition);
    table.getSelectionModel().setSelectionInterval(1, 2);
    LogData[] logData = dataTableModel.getLogData();
    assertFalse(acceptCondition.accept(logData[0]));
    assertTrue(acceptCondition.accept(logData[1]));
    assertTrue(acceptCondition.accept(logData[2]));
    assertFalse(acceptCondition.accept(logData[3]));
    assertTrue(acceptCondition.accept(logData[4]));

  }

}
