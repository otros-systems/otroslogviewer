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
  public static RenamedLevel TRACE = new RenamedLevel("TRACE", Level.FINEST.intValue());
  public static RenamedLevel DEBUG = new RenamedLevel("DEBUG", Level.FINE.intValue());
  public static RenamedLevel WARN = new RenamedLevel("WARN", Level.WARNING.intValue());
  public static RenamedLevel ERROR = new RenamedLevel("ERROR", Level.SEVERE.intValue());
  public static RenamedLevel FATAL = new RenamedLevel("FATAL", Level.SEVERE.intValue());

  //Levels to show in LevelFilter
  public static RenamedLevel FINEST_TRACE = new RenamedLevel("FINEST / TRACE", Level.FINEST.intValue());
  //    public static RenamedLevel FINER = new RenamedLevel("FINER", Level.FINER.intValue());
  public static RenamedLevel FINE_DEBUG = new RenamedLevel("FINE / DEBUG", Level.FINE.intValue());
  //    public static RenamedLevel CONFIG = new RenamedLevel("CONFIG", Level.CONFIG.intValue());
//    public static RenamedLevel INFO = new RenamedLevel("INFO", Level.INFO.intValue());
  public static RenamedLevel WARNING_WARN = new RenamedLevel("WARNING / WARN", Level.WARNING.intValue());
  public static RenamedLevel SEVERE_ERROR_FATAL = new RenamedLevel("SEVERE / ERROR / FATAL", Level.SEVERE.intValue());

  public RenamedLevel(String name, int value) {
    super(name, value);
  }

  public boolean equals(Level level) {
    return intValue() == level.intValue();
  }
}
