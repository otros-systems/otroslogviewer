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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;

import java.util.logging.Level;

public class LevelLowerAcceptConditionTest {

  @Test
  public void testAccept() {
    // given
    LogData ld = new LogData();
    ld.setLevel(Level.INFO);

    // when
    // then
    assertFalse(new LevelLowerAcceptCondition(Level.FINEST).accept(ld));
    assertFalse(new LevelLowerAcceptCondition(Level.FINER).accept(ld));
    assertFalse(new LevelLowerAcceptCondition(Level.FINE).accept(ld));
    assertFalse(new LevelLowerAcceptCondition(Level.CONFIG).accept(ld));
    assertFalse(new LevelLowerAcceptCondition(Level.INFO).accept(ld));
    assertTrue(new LevelLowerAcceptCondition(Level.WARNING).accept(ld));
    assertTrue(new LevelLowerAcceptCondition(Level.SEVERE).accept(ld));
  }

}
