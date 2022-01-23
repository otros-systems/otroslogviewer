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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cjwizard.pagetemplates.DefaultPageTemplate;
import com.github.cjwizard.pagetemplates.PageTemplate;
import com.github.cjwizard.utilities.ExceptionUtilities;

/**
 * This is the primary "Wizard" class. It must be instantiated with a
 * PageFactory and then treated as a JPanel.
 * 
 * @author rcreswick
 * 
 * @version 20150127
 * 
 */
public class WizardContainer extends JPanel implements WizardController {

   private static final long serialVersionUID = 20150127L;

   /**
    * Log instance
    */
   private final Logger log = LoggerFactory.getLogger(WizardContainer.class);

   /**
    * Resource to translate GUI elements.
    */
   private static final ResourceBundle msg = ResourceBundle.getBundle("com.github.cjwizard.i18n.cjwizard");

   private static final String I18N_NEXT = "NEXT";
   private static final String I18N_PREVIOUS = "PREVIOUS";
   private static final String I18N_FINISH = "FINISH";
   private static final String I18N_CANCEL = "CANCEL";

   /**
    * Storage for all the collected information.
    */
   private WizardSettings _settings;

   /**
    * The path from the start of the dialog to the current location.
    */
   private final List<WizardPage> _path;
   
   /**
     * In a wizard with a conditional path if after having traversed once the pages
     * I hit back and re-traverse the pages should I store the previously selected
     * path and use it or forget it. By default this is false.
     */
    private boolean forgetTraversedPath = false;

   /**
    * The path of already-visited pages starting from the current page.
    */
   private final Deque<WizardPage> _visitedPath;

   /**
    * List of listeners to update on wizard events.
    */
   private final List<WizardListener> _listeners;

   /**
    * The template to surround the wizard pages of this dialog.
    */
   private final PageTemplate _template;

   /**
    * The factory that generates pages for this wizard.
    */
   private final PageFactory _factory;

   /**
    * The panel containing any dynamically-added buttons.
    */
   private JPanel _extraButtonPanel;

   /**
    * Save the statuses of the Next button so we can restore it when going back.
    */
   private final Deque<Boolean> _nextStatuses;

   /**
    * Save the statuses of the Finish button so we can restore it when going back.
    */
   private final Deque<Boolean> _finishStatuses;

   /**
    * Save the statuses of the Previous button so we can restore it when going
    * back.
    */
   private final Deque<Boolean> _prevStatuses;

   /**
    * Save the statuses of the Cancel button so we can restore it when going back.
    */
   private final Deque<Boolean> _cancelStatuses;

   /**
    * Save the statuses of the Next button of visited pages.
    */
   private final Deque<Boolean> _visitedNextStatuses;

   /**
    * Save the statuses of the Finish button of visited pages.
    */
   private final Deque<Boolean> _visitedFinishStatuses;

   /**
    * Save the statuses of the Previous button of visited pages.
    */
   private final Deque<Boolean> _visitedPrevStatuses;

   /**
    * Save the statuses of the Cancel button of visited pages.
    */
   private final Deque<Boolean> _visitedCancelStatuses;

   private final AbstractAction _prevAction = new AbstractAction(msg.getString(I18N_PREVIOUS)) {

      private static final long serialVersionUID = -3544187996163884815L;

      {
         setEnabled(false);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         prev();
      }
   };

   private final AbstractAction _nextAction = new AbstractAction(msg.getString(I18N_NEXT)) {

      private static final long serialVersionUID = 4175292094891534750L;

      @Override
      public void actionPerformed(ActionEvent e) {
         next();
      }
   };

   private final AbstractAction _finishAction = new AbstractAction(msg.getString(I18N_FINISH)) {

      private static final long serialVersionUID = -1556582296948163049L;

      {
         setEnabled(false);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         finish();
      }
   };

   private final AbstractAction _cancelAction = new AbstractAction(msg.getString(I18N_CANCEL)) {

      private static final long serialVersionUID = 2757580981402133120L;

      @Override
      public void actionPerformed(ActionEvent e) {
         cancel();
      }
   };

   /**
    * Constructor, uses default PageTemplate and {@link StackWizardSettings}.
    * 
    * @param factory The factory that will create the pages.
    */
   public WizardContainer(PageFactory factory) {
      this(factory, new DefaultPageTemplate(), new StackWizardSettings());
   }

