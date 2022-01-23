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

/**
 * An interface to allow the CJWizardPage to extract values from
 * custom (and other non-swing) components.
 * 
 * @author rcreswick
 *
 */
public interface CustomWizardComponent {

   /**
    * Gets the current value of the component.
    * 
    * @return The value.
    */
   Object getValue();
   
   /**
    * Sets the value of the component.
    * 
    * @param o The value object.
    */
   void setValue(Object o);
   
   /**
    * Gets the name of this component.
    * (this is provided by the Swing component)
    * 
    * @return The name, as a string.
    */
   String getName();
}
