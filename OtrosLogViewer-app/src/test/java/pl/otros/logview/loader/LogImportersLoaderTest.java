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

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.importer.InitializationException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class LogImportersLoaderTest {

  @Test
  public void testLoadPropertyFileBased() throws InitializationException, URISyntaxException {
    LogImportersLoader importersLoader = new LogImportersLoader();
    Collection<LogImporter> loadPropertyFileBased = importersLoader.loadPropertyPatternFileFromDir(getFile(("plugins/logimporters/")));
    AssertJUnit.assertEquals(2, loadPropertyFileBased.size());
  }

  private File getFile(String name) throws URISyntaxException {
    URI uri = this.getClass().getClassLoader().getResource(name).toURI();
    return new File(uri);
  }
}


