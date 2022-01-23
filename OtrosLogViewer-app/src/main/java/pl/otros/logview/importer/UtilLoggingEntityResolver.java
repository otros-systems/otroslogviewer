/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.importer;

import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * An {@link EntityResolver} specifically designed to return <code>java 1.4's logging dtd, logger.dtd</code> which is embedded within the log4j jar file. Based
 * on EntityResolver.
 *
 * @author Paul Austin
 * @author Scott Deboy (sdeboy@apache.org)
 * @since 1.3
 */
public final class UtilLoggingEntityResolver implements EntityResolver {


  /**
   * {@inheritDoc}
   */
  public InputSource resolveEntity(final String publicId, final String systemId) {
    if (systemId.endsWith("logger.dtd")) {
      Class clazz = getClass();
      InputStream in = clazz.getResourceAsStream("/pl/otros/logview/importer/log4jxml/logger.dtd");
      if (in == null) {
        LoggerFactory.getLogger(UtilLoggingEntityResolver.class).error(
          "Could not find [logger.dtd]. Used [" + clazz.getClassLoader() + "] class loader in the search.");
        return null;
      } else {
        return new InputSource(in);
      }
    } else {
      return null;
    }
  }
}
