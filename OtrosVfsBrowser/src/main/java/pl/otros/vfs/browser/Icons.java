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

package pl.otros.vfs.browser;

import javax.swing.*;

/**
 */
public class Icons {
  private static Icons instance;
  private final Icon computer;
  private final Icon minusButton;
  private final Icon star;
  private final Icon file;
  private final Icon networkCloud;
  private final Icon drive;
  private final Icon folderOpen;
  private final Icon starPlus;
  private final Icon arrowCircleDouble;
  private final Icon arrowTurn90;
  private final Icon shortCut;
  private final Icon networkStatusAway;
  private final Icon networkStatusOnline;
  private final Icon networkStatusOffline;
  private final Icon sambaShare;
  private final Icon folderZipper;
  private final Icon jarIcon;
  private final ImageIcon editSignature;

  private Icons() {
    computer = new ImageIcon(this.getClass().getClassLoader().getResource("computer.png"));
    minusButton = new ImageIcon(this.getClass().getClassLoader().getResource("minus-button.png"));
    star = new ImageIcon(this.getClass().getClassLoader().getResource("star.png"));
    arrowTurn90 = new ImageIcon(this.getClass().getClassLoader().getResource("arrow-turn-090.png"));
    arrowCircleDouble = new ImageIcon(this.getClass().getClassLoader().getResource("arrow-circle-double.png"));
    starPlus = new ImageIcon(this.getClass().getClassLoader().getResource("star--plus.png"));
    folderOpen = new ImageIcon(this.getClass().getClassLoader().getResource("folder-open.png"));
    folderZipper = new ImageIcon(this.getClass().getClassLoader().getResource("folder-zipper.png"));
    drive = new ImageIcon(this.getClass().getClassLoader().getResource("drive.png"));
    sambaShare = new ImageIcon(this.getClass().getClassLoader().getResource("document-share.png"));
    networkCloud = new ImageIcon(this.getClass().getClassLoader().getResource("network-cloud.png"));
    file = new ImageIcon(this.getClass().getClassLoader().getResource("file.png"));
    shortCut = new ImageIcon(this.getClass().getClassLoader().getResource("shortcut.png"));
    networkStatusAway = new ImageIcon(this.getClass().getClassLoader().getResource("network-status-away.png"));
    networkStatusOnline = new ImageIcon(this.getClass().getClassLoader().getResource("network-status.png"));
    networkStatusOffline = new ImageIcon(this.getClass().getClassLoader().getResource("network-status-offline.png"));
    jarIcon = new ImageIcon(this.getClass().getClassLoader().getResource("jar.png"));
    editSignature = new ImageIcon(this.getClass().getClassLoader().getResource("edit-signature.png"));
  }

  public ImageIcon getEditSignature() {
    return editSignature;
  }

  public static Icons getInstance() {
    if (instance == null) {
      synchronized (Icons.class) {
        if (instance == null) {
          instance = new Icons();
        }
      }
    }
    return instance;
  }

  public Icon getSambaShare() {
    return sambaShare;
  }

  public Icon getNetworkStatusAway() {
    return networkStatusAway;
  }

  public Icon getNetworkStatusOnline() {
    return networkStatusOnline;
  }

  public Icon getNetworkStatusOffline() {
    return networkStatusOffline;
  }

  public Icon getShortCut() {
    return shortCut;
  }

  public Icon getComputer() {
    return computer;
  }

  public Icon getMinusButton() {
    return minusButton;
  }

  public Icon getStar() {
    return star;
  }

  public Icon getFile() {
    return file;
  }

  public Icon getNetworkCloud() {
    return networkCloud;
  }

  public Icon getDrive() {
    return drive;
  }

  public Icon getFolderOpen() {
    return folderOpen;
  }

  public Icon getStarPlus() {
    return starPlus;
  }

  public Icon getArrowCircleDouble() {
    return arrowCircleDouble;
  }

  public Icon getArrowTurn90() {
    return arrowTurn90;
  }

  public Icon getFolderZipper() {
    return folderZipper;
  }

  public Icon getJarIcon() {
    return jarIcon;
  }
}
