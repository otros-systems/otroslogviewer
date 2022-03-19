package pl.otros.logview.api.services;

import pl.otros.logview.api.Ide;
import pl.otros.logview.api.model.LocationInfo;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class JumpToCodeServiceStatsWrapper implements JumpToCodeService {

  private JumpToCodeService jumpToCodeService;
  private StatsService statsService;

  public JumpToCodeServiceStatsWrapper(JumpToCodeService jumpToCodeService, StatsService statsService) {
    this.jumpToCodeService = jumpToCodeService;
    this.statsService = statsService;
  }

  @Override
  public void clearLocationCaches() {
    jumpToCodeService.clearLocationCaches();
  }

  @Override
  public boolean isIdeAvailable() {
    return jumpToCodeService.isIdeAvailable();
  }

  @Override
  public boolean isIdeAvailable(String host, int port) {
    return jumpToCodeService.isIdeAvailable(host, port);
  }

  @Override
  public Ide getIde() {
    return jumpToCodeService.getIde();
  }

  @Override
  public void jump(LocationInfo locationInfo) throws IOException {
    statsService.jumpToCodeExecuted();
    jumpToCodeService.jump(locationInfo);
  }

  @Override
  public boolean isJumpable(LocationInfo locationInfo) throws IOException {
    return jumpToCodeService.isJumpable(locationInfo);
  }

  @Override
  public String getContent(LocationInfo locationInfo) throws IOException {
    statsService.contentReadFromIde();
    return jumpToCodeService.getContent(locationInfo);
  }

  @Override
  public Set<Capabilities> capabilities() throws IOException {
    return jumpToCodeService.capabilities();
  }

  @Override
  public Set<String> loggerPatterns() throws IOException {
    return jumpToCodeService.loggerPatterns();
  }

  @Override
  public Optional<String> getContentOptional(LocationInfo locationInfo) {
    return jumpToCodeService.getContentOptional(locationInfo);
  }
}
