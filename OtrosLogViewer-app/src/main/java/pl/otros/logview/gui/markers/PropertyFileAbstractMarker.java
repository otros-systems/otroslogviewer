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
package pl.otros.logview.gui.markers;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.MarkerColors;

import java.util.Properties;

public abstract class PropertyFileAbstractMarker extends AbstractAutomaticMarker {

  public static final String FILE = "file";
  public static final String TYPE = "type";
  public static final String CONDITION = "condition";
  public static final String DESCRIPTION = "description";
  public static final String GROUPS = "groups";
  public static final String NAME = "name";
  public static final String INCLUDE = "include";
  public static final String IGNORE_CASE = "ignoreCase";
  public static final String COLOR = "color";
  public static final String PRECONDITION = "precondition";
  public static final String PRECONDITION_INCLUDE = "preconditionInclude";
  public static final String TEST_STRING_1 = "test1";
  public static final String TEST_STRING_2 = "test2";
  public static final String TEST_STRING_3 = "test3";

  public static final String TYPE_STRING = "string";
  public static final String TYPE_REGEX = "regex";

  private String fileName;

  protected String condition;
  protected boolean include = true;
  protected boolean ignoreCase = true;
  private String test1 = "";
  private String test2 = "";
  private String test3 = "";

  public PropertyFileAbstractMarker(Properties p) throws Exception {
    super("", "", MarkerColors.Aqua);
    if (!p.containsKey(CONDITION) || !p.containsKey(DESCRIPTION) || !p.containsKey(GROUPS) || !p.containsKey(NAME) || !p.containsKey(IGNORE_CASE)) {
      throw new Exception("Not enought parameters");
    }
    this.ignoreCase = Boolean.parseBoolean(p.getProperty(IGNORE_CASE));

    if (ignoreCase) {
      this.condition = p.getProperty(CONDITION).toLowerCase();
    } else {
      this.condition = p.getProperty(CONDITION);
    }

    this.description = p.getProperty(DESCRIPTION);
    this.groups = p.getProperty(GROUPS).split(",");
    for (int i = 0; i < groups.length; i++) {
      groups[i] = groups[i].trim();
    }
    this.name = p.getProperty(NAME);
    this.include = Boolean.parseBoolean(p.getProperty(INCLUDE, "true"));
    this.markerColors = MarkerColors.fromString(p.getProperty(COLOR, ""));
    if (p.containsKey(FILE)) {
      fileName = p.getProperty(FILE);
    }
    test1 = p.getProperty(TEST_STRING_1, "");
    test2 = p.getProperty(TEST_STRING_2, "");
    test3 = p.getProperty(TEST_STRING_3, "");
  }

  public Properties toProperties() {
    Properties p = new Properties();
    p.put(CONDITION, condition);
    p.put(DESCRIPTION, description);
    p.put(GROUPS, StringUtils.join(groups, ","));
    p.put(NAME, name);
    p.put(IGNORE_CASE, Boolean.toString(ignoreCase));
    p.put(COLOR, markerColors.toString());
    p.put(INCLUDE, Boolean.toString(include));
    p.put(FILE, fileName);
    p.put(TEST_STRING_1, test1);
    p.put(TEST_STRING_2, test2);
    p.put(TEST_STRING_3, test3);
    return p;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;

  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isInclude() {
    return include;
  }

  public void setInclude(boolean include) {
    this.include = include;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public void setColors(MarkerColors markerColors) {
    this.markerColors = markerColors;
  }

  public void setMarkerGroups(String[] markerGroups) {
    groups = markerGroups;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPluginableId() {
    return fileName;
  }

}
