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
package pl.otros.logview.filter;

import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.theme.Theme;

import java.util.Properties;
import java.util.logging.Level;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class LevelFilterTest {

  @Test
  public void testAccept() {
    LogData ld = new LogData();
    ld.setLevel(Level.FINEST);

    LevelFilter filter = new LevelFilter();
    filter.init(new Properties(), new LogDataTableModel(), mock(Theme.class));
    filter.setPassLevel(Level.ALL.intValue());
    assertTrue(filter.accept(ld, 0));

    filter.setPassLevel(Level.FINEST.intValue());
    assertTrue(filter.accept(ld, 0));

    filter.setPassLevel(Level.FINER.intValue());
    assertFalse(filter.accept(ld, 0));

    filter.setPassLevel(Level.INFO.intValue());
    assertFalse(filter.accept(ld, 0));

    ld.setLevel(Level.INFO);
    assertTrue(filter.accept(ld, 0));

    ld.setLevel(Level.SEVERE);
    assertTrue(filter.accept(ld, 0));

    filter.setPassLevel(Level.WARNING.intValue());
    assertTrue(filter.accept(ld, 0));

  }

}
