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
package pl.otros.logview.gui.message;

import pl.otros.logview.pluginable.PluginableElement;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.Collection;

public interface MessageColorizer extends PluginableElement {

  int MESSAGE_COLORIZER_VERSION_1 = 1;
	int MESSAGE_COLORIZER_VERSION_2 = 2;
	int MESSAGE_COLORIZER_VERSION_CURRENT = MESSAGE_COLORIZER_VERSION_2;

  boolean colorizingNeeded(String message);

  Collection<MessageFragmentStyle> colorize(String textToColorize) throws BadLocationException;

}
