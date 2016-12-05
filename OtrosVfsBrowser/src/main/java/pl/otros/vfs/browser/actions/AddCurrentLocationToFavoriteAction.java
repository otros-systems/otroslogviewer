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

package pl.otros.vfs.browser.actions;

import pl.otros.vfs.browser.Icons;
import pl.otros.vfs.browser.VfsBrowser;
import pl.otros.vfs.browser.favorit.Favorite;
import pl.otros.vfs.browser.i18n.Messages;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class AddCurrentLocationToFavoriteAction extends AbstractAction {

  private VfsBrowser vfsBrowser;
  private Set<String> remoteSchemas;

  public AddCurrentLocationToFavoriteAction(VfsBrowser vfsBrowser) {
    this.vfsBrowser = vfsBrowser;
    putValue(NAME, Messages.getMessage("nav.AddToFavorites"));
    putValue(SHORT_DESCRIPTION, Messages.getMessage("nav.AddToFavorites"));
    putValue(SMALL_ICON, Icons.getInstance().getStarPlus());
    remoteSchemas = new HashSet<String>();
    remoteSchemas.add("sftp");
    remoteSchemas.add("ftp");
    remoteSchemas.add("smb");
    remoteSchemas.add("http");
    remoteSchemas.add("https");


  }

  @Override
  public void actionPerformed(ActionEvent e) {
    FileObject currentLocation = vfsBrowser.getCurrentLocation();
    if (currentLocation != null) {

      String name = currentLocation.getName().getBaseName();
      if (remoteSchemas.contains(currentLocation.getName().getScheme())) {
        try {
          URI uri = new URI(currentLocation.getName().getURI());
          name = uri.getHost() + "/" + name;
        } catch (URISyntaxException e1) {
          e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

      }
      Favorite favorite;
      try {
        favorite = new Favorite(name, currentLocation.getURL().toExternalForm(), Favorite.Type.USER);
        vfsBrowser.getFavoritesUserListModel().add(favorite);
      } catch (FileSystemException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }
}
