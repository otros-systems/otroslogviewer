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

package pl.otros.vfs.browser;

/**
 */
public class TaskContext {
  private int max;
  private volatile int currentProgress;
  private volatile boolean stop;
  private String name;

  private boolean indeterminate;

  public TaskContext(String name, int max) {
    this.max = max;
    this.name = name;
  }

  public void setIndeterminate(boolean indeterminate) {
    this.indeterminate = indeterminate;
  }

  public boolean isIndeterminate() {
    return indeterminate;
  }

  public String getName() {
    return name;
  }

  public boolean isStop() {
    return stop;
  }

  public void setStop(boolean stop) {
    this.stop = stop;
  }

  public int getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(int currentProgress) {
    this.currentProgress = currentProgress;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }
}