   /**
    * Customized constructor.
    * @param factory The factory which will be used to create the pages.
    * @param template Template to put inside the pages.
    * @param settings The settings to store the values put by the user.
    */
   public WizardContainer(PageFactory factory, PageTemplate template,
         WizardSettings settings) {
      _factory = factory;
      _template = template;
      _settings = settings;

      _path = new LinkedList<>();
      _visitedPath = new ArrayDeque<>();
      _listeners = new LinkedList<>();

      _cancelStatuses = new ArrayDeque<>();
      _prevStatuses = new ArrayDeque<>();
      _nextStatuses = new ArrayDeque<>();
      _finishStatuses = new ArrayDeque<>();

      _visitedCancelStatuses = new ArrayDeque<>();
      _visitedPrevStatuses = new ArrayDeque<>();
      _visitedNextStatuses = new ArrayDeque<>();
      _visitedFinishStatuses = new ArrayDeque<>();

      initComponents();
      _template.registerController(this);

      // get the first page:
      next();
   }

   /**
    * 
    */
   private void initComponents() {
      final JButton prevBtn = new JButton(_prevAction);
      final JButton nextBtn = new JButton(_nextAction);
      final JButton finishBtn = new JButton(_finishAction);
      final JButton cancelBtn = new JButton(_cancelAction);

      _extraButtonPanel = new JPanel();
      _extraButtonPanel.setLayout(new BoxLayout(_extraButtonPanel,
            BoxLayout.LINE_AXIS));

      final JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
      buttonPanel.add(_extraButtonPanel);
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.add(prevBtn);
      buttonPanel.add(Box.createHorizontalStrut(5));
      buttonPanel.add(nextBtn);
      buttonPanel.add(Box.createHorizontalStrut(10));
      buttonPanel.add(finishBtn);
      buttonPanel.add(Box.createHorizontalStrut(10));
      buttonPanel.add(cancelBtn);

      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
      this.setLayout(new BorderLayout());

      this.add(_template, BorderLayout.CENTER);
      this.add(buttonPanel, BorderLayout.SOUTH);
   }

   /**
    * Add additional buttons to the wizard controls. Any previously-added
    * buttons are cleared on each call to this method.
    * 
    * @param buttons
    *           The buttons to add to the wizard controls.
    */
   public void setButtons(JButton... buttons) {
      _extraButtonPanel.removeAll();
      for (JButton button : buttons) {
         _extraButtonPanel.add(button);
         _extraButtonPanel.add(Box.createHorizontalStrut(10));
      }
   }

   /**
    * The PageFactory is not queried for pages when moving *backwards*.
    */
   public void prev() {
      log.debug("prev. page");

      // store visited pages
      WizardPage removing = _path.get(_path.size() - 1);

      // First check if we should really go back
      if (!removing.onPrev(getSettings())) {
         return;
      }
      
      // Now we need to remove it from path
      _path.remove(_path.size() - 1);

      _visitedPath.push(removing);

      // Save settings (so can be restored if we return to the same page).
      removing.updateSettings(getSettings());
      getSettings().commit();
      // But don't let it as actual settings.
      getSettings().rollBack();

      // Save current buttons statuses.
      _visitedCancelStatuses.push(_cancelAction.isEnabled());
      _visitedPrevStatuses.push(_prevAction.isEnabled());
      _visitedNextStatuses.push(_nextAction.isEnabled());
      _visitedFinishStatuses.push(_finishAction.isEnabled());

      // Restore the buttons status.
      _cancelAction.setEnabled(_cancelStatuses.pop());
      _prevAction.setEnabled(_prevStatuses.pop());
      _nextAction.setEnabled(_nextStatuses.pop());
      _finishAction.setEnabled(_finishStatuses.pop());

      assert 1 <= _path.size() : "Invalid path size! 0";
      setPrevEnabled(_path.size() > 1);

      WizardPage curPage = _path.get(_path.size() - 1);

      firePageChanging(curPage, getPath());
      // tell the page that it is about to be rendered:
      curPage.rendering(getPath(), getSettings());
      _template.setPage(curPage);

      firePageChanged(curPage, getPath());
   }

