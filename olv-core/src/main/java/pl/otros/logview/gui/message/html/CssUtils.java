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

package pl.otros.logview.gui.message.html;

import java.util.Map;

public class CssUtils {

  public String toString(String styleName, Map<String, String> css) {
    StringBuilder sb = new StringBuilder();
    sb.append("span.").append(styleName).append(" {\n");
    for (String key : css.keySet()) {
      sb.append("\t").append(key).append(": ").append(css.get(key)).append(";\n");
    }

    sb.append("}\n");
    return sb.toString();
  }


}
