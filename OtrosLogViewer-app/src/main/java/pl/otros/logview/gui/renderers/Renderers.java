package pl.otros.logview.gui.renderers;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.table.TableCellRenderer;
import java.text.SimpleDateFormat;

import static pl.otros.logview.gui.ConfKeys.*;

public class Renderers {

  public static Renderers instance;
  private final DateRenderer dateRenderer;
  private final ClassWrapperRenderer classWrapperRenderer;
  private final LevelRenderer levelRenderer;
  private final StringRenderer stringRenderer;

  private Renderers(OtrosApplication otrosApplication) {
    final DataConfiguration configuration = otrosApplication.getConfiguration();
    classWrapperRenderer = new ClassWrapperRenderer();
    classWrapperRenderer.reloadConfiguration(configuration.getString(LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS, StringUtils.EMPTY));
    levelRenderer = new LevelRenderer(configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly));
    dateRenderer = new DateRenderer(configuration.getString(ConfKeys.LOG_TABLE_FORMAT_DATE_FORMAT, "HH:mm:ss.SSS"));
    stringRenderer = new StringRenderer();
    configuration.addConfigurationListener(event -> {
      if (event.isBeforeUpdate()) {
        return;
      }
      final String property = event.getPropertyName();
      final String value = event.getPropertyValue()!=null?event.getPropertyValue().toString():"";
      switch (property) {
        case LOG_TABLE_FORMAT_PACKAGE_ABBREVIATIONS:
          classWrapperRenderer.reloadConfiguration(value);
          break;
        case LOG_TABLE_FORMAT_LEVEL_RENDERER:
          final LevelRenderer.Mode mode = configuration.get(LevelRenderer.Mode.class, ConfKeys.LOG_TABLE_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly);
          levelRenderer.setMode(mode);
          break;
        case LOG_TABLE_FORMAT_DATE_FORMAT:
          dateRenderer.setDateFormatter(new SimpleDateFormat(value));
          break;
        default:
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

  public TableCellRenderer getStringRenderer() {
    return stringRenderer;
  }
}