   /**
    * Called when the page must advance to the next page.
    */
   public void next() {
      log.debug("next page");

      WizardPage lastPage = currentPage();
      if (null != lastPage) {

         if (!lastPage.onNext(getSettings())) {
            return;
         }

         // get the settings from the page that is going away:
         lastPage.updateSettings(getSettings());

         // Save the current buttons status
         _cancelStatuses.push(_cancelAction.isEnabled());
         _prevStatuses.push(_prevAction.isEnabled());
         _nextStatuses.push(_nextAction.isEnabled());
         _finishStatuses.push(_finishAction.isEnabled());

      }

      // Get the next page
      WizardPage nextPage;
      if (_visitedPath.isEmpty()
            || _factory.isTransient(getPath(), getSettings()) || forgetTraversedPath) {

         // If we can't use the cached page, create a new one.
         nextPage = _factory.createPage(getPath(), getSettings());

         /*
          * We can't know if next pages buttons statuses are ok to be restored,
          * so we discard the visited statuses and use the defaults.
          */
         if (!_visitedPath.isEmpty()) {

            _visitedPath.pop();
            _visitedCancelStatuses.pop();
            _visitedPrevStatuses.pop();
            _visitedNextStatuses.pop();
            _visitedFinishStatuses.pop();

         }

         // Estará activado por defecto si había alguna página antes.
         setPrevEnabled(_path.size() >= 1);

      } else {

         // Else use the previously cached page.
         nextPage = _visitedPath.pop();

         // Restore the buttons status.
         _cancelAction.setEnabled(_visitedCancelStatuses.pop());
         _prevAction.setEnabled(_visitedPrevStatuses.pop());
         _nextAction.setEnabled(_visitedNextStatuses.pop());
         _finishAction.setEnabled(_visitedFinishStatuses.pop());

      }

      nextPage.registerController(this);

      _path.add(nextPage);

      // And add a page for its settings.
      getSettings().newPage(nextPage.getId());

      firePageChanging(nextPage, getPath());
      // tell the page that it is about to be rendered:
      nextPage.rendering(getPath(), getSettings());
      _template.setPage(nextPage);

      firePageChanged(nextPage, getPath());
   }

   public void visitPage(WizardPage page) {
      int idx = _path.indexOf(page);

      WizardPage lastPage = currentPage();
      if (null != lastPage) {
         // update the settings before leaving
         lastPage.updateSettings(getSettings());
      }

      // Buttons status of the new page.
      boolean cancelEnabled = true, previousEnabled = true, nextEnabled = true, finishEnabled = false;

      if (-1 == idx) {
         // new page
         if (null != lastPage) {
            // Save the current statuses
            _cancelStatuses.push(_cancelAction.isEnabled());
            _prevStatuses.push(_prevAction.isEnabled());
            _nextStatuses.push(_nextAction.isEnabled());
            _finishStatuses.push(_finishAction.isEnabled());
         }

         // add back all visited pages
         while (!_visitedPath.isEmpty()) {

            cancelEnabled = _visitedCancelStatuses.pop();
            previousEnabled = _visitedPrevStatuses.pop();
            nextEnabled = _visitedNextStatuses.pop();
            finishEnabled = _visitedFinishStatuses.pop();

            WizardPage visited = _visitedPath.pop();
            getPath().add(visited);

            if (visited == page) {

               // We have found it, use its statuses and exit from while.

               _cancelAction.setEnabled(cancelEnabled);
               setPrevEnabled(previousEnabled);
               setNextEnabled(nextEnabled);
               setFinishEnabled(finishEnabled);

               break;

            } else {

               _cancelStatuses.push(cancelEnabled);
               _prevStatuses.push(previousEnabled);
               _nextStatuses.push(nextEnabled);
               _finishStatuses.push(finishEnabled);

            }

         }

         // If we have never seen the page, we add it to the tail.
         if (currentPage() != page) {
            
            assert _visitedCancelStatuses.isEmpty();
            assert _visitedPrevStatuses.isEmpty();
            assert _visitedNextStatuses.isEmpty();
            assert _visitedFinishStatuses.isEmpty();
            
            getPath().add(page);
         }

         // And we prepare the page of settings for the new page.
         getSettings().newPage(page.getId());

      } else {
         // page is in the path at idx.

         // first, roll back the settings and trim the path:
         for (int i = _path.size() - 1; i > idx; i--) {
            getSettings().rollBack();
            // save visited pages
            _visitedPath.push(_path.remove(i));
            
            final Boolean cancelStatus = _cancelStatuses.pop();
            final Boolean prevStatus = _prevStatuses.pop();
            final Boolean nextStatus = _nextStatuses.pop();
            final Boolean finishStatus = _finishStatuses.pop();
            
            // Save current buttons statuses.
            _visitedCancelStatuses.push(cancelStatus);
            _visitedPrevStatuses.push(prevStatus);
            _visitedNextStatuses.push(nextStatus);
            _visitedFinishStatuses.push(finishStatus);
            
            // Restore the buttons status.
            cancelEnabled = cancelStatus;
            previousEnabled = prevStatus;
            nextEnabled = nextStatus;
            finishEnabled = finishStatus;
         }

         _cancelAction.setEnabled(cancelEnabled);
         setPrevEnabled(previousEnabled);
         setNextEnabled(nextEnabled);
         setFinishEnabled(finishEnabled);

      }

      firePageChanging(page, getPath());
      page.rendering(_path, getSettings());
      _template.setPage(page);
      firePageChanged(page, _path);
   }


