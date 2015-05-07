package pl.otros.logview.gui.message.json;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JsonMessageFormatterTest {

    private String formatted4x = "tickets {\n" +
            "    \"tickets\": {\n" +
            "        \"city\": \"Sacramento\",\n" +
            "        \"date\": \"2014-10-23\"\n" +
            "    }\n" +
            "}";
    private String singleLine = "tickets {\"tickets\":{\"city\":\"Sacramento\",\"date\":\"2014-10-23\"}}";
    private String format2x = "tickets \n{\"tickets\": {\n" +
            "  \"city\": \"Sacramento\",\n" +
            "  \"date\": \"2014-10-23\"\n" +
            "  }}";

    private String _2jsons = "tickets {\"tickets\":{\"city\":\"Sacramento\",\"date\":\"2014-10-23\"}} tickets {\"tickets\":{\"city\":\"Sacramento\",\"date\":\"2014-10-23\"}} something";
    private String _2JsonsFormatted = "tickets \n" +
            "{\"tickets\": {\n" +
            "  \"city\": \"Sacramento\",\n" +
            "  \"date\": \"2014-10-23\"\n" +
            "  }}\n" +
            " tickets \n" +
            "{\"tickets\": {\n" +
            "  \"city\": \"Sacramento\",\n" +
            "  \"date\": \"2014-10-23\"\n" +
            "  }}\n" +
            " something";


    private JsonMessageFormatter underTest;

    @BeforeMethod
    public void before(){
        underTest = new JsonMessageFormatter();
    }

    @DataProvider(name = "formattingNeeded")
    public Object[][] formattingNeededDataProvider() {
        return new Object[][]{
                {singleLine, true, "Single line with JSON"},
                {formatted4x, true, "Formatted with 4x"},
                {format2x, true, "Formatted with 2x"},
                {"some message",false,"Without json" }
        };
    }


    @Test(dataProvider = "formattingNeeded")
    public void testFormattingNeeded(String input,boolean expected, String message) throws Exception {
        Assert.assertEquals(underTest.formattingNeeded(input),expected,message);
    }


    @DataProvider(name = "formatData")
    public Object[][] formatDataProvider() {
        return new Object[][]{
                {singleLine, format2x, "Single line to formatted"},
                {formatted4x, format2x, "Formatted with 4x spaces to 2x"},
                {format2x, format2x, "Formatted with 2x spaces to 2x"},
                {_2jsons, _2JsonsFormatted, "2 JSON's in one message"}
        };
    }

    @Test(dataProvider = "formatData")
    public void testFormat(String input, String expected, String description) throws Exception {
        final String format = underTest.format(input);
        System.out.println("formatted:\n" + format);

        Assert.assertEquals(format, expected, description);
    }
}