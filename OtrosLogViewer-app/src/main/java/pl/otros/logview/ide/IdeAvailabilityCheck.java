package pl.otros.logview.ide;

import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdeAvailabilityCheck implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(IdeAvailabilityCheck.class.getName());
  private static final String ICE_DISCONNECTED_TOOLTIP = "<HTML>OtrosLogViewer can't connect with your IDE.<BR/>Click to configure</HTML>";
  private static final String IDE_CONNECTED_TOOLTIP ="<HTML>OtrosLogViewer is connected with your IDE.<BR/>Click to configure</HTML>";
  private final JButton iconLabel;
  private final JumpToCodeService jumpToCodeService;
  private volatile Ide lastIde = Ide.IDEA;
  private boolean lastTimeIdeAvailable = false;

  public IdeAvailabilityCheck(JButton iconLabel, JumpToCodeService jumpToCodeService) {
    this.iconLabel = iconLabel;
    this.jumpToCodeService = jumpToCodeService;
  }

  @Override
  public void run() {
    LOGGER.debug("Checking if IDE is available");
    boolean ideAvailable = jumpToCodeService.isIdeAvailable();
    Ide ide = jumpToCodeService.getIde();
    if (ide!=null && !ide.equals(Ide.DISCONNECTED)){
      lastIde=ide;
    } else {
      ide = lastIde;
    }
    if (!lastTimeIdeAvailable && ideAvailable){
      jumpToCodeService.clearLocationCaches();
    }
    lastTimeIdeAvailable = ideAvailable;
    try {

    LOGGER.debug("IDE is available: " + ideAvailable + ", current IDE is: " + ide);
    final Icon icon = ideAvailable?ide.getIconConnected():ide.getIconDiscounted();
    final String toolTip = ideAvailable?IDE_CONNECTED_TOOLTIP: ICE_DISCONNECTED_TOOLTIP;

    SwingUtilities.invokeLater(() -> {
      iconLabel.setIcon(icon);
      iconLabel.setToolTipText(toolTip);
    });
    } catch (Exception e){
      LOGGER.error("Exception when checking IDE",e);
    }

  }
}
