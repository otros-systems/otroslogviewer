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

import org.jdesktop.swingx.tips.TipOfTheDayModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Randomize order of tips.
 * 
 */
public class RandomTipOfTheDayModel implements TipOfTheDayModel {

  private TipOfTheDayModel model;
  private List<Integer> randomMapping;

  public RandomTipOfTheDayModel(TipOfTheDayModel model) {
    super();
    this.model = model;
    randomMapping = new ArrayList<Integer>();
    for (int i = 0; i < model.getTipCount(); i++) {
      randomMapping.add(Integer.valueOf(i));
    }
    Collections.shuffle(randomMapping);
  }

  @Override
  public Tip getTipAt(int tip) {
    return model.getTipAt(randomMapping.get(tip));
  }

  @Override
  public int getTipCount() {
    return model.getTipCount();
  }

}
