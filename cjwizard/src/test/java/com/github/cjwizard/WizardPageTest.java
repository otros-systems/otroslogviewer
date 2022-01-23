/**
 *
 */
package com.github.cjwizard;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author rogue
 *
 */
public class WizardPageTest {

    private static class CustomTestComponent extends JPanel implements CustomWizardComponent {

        private static final long serialVersionUID = -6865024622146347310L;

        private String value;

        /* (non-Javadoc)
         * @see com.github.cjwizard.CustomWizardComponent#getValue()
         */
        @Override
        public Object getValue() {
            return value;
        }

        /* (non-Javadoc)
         * @see com.github.cjwizard.CustomWizardComponent#setValue(java.lang.Object)
         */
        @Override
        public void setValue(Object o) {
            this.value = o.toString();
        }
    }

    private static class TestWizardPage extends WizardPage {

        private static final long serialVersionUID = -4774100981074019102L;

        /**
         * Make a basic {@link WizardPage} to test with it.
         * @param title The title of the page.
         * @param description The description of the page.
         */
        public TestWizardPage(String title, String description) {
            super(title, description);

            JTextField field = new JTextField();
            field.setName("test field");
            field.setText("some text");

            JCheckBox box = new JCheckBox("check box");
            box.setName("test box");

            CustomTestComponent ctc = new CustomTestComponent();
            ctc.setName("custom comp");
            ctc.setValue("sample value");

            JFormattedTextField formated = new JFormattedTextField(
                    NumberFormat.getIntegerInstance());
            formated.setName("formatted field");
            formated.setValue(42L);

            add(new JLabel("test label"));

            add(field);
            add(box);
            add(ctc);
            add(formated);
        }

    }

    /**
     * Verifies that widgets added to a WizardPage with names are located and
     * accessed properly.
     *
     * Test method for {@link com.github.cjwizard.WizardPage#getNamedComponents()}.
     */
    @Test
    public void testGetNamedComponents_countComponents() {
        WizardPage page = new TestWizardPage("Test Page", "");

        Set<Component> components = page.getNamedComponents();

        Assert.assertEquals(components.size(), 4, "wrong number of named components");
    }

    /**
     * Verifies that widgets added to a WizardPage with names are located and
     * accessed properly.
     *
     * Test method for {@link com.github.cjwizard.WizardPage#updateSettings(com.github.cjwizard.WizardSettings)}.
     */
    @Test
    public void testUpdateSettings_keysExist() {
        WizardPage page = new TestWizardPage("Test Page", "");

        WizardSettings settings = new StackWizardSettings();
        page.updateSettings(settings);

        Assert.assertTrue(settings.keySet().contains("test box"), "Key set did not contain needed key.");
        Assert.assertTrue(settings.keySet().contains("test field"), "Key set did not contain needed key.");
        Assert.assertTrue(settings.keySet().contains("custom comp"), "Key set did not contain needed key.");
        Assert.assertTrue(settings.keySet().contains("formatted field"), "Key set did not contain needed key.");
    }

    /**
     * Verifies that widgets added to a WizardPage with names are located and
     * accessed properly.
     *
     * Test method for {@link com.github.cjwizard.WizardPage#updateSettings(com.github.cjwizard.WizardSettings)}.
     */
    @Test
    public void testUpdateSettings_getValues() {
        WizardPage page = new TestWizardPage("Test Page", "");

        WizardSettings settings = new StackWizardSettings();
        page.updateSettings(settings);


        boolean checkVal = (Boolean) settings.get("test box");
        Assert.assertFalse(checkVal, "Wrong value.");

        String fieldVal = (String) settings.get("test field");
        Assert.assertEquals(fieldVal, "some text", "Wrong value.");

        String ctcValue = (String) settings.get("custom comp");

        Assert.assertEquals(ctcValue, "sample value", "Wrong value.");

        Long formatVal = (Long) settings.get("formated field");
        Assert.assertEquals(formatVal, Long.valueOf(42L), "Wrong value.");

    }
}