   private void firePageChanging(WizardPage curPage, List<WizardPage> path) {
      for (WizardListener l : _listeners) {
         l.onPageChanging(curPage, getPath());
      }
   }

   private void firePageChanged(WizardPage curPage, List<WizardPage> path) {
      for (WizardListener l : _listeners) {
         l.onPageChanged(curPage, getPath());
      }
   }

   /**
    * 
    */
   public void finish() {
      log.debug("finish");

      WizardPage lastPage = currentPage();

      if (null != lastPage) {

         if (!lastPage.onNext(getSettings())) {
            return;
         }

         // get the settings from the page that is going away:
         getSettings().newPage(lastPage.getId());
         // And update the settings.
         lastPage.updateSettings(getSettings());

      }

      for (WizardListener l : _listeners) {
         l.onFinished(getPath(), getSettings());
      }
   }

   /**
    * 
    */
   public void cancel() {
      log.debug("cancel");

      for (WizardListener l : _listeners) {
         l.onCanceled(getPath(), getSettings());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.github.cjwizard.WizardController#addWizardListener(com.stottlerhenke
    * .presentwell.wizard.WizardListener)
    */
   public void addWizardListener(WizardListener listener) {
      ExceptionUtilities.checkNull(listener, "listener");
      if (!_listeners.contains(listener)) {
         _listeners.add(listener);
         WizardPage curPage = _path.get(_path.size() - 1);
         listener.onPageChanged(curPage, getPath());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.github.cjwizard.WizardController#removeWizardListener(com.stottlerhenke
    * .presentwell.wizard.WizardListener)
    */
   public void removeWizardListener(WizardListener listener) {
      ExceptionUtilities.checkNull(listener, "listener");
      _listeners.remove(listener);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#getSettings()
    */
   public WizardSettings getSettings() {
      return _settings;
   }

   /**
    * Set/load the specified settings map nad re-render the current page.
    * 
    * @param settings
    *           The settings to load.
    */
   public void setSettings(WizardSettings settings) {
      _settings = settings;
      currentPage().rendering(_path, _settings);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#getPath()
    */
   public List<WizardPage> getPath() {
      return _path;
   }

   /**
    * @return The last (current) page of the current {@link #getPath()} or null if
    *         the path is empty.
    */
   public WizardPage currentPage() {
      int lastIdx = _path.size() - 1;
      return (lastIdx < 0) ? null : _path.get(lastIdx);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#setNextEnabled(boolean)
    */
   public void setNextEnabled(boolean enabled) {
      _nextAction.setEnabled(enabled);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#setPrevEnabled(boolean)
    */
   public void setPrevEnabled(boolean enabled) {
      _prevAction.setEnabled(enabled);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#setFinishEnabled(boolean)
    */
   public void setFinishEnabled(boolean enabled) {
      _finishAction.setEnabled(enabled);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.github.cjwizard.WizardController#setCancelEnabled(boolean)
    */
   public void setCancelEnabled(boolean enabled) {
      _cancelAction.setEnabled(enabled);
   }

    /**
     * @param forgetTraversedPath the forgetTraversedPath to set
     */
    public void setForgetTraversedPath(boolean forgetTraversedPath)
    {
        this.forgetTraversedPath = forgetTraversedPath;
    }

}
