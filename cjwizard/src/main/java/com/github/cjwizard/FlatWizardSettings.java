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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This settings can save pairs of key and values in a plain {@link HashMap}.
 *
 * Note that this class doesn't implement the transaction model, and so
 * {@link #newPage(String)}, {@link #rollBack()} and {@link #commit()} methods
 * don't do anything.
 *
 * @author ddearing
 */
public class FlatWizardSettings
        implements WizardSettings {

    Map<String, Object> map = new HashMap<>();
    /**
     * Serial version number, remember to change it when the class isn't binary
     * compatible with older versions.
     */
    private static final long serialVersionUID = 20141205L;

    /**
     * Log instance
     */
    private static final Logger log = LoggerFactory.getLogger(FlatWizardSettings.class);

    /* (non-Javadoc)
     * @see com.github.cjwizard.WizardSettings#newPage(java.lang.String)
     */
    @Override
    public void newPage(String id) {
        // no-op
    }

    /* (non-Javadoc)
     * @see com.github.cjwizard.WizardSettings#rollBack()
     */
    @Override
    public void rollBack() {
        // no-op
    }

    /*
     * (non-Javadoc)
     * @see com.github.cjwizard.WizardSettings#commit()
     */
    @Override
    public void commit() {
        // no-op
    }

    /**
     * Serialize the current instance of {@link FlatWizardSettings} to the given
     * filename.
     *
     * @param filename The filename.
     */
    public void serialize(String filename) {
        ObjectOutputStream out = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(filename);
            out = new ObjectOutputStream(fout);
            out.writeObject(this);
        } catch (IOException ioe) {
            log.error("Error writing settings", ioe);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    log.error("Error closing output stream", ioe);
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException ioe) {
                    log.error("Error closing file", ioe);
                }
            }
        }
    }

    /**
     * Deserialize a {@link FlatWizardSettings} object from the specified file.
     *
     * @param filename The filename.
     * @return The deserialized {@link FlatWizardSettings}.
     */
    static public FlatWizardSettings deserialize(String filename) {
        ObjectInputStream in = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filename);
            in = new ObjectInputStream(fin);
            return (FlatWizardSettings) in.readObject();
        } catch (IOException ioe) {
            log.error("Error reading settings", ioe);
        } catch (ClassNotFoundException cnfe) {
            log.error("Couldn't instantiate seralized class", cnfe);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    log.error("Error closing inputstream", ioe);
                }
            }
            if (null != fin) {
                try {
                    fin.close();
                } catch (IOException ioe) {
                    log.error("Error closing file", ioe);
                }
            }
        }
        return null;
    }

    @Override
    public <V> V put(String key, V value) {
        map.put(key, value);
        return value;
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getOrDefault(String name, V defaultValue) {
        return (V) map.getOrDefault(name, defaultValue);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Object remove(String key) {
        return map.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }


}
