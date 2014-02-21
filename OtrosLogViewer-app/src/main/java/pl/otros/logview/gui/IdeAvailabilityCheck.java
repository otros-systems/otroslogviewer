package pl.otros.logview.gui;

import pl.otros.logview.gui.services.jumptocdoe.JumpToCodeService;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class IdeAvailabilityCheck implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(IdeAvailabilityCheck.class.getName());
  private static final String IDE_DISCONNCTED_TOOLTIP = "<HTML>You can connect OtrosLogViewer with your IDE. </BR> After clicking on stack trace element in OtrosLogViewer " +
                                                        "your IDE will open these class at selected location</BR>" +
                                                        "<B>Introduction how to configure IDE you can find at OtrosLogViewer Wiki: //TODO URL</HTML>";
  private static final String IDE_CONNECTED_TOOLTIP ="<HTML><p>Your IDE is connected with OtrosLogViewer.</P> After clicking on stack trace element in OtrosLogViewer " +
                                                     "your IDE will open these class at selected location</HTML>" ;
  private JLabel iconLabel;
  private JumpToCodeService jumpToCodeService;
  private volatile JumpToCodeService.IDE lastIde = JumpToCodeService.IDE.IDEA;
  private volatile Icon lastUnavailableIcon = lastIde.getIconDiscounted();
  private boolean lastTimeIdeAvailable = false;

  IdeAvailabilityCheck(JLabel iconLabel, JumpToCodeService jumpToCodeService) {
    this.iconLabel = iconLabel;
    this.jumpToCodeService = jumpToCodeService;
  }

  @Override
  public void run() {
    LOGGER.fine("Checking if IDE is available");
    boolean ideAvailable = jumpToCodeService.isIdeAvailable();
    JumpToCodeService.IDE ide = jumpToCodeService.getIde();
    if (ide!=null && !ide.equals(JumpToCodeService.IDE.DISONECTED)){
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
    final String toolTip = ideAvailable?IDE_CONNECTED_TOOLTIP:IDE_DISCONNCTED_TOOLTIP;

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
