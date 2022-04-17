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
public class CompositeTaskContext extends TaskContext {

  private TaskContext[] taskContext;

  public CompositeTaskContext(String name, TaskContext[] taskContext) {
    super(name, 0);
    this.taskContext = taskContext;
  }

  @Override
  public void setStop(boolean stop) {
    for (TaskContext context : taskContext) {
      context.setStop(stop);
    }
  }

  @Override
  public int getMax() {
    int max = 0;
    for (TaskContext context : taskContext) {
      max += context.getMax();
    }
    return max;
  }

  @Override
  public int getCurrentProgress() {
    int progress = 0;
    for (TaskContext context : taskContext) {
      progress += context.getMax();
    }
    return progress;
  }
}
