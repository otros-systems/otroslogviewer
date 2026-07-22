package pl.otros.logview.gui.firstuse;

import pl.otros.logview.gui.GuiUtils;
import pl.otros.logview.gui.util.LookAndFeelUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class FirstTimeUseWizard {


  public void show(Window parent, Function<InitialConfiguration, Void> callback) {

    final JDialog wizard = new JDialog(parent, "OtrosLogViewer first use configuration");

    final WizardContext settings = new WizardContext();
    final List<FirstUsePage> pages = List.of(new LookAndFeelPage(), new LogPatternsPage());
    final int[] currentPage = {0};

    final JLabel title = new JLabel();
    Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    Border innerBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, title.getForeground());
    title.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

    final CardLayout cardLayout = new CardLayout();
    final JPanel cardsPanel = new JPanel(cardLayout);
    for (int i = 0; i < pages.size(); i++) {
      cardsPanel.add(pages.get(i).getView(), "page-" + i);
    }

    final JButton backButton = new JButton("Back");
    final JButton nextButton = new JButton("Next");
    final JButton finishButton = new JButton("Finish");
    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(backButton);
    buttons.add(nextButton);
    buttons.add(finishButton);

    final Runnable updateUi = () -> {
      FirstUsePage page = pages.get(currentPage[0]);
      title.setText(page.getTitle());
      title.setToolTipText(page.getDescription());
      backButton.setEnabled(currentPage[0] > 0);
      nextButton.setEnabled(currentPage[0] < pages.size() - 1);
      finishButton.setEnabled(currentPage[0] == pages.size() - 1);
      cardLayout.show(cardsPanel, "page-" + currentPage[0]);
      page.onPageShown(settings);
    };

    backButton.addActionListener(e -> {
      if (currentPage[0] > 0) {
        currentPage[0]--;
        updateUi.run();
      }
    });

    nextButton.addActionListener(e -> {
      FirstUsePage page = pages.get(currentPage[0]);
      if (page.onNext(settings) && currentPage[0] < pages.size() - 1) {
        currentPage[0]++;
        updateUi.run();
      }
    });

    finishButton.addActionListener(e -> {
      FirstUsePage page = pages.get(currentPage[0]);
      if (!page.onNext(settings)) {
        return;
      }
      wizard.setVisible(false);
      wizard.dispose();
      String lookAndFeelClassname = LookAndFeelUtil.checkSupportedLookAndFeelOrReturnDefault((String) settings.get(Config.LOOK_AND_FEEL));

      final Boolean checkForNewVersion = settings.containsKey(Config.CHECK_FOR_NEW_VERSION) ? (Boolean) settings.get(Config.CHECK_FOR_NEW_VERSION) : Boolean.TRUE;

      final Collection<LogPattern> logPatterns = ((LogPatterns) settings.get(Config.LOG_PATTERNS)).getLogPatterns();

      callback.apply(
        new InitialConfiguration(lookAndFeelClassname,
          logPatterns,
          checkForNewVersion
        )
      );
    });

    JPanel content = new JPanel(new BorderLayout());
    content.add(title, BorderLayout.NORTH);
    content.add(cardsPanel, BorderLayout.CENTER);
    content.add(buttons, BorderLayout.SOUTH);
    wizard.getContentPane().add(content);

    updateUi.run();
    wizard.setSize(800, 600);
    GuiUtils.centerOnScreen(wizard);
    wizard.setVisible(true);
  }

}
