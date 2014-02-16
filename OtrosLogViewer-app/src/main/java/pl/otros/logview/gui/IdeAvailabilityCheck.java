package pl.otros.logview.gui;

import pl.otros.logview.gui.services.jumptocdoe.JumpToCodeService;

import javax.swing.*;

class IdeAvailabilityCheck implements Runnable {
  private static final String IDE_DISCONNCTED_TOOLTIP = "<HTML>You can connect OtrosLogViewer with your IDE. </BR> After clicking on stack trace element in OtrosLogViewer " +
                                                        "your IDE will open these class at selected location</BR>" +
                                                        "<B>Introduction how to configure IDE you can find at OOtrosLogViewer Wiki: //TODO URL</HTML>";
  private static final String IDE_CONNECTED_TOOLTIP ="<HTML><p>Your IDE is connected with OtrosLogViewer.</P> After clicking on stack trace element in OtrosLogViewer " +
                                                     "your IDE will open these class at selected location" ;
  private JLabel iconLabel;
  private JumpToCodeService jumpToCodeService;
  private JumpToCodeService.IDE lastIde = JumpToCodeService.IDE.IDEA;

  IdeAvailabilityCheck(JLabel iconLabel, JumpToCodeService jumpToCodeService) {
    this.iconLabel = iconLabel;
    this.jumpToCodeService = jumpToCodeService;
  }

  @Override
  public void run() {
    boolean ideAvailable = jumpToCodeService.isIdeAvailable();
    JumpToCodeService.IDE ide = jumpToCodeService.getIde();
    if (ide!=null){
      lastIde=ide;
    } else {
      ide = lastIde;
    }
    final Icon icon = ideAvailable?ide.getIconConnected():ide.getIconDiscounted();
    final String toolTip = ideAvailable?IDE_CONNECTED_TOOLTIP:IDE_DISCONNCTED_TOOLTIP;
    //TODO set tooltip
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        iconLabel.setIcon(icon);
        iconLabel.setToolTipText(toolTip);
      }
    });
  }
}
