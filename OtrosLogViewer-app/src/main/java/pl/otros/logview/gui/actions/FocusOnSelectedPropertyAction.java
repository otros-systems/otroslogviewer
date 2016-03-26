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
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.filter.PropertyFilter;
import pl.otros.logview.api.OtrosApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FocusOnSelectedPropertyAction extends FocusOnThisAbstractAction<PropertyFilter> {

	/**
	 *
	 */
	private static final long serialVersionUID = 3032705743589544542L;
    private final String key;
    private final String value;

    public FocusOnSelectedPropertyAction(PropertyFilter filter, JCheckBox filterEnableCheckBox, OtrosApplication otrosApplication,String key, String value) {
		super(filter, filterEnableCheckBox, otrosApplication);
        this.key = key;
        this.value = value;
        this.putValue(NAME, "Focus on events with property " + key + "=" + value);
		this.putValue(SHORT_DESCRIPTION, getValue(NAME));
	}

	@Override
	public void action(ActionEvent e, PropertyFilter filter, LogData... selectedLogData) {
        filter.setCondition(key,value);
	}

}
