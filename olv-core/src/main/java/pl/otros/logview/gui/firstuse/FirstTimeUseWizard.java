package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.*;
import pl.otros.logview.gui.GuiUtils;
import pl.otros.logview.gui.util.LookAndFeelUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class FirstTimeUseWizard {


  public void show(Window parent, Function<InitialConfiguration, Void> callback) {

    final JDialog wizard = new JDialog(parent, "OtrosLogViewer first use configuration");

    final WizardContainer wizardContainer = new WizardContainer(
      new PageFactory() {


        @Override
        public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
          switch (path.size()) {
            case 0:
              return new LookAndFeelPage();
            case 1:
              return new LogPatternsPage();
            default:
              throw new IllegalArgumentException("No page found, size = " + path.size());
          }

        }

        @Override
        public boolean isTransient(List<WizardPage> path, WizardSettings settings) {
          return true;
        }
      },
      new TitledPageTemplate(),
      new FlatWizardSettings()
    );
    wizardContainer.setCancelEnabled(false);
    wizardContainer.addWizardListener(new WizardListener() {
      @Override
      public void onPageChanging(WizardPage newPage, List<WizardPage> path) {
        //do nothing
      }

      @Override
      public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
        boolean last = newPage instanceof LogPatternsPage;
        newPage.getController().setFinishEnabled(last);
        newPage.getController().setNextEnabled(!last);
      }

      @Override
      public void onFinished(List<WizardPage> path, WizardSettings settings) {
        wizard.setVisible(false);
        wizard.dispose();
        String lookAndFeelClassname = LookAndFeelUtil.checkSupportedLookAndFeelOrReturnDefault((String) settings.get(Config.LOOK_AND_FEEL));

        final Boolean checkForNewVersion = settings.containsKey(Config.CHECK_FOR_NEW_VERSION)?(Boolean) settings.get(Config.CHECK_FOR_NEW_VERSION):Boolean.TRUE;

        final Collection<LogPattern> logPatterns = ((LogPatterns) settings.get(Config.LOG_PATTERNS)).getLogPatterns();

        callback.apply(
          new InitialConfiguration(lookAndFeelClassname,
            logPatterns,
            checkForNewVersion
          )
        );
      }

      @Override
      public void onCanceled(List<WizardPage> path, WizardSettings settings) {
        //nothing
      }
    });
    wizard.getContentPane().add(wizardContainer);
    wizard.setSize(800, 600);
    GuiUtils.centerOnScreen(wizard);
    wizard.setVisible(true);
  }

}
