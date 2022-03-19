/*
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
 */

package pl.otros.starter;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Krzysztof Otrebski
 * Date: 3/27/12
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlatFileClassLoaderResolver implements ClassLoaderResolver {
  private static final Logger LOGGER = Logger.getLogger(FlatFileClassLoaderResolver.class.getName());

  @Override
  public URL[] getClassPathUrls(InputStream inputStream) throws IOException {
    String olvHome = System.getProperty("OLV_HOME");
    ArrayList<URL> result = new ArrayList<URL>();
    BufferedReader bin = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = bin.readLine()) != null) {
      File f = new File(olvHome,line);

      try {
        result.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        LOGGER.severe(String.format("Can't add resources %s: %s", line, e.getMessage()));
      }

    }

    return result.toArray(new URL[result.size()]);

  }
}
