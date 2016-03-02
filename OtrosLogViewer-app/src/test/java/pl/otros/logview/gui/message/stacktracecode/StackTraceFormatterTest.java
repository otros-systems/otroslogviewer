package pl.otros.logview.gui.message.stacktracecode;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

public class StackTraceFormatterTest {

    String message = "java.util.concurrent.ExecutionException: java.io.IOException: Error executing request, connection broken.... :)\n" +
            "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n" +
            "\tat test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)\n" +
            "\tat test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)\n" +
            "Caused by: java.io.IOException: Error executing request, connection broken.... :)\n" +
            "\tat test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)\n" +
            "\tat test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$9/1549863774.call(Unknown Source)\n" +
            "\tat test.sampleapp.executors.MdcCallableWrapper.call(MdcCallableWrapper.java:32)\n" +
            "\t... 4 more";
    String expected = "java.util.concurrent.ExecutionException: java.io.IOException: Error executing request, connection broken.... :)\n" +
            "  at java.util.concurrent.FutureTask.report(FutureTask.java:122)\t //code\n" +
            "  at test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)\t //code\n" +
            "  at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)\n" +
            "Caused by: java.io.IOException: Error executing request, connection broken.... :)\n" +
            "  at test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)\t //code\n" +
            "  at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$9/1549863774.call(Unknown Source)\n" +
            "  at test.sampleapp.executors.MdcCallableWrapper.call(MdcCallableWrapper.java:32)\t //code\n" +
            "\t... 4 more";

    @Test
    public void testFormat() throws Exception {
        //given
        final JumpToCodeService mock = Mockito.mock(JumpToCodeService.class);
        Mockito.when(mock.getContentOptional(Mockito.any(LocationInfo.class))).thenAnswer(invocation -> {
            LocationInfo li = (LocationInfo) invocation.getArguments()[0];
            return li.getLineNumber().map(l->l + ": code");
        });
        final StackTraceFormatter formatter = new StackTraceFormatter(mock);

        //when
        final String format = formatter.format(message);

        //then
        Assert.assertEquals(format, expected);

    }
}