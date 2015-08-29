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
package pl.otros.logview.uml;

import java.awt.*;
import java.util.LinkedHashMap;

public class LogUmlMapper {

  private final LinkedHashMap<Integer, Point> idToPointMap;

  public LogUmlMapper() {
    idToPointMap = new LinkedHashMap<>();
  }

  public void addMapping(int logId, Point p) {
    idToPointMap.put(logId, p);
  }

  public Point getPoint(int logId) {
    return idToPointMap.get(logId);
  }

  public int getLogId(int yPosition) {
    int lastDistance = Integer.MAX_VALUE;
    for (Integer id : idToPointMap.keySet()) {
      Point p = idToPointMap.get(id);
      if (Math.abs(p.y - yPosition) <= lastDistance) {
        lastDistance = Math.abs(p.y - yPosition);
      } else {
        return id;
      }
    }
    return 0;
  }
}
