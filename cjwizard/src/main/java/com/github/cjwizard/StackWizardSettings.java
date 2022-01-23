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

import java.util.*;

/**
 * This class implement a {@link WizardSettings} that store its pages in a stack
 * and in a cache so you can commit the current page and it will be used when a
 * new page with the same id is restored.
 *
 * @author rcreswick
 * @version 20141205
 */
public class StackWizardSettings implements WizardSettings {

    private final Stack<IdMapTuple> _pageStack = new Stack<>();

    private final Map<String, IdMapTuple> _oldPageMaps = new HashMap<>();

    /**
     * Create a new empty instance of the wizard settings.
     */
    public StackWizardSettings() {
        // start with an empty new page:
        newPage("");
    }

    /**
     * Gets the set of keys on this WizardSettings object.
     *
     * @return A set of all the keys currently active.
     */
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();

        for (IdMapTuple tuple : _pageStack) {
            keys.addAll(tuple.map.keySet());
        }
        return keys;
    }

    /**
     * Check if the specified was added.
     *
     * @param key The key that must be checked.
     * @return <code>true</code> if this settings contains the specified key or
     * <code>false</code> in other case.
     */
    public boolean containsKey(String key) {
        return keySet().contains(key);
    }

    /**
     *
     */
    public void rollBack() {
        _pageStack.pop();
    }

    /**
     * Confirm the current page so it isn't lost when you do a rollBack.
     *
     * @see StackWizardSettings#rollBack()
     * @since 20141205
     */
    public void commit() {

        IdMapTuple currentPage = current();

        // If there was a page, store it by its id.
        if (currentPage != null) {
            _oldPageMaps.put(currentPage.id, currentPage);
        }

    }

    /**
     * Create a new page of settings, committing the current page.
     *
     * @param id The id for the page.
     */
    public void newPage(String id) {

        // First, save the current page
        commit();

        // If we've seen this ID before, use it again:
        IdMapTuple curTuple;
        if (_oldPageMaps.containsKey(id)) {
            curTuple = _oldPageMaps.get(id);
        } else {
            curTuple = new IdMapTuple(id, new HashMap<>());
        }

        // push the new map:
        _pageStack.push(curTuple);
    }

    public <V> V put(String key, V value) {
        Objects.requireNonNull(current()).map.put(key, value);
        return value;
    }

    /**
     * Gets the value associated with the key.
     *
     * @param key The key of the setting.
     * @return The value of the setting or null if not found.
     */
    @Override
    public Object get(String key) {
        Object value = null;

        for (int i = _pageStack.size() - 1; null == value && i >= 0; i--) {
            IdMapTuple tuple = _pageStack.get(i);
            value = tuple.map.get(key);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getOrDefault(String name, V defaultValue) {
        return Optional.ofNullable((V) get(name)).orElse(defaultValue);
    }


    /**
     * Gets the current page in the stack (without removing it).
     *
     * @return The current page in the stack or null if no page have been
     * created yet.
     */
    private IdMapTuple current() {
        if (0 == _pageStack.size())
            return null;

        return _pageStack.peek();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("WizardSettings: ");

        for (String key : keySet()) {
            str.append("[").append(key).append("=").append(get(key)).append("] ");
        }
        return str.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        _oldPageMaps.clear();
        _pageStack.clear();
        // initialize the first page again:
        newPage("");
    }


    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        boolean containsVal = false;

        for (int i = _pageStack.size() - 1; !containsVal && i >= 0; i--) {
            IdMapTuple tuple = _pageStack.get(i);
            containsVal = tuple.map.containsValue(value);
        }
        return containsVal;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return keySet().size() == 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ?> m) {
        Objects.requireNonNull(current()).map.putAll(m);
    }

    /**
     * Removes the mapping for a key from the current page if it is present.
     * <p>
     * Note that this don't remove the key in all pages, only in the current one,
     * so this settings can still contains the key in previous pages. This break
     * the {@link Map#remove(Object)} method definition.
     * <p>
     * Also note that it can also return null if the key exist and its value is
     * null.
     *
     * @return The associated value to the key which is removed or null if the
     * current page don't contains it.
     */
    @Override
    public Object remove(String key) {
        return Objects.requireNonNull(current()).map.remove(key);

    }

    /**
     * Remove the key (and its associated values) in all the pages of the stack.
     * <p>
     * Note that this doesn't remove the key from committed pages.
     *
     * @param key The key that will be removed.
     */
    public void removeAll(String key) {
        this._pageStack.forEach(page -> page.map.remove(key));

    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return keySet().size();
    }


    private static class IdMapTuple {
        public final String id;
        public final Map<String, Object> map;

        public IdMapTuple(String id, Map<String, Object> map) {
            this.id = id;
            this.map = map;
        }
    }

    /**
     * Compare if this active {@link WizardSettings} are equal to other
     * {@link WizardSettings}.
     *
     * @param ws The other settings to compare with.
     * @return <code>true</code> If active keys are the same in both settings and
     * its values are also equals.
     * @author <a href="mailto:PhoneixSegovia@gmail.com">Javier Alfonso</a>
     */
    public boolean settingsEquals(WizardSettings ws) {

        if (ws == this) {
            return true;
        }

        Set<String> otherKeys = ws.keySet();
        Set<String> thisKeys = this.keySet();

        if (otherKeys.equals(thisKeys)) {

            for (String key : thisKeys) {

                if (!this.get(key).equals(ws.get(key))) {

                    return false;

                }

            }

            return true;

        } else {

            return false;

        }
    }
}
