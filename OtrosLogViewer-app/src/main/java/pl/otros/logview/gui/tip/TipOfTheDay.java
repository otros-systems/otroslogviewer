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
package pl.otros.logview.gui.tip;

import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.JXTipOfTheDay.ShowOnStartupChoice;
import org.jdesktop.swingx.tips.TipLoader;
import org.jdesktop.swingx.tips.TipOfTheDayModel;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class TipOfTheDay {

  private static final String GUI_SHOW_TIP_OF_THE_DAY = "gui.showTipOfTheDay";
  private static final Logger LOGGER = Logger.getLogger(TipOfTheDay.class.getName());
  private DataConfiguration dataConfiguration;

  public TipOfTheDay(DataConfiguration dataConfiguration) {
    super();
    this.dataConfiguration = dataConfiguration;
  }

  public void showTipOfTheDayIfNotDisabled(Component parent) {
    Properties p = new Properties();
    try {
      p.load(this.getClass().getClassLoader().getResourceAsStream("tipoftheday.properties"));
      TipOfTheDayModel model = new RandomTipOfTheDayModel(TipLoader.load(p));
      JXTipOfTheDay jxTipOfTheDay = new JXTipOfTheDay(model);
      jxTipOfTheDay.showDialog(parent, new ShowOnStartupChoice() {

        @Override
        public void setShowingOnStartup(boolean arg0) {
          dataConfiguration.setProperty(GUI_SHOW_TIP_OF_THE_DAY, arg0);
        }

        @Override
        public boolean isShowingOnStartup() {
          return dataConfiguration.getBoolean(GUI_SHOW_TIP_OF_THE_DAY, true);
        }
      });
    } catch (IOException e) {
      LOGGER.warning("Can't load tip of the day: " + e.getMessage());
    }
  }

}
