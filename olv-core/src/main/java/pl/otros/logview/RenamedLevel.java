/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.otros.logview;

import java.util.logging.Level;

/**
 * @author Administrator
 */
public class RenamedLevel extends Level {
  //Log4J levels
  public static final Level TRACE = new RenamedLevel("TRACE", Level.FINEST.intValue());
  public static final Level DEBUG = new RenamedLevel("DEBUG", Level.FINE.intValue());
  public static final Level WARN = new RenamedLevel("WARN", Level.WARNING.intValue());
  public static final Level ERROR = new RenamedLevel("ERROR", Level.SEVERE.intValue());
  public static final Level FATAL = new RenamedLevel("FATAL", Level.SEVERE.intValue());

  //Levels to show in LevelFilter
  public static final Level FINEST_TRACE = new RenamedLevel("FINEST / TRACE", Level.FINEST.intValue());
  //    public static final Level FINER = new RenamedLevel("FINER", Level.FINER.intValue());
  public static final Level FINE_DEBUG = new RenamedLevel("FINE / DEBUG", Level.FINE.intValue());
  //    public static final Level CONFIG = new RenamedLevel("CONFIG", Level.CONFIG.intValue());
  //    public static final Level INFO = new RenamedLevel("INFO", Level.INFO.intValue());
  public static final Level WARNING_WARN = new RenamedLevel("WARNING / WARN", Level.WARNING.intValue());
  public static final Level SEVERE_ERROR_FATAL = new RenamedLevel("SEVERE / ERROR / FATAL", Level.SEVERE.intValue());

  public RenamedLevel(String name, int value) {
    super(name, value);
  }
}
