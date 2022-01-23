/**
 * Copyright 2008  Eugene Creswick
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
package com.github.cjwizard.utilities;

/**
 * @author rcreswick
 *
 */
public class ExceptionUtilities {

   /**
    * Checks to see if obj is null, and if so throws an
    * IllegalArgumentException with the given message.
    * 
    * @param obj the object to check
    * @param msg an error message
    * @throws IllegalArgumentException if the referenced object is null
    */
   public static void checkNull(Object obj, String msg) {
      if (null == obj)
         throw new IllegalArgumentException(msg);
   }

}
