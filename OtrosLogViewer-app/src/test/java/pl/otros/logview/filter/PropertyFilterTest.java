package pl.otros.logview.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

@Test
public class PropertyFilterTest {

  enum Case {
    CaseIgnore, CaseSensitive
  }

  @DataProvider(name = "testAcceptDataProvider")
  public Object[][] testAcceptDataProvider() {
    LogData ld1 = new LogDataBuilder()
      .withClass("a.b.Class")
      .withMessage("Ala ma kota")
      .withDate(new Date())
      .build();

    Map<String, String> props = new HashMap<>();
    props.put("A", "Value");
    props.put("B", "Value");
    LogData ldWithProps = new LogDataBuilder()
      .withClass("a.b.Class")
      .withMessage("Ala ma kota")
      .withDate(new Date())
      .withProperties(props)
      .build();


    return new Object[][]{
      {ldWithProps, "A=B", Case.CaseIgnore, false},
      {ldWithProps, "A=ValuE", Case.CaseIgnore, true},
      {ldWithProps, "A=value", Case.CaseIgnore, true},
      {ldWithProps, "A=ValuE", Case.CaseSensitive, false},
      {ldWithProps, "A=Value", Case.CaseSensitive, true},

      {ldWithProps, "A~=Val", Case.CaseIgnore, true},
      {ldWithProps, "A~=val", Case.CaseIgnore, true},
      {ldWithProps, "A~=Val", Case.CaseSensitive, true},
      {ldWithProps, "A~=val", Case.CaseSensitive, false},

      {ldWithProps, "", Case.CaseIgnore, true},

      {ldWithProps, "A=", Case.CaseIgnore, true},
      {ldWithProps, "A~=", Case.CaseIgnore, true},
      {ldWithProps, "A=B", Case.CaseIgnore, false},

      {ldWithProps, "C", Case.CaseIgnore, false},
      {ldWithProps, "C=", Case.CaseIgnore, false},
      {ldWithProps, "C=B", Case.CaseIgnore, false},

      {ldWithProps, "Value", Case.CaseIgnore, true},
      {ldWithProps, "value", Case.CaseIgnore, true},
      {ldWithProps, "Value", Case.CaseSensitive, true},
      {ldWithProps, "value", Case.CaseSensitive, false},
    };
  }

  @Test(dataProvider = "testAcceptDataProvider")
  public void testAccept(LogData logData, String text, Case caseSensitive, boolean expected) {
    final PropertyFilter propertyFilter = new PropertyFilter();
    propertyFilter.setIgnoreCase(caseSensitive.equals(Case.CaseIgnore));
    propertyFilter.setFilteringText(text);
    propertyFilter.performPreFiltering();
    final boolean actual = propertyFilter.accept(logData, 0);
    assertEquals(expected, actual);
  }
}