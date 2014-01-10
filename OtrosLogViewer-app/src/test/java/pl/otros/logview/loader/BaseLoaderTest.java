/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.loader;

import org.junit.Assert;
import org.junit.Test;
import pl.otros.logview.filter.LogFilter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class BaseLoaderTest {
  private BaseLoader baseLoader = new BaseLoader();

  @Test
  public void testLoad() throws URISyntaxException {
    File dir = getFile("plugins/filters/");
    Collection<LogFilter> load = baseLoader.load(dir, LogFilter.class);
    Assert.assertEquals(2, load.size());
  }

  @Test
  public void testLoadFromDir() throws URISyntaxException {
    Collection<LogFilter> load = baseLoader.loadFromDir(getFile("plugins/filters/"), LogFilter.class);
    Assert.assertEquals(1, load.size());
  }

  @Test
  public void testLoadFromJar() throws URISyntaxException {
    Collection<LogFilter> load = baseLoader.loadFromJar(getFile("plugins/filters/filters.jar"), LogFilter.class);
    Assert.assertEquals(1, load.size());
  }

  private File getFile(String name) throws URISyntaxException {
    URI uri = this.getClass().getClassLoader().getResource(name).toURI();
    return new File(uri);
  }
}
