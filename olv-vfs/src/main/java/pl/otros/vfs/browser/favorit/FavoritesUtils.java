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

package pl.otros.vfs.browser.favorit;

import pl.otros.vfs.browser.favorit.Favorite.Type;
import net.sf.vfsjfilechooser.accessories.bookmarks.BookmarksReader;
import net.sf.vfsjfilechooser.accessories.bookmarks.TitledURLEntry;
import org.apache.commons.configuration.DataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavoritesUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(FavoritesUtils.class);

  public static final File HOME_DIRECTORY = new File(System.getProperty("user.home"));

  public static final File VFS_JFC_CONFIG_DIRECTORY = new File(HOME_DIRECTORY, ".vfsjfilechooser");
  public static final File VFS_JFC_BOOKMARKS_FILE = new File(VFS_JFC_CONFIG_DIRECTORY, "favorites.xml");

  public List<Favorite> loadFavorite() {
    return new ArrayList<Favorite>();
  }

  public static List<Favorite> getSystemLocations() {
    ArrayList<Favorite> list = new ArrayList<Favorite>();
    File[] listRoots = File.listRoots();
    for (File file : listRoots) {
      list.add(new Favorite(file.getAbsolutePath(), file.getAbsolutePath(), Favorite.Type.SYSTEM));
    }

    list.add(new Favorite("Home", new File(System.getProperty("user.home")).getAbsolutePath(), Favorite.Type.SYSTEM));
    return list;
  }

  public static List<Favorite> getJvfsFileChooserBookmarks() {
    ArrayList<Favorite> list = new ArrayList<Favorite>();
    if (VFS_JFC_BOOKMARKS_FILE.exists()) {
      LOGGER.info("Loading JVfsFileChooser bookmarks");

      List<TitledURLEntry> parsedEntries = new BookmarksReader(VFS_JFC_BOOKMARKS_FILE).getParsedEntries();
      LOGGER.info("Loaded {} JVfsFileChooser bookmarks", parsedEntries.size());
      for (TitledURLEntry titledURLEntry : parsedEntries) {
        list.add(new Favorite(titledURLEntry.getTitle(), titledURLEntry.getURL(), Type.JVFSFILECHOOSER));
      }
    }

    return list;
  }

  public static List<Favorite> loadFromProperties(DataConfiguration conf) {
    ArrayList<Favorite> list = new ArrayList<Favorite>();
    int count = conf.getInt("favorites.count", 0);
    LOGGER.info("Loading favorites {}", count);
    for (int i = 0; i < count; i++) {
      String name = conf.getString(String.format("favorite.item_%d.name", i));
      String url = conf.getString(String.format("favorite.item_%d.url", i));
      Favorite favorite = new Favorite(name, url, Type.USER);
      list.add(favorite);
    }
    return list;
  }

  public static void storeFavorites(DataConfiguration configuration, List<Favorite> favoriteList) {
    int i = 0;
    for (Favorite favorite : favoriteList) {
      if (favorite.getType().equals(Type.USER)) {
        configuration.setProperty(String.format("favorite.item_%d.name", i), favorite.getName());
        configuration.setProperty(String.format("favorite.item_%d.url", i), favorite.getUrl());
        i++;
      }
    }
    configuration.setProperty("favorites.count", i);
  }
}
