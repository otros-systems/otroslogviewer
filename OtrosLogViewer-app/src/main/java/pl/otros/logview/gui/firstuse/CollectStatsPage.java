package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.WizardPage;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class CollectStatsPage extends WizardPage {

  private static final String EXAMPLE_STATS = "action:pl.otros.logview.gui.actions.AdvanceOpenAction.executed                           =87\n" +
    "action:pl.otros.logview.gui.actions.CheckForNewVersionOnStartupAction.executed           =119\n" +
    "action:pl.otros.logview.gui.actions.ClearLogTableAction.executed                         =1\n" +
    "action:pl.otros.logview.gui.actions.ClearMarkingsAction.executed                         =1\n" +
    "action:pl.otros.logview.gui.actions.ConvertLogbackLog4jPatternAction.executed            =4\n" +
    "action:pl.otros.logview.gui.actions.ExitAction.executed                                  =1\n" +
    "action:pl.otros.logview.gui.actions.FocusOnEventsBefore.executed                         =1\n" +
    "action:pl.otros.logview.gui.actions.FocusOnThisThreadAction.executed                     =4\n" +
    "action:pl.otros.logview.gui.actions.FontSize.executed                                    =1\n" +
    "action:pl.otros.logview.gui.actions.JumpToMarkedAction.executed                          =2\n" +
    "action:pl.otros.logview.gui.actions.MarkAllFoundAction.executed                          =1\n" +
    "action:pl.otros.logview.gui.actions.MarkRowAction.executed                               =1\n" +
    "action:pl.otros.logview.gui.actions.OpenPreferencesAction.executed                       =85\n" +
    "action:pl.otros.logview.gui.actions.ParseClipboard.executed                              =13\n" +
    "action:pl.otros.logview.gui.actions.SearchByLevel.executed                               =10\n" +
    "action:pl.otros.logview.gui.actions.ShowLoadedPlugins.executed                           =1\n" +
    "action:pl.otros.logview.gui.actions.ShowLogPatternParserEditor.executed                  =7\n" +
    "action:pl.otros.logview.gui.actions.ShowMessageColorizerEditor.executed                  =14\n" +
    "action:pl.otros.logview.gui.actions.ShowOlvLogs.executed                                 =1\n" +
    "action:pl.otros.logview.gui.actions.ShowStats.executed                                   =4\n" +
    "action:pl.otros.logview.gui.actions.SwitchAutoJump.executed                              =1\n" +
    "action:pl.otros.logview.gui.actions.TailLogWithAutoDetectActionListener.executed         =66\n" +
    "action:pl.otros.logview.gui.actions.read.DragAndDropFilesHandler$OpenLogUsingDnd.executed=3\n" +
    "action:pl.otros.logview.gui.actions.search.SearchAction.executed                         =95\n" +
    "action:pl.otros.logview.ide.IdeIntegrationConfigAction.executed                          =3\n" +
    "imported:filesToView:1                                                                   =63\n" +
    "imported:filesToView:2                                                                   =19\n" +
    "imported:scheme:file                                                                     =165\n" +
    "imported:scheme:sftp                                                                     =3\n" +
    "io:importedLogEvents.file                                                                =3330628\n" +
    "io:importedLogEvents.sftp                                                                =230\n" +
    "io:parsedBytes.file                                                                      =1228178455\n" +
    "io:parsedBytes.sftp                                                                      =18756\n" +
    "logParser:pl.otros.logview.importer.DetectOnTheFlyLogImporter.used                       =81\n" +
    "logParser:pl.otros.logview.importer.UtilLoggingXmlLogImporter.used                       =81\n" +
    "logParser:pl.otros.logview.importer.log4jxml.Log4jXmlLogImporter.used                    =2\n" +
    "logParser:pl.otros.logview.parser.json.log4j2.Log4j2JsonLogParser.used                   =1\n" +
    "logParser:pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser.used              =3";

  CollectStatsPage() {
    super("Send anonymous usage data", "Sends anonymous statistic");
    this.setLayout(new MigLayout());

    final JCheckBox sendAnonymousStatsData = new JCheckBox("Send anonymous stats data", true);
    sendAnonymousStatsData.setName(Config.COLLECT_STATS);

    final JCheckBox checkVersionOnStartup = new JCheckBox("Check for new version on startup", true);
    checkVersionOnStartup.setName(Config.CHECK_FOR_NEW_VERSION);

    final JTextArea textArea = new JTextArea(EXAMPLE_STATS);
    textArea.setEditable(false);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize()));
    final JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(BorderFactory.createTitledBorder("Example stats"));
    this.add(checkVersionOnStartup, "wrap,left");
    this.add(sendAnonymousStatsData, "wrap, left");
    this.add(scrollPane);
  }
}
