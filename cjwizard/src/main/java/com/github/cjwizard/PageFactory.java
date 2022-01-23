/**
 * Copyright 2008 Eugene Creswick
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
 *
 * This interface must be implemented by classes that provide pages for the
 * Wizard.
 *
 * @author rcreswick
 * @version 20140929
 */
public interface PageFactory {

    /**
     * Creates (or retrieves) a wizard page based on the path of pages covered so
     * far between now and the start of the dialog, and the map of settings.
     *
     * @param path
     *           The list of all WizardPages seen so far.
     * @param settings
     *           The Map of settings collected.
     * @return The next WizardPage.
     */
    WizardPage createPage(List<WizardPage> path, WizardSettings settings);

    /**
     * Check if we must call to {@link #createPage(List, WizardSettings)} to get
     * the next page or if we can use a cached version.
     *
     * @param path
     *           The list of all WizardPages seen so far.
     * @param settings
     *           The Map of settings collected.
     * @return <code>true</code> if is needed to call to createPage to obtain the
     *         next page, <code>false</code> in other case.
     * @since 20140929
     */
    boolean isTransient(List<WizardPage> path, WizardSettings settings);

}
