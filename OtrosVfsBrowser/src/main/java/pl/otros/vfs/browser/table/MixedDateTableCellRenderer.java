/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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
 */

package pl.otros.vfs.browser.table;

import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MixedDateTableCellRenderer extends DefaultTableCellRenderer {

	private static final long DURATION_THRESHOLD = 1000l*60*60*24*60;


	private RelativeDateTableCellRenderer relativeDateTableCellRenderer = new RelativeDateTableCellRenderer();
	private DateTableCellRenderer dateTableCellRenderer = new DateTableCellRenderer();


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if ( value !=null && value instanceof Date && ((Date)value).getTime()>System.currentTimeMillis()-DURATION_THRESHOLD){
			return relativeDateTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		return dateTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	}

}
