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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

public class AuthStoreUtils {

  private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AuthStoreUtils.class.getName());

  public static final String USER = "user";
  public static final String HOST = "host";
  public static final String PROTOCOL = "protocol";
  public static final String ENTRY = "Entry";
  public static final String USER_AUTHENTICATION_DATA = "UserAuthenticationData";
  public static final String TYPE = "Type";
  public static final String DATA = "Data";
  public static final String ALGORITHM_BLOW_FISH = "Blowfish";
  public static final int SALT_LENGTH = 64;
  private char[] password = null;

  private PasswordProvider passwordProvider;

  public AuthStoreUtils(PasswordProvider passwordProvider) {
    this.passwordProvider = passwordProvider;
  }

  public void save(AuthStore authStore, OutputStream out) throws IOException {
    Collection<UserAuthenticationInfo> all = authStore.getAll();
    for (UserAuthenticationInfo userAuthenticationInfo : all) {
      UserAuthenticationData userAuthenticationData = authStore.getUserAuthenticationData(userAuthenticationInfo);
    }
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = null;
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      Element root = document.createElement("root");

      document.appendChild(root);
      for (UserAuthenticationInfo userAuthenticationInfo : all) {
        UserAuthenticationDataWrapper userAuthenticationData = authStore.getUserAuthenticationData(userAuthenticationInfo);
        Element entry = document.createElement(ENTRY);
        entry.setAttribute(PROTOCOL, userAuthenticationInfo.getProtocol());
        entry.setAttribute(HOST, userAuthenticationInfo.getHost());
        entry.setAttribute(USER, userAuthenticationInfo.getUser());
        Map<UserAuthenticationData.Type, char[]> addedTypes = userAuthenticationData.getAddedTypes();

        for (UserAuthenticationData.Type type : addedTypes.keySet()) {
          Element elementUserAuthenticationData = document.createElement(USER_AUTHENTICATION_DATA);
          char[] data = userAuthenticationData.getData(type);
          String value;
//          if (UserAuthenticationData.PASSWORD.equals(type)) {
//            if (password == null){
//              password = passwordProvider.getPassword("Enter password for password store");
//            }
//            if (password == null || password.length==0){
//              throw new IOException("Password for password store not entered");
//            }
//            value = saltAndEncrypt(data);
//          } else {
//            value = new String(data);
//          }
          value = new String(data);
          Element elementType = document.createElement(TYPE);
          elementType.setTextContent(type.toString());
          Element elementData = document.createElement(DATA);
          elementData.setTextContent(value);
          elementUserAuthenticationData.appendChild(elementType);
          elementUserAuthenticationData.appendChild(elementData);
          entry.appendChild(elementUserAuthenticationData);
        }
        root.appendChild(entry);
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance();

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
    } catch (Exception e) {
      throw new IOException(e);
    }

  }

  private String saltAndEncrypt(char[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    String value;
    Hex hex = new Hex();
    char[] saltedData = addSalt(data);

    byte[] encode = encrypt(saltedData, password);
    value = new String(hex.encode(encode));
    return value;
  }

  protected char[] addSalt(char[] data) {
    char[] saltedData = new char[SALT_LENGTH + data.length];
    for (int i = 0; i < SALT_LENGTH; i++) {
      saltedData[i] = 'a';// (char) random.nextInt();
    }
    for (int i = 0; i < data.length; i++) {
      saltedData[i + SALT_LENGTH] = data[i];
    }
    return saltedData;
  }

  protected char[] removeSalt(char[] data) {
    char[] deSalted = new char[data.length - SALT_LENGTH];
    System.arraycopy(data, SALT_LENGTH, deSalted, 0, deSalted.length);
    return deSalted;
  }

  public void load(AuthStore authStore, InputStream in) throws IOException {
    try {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setContentHandler(new AuthStoreHandler(authStore));
      xmlReader.parse(new InputSource(in));
    } catch (SAXException e) {
      throw new IOException(e);
    }
  }

  private class AuthStoreHandler extends DefaultHandler {

    private UserAuthenticationDataWrapper userAuthenticationData;
    private UserAuthenticationInfo info;
    private StringBuilder sb = new StringBuilder();
    private String data;
    private String type;

    private AuthStore authStore;

    private AuthStoreHandler(AuthStore authStore) {
      this.authStore = authStore;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

      if (ENTRY.equals(localName)) {
        userAuthenticationData = new UserAuthenticationDataWrapper();
        info = new UserAuthenticationInfo(atts.getValue(PROTOCOL), atts.getValue(HOST), atts.getValue(USER));

      }
      sb.setLength(0);

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (ENTRY.equals(localName)) {
        authStore.add(info, userAuthenticationData);
      } else if (USER_AUTHENTICATION_DATA.equals(localName)) {
//        if (UserAuthenticationData.PASSWORD.equals(new UserAuthenticationData.Type(type))) {
//          if (password == null){
//            password = passwordProvider.getPassword("Enter password for password store");
//          }
//          if (password == null || password.length==0){
//               throw new SAXException("Password for password store not entered");
//          }
//          Hex hex = new Hex();
//          try {
//            byte[] decode = (byte[]) hex.decode(data.trim());
//            byte[] decrypted = decrypt(decode, password);
//            char[] passwordWithSalt = bytesToChars(decrypted);
//            char[] password = removeSalt(passwordWithSalt);
//            data = new String(password);
//          } catch (Exception e) {
//            password=null;
//            throw new SAXException("Can't decrypt password", e);
//          }
//        }
        userAuthenticationData.setData(new UserAuthenticationData.Type(type), data.toCharArray());
      } else if (DATA.equals(localName)) {
        data = sb.toString();
      } else if (TYPE.equals(localName)) {
        type = sb.toString();
      }

      sb.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      sb.append(ch, start, length);
    }
  }

  protected byte[] encrypt(char[] bytes, char[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    byte[] passBytes = charsToBytes(password);
    SecretKeySpec secretKeySpec = new SecretKeySpec(passBytes, ALGORITHM_BLOW_FISH);

    Cipher cipher = Cipher.getInstance(ALGORITHM_BLOW_FISH);

    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    byte[] encrypted = cipher.doFinal(new String(bytes).getBytes("UTF-8"));
    return encrypted;
  }

  protected byte[] decrypt(byte[] bytes, char[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    byte[] passBytes = charsToBytes(password);
    SecretKeySpec secretKeySpec = new SecretKeySpec(passBytes, ALGORITHM_BLOW_FISH);
    Cipher cipher = Cipher.getInstance(ALGORITHM_BLOW_FISH);
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
    byte[] decrypted = cipher.doFinal(bytes);
    return decrypted;
  }

  protected byte[] charsToBytes(char[] chars) throws UnsupportedEncodingException {
    return new String(chars).getBytes("UTF-8");
  }

  protected char[] bytesToChars(byte[] bytes) {
    return new String(bytes, Charset.forName("UTF-8")).toCharArray();
  }

  public PasswordProvider getPasswordProvider() {
    return passwordProvider;
  }

  public void setPasswordProvider(PasswordProvider passwordProvider) {
    this.passwordProvider = passwordProvider;
  }

}
