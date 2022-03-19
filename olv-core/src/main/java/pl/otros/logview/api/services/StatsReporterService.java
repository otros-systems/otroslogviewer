package pl.otros.logview.api.services;

import java.util.Map;

public interface StatsReporterService {

  void sendStats(Map<String, Long> stats, String uuid, String olvVersion, String javaVersion);

}
