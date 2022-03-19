/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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
 */

package pl.otros.vfs.browser.table;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class VfsTableModelFileNameRowFilterTest {

  @DataProvider(name = "testCheckIfIncludeDataProvider")
  public Object[][] testCheckIfIncludeDataProvider() {
    return new Object[][]{
        {"file name", "file", true},
        {"File Name", "file", true},
        {"fi", "file", false},

        {"file 011", "fi*", true},
        {"file 011", "f*1", true},
        {"file 011", "f*0", false},
        {"file 011", "fi?e?011", true},

        {"file011", "/\\w+011", true},
        {"file011", "/\\w{4}011", true},
        {"file 011", "/\\w{4}\\s011", true},
        {"file 011", "/\\w{4}\\s\\d+", true},
        {"file 011", "/\\w{4}\\s\\d", false},

    };
  }

  @Test(dataProvider = "testCheckIfIncludeDataProvider")
  public void testCheckIfInclude(String baseName, String patternText, boolean expected) throws Exception {
    //given
    VfsTableModelFileNameRowFilter rowFilter = new VfsTableModelFileNameRowFilter(null);

    //when
    boolean result = rowFilter.checkIfInclude(baseName, patternText);

    //then
    Assert.assertEquals(result, expected, String.format("%s with pattern %s should be accepted: %s but result was %s", baseName, patternText,
        expected, result
    ));
  }

  @Test
  public void testPreparePatternString() throws Exception {
    //TODO write test

  }

  @Test
  public void testConvertRegex() throws Exception {
    //TODO write test
  }
}
