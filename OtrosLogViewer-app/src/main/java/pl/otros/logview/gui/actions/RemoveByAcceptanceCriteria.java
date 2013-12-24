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

import pl.otros.logview.accept.AcceptCondition;
import pl.otros.logview.gui.HasIcon;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.StatusObserver;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveByAcceptanceCriteria extends OtrosAction {

	private AcceptCondition acceptCondition;

	public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication) {
		this(acceptCondition, otrosApplication, (Icon) null);
	}

	public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, Icon icon) {
		super(otrosApplication);
		this.acceptCondition = acceptCondition;
		putValue(NAME, acceptCondition.getName());
		putValue(SHORT_DESCRIPTION, acceptCondition.getDescription());
		if (icon == null && acceptCondition instanceof HasIcon) {
			icon = ((HasIcon) acceptCondition).getIcon();
		}
		putValue(SMALL_ICON, icon);
	}

	public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, String name, Icon icon) {
		this(acceptCondition, otrosApplication, icon);
		putValue(NAME, name);
	}

	public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, String name) {
		this(acceptCondition, otrosApplication, name, null);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		StatusObserver observer = getOtrosApplication().getStatusObserver();
		LogDataTableModel dataTableModel = getOtrosApplication().getSelectedPaneLogDataTableModel();
		int removeRows = dataTableModel.removeRows(acceptCondition);
		if (observer != null) {
			observer.updateStatus(String.format("Removed %d rows using \"%s\"", removeRows, acceptCondition.getName()));
		}

	}

}
