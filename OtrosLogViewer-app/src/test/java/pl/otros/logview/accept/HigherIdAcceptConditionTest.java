/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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

public class HigherIdAcceptConditionTest extends 
SelectionAwareAcceptConditionTestBase {

  @Test
  public void testAccept() {
    HigherIdAcceptCondition acceptCondition = new HigherIdAcceptCondition(table, dataTableModel);
    table.getSelectionModel().addListSelectionListener(acceptCondition);
    table.getSelectionModel().setSelectionInterval(3, 3);
    LogData[] logData = dataTableModel.getLogData();
    for (LogData logData2 : logData) {
      if (logData2.getId() > 3) {
        assertTrue(acceptCondition.accept(logData2));
      } else {
        assertFalse(acceptCondition.accept(logData2));
      }
    }

  }

}
