package pl.otros.logview.gui.message.stacktracecode;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import pl.otros.logview.api.Ide;
import pl.otros.logview.api.model.LocationInfo;
import pl.otros.logview.api.services.JumpToCodeService;

import java.io.IOException;
import java.util.Set;

public class StackTraceFormatterTest {

    @Test
    public void testFormat() throws Exception {
        //given
        final JumpToCodeService jumpToCodeService = new JumpToCodeService() {
            @Override
            public void clearLocationCaches() {

            }

            @Override
            public boolean isIdeAvailable() {
                return false;
            }

            @Override
            public boolean isIdeAvailable(String host, int port) {
                return false;
            }

            @Override
            public Ide getIde() {
                return null;
            }

            @Override
            public void jump(LocationInfo locationInfo) throws IOException {

            }

            @Override
            public boolean isJumpable(LocationInfo locationInfo) throws IOException {
                return false;
            }

            @Override
            public String getContent(LocationInfo locationInfo) throws IOException {
                return locationInfo.getLineNumber().map(l->l + ": code").get();
            }

            @Override
            public Set<Capabilities> capabilities() throws IOException {
                return Set.of();
            }

            @Override
            public Set<String> loggerPatterns() throws IOException {
                return Set.of();
            }
        };

        final StackTraceFormatter formatter = new StackTraceFormatter(jumpToCodeService);

        //when
        String message = "java.util.concurrent.ExecutionException: java.io.IOException: Error executing request, connection broken.... :)\n" +
          "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n" +
          "\tat test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)\n" +
          "\tat test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)\n" +
          "Caused by: java.io.IOException: Error executing request, connection broken.... :)\n" +
          "\tat test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)\n" +
          "\tat test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$9/1549863774.call(Unknown Source)\n" +
          "\tat test.sampleapp.executors.MdcCallableWrapper.call(MdcCallableWrapper.java:32)\n" +
          "\t... 4 more";
        final String format = formatter.format(message);

        //then
        String expected = "java.util.concurrent.ExecutionException: java.io.IOException: Error executing request, connection broken.... :)\n" +
          "  at java.util.concurrent.FutureTask.report(FutureTask.java:122)\t //code\n" +
          "  at test.sampleapp.SampleAppMultiThreadedFix2.lambda$performRequests$16(SampleAppMultiThreadedFix2.java:36)\t //code\n" +
          "  at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$16/2016207428.accept(Unknown Source)\n" +
          "Caused by: java.io.IOException: Error executing request, connection broken.... :)\n" +
          "  at test.sampleapp.services.hotels.HotelsService.getHotels(HotelsService.java:30)\t //code\n" +
          "  at test.sampleapp.SampleAppMultiThreadedFix2$$Lambda$9/1549863774.call(Unknown Source)\n" +
          "  at test.sampleapp.executors.MdcCallableWrapper.call(MdcCallableWrapper.java:32)\t //code\n" +
          "\t... 4 more";
        Assert.assertEquals(format, expected);

    }
}