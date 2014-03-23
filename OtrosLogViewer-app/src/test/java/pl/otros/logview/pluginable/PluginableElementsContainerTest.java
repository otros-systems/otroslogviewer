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

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.Collection;

public class PluginableElementsContainerTest {

  public PluginableElementsContainer<PluginableClass> container;

  @BeforeMethod
public void setup() {
    container = new PluginableElementsContainer<PluginableElementsContainerTest.PluginableClass>();
  }

  @Test
  public void testGetElements() {
    PluginableClass p = new PluginableClass();
    container.addElement(p);
    Collection<PluginableClass> elements = container.getElements();
    AssertJUnit.assertEquals(1, elements.size());

  }

  @Test
  public void testAddElement() {
    PluginableListener listener = new PluginableListener();
    container.addListener(listener);

    PluginableClass p1 = new PluginableClass();
    container.addElement(p1);
    AssertJUnit.assertTrue(listener.added);

    container.addElement(p1);
    AssertJUnit.assertTrue(listener.changed);

    Collection<PluginableClass> elements = container.getElements();
    AssertJUnit.assertEquals(1, elements.size());
  }

  @Test
  public void testRemoveElement() {
    PluginableListener listener = new PluginableListener();
    container.addListener(listener);

    PluginableClass p1 = new PluginableClass();
    container.addElement(p1);
    AssertJUnit.assertTrue(listener.added);
    Collection<PluginableClass> elements = container.getElements();
    AssertJUnit.assertEquals(1, elements.size());

    container.removeElement(p1);
    AssertJUnit.assertTrue(listener.removed);
    elements = container.getElements();
    AssertJUnit.assertEquals(0, elements.size());
  }

  static class PluginableListener implements PluginableElementEventListener<PluginableClass> {

    boolean added = false;
    boolean removed = false;
    boolean changed = false;

    @Override
    public void elementAdded(PluginableClass element) {
      added = true;
    }

    @Override
    public void elementRemoved(PluginableClass element) {
      removed = true;
    }

    @Override
    public void elementChanged(PluginableClass element) {
      changed = true;
    }

  }

  static class PluginableClass implements PluginableElement {

    @Override
    public String getName() {
      return "name";
    }

    @Override
    public String getDescription() {
      return "desc";
    }

    @Override
    public String getPluginableId() {
      return "id";
    }

    @Override
    public int getApiVersion() {
      return 1;
    }

  }
}
