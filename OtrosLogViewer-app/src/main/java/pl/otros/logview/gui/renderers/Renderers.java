package pl.otros.logview.gui.renderers;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.OtrosApplication;

import java.text.SimpleDateFormat;

import static pl.otros.logview.gui.ConfKeys.*;

public class Renderers {

  public static Renderers instance;
  private final DateRenderer dateRenderer;
  private ClassWrapperRenderer classWrapperRenderer;
  private LevelRenderer levelRenderer;

  public Renderers(OtrosApplication otrosApplication) {
    final DataConfiguration configuration = otrosApplication.getConfiguration();
    classWrapperRenderer = new ClassWrapperRenderer();
    classWrapperRenderer.reloadConfiguration(configuration.getString(LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS));
    levelRenderer = new LevelRenderer(configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly));
    dateRenderer = new DateRenderer(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT, "HH:mm:ss.SSS"));
    configuration.addConfigurationListener(new ConfigurationListener() {
      @Override
      public void configurationChanged(ConfigurationEvent event) {
        if (event.isBeforeUpdate()) {
          return;
        }
        final String property = event.getPropertyName();
        final String value = event.getPropertyValue().toString();
        if (property.equals(LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS)) {
          classWrapperRenderer.reloadConfiguration(value);
        } else if (property.equals(LOG_TABLE_FORMAT_LEVEL_RENDERER)) {
          final LevelRenderer.Mode mode = configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly);
          levelRenderer.setMode(mode);
        } else if (property.equals(LOG_TABLE_FORMAT_DATE_FORMAT)) {
          dateRenderer.setDateFormatter(new SimpleDateFormat(value));
        }
      }
    });
  }

  public static Renderers getInstance(OtrosApplication otrosApplication) {
    if (instance == null) {
      synchronized (Renderers.class) {
        if (instance == null) {
          instance = new Renderers(otrosApplication);
        }
      }
    }
    return instance;
  }

  public DateRenderer getDateRenderer() {
    return dateRenderer;
  }

  public ClassWrapperRenderer getClassWrapperRenderer() {
    return classWrapperRenderer;
  }

  public LevelRenderer getLevelRenderer() {
    return levelRenderer;
  }
}
