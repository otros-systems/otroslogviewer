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
package pl.otros.vfs.browser.util;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.jcraft.jsch.JSchException;
import jcifs.smb.SmbAuthException;
import net.sf.vfsjfilechooser.utils.VFSURIParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.UserAuthenticationData.Type;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http.HttpFileObject;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.vfs.browser.Icons;
import pl.otros.vfs.browser.LinkFileObject;
import pl.otros.vfs.browser.TaskContext;
import pl.otros.vfs.browser.auth.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class to deal with commons-vfs file abstractions
 *
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 * @author Jojada Tirtowidjojo <jojada at users.sourceforge.net>
 * @author Stephan Schuster <stephanschuster at users.sourceforge.net>
 * @version 0.0.5
 */
public final class VFSUtils {
  private static final int SYMBOLIC_LINK_MAX_SIZE = 128;


  // private static members
  private static FileSystemManager fileSystemManager;
  private static FileSystemOptions opts = new FileSystemOptions();
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
  private static final String PROTO_PREFIX = "://";
  private static final String FILE_PREFIX = OS_NAME.startsWith("windows") ? "file:///" : "file://";
  private static final int FILE_PREFIX_LEN = FILE_PREFIX.length();
  private static final File HOME_DIRECTORY = new File(System.getProperty("user.home"));

  //  public static final File CONFIG_DIRECTORY = new File(HOME_DIRECTORY, ".vfsjfilechooser");
  public static final File CONFIG_DIRECTORY = new File(HOME_DIRECTORY, ".otrosvfsbrowser");
  public static final File USER_AUTH_FILE = new File(CONFIG_DIRECTORY, "auth.xml");
  public static final File USER_AUTH_FILE_BAK = new File(CONFIG_DIRECTORY, "auth.xml.bak");
  private static ReadWriteLock aLock = new ReentrantReadWriteLock(true);

  // File size localized strings

  private static final Logger LOGGER = LoggerFactory.getLogger(VFSUtils.class);
  private static final Map<String, Icon> schemeIconMap = new HashMap<String, Icon>();
  private static final Set<String> archivesSuffixes = new HashSet<String>();
  private static AuthStore sessionAuthStore = new MemoryAuthStore();

  //TODO change to persistent auth store
  private static AuthStore persistentAuthStore = new MemoryAuthStore();
  private static AuthStoreUtils authStoreUtils;
  private static boolean authStoreLoaded = false;

  static {
    schemeIconMap.put("file", Icons.getInstance().getDrive());
    schemeIconMap.put("sftp", Icons.getInstance().getNetworkCloud());
    schemeIconMap.put("ftp", Icons.getInstance().getNetworkCloud());
    schemeIconMap.put("smb", Icons.getInstance().getSambaShare());
    schemeIconMap.put("http", Icons.getInstance().getNetworkCloud());
    schemeIconMap.put("https", Icons.getInstance().getNetworkCloud());
    schemeIconMap.put("zip", Icons.getInstance().getFolderZipper());
    schemeIconMap.put("tar", Icons.getInstance().getFolderZipper());
    schemeIconMap.put("jar", Icons.getInstance().getFolderZipper());
    schemeIconMap.put("tgz", Icons.getInstance().getFolderZipper());
    schemeIconMap.put("tbz", Icons.getInstance().getFolderZipper());

    archivesSuffixes.add("zip");
    archivesSuffixes.add("tar");
    archivesSuffixes.add("jar");
    archivesSuffixes.add("tgz");
    archivesSuffixes.add("gz");
    archivesSuffixes.add("tar");
    archivesSuffixes.add("tbz");
    archivesSuffixes.add("tgz");

    //TODO fix this
    authStoreUtils = new AuthStoreUtils(new StaticPasswordProvider("Password".toCharArray()));
  }

  // prevent unnecessary calls
  private VFSUtils() {
    throw new AssertionError("Trying to create a VFSUtils object");
  }

