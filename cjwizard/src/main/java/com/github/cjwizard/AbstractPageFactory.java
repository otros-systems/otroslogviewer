/**
 * Copyright 2008 Eugene Creswick
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
package com.github.cjwizard;

import java.util.List;

/**
 * Class that let you implement a PageFactory more easily providing you a
 * default implementations of not so necessary methods.
 * 
 * It is recommended to use it, if you can, for your {@link PageFactory}
 * implementations so you don't get in problems if new functionality is added
 * to the {@link PageFactory} interface.
 *
 * @author <a href="mailto:phoneixsegovia@gmail.com">Javier Alfonso</a>
 * @version 20140929
 *
 */
public abstract class AbstractPageFactory implements PageFactory {

   /*
    * (non-Javadoc)
    * @see com.github.cjwizard.PageFactory#isTransient(java.util.List, com.github.cjwizard.WizardSettings)
    */
   @Override
   public boolean isTransient(List<WizardPage> path, WizardSettings settings) {
      return false;
   }

}
