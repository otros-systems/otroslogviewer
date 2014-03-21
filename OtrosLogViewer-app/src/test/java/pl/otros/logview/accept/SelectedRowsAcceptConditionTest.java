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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;

public class SelectedRowsAcceptConditionTest extends SelectionAwareAcceptConditionTestBase {

  @Test
  public void testAccept() {
    SelectedEventsAcceptCondition acceptCondition = new SelectedEventsAcceptCondition(table, dataTableModel);
    table.getSelectionModel().addListSelectionListener(acceptCondition);
    table.getSelectionModel().setSelectionInterval(2, 3);

    LogData[] logData = dataTableModel.getLogData();
    assertFalse(acceptCondition.accept(logData[0]));
    assertFalse(acceptCondition.accept(logData[1]));
    assertTrue(acceptCondition.accept(logData[2]));
    assertTrue(acceptCondition.accept(logData[3]));
    assertFalse(acceptCondition.accept(logData[4]));
  }

}