  /**
   * Returns the global filesystem manager
   *
   * @return the global filesystem manager
   */
  public static FileSystemManager getFileSystemManager() {
    aLock.readLock().lock();

    try {
      if (fileSystemManager == null) {
        try {
          StandardFileSystemManager fm = new StandardFileSystemManager();
          fm.setCacheStrategy(CacheStrategy.MANUAL);
          fm.init();
          LOGGER.info("Supported schemes: {} ", Joiner.on(", ").join(fm.getSchemes()));
          fileSystemManager = fm;
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      }

      return fileSystemManager;
    } finally {
      aLock.readLock().unlock();
    }
  }

  // -----------------------------------------------------------------------


  /**
   * Returns a file representation
   *
   * @param filePath The file path
   * @return a file representation
   */
  public static FileObject createFileObject(String filePath) {
    try {
      return getFileSystemManager().resolveFile(filePath, opts);
    } catch (FileSystemException ex) {
      return null;
    }
  }

  /**
   * Remove user credentials information
   *
   * @param fileName The file name
   * @return The "safe" display name without username and password information
   */
  public static String getFriendlyName(String fileName) {
    return getFriendlyName(fileName, true);
  }

  public static String getFriendlyName(String fileName, boolean excludeLocalFilePrefix) {
    if (fileName == null) {
      return "";
    }
    StringBuilder filePath = new StringBuilder();


    int pos = fileName.lastIndexOf('@');

    if (pos == -1) {
      filePath.append(fileName);
    } else {
      int pos2 = fileName.indexOf(PROTO_PREFIX);

      if (pos2 == -1) {
        filePath.append(fileName);
      } else {
        String protocol = fileName.substring(0, pos2);

        filePath.append(protocol).append(PROTO_PREFIX).append(fileName.substring(pos + 1, fileName.length()));
      }
    }

    String returnedString = filePath.toString();

    if (excludeLocalFilePrefix && returnedString.startsWith(FILE_PREFIX)) {
      return filePath.substring(FILE_PREFIX_LEN);
    }

    return returnedString;
  }

  /**
   * Returns the root filesystem of a given file
   *
   * @param fileObject A file
   * @return the root filesystem of a given file
   */
  public static FileObject createFileSystemRoot(FileObject fileObject) {
    try {
      return fileObject.getFileSystem().getRoot();
    } catch (FileSystemException ex) {
      return null;
    }
  }

  /**
   * Returns all the files of a folder
   *
   * @param folder A folder
   * @return the files of a folder
   */
  public static FileObject[] getFiles(FileObject folder)
  throws FileSystemException {
      return getChildren(folder);
  }

  /**
   * Returns the root file system of a file representation
   *
   * @param fileObject A file abstraction
   * @return the root file system of a file representation
   */
  public static FileObject getRootFileSystem(FileObject fileObject) {
    try {
      if ((fileObject == null) || !fileObject.exists()) {
        return null;
      }

      return fileObject.getFileSystem().getRoot();
    } catch (FileSystemException ex) {
      return null;
    }
  }

  /**
   * Tells whether a file is hidden
   *
   * @param fileObject a file representation
   * @return whether a file is hidden
   */
  public static boolean isHiddenFile(FileObject fileObject) {
    try {
      return fileObject.getName().getBaseName().charAt(0) == '.';
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Tells whether a file is the root file system
   *
   * @param fileObject A file representation
   * @return whether a file is the root file system
   */
  public static boolean isRoot(FileObject fileObject) {
    try {
      return fileObject.getParent() == null;
    } catch (FileSystemException ex) {
      return false;
    }
  }

  /**
   * Returns a file representation
   *
   * @param filePath The file path
   * @return a file representation
   * @throws FileSystemException
   */
  public static FileObject resolveFileObject(String filePath) throws FileSystemException {
    LOGGER.info("Resolving file: {}", filePath);
    if (filePath.startsWith("sftp://")) {
      SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
      builder.setStrictHostKeyChecking(opts, "no");
      builder.setUserDirIsRoot(opts, false);
      builder.setCompression(opts, "zlib,none");

    } else if (filePath.startsWith("smb://")) {

    } else if (filePath.startsWith("ftp://")) {
      FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
    }
    UserAuthenticatorFactory factory = new UserAuthenticatorFactory();

    OtrosUserAuthenticator authenticator = factory.getUiUserAuthenticator(persistentAuthStore, sessionAuthStore, filePath, opts);

    if (pathContainsCredentials(filePath)) {
      authenticator = null;
    }
    return resolveFileObject(filePath, opts, authenticator, persistentAuthStore, sessionAuthStore);
  }

  private static boolean pathContainsCredentials(String filePath) {
    VFSURIParser parser = new VFSURIParser(filePath);
    return StringUtils.isNotBlank(parser.getUsername()) && StringUtils.isNotBlank(parser.getPassword());
  }

  public static FileObject resolveFileObject(URI uri) throws FileSystemException, MalformedURLException {
    return resolveFileObject(uri.toURL().toExternalForm());
  }

  /**
   * Returns a file representation
   *
   * @param filePath The file path
   * @param options  The filesystem options
   * @return a file representation
   * @throws FileSystemException
   */
  public static FileObject resolveFileObject(String filePath, FileSystemOptions options, OtrosUserAuthenticator authenticator, AuthStore persistentAuthStore, AuthStore sessionAuthStore) throws FileSystemException {
    if (filePath.startsWith("sftp://")) {
      SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
      builder.setStrictHostKeyChecking(opts, "no");
      builder.setUserDirIsRoot(opts, false);
      builder.setCompression(opts, "zlib,none");
    }

    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, authenticator);
    FileObject resolveFile;


    VFSURIParser parser = new VFSURIParser(filePath);
    //Get file type to force authentication
    try {
      resolveFile = getFileSystemManager().resolveFile(filePath, options);
      resolveFile.getType();
    } catch (FileSystemException e) {
      LOGGER.error("Error resolving file " + filePath, e);
      Throwable rootCause = Throwables.getRootCause(e);
      boolean authorizationFailed = false;
      authorizationFailed = checkForWrongCredentials(rootCause);
      if (authorizationFailed) {
        LOGGER.error("Wrong user name or password for " + filePath);
        //clear last data
        //authenticator can be null if user/password was entered in URL
        if (authenticator != null) {
          UserAuthenticationDataWrapper lastUserAuthenticationData = authenticator.getLastUserAuthenticationData();
          lastUserAuthenticationData.remove(UserAuthenticationDataWrapper.PASSWORD);
          String user = new String(lastUserAuthenticationData.getData(UserAuthenticationData.USERNAME));
          UserAuthenticationInfo auInfo = new UserAuthenticationInfo(parser.getProtocol().getName(), parser.getHostname(), user);
          sessionAuthStore.remove(auInfo);
          sessionAuthStore.add(auInfo, lastUserAuthenticationData);
          LOGGER.info("Removing password for {} on {}", new Object[]{
              new String(lastUserAuthenticationData.getData(UserAuthenticationData.USERNAME)), filePath});
        }
      }
      throw e;
    }

    if (resolveFile != null && authenticator != null && authenticator.getLastUserAuthenticationData() != null) {
      UserAuthenticationDataWrapper lastUserAuthenticationData = authenticator.getLastUserAuthenticationData();
      Map<Type, char[]> addedTypes = lastUserAuthenticationData.getAddedTypes();
      String user = new String(addedTypes.get(UserAuthenticationData.USERNAME));
      UserAuthenticationInfo auInfo = new UserAuthenticationInfo(parser.getProtocol().getName(), parser.getHostname(), user);
      sessionAuthStore.add(auInfo, lastUserAuthenticationData.copy());
      if (authenticator.isPasswordSave()) {
        LOGGER.info("Saving password for {}://{}@{}", new Object[]{parser.getProtocol().getName(), user, parser.getHostname()});
        persistentAuthStore.add(auInfo, lastUserAuthenticationData);
        saveAuthStore();
      }
    }
    return resolveFile;
  }

  public static boolean checkForWrongCredentials(Throwable rootCause) {
    boolean authorizationFailed = false;
    String message = rootCause.getMessage();
    if (rootCause instanceof SmbAuthException && message.contains("The specified network password is not correct")) {
      authorizationFailed = true;
    } else if (rootCause instanceof JSchException && message.contains("Auth fail")) {
      authorizationFailed = true;

    }
    return authorizationFailed;
  }

  /**
   * Returns a file representation
   *
   * @param folder   A folder
   * @param filename A filename
   * @return a file contained in a given folder
   */
  public static FileObject resolveFileObject(FileObject folder, String filename) {
    try {
      return folder.resolveFile(filename);
    } catch (FileSystemException ex) {
      return null;
    }
  }

  /**
   * Tells whether a file exists
   *
   * @param fileObject A file representation
   * @return whether a file exists
   */
  public static boolean exists(FileObject fileObject) {
    if (fileObject == null) {
      return false;
    }

    try {
      return fileObject.exists();
    } catch (FileSystemException ex) {
      return false;
    }
  }

  /**
   * y
   *
   * @param fileObject A file object representation
   * @return whether a file object is a directory
   */
  public static boolean isDirectory(FileObject fileObject) {
    try {
      return fileObject.getType().equals(FileType.FOLDER);
    } catch (FileSystemException ex) {
      LOGGER.info("Exception when checking if fileobject is folder", ex);
      return false;
    }
  }

  /**
   * Returns whether a file object is a local file
   *
   * @param fileObject
   * @return true of {@link FileObject} is a local file
   */
  public static boolean isLocalFile(FileObject fileObject) {
    try {
      return fileObject.getURL().getProtocol().equalsIgnoreCase("file") && FileType.FILE.equals(fileObject.getType());
    } catch (FileSystemException e) {
      LOGGER.info("Exception when checking if fileobject is local file", e);
      return false;
    }
  }

  /**
   * Tells whether a folder is the root filesystem
   *
   * @param folder A folder
   * @return whether a folder is the root filesystem
   */
  public static boolean isFileSystemRoot(FileObject folder) {
    return isRoot(folder);
  }

  /**
   * Returns whether a folder contains a given file
   *
   * @param folder A folder
   * @param file   A file
   * @return whether a folder contains a given file
   */
  public static boolean isParent(FileObject folder, FileObject file) {
    try {
      FileObject parent = file.getParent();

      return parent != null && parent.equals(folder);

    } catch (FileSystemException ex) {
      return false;
    }
  }

  public static FileObject getUserHome() throws FileSystemException {
    return resolveFileObject(System.getProperty("user.home"));
  }

  public static void checkForSftpLinks(FileObject[] files, TaskContext taskContext) {
    LOGGER.debug("Checking for SFTP links");
    taskContext.setMax(files.length);
    long ts = System.currentTimeMillis();
    for (int i = 0; i < files.length && !taskContext.isStop(); i++) {
      FileObject fileObject = files[i];
      try {
        if (fileObject instanceof SftpFileObject && FileType.FILE.equals(fileObject.getType())) {
          SftpFileObject sftpFileObject = (SftpFileObject) fileObject;
          long size = sftpFileObject.getContent().getSize();
          if (size < SYMBOLIC_LINK_MAX_SIZE && size != 0) {
            if (!pointToItself(sftpFileObject)) {
              files[i] = new LinkFileObject(sftpFileObject);
            }
          }

        }

      } catch (Exception e) {
        //ignore
      } finally {
        taskContext.setCurrentProgress(i);
      }

    }
    long checkDuration = System.currentTimeMillis() - ts;
    LOGGER.info("Checking SFTP links took {} ms [{}ms/file]", checkDuration, (float) checkDuration / files.length);
  }

  public static boolean pointToItself(FileObject fileObject) throws FileSystemException {
    if (!fileObject.getURL().getProtocol().equalsIgnoreCase("file") && FileType.FILE.equals(fileObject.getType())) {
      LOGGER.debug("Checking if {} is pointing to itself", fileObject.getName().getFriendlyURI());
      FileObject[] children = VFSUtils.getChildren(fileObject);
      LOGGER.debug("Children number of {} is {}", fileObject.getName().getFriendlyURI(), children.length);
      if (children.length == 1) {
        FileObject child = children[0];
        if (child.getContent().getSize() != child.getContent().getSize()) {
          return false;
        }
        if (child.getName().getBaseName().equals(fileObject.getName().getBaseName())) {
          return true;
        }
      }
    }
    return false;
  }

  public static FileObject[] getChildren(FileObject fileObject) throws FileSystemException {
    FileObject[] result;
    if (isHttpProtocol(fileObject)) {
      result = extractHttpFileObjectChildren(fileObject);
    } else if (isLocalFileSystem(fileObject) && isArchive(fileObject)) {
      String extension = fileObject.getName().getExtension();
      result = VFSUtils.resolveFileObject(extension + ":" + fileObject.getURL().toString() + "!/").getChildren();
    } else {
      result = fileObject.getChildren();
    }
    return result;
  }

  public static boolean isArchive(FileObject fileObject) {
    return isArchive(fileObject.getName());
  }

  public static boolean isArchive(FileName fileName) {
    String extension = fileName.getExtension();
    return archivesSuffixes.contains(extension.toLowerCase());
  }


  private static boolean isLocalFileSystem(FileObject fileObject) {
    return fileObject.getName().getScheme().equalsIgnoreCase("file");
  }

  private static FileObject[] extractHttpFileObjectChildren(FileObject fileObject) throws FileSystemException {
    FileObject[] result;
    HttpFileObject fo = (HttpFileObject) fileObject;
    FileContent content = fo.getContent();
    String contentType = content.getContentInfo().getContentType();
    result = new FileObject[]{fileObject};
    if (contentType.equalsIgnoreCase("text/html")) {
      try {
        String html = IOUtils.toString(content.getInputStream());
        if (html.toLowerCase().contains("index of")) {
          LOGGER.info("Page contains \"index of\", resolving children");
          //a href="DSC_0410.JPG">
          Pattern p = Pattern.compile("<A .*?href=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
          Matcher matcher = p.matcher(html);
          ArrayList<FileObject> list = new ArrayList<FileObject>();
          while (matcher.find()) {
            String link = matcher.group(1);
            LOGGER.info("Getting children from link {}", link);
            if (StringUtils.isBlank(link)) {
              LOGGER.debug("URL link is blank");
              continue;
            }
            FileObject child;
            URL url = fileObject.getURL();
            child = extractHttpFileObject(link, url);
            list.add(child);
          }
          result = list.toArray(result);
        }
        //TODO extract links
      } catch (Exception e) {
        throw new FileSystemException(e);
      }
    }
    return result;
  }

  private static FileObject extractHttpFileObject(String link, URL url) throws FileSystemException, URISyntaxException {
    FileObject child;
    LOGGER.info("Extracting {} from URL {}", link, url);
    if (link.startsWith("/")) {

      URI uri = url.toURI();

      String host = uri.getHost();
      int port = uri.getPort();
      if (port != -1) {
        child = resolveFileObject(String.format("%s://%s:%s%s", url.getProtocol(), host, port, link));
      } else {
        child = resolveFileObject(String.format("%s://%s%s", url.getProtocol(), host, link));
      }
    } else if (link.matches("(http|https|smb|ftp|sftp)://.*")) {
      child = resolveFileObject(link);
    } else {
      child = resolveFileObject(url + "/" + link);
    }
    return child;
  }

  public static Icon getIconForFileSystem(String url) {
    String schema = "file";
    if (null != url) {
      int indexOf = url.indexOf("://");
      if (indexOf > 0) {
        schema = url.substring(0, indexOf);
      }
    }
    return schemeIconMap.get(schema);
  }

  public static boolean isHttpProtocol(FileObject fileObject) throws FileSystemException {
    return fileObject != null && fileObject.getURL().getProtocol().startsWith("http");
  }

  public static void loadAuthStore() {
    FileInputStream fin = null;
    if (!USER_AUTH_FILE.exists()) {
      return;
    }
    try {
      fin = new FileInputStream(USER_AUTH_FILE);
      authStoreUtils = new AuthStoreUtils(new StaticPasswordProvider("Password".toCharArray()));
      authStoreUtils.load(persistentAuthStore, fin);
      authStoreLoaded = true;
    } catch (IOException e) {
      LOGGER.error("Can't load auth store", e);
    } finally {
      IOUtils.closeQuietly(fin);
    }
  }

  public static void saveAuthStore() {
    FileOutputStream out = null;
    try {
      if (!CONFIG_DIRECTORY.exists()) {
        CONFIG_DIRECTORY.mkdirs();
      }
      out = new FileOutputStream(USER_AUTH_FILE_BAK);
      authStoreUtils.save(persistentAuthStore, out);
      out.close();
      FileUtils.copyFile(USER_AUTH_FILE_BAK, USER_AUTH_FILE);
      FileUtils.deleteQuietly(USER_AUTH_FILE_BAK);
    } catch (IOException e) {
      LOGGER.error("Can't save auth store", e);
    } finally {
      IOUtils.closeQuietly(out);
    }

  }

  public static boolean isAuthStoreLoaded() {
    return authStoreLoaded;
  }

  public static boolean canGoUrl(FileObject fileObject) {
    //http files
    try {
      if (!fileObject.getURL().getProtocol().startsWith("http") && VFSUtils.pointToItself(fileObject)) {
        return false;
      }
    } catch (FileSystemException e1) {
      LOGGER.error("Can't check if file is link", e1);
    }

    //Local files
    if (VFSUtils.isLocalFile(fileObject)) {
      return false;
    }
    return true;

  }
}
