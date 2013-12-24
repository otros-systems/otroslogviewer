/*
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.api.plugins;

import pl.otros.logview.loader.BaseLoader;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 */
public class PluginManager {

    private  static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
    public Collection<PluginInfo> loadPlugins(File dirWithPlugins){
        BaseLoader baseLoader = new BaseLoader();
        File[] dirs = dirWithPlugins.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        Collection<PluginInfo> pluginCollection = new ArrayList<PluginInfo>();
        for (File dir : dirs) {
            pluginCollection.addAll(baseLoader.load(dir, PluginInfo.class));
            LOGGER.info(String.format("Loaded %d plugins from %s", baseLoader.load(dir, PluginInfo.class).size(), dir.getAbsolutePath()));
        }

        return pluginCollection;
    }

    public static void main(String[] args) {
        new PluginManager().loadPlugins(new File("./"));
    }
}
