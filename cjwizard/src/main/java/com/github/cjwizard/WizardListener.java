/**
 * Copyright 2008  Eugene Creswick
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cjwizard;

import java.util.List;


/**
 * A simple interface to enable arbitrary code to act in
 * response to wizard actions.
 *
 * @author rcreswick
 *
 */
public interface WizardListener {

    /**
     * Invoked when the wizard is about to change to display a new page.
     *
     * @param newPage  The new page being displayed.
     * @param path The list of WizardPages shown so far.
     */
    void onPageChanging(WizardPage newPage, List<WizardPage> path);

    /**
     * Invoked when the wizard changes to display a new page.
     *
     * @param newPage  The new page displayed.
     * @param path The list of WizardPages shown so far.
     */
    void onPageChanged(WizardPage newPage, List<WizardPage> path);

    /**
     * Invoked when the user clicks the Finish button.
     *
     * @param path The list of WizardPages shown so far.
     * @param settings The collection of settings gleaned during the wizard.
     */
    void onFinished(List<WizardPage> path, WizardSettings settings);

    /**
     * Invoked when the user clicks the Cancel button.
     *
     * @param path The list of WizardPages shown so far.
     * @param settings The collection of settings gleaned during the wizard.
     */
    void onCanceled(List<WizardPage> path, WizardSettings settings);
}
