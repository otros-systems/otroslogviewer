/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.api;

import java.util.Map;

public class MapCompare {

  public static <K, V> boolean areMapsEquals(Map<K, V> m1, Map<K, V> m2) {
    if (null == m1 ^ m2 == null) {
      return false;
    }
    if (m1 == null && m2 == null) {
      return true;
    }
    if (m1.size() != m2.size()) {
      return false;
    }

    for (K key : m1.keySet()) {
      if (!m2.containsKey(key)) {
        return false;
      }
      V v1 = m1.get(key);
      V v2 = m2.get(key);
      if (null == v1 ^ v2 == null) {
        return false;
      }
      if (v1 != null && !v1.equals(v2)) {
        return false;
      }
    }
    return true;
  }

}
