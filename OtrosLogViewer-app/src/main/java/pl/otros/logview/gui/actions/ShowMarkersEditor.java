/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.gui.markers.editor.MarkersEditor;

import java.awt.event.ActionEvent;

public class ShowMarkersEditor extends OtrosAction {

	private MarkersEditor markersEditor;

	public ShowMarkersEditor(OtrosApplication otrosApplication) {
		super(otrosApplication);
		putValue(NAME, "Show markers editor");
		putValue(SHORT_DESCRIPTION, "Show markers editor. You can edit or create new marker.");
		putValue(SMALL_ICON, Icons.MARKER);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (markersEditor == null) {
			markersEditor = new MarkersEditor();
		}
    getOtrosApplication().addClosableTab("Markers editor","Markers editor", Icons.MARKER,markersEditor,true);
	}

}
