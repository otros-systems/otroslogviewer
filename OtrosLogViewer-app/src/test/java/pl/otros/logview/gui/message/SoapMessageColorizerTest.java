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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

public class SoapMessageColorizerTest {

	private SoapMessageColorizer colorizer = new SoapMessageColorizer();

	protected String loadResources(String resources) throws IOException {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resources);
		return IOUtils.toString(resourceAsStream);
	}


	@Test
	public void testColorize() throws Exception {
		String message = loadResources("soap/soap.1.request.xml");
		Collection<MessageFragmentStyle> colorize = colorizer.colorize(message);
		HashMap<Integer, MessageFragmentStyle> map = new HashMap<Integer, MessageFragmentStyle>(colorize.size());
		for (MessageFragmentStyle messageFragmentStyle : colorize) {
			map.put(Integer.valueOf(messageFragmentStyle.getOffset()), messageFragmentStyle);
		}

		AssertJUnit.assertEquals(map.get(Integer.valueOf(88)).getStyle(), colorizer.styleOperator);
		AssertJUnit.assertEquals(map.get(Integer.valueOf(89)).getStyle(), colorizer.styleElementName);
		AssertJUnit.assertEquals(map.get(Integer.valueOf(121)).getStyle(), colorizer.styleAttribtuteName);
		AssertJUnit.assertEquals(map.get(Integer.valueOf(129)).getStyle(), colorizer.styleAttribtuteValue);
		AssertJUnit.assertEquals(map.get(Integer.valueOf(180)).getStyle(), colorizer.styleContent);
	}
}
