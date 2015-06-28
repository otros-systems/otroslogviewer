package pl.otros.logview.accept.query.org.apache.log4j.suggestion;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class QuerySuggestionSourceTest {

  QuerySuggestionSource underTest =new QuerySuggestionSource(new ArrayList<>(0));

  @Test
  public void testGetSuggestions() throws Exception {
    Assert.fail("Not implemented");
  }


//  Examples:
//    * date<'2012-02-22 19:35:43' -> events before 2012-02-22 19:35:43
//    * (date>'2012-02-22 19:35:43' || msg~=import) && !(class~=Parser) -> events after 2012-02-22 19:35:43 or message contains string "import", but class name do not contains string Parser
//  * mark==true -> marked events
//  * mark==Aqua -> marked with color Aqua
  @DataProvider(name = "getExpectedTypeProvider")
  public Object[][] getExpectedTypeProvider() {
    return new Object[][]{
      new Object[]{"level>INFO && message ~= ala", QuerySuggestionSource.ExpectedType.FIELD},
      new Object[]{"", QuerySuggestionSource.ExpectedType.FIELD},
      new Object[]{"level", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"level ", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"level >", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"level <", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"level >", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"level == ", QuerySuggestionSource.ExpectedType.VALUE_LEVEL},
      new Object[]{"level ==", QuerySuggestionSource.ExpectedType.VALUE_LEVEL},
      new Object[]{"message == ", QuerySuggestionSource.ExpectedType.VALUE_MSG},
      new Object[]{"message ~", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"date >=", QuerySuggestionSource.ExpectedType.VALUE_DATE},
      new Object[]{"date >= ", QuerySuggestionSource.ExpectedType.VALUE_DATE},
      new Object[]{"level", QuerySuggestionSource.ExpectedType.FIELD},
      new Object[]{"mark ==", QuerySuggestionSource.ExpectedType.FIELD},
    };
  }

  @Test(dataProvider = "getExpectedTypeProvider")
  public void testGetExpectedType(String query, QuerySuggestionSource.ExpectedType expectedType) throws Exception {
    Assert.assertEquals(underTest.getExpectedType(query),expectedType);
  }
}