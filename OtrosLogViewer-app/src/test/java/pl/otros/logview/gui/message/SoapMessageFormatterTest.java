/*
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.gui.message;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.apache.commons.io.IOUtils;

public class SoapMessageFormatterTest {

	private final SoapMessageFormatter formatter = new SoapMessageFormatter();

	@Test
	public void testFormatWithoutRemovingMultiRefs() throws Exception {
		//given
		String soap = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.xml"));
		String expected = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.formatted.xml"));
		formatter.setRemoveMultiRefs(false);

		//when
		String format = formatter.format(soap).trim();
    System.out.println(format);
    //then
		AssertJUnit.assertEquals(expected.replaceAll("\r",""), format.replaceAll("\r",""));
	}

	@Test
	public void testFormatWithRemovingMultiRefs() throws Exception {
		//given
		String soap = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.xml"));
		String expected = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.formatted.flatted.xml"));
		formatter.setRemoveMultiRefs(true);

		//when
		String format = formatter.format(soap).trim();

		//then
		AssertJUnit.assertEquals(expected.replaceAll("\r", ""), format.replaceAll("\r",""));
	}

	@Test
	public void testRemoveMultiRefs() throws Exception {
		//given
		String soap = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.xml"));
		String expected = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithMultiref.flatted.xml"));

		//when
		String removeMultiRefs = formatter.removeMultiRefs(soap);

		//then
		AssertJUnit.assertEquals(expected, removeMultiRefs);
	}

  @Test
  public void removeXsiFromNulls() throws Exception {
    //given
    String soap = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithNull.xml"));
    String expected = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("soap/soapWithNull.formatted.xml"));

    //when
    String removeMultiRefs = formatter.removeXsiFromNulls(soap);

    //then
    System.out.println(removeMultiRefs);
    AssertJUnit.assertEquals(expected, removeMultiRefs);
  }

  @Test
  public void removeXsiFromNullsFragment(){
    String s = formatter.removeXsiFromNulls("<errorListArray xmlns:ns3=\"http://some.com/class/a/\" xsi:type=\"ns3:Error\" xsi:nil=\"true\"/>");
    System.out.println(s);
    System.out.flush();
    AssertJUnit.assertEquals("<errorListArray/>", s);
  }

}
