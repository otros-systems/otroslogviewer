/*
 * Copyright 2022 Krzysztof Otrebski (otros.systems@gmail.com)
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

package pl.otros.vfs.browser.util;

import com.jcraft.jsch.*;
import org.apache.commons.vfs2.provider.sftp.IdentityRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;


public final class PageantIdentityRepositoryFactory implements IdentityRepositoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageantIdentityRepositoryFactory.class);
    private static final byte SSH2_AGENTC_REQUEST_IDENTITIES = 11;
    private static final byte SSH2_AGENT_IDENTITIES_ANSWER = 12;
    private static final int MAX_AGENT_IDENTITIES = 2048;

    @Override
    public IdentityRepository create(JSch jsch) {
        try {
            AgentConnector con = new PageantConnector();
            return new AgentIdentityRepository(con);
        } catch (AgentProxyException | RuntimeException e) {
            LOGGER.info("Unable to load PageantConnector", e);
            return null;
        }

    }

    public static OptionalInt getIdentitiesCount() {
        try {
            AgentConnector connector = new PageantConnector();

            byte[] buf = new byte[1024];
            Buffer buffer = new Buffer(buf);

            int required_size = 1 + 4;
            buffer.reset();
            buffer.putInt(required_size - 4);
            buffer.putByte(SSH2_AGENTC_REQUEST_IDENTITIES);

            try {
                connector.query(buffer);
            } catch (AgentProxyException e) {
                return OptionalInt.empty();
            }

            int rcode = buffer.getByte();
            if (rcode != SSH2_AGENT_IDENTITIES_ANSWER) {
                return OptionalInt.empty();
            }

            int count = buffer.getInt();
            if (count <= 0 || count > MAX_AGENT_IDENTITIES) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(count);
        } catch (AgentProxyException e) {
            return OptionalInt.empty();
        }
    }
}
