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
package pl.otros.logview.accept;

import pl.otros.logview.api.gui.HasIcon;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.util.CompoundIcon;

import javax.swing.*;
import java.util.ArrayList;
import java.util.logging.Level;

public class LevelLowerAcceptCondition extends AbstractAcceptContidion implements HasIcon {

  protected Level level;
  protected Icon icon;
  private final int levelIntValue;

  public LevelLowerAcceptCondition(Level level) {
    super();
    this.level = level;
    levelIntValue = level.intValue();
    this.name = String.format("Level <%s", level.getName());
    this.description = String.format("Level of log event is lower than %s", level.getName());
    createIcon();
  }

  private void createIcon() {
    Level[] levels = {Level.FINEST,//
      Level.FINER,//
      Level.FINE,//
      Level.CONFIG,//
      Level.INFO,//
      Level.WARNING,//
      Level.SEVERE,//
    };
    ArrayList<Icon> iconsList = new ArrayList<>();
    for (Level l : levels) {
      if (l.intValue() < levelIntValue) {
        final Icon iconByLevel = LevelRenderer.getIconByLevel(l);
        iconsList.add(iconByLevel);
      }
    }
    icon = new CompoundIcon(iconsList);
  }

  @Override
  public boolean accept(LogData data) {
    return data.getLevel() != null && data.getLevel().intValue() < levelIntValue;
  }

  @Override
  public Icon getIcon() {
    return icon;
  }

}
