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
package pl.otros.logview.gui.actions.read;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.Stoppable;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.ref.SoftReference;

public class ReadingStopperForRemove implements HierarchyListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadingStopperForRemove.class.getName());

  private final SoftReference<Stoppable> reference;

  public ReadingStopperForRemove(Stoppable stoppable) {
    super();
    reference = new SoftReference<>(stoppable);
  }

  @Override
  public void hierarchyChanged(HierarchyEvent e) {
    if (e.getChangeFlags() == 1 && e.getChanged().getParent() == null) {
      Stoppable stoppable = reference.get();
      LOGGER.debug("Tab removed, stopping thread if reference is != null (actual: " + stoppable + ")");
      if (stoppable != null) {
        stoppable.stop();
      }
    }

  }

}
