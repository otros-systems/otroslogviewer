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
      new Object[]{"level", QuerySuggestionSource.ExpectedType.OPERATOR},
      new Object[]{"mark ==", QuerySuggestionSource.ExpectedType.FIELD},
    };
  }

  @Test(dataProvider = "getExpectedTypeProvider")
  public void testGetExpectedType(String query, QuerySuggestionSource.ExpectedType expectedType) throws Exception {
    Assert.assertEquals(underTest.getExpectedType(query),expectedType);
  }


  @DataProvider(name = "getLastNotFinishedCondition")
  public Object[][] getLastNotFinishedConditionDataProvider(){
    return new Object[][]{
      new Object[]{"lev", "lev"},
      new Object[]{"level", "level"},
      new Object[]{"level>", "level>"},
      new Object[]{"level >", "level >"},//date<'2012-02-22 19:35:43
      new Object[]{"level>INFO", ""},
      new Object[]{"level>INFO &&", ""},
      new Object[]{"level>INFO && ", ""},
      new Object[]{"level>INFO && mess", "mess"},
      new Object[]{"level>'INFO' && mess", "mess"},
      new Object[]{"level>\"INFO\" && mess", "mess"},
      new Object[]{"level>INFO && message", "message"},
      new Object[]{"level>INFO && message ~= ala", ""},
      new Object[]{"level>INFO && message ", "message "},
      new Object[]{"level>INFO && message ~=", "message ~="},
      new Object[]{"level>INFO && message ~= ", "message ~= "},
      new Object[]{"(level>INFO || level<ERROR) &&", ""},
      new Object[]{"(level>INFO || level<ERROR) && mes", "mes"},
      new Object[]{"(level>INFO || level<ERROR) && message", "message"},
      new Object[]{"(level>INFO || level<ERROR) && message~=", "message~="},
      new Object[]{"(level>INFO || level<ERROR) && message~=boom", ""},
    };
  }


  @Test(dataProvider = "getLastNotFinishedCondition")
  public void testGetLastNotFinishedCondition(String query, String rest){
    Assert.assertEquals(underTest.getLastNotFinishedCondition(query),rest);
  }

  @DataProvider(name = "balance")
  public Object[][] balanceDataProvider(){
    return new Object[][]{
      new Object[]{"",0},
      new Object[]{"(",1},
      new Object[]{"(lev=",1},
      new Object[]{"(level>INFO || level<ERROR) &&",0},
      new Object[]{"(level>INFO || (level<ERROR)) &&",0},
      new Object[]{"(level>INFO || (level<ERROR)",1},
    };
  }

  @Test(dataProvider = "balance")
  public void testCountParenthesisBalance(String s, int expected){
    Assert.assertEquals(underTest.countParenthesisBalance(s),expected);
  }
}