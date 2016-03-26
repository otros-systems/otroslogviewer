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
package pl.otros.logview.importer.log4jxml;

import org.apache.log4j.spi.LoggingEvent;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jUtil;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.*;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

public class Log4jXmlLogImporter extends AbstractPluginableElement implements
		LogImporter {

	private static final int DEFAULT_PARSE_BUFFER_SIZE = 16 * 1024;	
	private static final String LOG4J_XML_LOG_IMPORTER_DECODER = "Log4jXmlLogImporter.decoder";

	public Log4jXmlLogImporter() {
		super("Log4j xml", "Parser log4j xml");

	}

	@Override
	public void init(Properties properties) throws InitializationException {
	}

	@Override
	public void importLogs(InputStream in, LogDataCollector dataCollector,
			ParsingContext parsingContext) {
		try {
			XMLDecoder decoder = (XMLDecoder) parsingContext
					.getCustomConextProperties().get(
							LOG4J_XML_LOG_IMPORTER_DECODER);
			String line = null;
			int parseBufferSize = DEFAULT_PARSE_BUFFER_SIZE;
			byte[] buff = new byte[parseBufferSize];
			int read = 0;
			while ((read = in.read(buff)) > 0) {
				line = new String(buff, 0, read);
				Vector decodeEvents = decoder.decodeEvents(line);
				if (decodeEvents != null) {
					for (Object object : decodeEvents) {
						LoggingEvent event = (LoggingEvent) object;
						LogData logdata = Log4jUtil.translateLog4j(event);
						logdata.setId(parsingContext
								.getGeneratedIdAndIncrease());
						logdata.setLogSource(parsingContext.getLogSource());
						dataCollector.add(logdata);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getKeyStrokeAccelelator() {
		return null;
	}

	@Override
	public int getMnemonic() {
		return 0;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public void initParsingContext(ParsingContext parsingContext) {
		XMLDecoder decoder;
		try {
			decoder = XMLDecoder.class.newInstance();
			parsingContext.getCustomConextProperties().put(
					LOG4J_XML_LOG_IMPORTER_DECODER, decoder);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Cant create XMLDecoder", e);
		}
	}

	@Override
	public int getApiVersion() {
		return LOG_IMPORTER_VERSION_1;
	}

}
