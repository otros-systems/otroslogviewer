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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.github.cjwizard.WizardPage;

/**
 * Simple PageTemplate that lists the WizardPage title at the top of each page
 * in the wizard and also use the description of the page as tool tip of the
 * title.
 * 
 * @author rogue
 */
public class TitledPageTemplate extends PageTemplate {

   /**
    * Generated version uid.
    */
   private static final long serialVersionUID = -2282167921679786408L;

   /**
    * The label to display the current page description.
    */
   private final JLabel _title = new JLabel();
   
   /**
    * We'll use the DefaultPageTemplate to take advantage of it's page switching
    * logic, since it doesn't include any additional decorations it is ideal
    * for nesting.
    */
   private final PageTemplate _innerTemplate = new DefaultPageTemplate();
   
   /**
    * Constructor.  Sets up the inner template and the title label.
    */
   public TitledPageTemplate(){
      // Create a simple empty border to impose a bit of space around the title:
      Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
      // use an Matte border to add an underline:
      Border innerBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
      
      // combine the two borders to get the desired look:
      _title.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
      
      this.setLayout(new BorderLayout());
      this.add(_title, BorderLayout.NORTH);
      this.add(_innerTemplate, BorderLayout.CENTER);
   }
   
   /* (non-Javadoc)
    * @see com.github.cjwizard.pagetemplates.PageTemplate#setPage(com.github.cjwizard.WizardPage)
    */
   @Override
   public void setPage(final WizardPage page) {
      // Since we're using a nested DefaultPageTemplate, we just need to
      // delegate to that object, and then do whatever is necessary to update
      // the additional widgets introduced by this PageTemplate.
      
      // The only trick is that we should make this thread-safe, since we aren't
      // guaranteed that this will be invoked from the EDT:
      SwingUtilities.invokeLater(() -> {
         // delegate to the DefaultPageTemplate:
         _innerTemplate.setPage(page);

         // Set the new title text:
         _title.setText(page.getTitle());
         _title.setToolTipText(page.getDescription());
      });
   }

}
