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
package com.github.cjwizard.pagetemplates;

import java.awt.CardLayout;

import com.github.cjwizard.WizardContainer;
import com.github.cjwizard.WizardPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a point at which third-party code can
 * introduce custom wrappers around the WizardPages that are displayed.
 * To do so, implement the IPageTemplate interface and wrap this
 * PageTemplate class with your own custom components, delegating the setPage
 * invocation to the wrapped instance of PageTemplate.
 * 
 * @author rcreswick
 *
 */
public class DefaultPageTemplate extends PageTemplate {
   
   /**
    * Log instance
    */
   private final Logger log = LoggerFactory.getLogger(WizardContainer.class);
   
   private final CardLayout _layout = new CardLayout();
   
   public DefaultPageTemplate(){
      this.setLayout(_layout);
   }
   
   /* (non-Javadoc)
    * @see com.github.cjwizard.PageTemplate#setPage(com.github.cjwizard.WizardPage)
    */
   public void setPage(final WizardPage page){
      log.debug("Setting page: "+page);

      // remove the page, just in case it was added before:
      remove(page);
      validate();
      
      add(page, page.getId());
      _layout.show(this, page.getId());
   }
}
