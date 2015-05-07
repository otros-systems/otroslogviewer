package pl.otros.logview.gui.message.json;

import com.google.common.base.Joiner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.gui.message.SubText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonFinderTest {


    private JsonFinder underTest = new JsonFinder();


    @DataProvider(name = "testFindJsonFragments")
    public Object[][] testFindJsonFragmentsDataProvider() {
        return new Object[][]{
                {"ala{ma{kota}a}kot", Collections.singletonList(new SubText(3, 14))},
                {"aasdfasf", new ArrayList<SubText>()},
                {"{a}", Collections.singletonList(new SubText(0, 3))},
                {"", new ArrayList<SubText>()},
                {"a{b{v{d}s}a", new ArrayList<SubText>()},
                {"av{ala}ma{kota}", Arrays.asList(new SubText(2,7),new SubText(9,15))}
        };
    }

    @Test(dataProvider = "testFindJsonFragments")
    public void testFindJsonFragments(String string, List<SubText> expected) throws Exception {
        //when
        final ArrayList<SubText> jsonFragments = underTest.findJsonFragments(string);

        //then
        Assert.assertEquals(jsonFragments,expected);
    }



    @DataProvider(name="testGetSubTexts")
    public Object[][] testGetSubTextsDataProvider(){
        return new Object[][] {
                {"0012100110", Arrays.asList(new SubText(2,6),new SubText(7,10))},
                {"000000000", new ArrayList<SubText>()},
                {"112221121", new ArrayList<SubText>()}
        };
    }

    @Test(dataProvider = "testGetSubTexts")
    public void testGetSubTexts(String openParenthesisString, List<SubText> expected) throws Exception {
        //given
        List<Integer> openParenthesis = new ArrayList<Integer>();
        for (int i = 0; i < openParenthesisString.length(); i++) {
            openParenthesis.add(Integer.parseInt(openParenthesisString.substring(i,i+1)));
        }

        //when
        final ArrayList<SubText> actual = underTest.getSubTexts(openParenthesis);

        //then
        Assert.assertEquals(actual,expected);

    }


    @DataProvider(name = "testCountParenthesis")
    public Object[][] testCountParenthesisDataProvider() {
        return new Object[][]{
                {"ala{ma{kota}a}kot", "00011122222110000"},
                {"a", "0"},
                {"{a}", "110"},
                {"", ""},
                {"a{b{v{d}s}a", "01122332211"},
                {"av\\\"{ala\"{ma}\"{kota}}", "000011111111112222210"}
        };
    }

    @Test(dataProvider = "testCountParenthesis")
    public void testCountParenthesis(String string, String result) throws Exception {
        //given
        result = result.replace(" ", "");

        //when
        final List<Integer> actual = underTest.countParenthesis(string);

        //then
        final String actualString = Joiner.on("").join(actual);
        Assert.assertEquals(actualString, result,string);
    }
}