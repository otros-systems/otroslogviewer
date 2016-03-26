/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.api.pluginable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class PluginableElementsContainer<T extends PluginableElement> {

  private final HashSet<PluginableElementEventListener<T>> listeners;
  private final HashMap<String, T> elements;

  public PluginableElementsContainer() {
    listeners = new HashSet<>();
    elements = new HashMap<>();
  }

  public Collection<T> getElements() {
    return new ArrayList<>(elements.values());
  }

  public T getElement(String pluginableId) {
    return elements.get(pluginableId);
  }

  public boolean contains(T element) {
    return elements.containsKey(element.getPluginableId());
  }

  public void addElement(T element) {
    boolean allreadyHave = false;
    synchronized (elements) {
      allreadyHave = elements.containsKey(element.getPluginableId());
      elements.put(element.getPluginableId(), element);
    }
    synchronized (listeners) {
      if (allreadyHave) {
        notifyChange(element);
      } else {
        notifyAdd(element);
      }
    }
  }

  public void addListener(PluginableElementEventListener<T> listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void removeListener(PluginableElementEventListener<T> listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  public void removeElement(T element) {
    synchronized (elements) {
      elements.remove(element.getPluginableId());
    }
    synchronized (listeners) {
      for (PluginableElementEventListener l : listeners) {
        l.elementRemoved(element);
      }
    }
  }

  public void changeElement(T element) {
    synchronized (listeners) {
      for (PluginableElementEventListener l : listeners) {
        l.elementChanged(element);
      }
    }
  }

  protected void notifyAdd(T element) {
    synchronized (listeners) {
      for (PluginableElementEventListener l : listeners) {
        l.elementAdded(element);
      }
    }
  }

  protected void notifyRemove(T element) {
    synchronized (listeners) {
      for (PluginableElementEventListener l : listeners) {
        l.elementRemoved(element);
      }
    }
  }

  protected void notifyChange(T element) {
    synchronized (listeners) {
      for (PluginableElementEventListener l : listeners) {
        l.elementChanged(element);
      }
    }
  }
}
