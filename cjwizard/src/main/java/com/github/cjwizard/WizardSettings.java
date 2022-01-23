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

import java.util.Map;
import java.util.Set;

/**
 * This interface declare the minimum methods that all wizard settings classes
 * must implement.
 *
 * @author ddearing
 *
 * @version 20141205
 *
 */
public interface WizardSettings {

    <T> T put(String key, T value);

    void clear();

    boolean containsKey(String key);

    boolean containsValue(Object value);

    boolean isEmpty();

    void putAll(Map<? extends String, ?> m);

    int size();

    Object remove(String key);

    /**
     * Rolls back the current page, removing it from the settings.
     */
    void rollBack();

    /**
     * Create a new page of settings with the specified identifier.
     *
     * @param id The identifier of the new page.
     */
    void newPage(String id);

    /**
     * Confirm the current page so it isn't lost when you do a rollBack.
     *
     * @see #rollBack()
     *
     * @since 20141205
     */
    void commit();

    Set<String> keySet();

    Object get(String name);

    <V> V getOrDefault(String name, V defaultValue);

}
