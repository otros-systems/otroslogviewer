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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VfsTableModelHiddenFileRowFilterTest {


  @DataProvider(name = "candidateFiles")
  public Object[][] arbitraryName01() {
    return new Object[][]{
        {"_file01", true, true, true},
        {".file01", true, true, true},
        {"_file01", false, true, true},
        {".file01", false, true, true},

        {"_file01", true, false, false},
        {".file01", true, false, false},
        {"_file01", false, false, true},
        {".file01", false, false, false},


    };
  }

  @Test(dataProvider = "candidateFiles")
  public void testCheckIfInclude(String path, boolean hiddenAttribute, boolean showHidden, boolean expected) throws Exception {
    //given
    VfsTableModelHiddenFileRowFilter rowFilter = new VfsTableModelHiddenFileRowFilter(showHidden);
    FileObject fileObject = mock(FileObject.class);
    FileName fileName = mock(FileName.class);
    when(fileObject.getName()).thenReturn(fileName);
    when(fileName.getBaseName()).thenReturn(path);
    when(fileObject.isHidden()).thenReturn(hiddenAttribute);

    //when
    boolean result = rowFilter.checkIfInclude(fileObject);

    //then
    Assert.assertEquals(result, expected, String.format("Result for file \"%s\" with hidden attribute %s and showHidden checked %s should " +
        "be %s, was %s", path,
        hiddenAttribute, showHidden, result, expected));

  }
}
