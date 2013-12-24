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

public class PluginInfoBuilder {
    private String name;
    private String description;
    private String pluginableId;
    private int apiVersion;
    private Class<? extends Plugin> pluginClass;

    public PluginInfoBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public PluginInfoBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public PluginInfoBuilder setPluginableId(String pluginableId) {
        this.pluginableId = pluginableId;
        return this;
    }

    public PluginInfoBuilder setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public PluginInfoBuilder setPluginClass(Class<? extends Plugin> pluginClass) {
        this.pluginClass = pluginClass;
        return this;
    }

    public PluginInfo createPluginInfo() {
        return new PluginInfo(name, description, pluginableId, apiVersion, pluginClass);
    }
}