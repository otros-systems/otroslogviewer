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

package pl.otros.vfs.browser.preview;


public class PreviewStatus {

  public enum State {
    NA, NOT_STARTED, LOADING, FINISHED, CANCELLED, ERROR
  }

  private final State state;
  private final int loaded;
  private final int maxToLoad;
  private final String loadUnit;
  private final String name;
  private final byte[] content;


  public PreviewStatus(State state, int loaded, int maxToLoad, String loadUnit, String name, byte[] content) {
    super();
    this.state = state;
    this.loaded = loaded;
    this.maxToLoad = maxToLoad;
    this.loadUnit = loadUnit;
    this.name = name;
    this.content = content;
  }

  public State getState() {
    return state;
  }

  public int getLoaded() {
    return loaded;
  }

  public int getMaxToLoad() {
    return maxToLoad;
  }

  public String getLoadUnit() {
    return loadUnit;
  }

  public String getName() {
    return name;
  }

  public byte[] getContent() {
    return content;
  }

  @Override
  public String toString() {
    return "PreviewStatus [state=" + state + ", loaded=" + loaded + ", maxToLoad=" + maxToLoad + ", loadUnit=" + loadUnit + ", name="
        + name + ", content=" + content.length + "]";
  }


}
