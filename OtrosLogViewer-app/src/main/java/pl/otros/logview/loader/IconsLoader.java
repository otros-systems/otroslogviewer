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
package pl.otros.logview.loader;

import pl.otros.logview.gui.Icons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconsLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(IconsLoader.class.getName());

  public static void loadIcons() {
    Field[] fields = Icons.class.getFields();
    for (Field field : fields) {
      Path annotation = field.getAnnotation(Path.class);
      InputStream stream = IconsLoader.class.getResourceAsStream(annotation.path());
      if (stream != null) {
        try {
          ImageIcon imageIcon;
          Image image = ImageIO.read(stream);
          if (annotation.gray()) {
            image = GrayFilter.createDisabledImage(image);
          }
          imageIcon = new ImageIcon(image);
          field.set(null, imageIcon);
          LOGGER.debug("Icon " + annotation.path() + " for field " + field.getName() + " loaded");
        } catch (Exception e) {
          LOGGER.error("Can't load icon " + annotation.path() + " for field " + field.getName());
        }
      }
    }
  }

}
