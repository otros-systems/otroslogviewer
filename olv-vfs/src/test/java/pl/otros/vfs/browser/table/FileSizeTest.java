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

/**
 * Created with IntelliJ IDEA.
 * User: krzyh
 * Date: 10/30/13
 * Time: 7:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileSizeTest {

  @DataProvider (name = "testFileSizeDataProvider")
  public Object[][] testFileSizeDataProvider() {
    return new Object[][]{
        {"0", 0l},
        {"0 B", 0l},
        {"0B", 0l},
        {"1kB", 1024l},
        {"1 kB", 1024l},
        {"1,5kB", 1024l + 512},
        {"1.5kB", 1024l + 512},
        {"1 MB", 1024l * 1024},
        {"1 GB", 1024l * 1024 * 1024},
        {"1 TB", 1024l * 1024 * 1024 * 1024}
    };
  }

  @Test (dataProvider = "testFileSizeDataProvider")
  public void testFileSize(String constructorArgs, long bytes) {
    Assert.assertEquals(new FileSize(constructorArgs).getBytes(), bytes);
  }


  @DataProvider (name = "testToStringDataProvider")
  Object[][] testToStringDataProvider() {
    return new Object[][]{
        {0, "0 B"},
        {100, "100 B"},
        {10 * 1024, "10 KB"},
        {15l * 1024 * 1024, "15 MB"},
        {25l * 1024 * 1024 * 1024, "25 GB"},
        {35l * 1024 * 1024 * 1024 * 1024, "35 TB"},
        {1536, "1.5 KB"},
        {1025, "1 KB"},
        {1024 * 1024 - 1, "1024 KB"}

    };
  }

  @Test (dataProvider = "testToStringDataProvider")
  public void testToString(long size, String expectedResult) throws Exception {
    FileSize fileSize = new FileSize(size);
    Assert.assertEquals(fileSize.toString(), expectedResult);
  }

  @DataProvider (name = "testGetMultiplier")
  Object[][] testGetMultiplierDataProvider() {
    return new Object[][]{
        {'T', 1024l * 1024 * 1024 * 1024},
        {'t', 1024l * 1024 * 1024 * 1024},
        {'g', 1024 * 1024 * 1024},
        {'G', 1024 * 1024 * 1024},
        {'m', 1024 * 1024},
        {'M', 1024 * 1024},
        {'K', 1024},
        {'k', 1024},
        {'?', 1}
    };

  }

  @Test (dataProvider = "testGetMultiplier")
  public void testGetMultiplier(char c, long multiplier) throws Exception {
    FileSize fs = new FileSize(1);
    Assert.assertEquals(fs.getMultiplier(c), multiplier);
  }


  @Test (dataProvider = "testToStringDataProvider")
  public void testConvertToStringRepresentation(long size, String expectedResult) throws Exception {
    Assert.assertEquals(FileSize.convertToStringRepresentation(size), expectedResult);
  }


  @DataProvider (name = "testCompareTo")
  public Object[][] testCompareToDataProvider() {
    return new Object[][]{
        {1, 0, 1},
        {Long.MAX_VALUE, Long.MIN_VALUE, 1},
        {Long.MAX_VALUE, Long.MAX_VALUE, 0},
        {Long.MIN_VALUE, Long.MIN_VALUE, 0},
        {0, 0, 0},
    };
  }

  @Test (dataProvider = "testCompareTo")
  public void testCompareTo(long size1, long size2, int result) throws Exception {
    Assert.assertEquals(new FileSize(size1).compareTo(new FileSize(size2)), result);
  }
}
