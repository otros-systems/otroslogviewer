package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.pagetemplates.DefaultPageTemplate;
import com.github.cjwizard.pagetemplates.PageTemplate;
import net.miginfocom.swing.MigLayout;
import pl.otros.swing.OtrosSwingUtils;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Simple PageTemplate that lists the WizardPage title at the top of each page
 * in the wizard and also use the description of the page as tool tip of the
 * title.
 *
 * @author rogue
 */
public class TitledPageTemplate extends PageTemplate {

  /**
   * Generated version uid.
   */
  private static final long serialVersionUID = -2282167921679786408L;

  /**
   * The label to display the current page description.
   */
  private final JLabel title = new JLabel();

  /**
   * We'll use the DefaultPageTemplate to take advantage of it's page switching
   * logic, since it doesn't include any additional decorations it is ideal
   * for nesting.
   */
  private final PageTemplate innerTemplate = new DefaultPageTemplate();

  /**
   * Constructor.  Sets up the inner template and the title label.
   */
  TitledPageTemplate() {
    // Create a simple empty border to impose a bit of space around the title:
    Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    // use an Matte border to add an underline:
    Border innerBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, title.getForeground());

    // combine the two borders to get the desired look:
    title.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
    OtrosSwingUtils.fontSize2(title);

    this.setLayout(new MigLayout("center"));
    this.add(title, "center, wrap");
    this.add(innerTemplate,"center");
  }

  @Override
  public void setPage(final WizardPage page) {
    // Since we're using a nested DefaultPageTemplate, we just need to
    // delegate to that object, and then do whatever is necessary to update
    // the additional widgets introduced by this PageTemplate.

    // The only trick is that we should make this thread-safe, since we aren't
    // guaranteed that this will be invoked from the EDT:
    SwingUtilities.invokeLater(() -> {
      // delegate to the DefaultPageTemplate:
      innerTemplate.setPage(page);

      // Set the new title text:
      title.setText(page.getTitle());
      title.setToolTipText(page.getDescription());
    });
  }

}
