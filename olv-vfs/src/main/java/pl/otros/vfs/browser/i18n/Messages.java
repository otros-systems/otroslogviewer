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

package pl.otros.vfs.browser.i18n;

import org.jetbrains.annotations.PropertyKey;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Messages {

  public static final String BUNDLE_BASE_NAME = "i18n.messages";

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(Messages.BUNDLE_BASE_NAME);


  public static String getMessage(@PropertyKey(resourceBundle = "i18n.messages")String key, Object... args) {
    String string = BUNDLE.getString(key);
    return MessageFormat.format(string, args);

  }
}
