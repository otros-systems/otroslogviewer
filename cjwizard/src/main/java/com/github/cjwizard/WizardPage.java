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
package com.github.cjwizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is the basic implementation of a WizardPage.
 * 
 * @author rcreswick
 * @version 20140210
 */
public abstract class WizardPage extends JPanel {

   private static final long serialVersionUID = 20140210L;

   /**
    * Logger instance
    */
   private final Logger log = LoggerFactory.getLogger(WizardPage.class);

   /**
    * Count of WizardPages, used to get unique IDs
    */
   private static long _idCounter=0;

   /**
    * Unique ID for this wizard page.
    */
   private final long _id = _idCounter++;

   /**
    * The title of this wizard page (a 1-2 word string)
    */
   private final String _title;
   
   /**
    * A longer description of this wizard page.
    */
   private final String _description;

   /**
    * The WizardController that contains this wizard page.
    * (often the WizardContainer)
    */
   private WizardController _controller;

   /**
    * The collection of components that have been added to this
    * wizard page with set names.
    */
   protected Set<Component> _namedComponents = new HashSet<>();
   
   /**
    * Constructor.  Sets the title and description for
    * this wizard panel.
    * 
    * @param title The short (1-3 word) name of this page.
    * @param description A possibly longer description
    *       (but still under 1 sentence)
    */
   public WizardPage(String title, String description){
      _title = title;
      _description = description;
      
      addContainerListener(new WPContainerListener());
      this.setDoubleBuffered(true);
   }
   
   /**
    * Gets the unique identifier for this wizard page;
    * 
    * @return The identifier of this wizard page.
    */
   public final String getId(){
      return ""+_id;
   }
   
   /**
    * Gets the short 1-2 word description of this WizardPage
    * 
    * @return The WizardPage title
    */
   public String getTitle(){
      return _title;
   }
   
   /**
    * Gets a longer description of this WizardPage.
    * 
    * @return The WizardPage description.
    */
   public String getDescription(){
      return _description;
   }
   
   /**
    * Updates the settings map after this page has been
    * used by the user.
    * 
    * This method should update the WizardSettings Map so that it contains
    * the new key/value pairs from this page.
    * 
    * @param settings The settings object that will be updated.
    * 
    */
   public void updateSettings(WizardSettings settings){
      for (Component c : _namedComponents){
         settings.put(c.getName(), getValue(c));
      }
   }
   
   
   /**
    * Gets the value from a component.
    * 
    * @param c The component.
    * @return The value.
    */
   private Object getValue(Component c) {
      Object val = null;

      if (c instanceof CustomWizardComponent) {
         val = ((CustomWizardComponent) c).getValue();
      } else if (c instanceof JFormattedTextField) {
         val = ((JFormattedTextField) c).getValue();
      } else if (c instanceof JTextComponent) {
         val = ((JTextComponent) c).getText();
      } else if (c instanceof AbstractButton){
         val = ((AbstractButton) c).isSelected();
      } else if (c instanceof JComboBox){
         val = ((JComboBox<?>) c).getSelectedItem();
      } else if (c instanceof JList){
         val = ((JList<?>) c).getSelectedValuesList();
      } else {
         log.warn("Unknown component: "+c);
      }
      
      return val;
   }
   
   /**
    * Sets the value of a component.
    * 
    * @param c The component.
    * @param o The value.
    */
   private void setValue(Component c, Object o) {

      if (null == o) {
         // don't set null values
         return;
      }
      if (c instanceof CustomWizardComponent) {
         ((CustomWizardComponent) c).setValue(o);
      } else if (c instanceof JFormattedTextField) {
         ((JFormattedTextField)c).setValue(o);
      } else if (c instanceof JTextComponent) {
         String text = (String)o;
         if (!text.isEmpty()) {
            ((JTextComponent) c).setText((String)o);
         }
      } else if (c instanceof AbstractButton){
         ((AbstractButton) c).setSelected((Boolean)o);
      } else if (c instanceof JComboBox){
         ((JComboBox<?>) c).setSelectedItem(o);
      } else if (c instanceof JList){
         List<Object> items = Arrays.asList((Object[])o);
         JList<?> list = (JList<?>)c;
         int[] indices = new int[items.size()];
         int i = 0;
         for (int j = 0; j < list.getModel().getSize(); j++) {
            Object e = list.getModel().getElementAt(j);
            if (items.contains(e)) {
               indices[i++] = j;
            }
         }
         list.setSelectedIndices(indices);
      } else {
         log.warn("Unknown component: "+c);
      }
      
   }

