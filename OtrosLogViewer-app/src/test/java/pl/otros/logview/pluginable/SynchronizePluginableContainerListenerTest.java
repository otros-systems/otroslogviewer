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
package pl.otros.logview.pluginable;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import pl.otros.logview.gui.message.SoapMessageFormatter;

import static org.mockito.Mockito.*;

public class SynchronizePluginableContainerListenerTest {

  PluginableElementsContainer<PluginableElement> d = null;
  SynchronizePluginableContainerListener<PluginableElement> s = null;

  @BeforeMethod
public void setup() {
    d = mock(PluginableElementsContainer.class);
    s = new SynchronizePluginableContainerListener<>(d);
  }

  @Test
  public void testSynchronizePluginableContainerListener() {
    assertEquals(d, s.destination);
  }

  @Test
  public void testElementAdded() {
    SoapMessageFormatter element = new SoapMessageFormatter();
    s.elementAdded(element);
    verify(d, only()).addElement(element);
  }

  @Test
  public void testElementRemoved() {
    SoapMessageFormatter element = new SoapMessageFormatter();
    s.elementRemoved(element);
    verify(d, only()).removeElement(element);
  }

  @Test
  public void testElementChanged() {
    SoapMessageFormatter element = new SoapMessageFormatter();
    s.elementChanged(element);
    verify(d, only()).changeElement(element);
  }

}
