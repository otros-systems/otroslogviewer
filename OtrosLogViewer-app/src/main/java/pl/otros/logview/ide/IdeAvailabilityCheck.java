package pl.otros.logview.ide;

import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeAvailabilityCheck implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(IdeAvailabilityCheck.class.getName());
  private static final String ICE_DISCONNECTED_TOOLTIP = "<HTML>You can connect OtrosLogViewer with your IDE. <BR/> After clicking on stack trace element in OtrosLogViewer " +
                                                        "your IDE will open these class at selected location<BR/>" +
                                                        "<B>Introduction how to configure IDE you can find at OtrosLogViewer Wiki: //TODO URL</HTML>";
  private static final String IDE_CONNECTED_TOOLTIP ="<HTML>Your IDE is connected with OtrosLogViewer.</br> After clicking on stack trace element in OtrosLogViewer " +
                                                     "your IDE will open these class at selected location</HTML>" ;
  private JButton iconLabel;
  private JumpToCodeService jumpToCodeService;
  private volatile Ide lastIde = Ide.IDEA;
  private volatile Icon lastUnavailableIcon = lastIde.getIconDiscounted();
  private boolean lastTimeIdeAvailable = false;

  public IdeAvailabilityCheck(JButton iconLabel, JumpToCodeService jumpToCodeService) {
    this.iconLabel = iconLabel;
    this.jumpToCodeService = jumpToCodeService;
  }

  @Override
  public void run() {
    LOGGER.fine("Checking if IDE is available");
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

    LOGGER.fine("IDE is available: " + ideAvailable + ", current IDE is: " + ide);
    final Icon icon = ideAvailable?ide.getIconConnected():ide.getIconDiscounted();
    final String toolTip = ideAvailable?IDE_CONNECTED_TOOLTIP: ICE_DISCONNECTED_TOOLTIP;

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        iconLabel.setIcon(icon);
        iconLabel.setToolTipText(toolTip);
      }
    });
    } catch (Exception e){
      LOGGER.log(Level.SEVERE,"Exception when checking IDE",e);
    }

  }
}
