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

/**
 */
public class PluginInfo {

    private final String name;

    private final String description;

    private final String pluginableId;

    private final int apiVersion;

    private final Class<? extends Plugin> pluginClass;



    PluginInfo(String name, String description, String pluginableId, int apiVersion, Class<? extends Plugin> pluginClass) {
        this.name = name;
        this.description = description;
        this.pluginableId = pluginableId;
        this.apiVersion = apiVersion;
        this.pluginClass = pluginClass;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPluginId() {
        return pluginableId;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    @Override
    public String toString() {
        return "PluginInfo{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", pluginableId='" + pluginableId + '\'' +
                ", apiVersion=" + apiVersion +
                ", pluginClass=" + pluginClass +
                '}';
    }
}
