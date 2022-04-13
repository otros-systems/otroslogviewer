/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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

package pl.otros.vfs.browser.auth;


import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.testng.annotations.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


public class AuthStoreUtilsTest {

  private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AuthStoreUtilsTest.class.getName());
  private AuthStoreUtils authStoreUtils = new AuthStoreUtils(new StaticPasswordProvider("Password".toCharArray()));
  
  @Test(enabled = false)
  public void testSave() throws Exception {
    //given
    AuthStore authStore = new MemoryAuthStore();
    UserAuthenticationDataWrapper authenticationData1 = new UserAuthenticationDataWrapper();
    authenticationData1.setData(UserAuthenticationData.USERNAME, "Stefan".toCharArray());
    authenticationData1.setData(UserAuthenticationData.PASSWORD, "Password".toCharArray());
    authenticationData1.setData(new UserAuthenticationData.Type("path"), "c:\\file".toCharArray());

    UserAuthenticationDataWrapper authenticationData2 = new UserAuthenticationDataWrapper();
    authenticationData2.setData(UserAuthenticationData.USERNAME, "Stefaan".toCharArray());
    authenticationData2.setData(UserAuthenticationData.PASSWORD, "Passwodrd".toCharArray());
    authenticationData2.setData(UserAuthenticationData.DOMAIN, "MS".toCharArray());

    UserAuthenticationDataWrapper authenticationData3 = new UserAuthenticationDataWrapper();
    authenticationData3.setData(UserAuthenticationData.USERNAME, "Stefan".toCharArray());
    authenticationData3.setData(UserAuthenticationData.PASSWORD, "Passwo@rd".toCharArray());
    authenticationData3.setData(UserAuthenticationData.DOMAIN, "MSzx!X%a".toCharArray());

    authStore.add(new UserAuthenticationInfo("ftp", "host1", "stefan"), authenticationData1);
    authStore.add(new UserAuthenticationInfo("ftp", "host1", "stefan9"), authenticationData1);
    authStore.add(new UserAuthenticationInfo("sftp", "host1a", "astefan"), authenticationData1);

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    String expected = IOUtils.toString(this.getClass().getResourceAsStream("credentials.xml"));
    expected = expected.replaceAll(">\\s+", ">").replaceAll("\\s+<", "<").replaceAll("<Type>password</Type><Data>.*?<", "<Type>password</Type><Data><").trim();

    //when
    authStoreUtils.save(authStore, bout);

    //then
    System.out.println(new String(bout.toByteArray()));
    String result = new String(bout.toByteArray()).replaceAll(">\\s+", ">").replaceAll("\\s+<", "<").replaceAll("<Type>password</Type><Data>.*?<", "<Type>password</Type><Data><").trim();
    assertEquals(result, expected);

  }

  @Test
  public void testEncrypt() throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException {
    //given
    
    String password = "alma";
    String message = "text";

    //when
    byte[] encode = authStoreUtils.encrypt(message.toCharArray(), password.toCharArray());
    System.out.flush();

    byte[] decode = authStoreUtils.decrypt(encode, password.toCharArray());


    //then
    assertEquals(new String(decode), message);
  }

  @Test
  public void testSalting(){
    //given
    char[] src = "ale ma kota a kot ma ale".toCharArray();
    
    //when
    char[] salted = authStoreUtils.addSalt(src);
    char[] deSalted = authStoreUtils.removeSalt(salted);

    //then
    assertEquals(deSalted, src);
    assertTrue(src.length<salted.length);
  }
  
  @Test
  public void testCharsByteConversion() throws UnsupportedEncodingException{
    //given
    char[] src = "ale ma kota a kot ma ale".toCharArray();
    
    //when
    byte[] charsToBytes = authStoreUtils.charsToBytes(src);
    char[] bytesToChars = authStoreUtils.bytesToChars(charsToBytes);
    
    //then
    assertEquals(bytesToChars, src);
  }

  @Test
  public void testLoad() throws Exception {
    //given
    MemoryAuthStore memoryAuthStore = new MemoryAuthStore();
    InputStream resourceAsStream = this.getClass().getResourceAsStream("credentials.xml");

    //when
    authStoreUtils.load(memoryAuthStore, resourceAsStream);

    //then
    assertEquals(memoryAuthStore.getAll().size(), 3);
    UserAuthenticationDataWrapper userAuthenticationData1 = memoryAuthStore.getUserAuthenticationData(new UserAuthenticationInfo("ftp", "host1", "stefan"));
    assertEquals(userAuthenticationData1.getData(UserAuthenticationData.USERNAME), "Stefan".toCharArray());
    LOGGER.info("Password for stefan:" + new String(userAuthenticationData1.getData(UserAuthenticationData.PASSWORD)));
    assertEquals(userAuthenticationData1.getData(UserAuthenticationData.PASSWORD), "Password".toCharArray());
    assertEquals(userAuthenticationData1.getData(new UserAuthenticationData.Type("path")), "c:\\file".toCharArray());

  }
}