   /**
    * Invoked immediately prior to rendering the wizard page on screen.
    * 
    * This provides an opportunity to adjust the next/finish buttons and
    * customize the ui based on feedback.
    * 
    * @param path The current path.
    * @param settings The current settings.
    * 
    */
   public void rendering(List<WizardPage> path, WizardSettings settings) {
      for (Component c : _namedComponents){
         setValue(c, settings.get(c.getName()));
      }
   }
   
   /**
    * Registers the controller with this WizardPage.
    * 
    * The default visibility is intentional, but protected would be fine too.
    */
   void registerController(WizardController controller){
      _controller = controller;
   }
   
   /**
    * Set the enabled status of the Next button.
    * 
    * @param enabled true to enable it, false otherwise.
    */
   protected void setNextEnabled(boolean enabled){
      if (null != _controller)
         _controller.setNextEnabled(enabled);
   }
   
   /**
    * Set the enabled status of the Prev button.
    * 
    * @param enabled true to enable it, false otherwise.
    */
   protected void setPrevEnabled(boolean enabled){
      if (null != _controller)
         _controller.setPrevEnabled(enabled);
   }
   
   /**
    * Set the enabled status of the Finished button.
    * 
    * @param enabled true to enable it, false otherwise.
    */
   protected void setFinishEnabled(boolean enabled){
      if (null != _controller)
         _controller.setFinishEnabled(enabled);
   }
   
   /**
    * Set the enabled status of the Cancel button.
    * 
    * @param enabled true to enable it, false otherwise.
    * 
    * @since 20150127
    * 
    */
   protected void setCancelEnabled(boolean enabled){
      if (null != _controller)
         _controller.setCancelEnabled(enabled);
   }
   
   /**
    * Returns a string reperesntation of this wizard page.
    */
   @Override
   public String toString(){
      return getId() + ": " +getTitle();
   }

   /**
    * 
    * Obtain the current components with names that where added to the page.
    * 
    * @return A set with the named components.
    */
   protected Set<Component> getNamedComponents() {
      return _namedComponents;
   }

   /**
    * Listener to keep track of the components as they are added and removed
    * from this wizard page.
    * 
    * @author rogue
    */
   private class WPContainerListener implements ContainerListener {

      /* (non-Javadoc)
       * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.ContainerEvent)
       */
      @Override
      public void componentAdded(ContainerEvent e) {
         log.trace("component added: "+e.getChild());
         Component newComp = e.getChild();
         
         storeIfNamed(newComp);

      }

      private void storeIfNamed(Component newComp) {
         if (newComp instanceof CustomWizardComponent
               && null != newComp.getName()){
            _namedComponents.add(newComp);
            // don't recurse into custom components.
            return;
         }
         
         if (newComp instanceof Container){
            // recurse:
            Component[] children = ((Container)newComp).getComponents();
            for (Component c : children){
               storeIfNamed(c);
            }
         }
         
         if (null != newComp.getName()){
            _namedComponents.add(newComp);
         }
      }

      /* (non-Javadoc)
       * @see java.awt.event.ContainerListener#componentRemoved(java.awt.event.ContainerEvent)
       */
      @Override
      public void componentRemoved(ContainerEvent e) {
         log.trace("component removed: "+e.getChild());
         _namedComponents.remove(e.getChild());
      }
   }

   /**
    * Will be called before moving on next page or finishing.
    * 
    * Descendants can overload this function to do checks on its fields and then
    * warn user on incorrect fields.
    * 
    * @param settings
    *           The current settings without update the current page settings.
    * 
    * @return If fields are valid (<code>true</code>) or not. If
    *         <code>false</code> is returned the page change will be cancelled.
    *         <code>true</code> by default.
    * @since 20141002
    */
   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
   public boolean onNext(WizardSettings settings) {
      return true;
   }

   /**
    * Will be called before moving back.
    * 
    * Descendants can overload this function to do checks on its fields and then
    * warn user on incorrect fields or lost data.
    * 
    * @param settings
    *           The current settings without update the current page settings.
    * 
    * @return If fields are valid (<code>true</code>) or not. If
    *          <code>false</code> is returned the page change will be cancelled.
    *          <code>true</code> by default.
    * @since 20141002
    */
   public boolean onPrev(WizardSettings settings) {

      return true;

   }

    /**
     * the wizard controller that this wizard page is attached to
     * @since 1.0.9
     * @return a reference to the controller
     */
    public WizardController getController() {
        return _controller;
    }
}
