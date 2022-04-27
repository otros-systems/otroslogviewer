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
package pl.otros.logview.gui.message;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import pl.otros.logview.TestUtils;

import java.util.SortedSet;

public class SoapFinderTest {

  @Test
  public void testFindSoapTag() throws Exception {
    String soap1Formatted = TestUtils.getResourceFile("soap/1-soapMessageFormmated.txt");
    String soap1UnFormatted = TestUtils.getResourceFile("soap/1-soapMessage.txt");
    String soap2Formatted = TestUtils.getResourceFile("soap/2-soapMessageFormmated.txt");
    String soap2UnFormatted = TestUtils.getResourceFile("soap/2-soapMessage.txt");

    String soap1xRequestUnFormatted = TestUtils.getResourceFile("soap/soap.1.request.xml");
    String soap2xRequestUnFormatted = TestUtils.getResourceFile("soap/soap.2.request.xml");
    String soap3xRequestUnFormatted = TestUtils.getResourceFile("soap/soap.3.request.xml");
    String soap4xRequestUnFormatted = TestUtils.getResourceFile("soap/soap.4.request.xml");

    String soap1xRequestFormatted = TestUtils.getResourceFile("soap/soap.1.request.formatted.xml");
    String soap2xRequestFormatted = TestUtils.getResourceFile("soap/soap.2.request.formatted.xml");
    String soap3xRequestFormatted = TestUtils.getResourceFile("soap/soap.3.request.formatted.xml");
    String soap4xRequestFormatted = TestUtils.getResourceFile("soap/soap.4.request.formatted.xml");

    String soap1xResponseUnFormatted = TestUtils.getResourceFile("soap/soap.1.response.xml");
    String soap2xResponseUnFormatted = TestUtils.getResourceFile("soap/soap.2.response.xml");
    String soap3xResponseUnFormatted = TestUtils.getResourceFile("soap/soap.3.response.xml");
    String soap4xResponseUnFormatted = TestUtils.getResourceFile("soap/soap.4.response.xml");

    String soap1xResponseFormatted = TestUtils.getResourceFile("soap/soap.1.response.formatted.xml");
    String soap2xResponseFormatted = TestUtils.getResourceFile("soap/soap.2.response.formatted.xml");
    String soap3xResponseFormatted = TestUtils.getResourceFile("soap/soap.3.response.formatted.xml");
    String soap4xResponseFormatted = TestUtils.getResourceFile("soap/soap.4.response.formatted.xml");

    SoapFinder soapFinder = new SoapFinder();
    AssertJUnit.assertEquals("SoAp", soapFinder.findSoapTag(soap1Formatted));
    AssertJUnit.assertEquals("SoAp", soapFinder.findSoapTag(soap1UnFormatted));
    AssertJUnit.assertEquals("soap", soapFinder.findSoapTag(soap2Formatted));
    AssertJUnit.assertEquals("soap", soapFinder.findSoapTag(soap2UnFormatted));

    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap1xRequestFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap2xRequestFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap3xRequestFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap4xRequestFormatted));

    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap1xRequestUnFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap2xRequestUnFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap3xRequestUnFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap4xRequestUnFormatted));

    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap1xResponseFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap2xResponseFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap3xResponseFormatted));
    AssertJUnit.assertEquals("SOAP-ENV", soapFinder.findSoapTag(soap4xResponseFormatted));

    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap1xResponseUnFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap2xResponseUnFormatted));
    AssertJUnit.assertEquals("env", soapFinder.findSoapTag(soap3xResponseUnFormatted));
    AssertJUnit.assertEquals("SOAP-ENV", soapFinder.findSoapTag(soap4xResponseUnFormatted));

  }

  @Test
  public void testFindSoap() throws Exception {

    String stringWithSoaps = TestUtils.getResourceFile("soap/stringWithSoap.txt");
    AssertJUnit.assertFalse("The test expected file 'soap/stringWithSoap.txt' in unix format.", stringWithSoaps.contains("\r"));
    SoapFinder finder = new SoapFinder();
    SortedSet<SubText> findSoaps = finder.findSoaps(stringWithSoaps);
    // 60,300
    AssertJUnit.assertEquals(2, findSoaps.size());
    SubText first = findSoaps.first();
    AssertJUnit.assertEquals(38, first.getStart());
    AssertJUnit.assertEquals(299, first.getEnd());
    SubText last = findSoaps.last();
    AssertJUnit.assertEquals(366, last.getStart());
    AssertJUnit.assertEquals(634, last.getEnd());

  }
}
