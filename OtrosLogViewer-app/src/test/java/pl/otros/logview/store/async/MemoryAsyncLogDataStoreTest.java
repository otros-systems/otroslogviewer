package pl.otros.logview.store.async;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.*;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import static org.junit.Assert.*;

@Ignore
public class MemoryAsyncLogDataStoreTest {

  public static final int LOG_EVENT_COUNT = 1000;
  public static final String TEST_THREAD_POOL_NAME = "TestThreadPoolName";
  MemoryAsyncLogDataStore store;
  private String[] classes = new String[]{
      "com.package.p1.Class",
      "com.package.p1.Dao",
      "com.package.p1.Bean",
      "com.package.p2.ExtraBean",
      "com.package.p2.Class",
  };
  private String[] threads = new String[]{
      "t1",
      "t3",
      "tsfsa",
      "tsdf",
      "tsf",
  };

  @Before
  public void setUp() throws Exception {
    ExecutorService executorService =
        Executors.newSingleThreadExecutor(new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
            return new Thread(r, TEST_THREAD_POOL_NAME);
          }
        });
    ListeningExecutorService service = MoreExecutors.listeningDecorator(executorService);
    final MemoryLogDataStore memorylogDataStore = new MemoryLogDataStore();
    LogDataStore logDataStore = (LogDataStore) Proxy.newProxyInstance(memorylogDataStore.getClass().getClassLoader(),
        new Class[]{LogDataStore.class}, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String logStoreThreadName = Thread.currentThread().getName();
        Assert.assertEquals("Async operation was performed out of designed thread pool", "TestThreadPoolName", logStoreThreadName);
        return method.invoke(memorylogDataStore, args);
      }
    });


    store = new MemoryAsyncLogDataStore(service, logDataStore);
    for (int i = 0; i < LOG_EVENT_COUNT; i++) {
      LogDataBuilder builder = new LogDataBuilder();
      builder.withDate(new Date(i * 1000 * 60 * 60l));
      builder.withLevel(Level.INFO);
      builder.withClass(classes[i % classes.length]);
      builder.withMessage("My message " + i);
      builder.withThread(threads[i % threads.length]);
      builder.withId(i);
      builder.withNote(new Note("Note" + i));
      if (i % 10 == 0) {
        builder.withMarked(true).withMarkerColors(MarkerColors.Aqua);
      }
      store.add(builder.build());
    }

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetCount() throws Exception {
    Assert.assertEquals(LOG_EVENT_COUNT, store.getCount());

  }

  @Test
  public void testRemove() throws Exception {
    //given
    LogData logDataRow1 = store.getLogData(11);

    //when
    store.remove(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22, 23).get();

    //then
//    Assert.assertEquals(logDataRow1, store.getLogData(0));
    Assert.assertEquals(10, store.getLogData(0).getId());
    Assert.assertEquals(24, store.getLogData(10).getId());
    Assert.assertEquals(986, store.getCount());

  }

  @Test
  public void testFilter() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testSearch() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testGetLogData() throws Exception {
    LogData logData = store.getLogData(0);

    Assert.assertEquals(0, logData.getDate().getTime());
    Assert.assertEquals(classes[0], logData.getClazz());
  }

  @Test
  public void testGetLogDataIdInRow() throws Exception {
    //given
    //when
    //then
    for (int i = 0; i < LOG_EVENT_COUNT; i++) {
      assertEquals(Integer.valueOf(i), store.getLogDataIdInRow(i));
    }
  }

  @Test
  public void testGetLimit() throws Exception {
    store.setLimit(100);
    Assert.assertEquals(100, store.getLimit());
  }

  @Test
  public void testSetLimit() throws Exception {
    store.setLimit(100);
    Assert.assertEquals(100, store.getCount());
  }

  @Test
  public void testAdd() throws Exception {
    String testMessage = "testAdd";
    LogData ld = new LogDataBuilder().withDate(new Date()).withMessage(testMessage).build();
    store.add(ld);
    Assert.assertEquals(LOG_EVENT_COUNT + 1, store.getCount());
    Assert.assertEquals(testMessage, store.getLogData(store.getCount() - 1).getMessage());
  }

  @Test
  public void testGetLogData1() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testClear() throws Exception {
    //given
    //when
    int size = store.clear();
    ///then
    Assert.assertEquals(LOG_EVENT_COUNT, size);
    Assert.assertEquals(0, store.getCount());

  }

  @Test
  public void testAddNoteToRow() throws Exception {
    //given
    //when
    store.addNoteToRow(10, new Note("A"));
    //then
    assertEquals("A", store.getNote(10).getNote());
  }

  @Test
  public void testGetNote() throws Exception {
    for (int i = 0; i < LOG_EVENT_COUNT; i++) {
      Assert.assertEquals("Note" + i, store.getNote(i).getNote());
    }
  }

  @Test
  public void testRemoveNote() throws Exception {
    store.removeNote(10);
    Assert.assertEquals(null, store.getNote(10));

  }

  @Test
  public void testClearNotes() throws Exception {
    //given
    //when
    store.clearNotes();
    //then
    assertEquals(0, store.getAllNotes().size());
  }

  @Test
  public void testGetAllNotes() throws Exception {
    //given
    //when
    TreeMap<Integer, Note> allNotes = store.getAllNotes();
    //then
    assertEquals(LOG_EVENT_COUNT, allNotes.size());
    assertEquals("Note100", allNotes.get(Integer.valueOf(100)).getNote());
  }

  @Test
  public void testIterator() throws Exception {
    Iterator<LogData> iterator = store.iterator();

    int count = 0;
    Assert.assertNotNull(iterator);
    while (iterator.hasNext()) {
      LogData next = iterator.next();
      Assert.assertEquals(next.getId(), count);
      count++;
    }
    Assert.assertEquals(LOG_EVENT_COUNT, count);
  }

  @Test
  public void testIsMarked() throws Exception {
    Assert.assertTrue(store.isMarked(10));
    Assert.assertTrue(store.isMarked(20));
    Assert.assertTrue(store.isMarked(300));
    assertFalse(store.isMarked(11));
    assertFalse(store.isMarked(21));
    assertFalse(store.isMarked(301));
  }

  @Test
  public void testGetMarkerColors() throws Exception {
    assertEquals(MarkerColors.Aqua, store.getMarkerColors(10));
    assertEquals(MarkerColors.Aqua, store.getMarkerColors(20));
    assertEquals(MarkerColors.Aqua, store.getMarkerColors(30));
    assertEquals(null, store.getMarkerColors(31));
  }

  @Test
  public void testMarkRows() throws Exception {
    //give
    //when
    store.markRows(MarkerColors.Black, 10);
    store.markRows(MarkerColors.Brown, 12);
    //then
    assertEquals(MarkerColors.Black, store.getMarkerColors(10));
    assertEquals(MarkerColors.Brown, store.getMarkerColors(12));

  }

  @Test
  public void testUnmarkRows() throws Exception {
    //give
    //when
    store.unmarkRows(10, 20);
    //then
    assertFalse(store.isMarked(10));
    assertFalse(store.isMarked(20));
  }
}
