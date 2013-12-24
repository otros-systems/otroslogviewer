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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Krzysztof Otrebski
 * Date: 3/27/12
 * Time: 9:12 PM
 */
public class FlatFileClassLoaderReoslverTest {

    private FlatFileClassLoaderResolver flatFileClassLoaderResolver = new FlatFileClassLoaderResolver();

    @Test
    public void testGetClassPathUrls() throws Exception {
        String cp = "./lib/lib1.jar\n./lib/lib2.jar";

        URL[] classPathUrls = flatFileClassLoaderResolver.getClassPathUrls(new ByteArrayInputStream(cp.getBytes()));

        Assert.assertEquals(2, classPathUrls.length);
    }
}
