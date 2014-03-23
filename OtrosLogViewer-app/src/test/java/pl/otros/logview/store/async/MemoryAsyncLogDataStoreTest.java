package pl.otros.logview.store.async;

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

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;


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

  @BeforeMethod
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
        AssertJUnit.assertEquals("Async operation was performed out of designed thread pool", "TestThreadPoolName", logStoreThreadName);
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


  @Test
  public void testGetCount() throws Exception {
    AssertJUnit.assertEquals(LOG_EVENT_COUNT, store.getCount());

  }

  @Test(enabled=false)
  public void testRemove() throws Exception {
    //given
    LogData logDataRow1 = store.getLogData(11);

    //when
    store.remove(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22, 23).get();

    //then
    //    Assert.assertEquals(logDataRow1, store.getLogData(0));
    AssertJUnit.assertEquals(10, store.getLogData(0).getId());
    AssertJUnit.assertEquals(24, store.getLogData(10).getId());
    AssertJUnit.assertEquals(986, store.getCount());

  }

  @Test(enabled=false)
  public void testFilter() throws Exception {
    Assert.fail("not implemented");
  }

  @Test(enabled=false)
  public void testSearch() throws Exception {
    Assert.fail("not implemented");
  }

  @Test
  public void testGetLogData() throws Exception {
    LogData logData = store.getLogData(0);

    AssertJUnit.assertEquals(0, logData.getDate().getTime());
    AssertJUnit.assertEquals(classes[0], logData.getClazz());
  }

  @Test
  public void testGetLogDataIdInRow() throws Exception {
    //given
    //when
    //then
    for (int i = 0; i < LOG_EVENT_COUNT; i++) {
      AssertJUnit.assertEquals(Integer.valueOf(i), store.getLogDataIdInRow(i));
    }
  }

  @Test
  public void testGetLimit() throws Exception {
    store.setLimit(100);
    AssertJUnit.assertEquals(100, store.getLimit());
  }

  @Test
  public void testSetLimit() throws Exception {
    store.setLimit(100);
    AssertJUnit.assertEquals(100, store.getCount());
  }

  @Test
  public void testAdd() throws Exception {
    String testMessage = "testAdd";
    LogData ld = new LogDataBuilder().withDate(new Date()).withMessage(testMessage).build();
    store.add(ld);
    AssertJUnit.assertEquals(LOG_EVENT_COUNT + 1, store.getCount());
    AssertJUnit.assertEquals(testMessage, store.getLogData(store.getCount() - 1).getMessage());
  }

  @Test(enabled=false)
  public void testGetLogData1() throws Exception {
    Assert.fail("not implemented");
  }

  @Test
  public void testClear() throws Exception {
    //given
    //when
    int size = store.clear();
    ///then
    AssertJUnit.assertEquals(LOG_EVENT_COUNT, size);
    AssertJUnit.assertEquals(0, store.getCount());

  }

  @Test
  public void testAddNoteToRow() throws Exception {
    //given
    //when
    store.addNoteToRow(10, new Note("A"));
    //then
    AssertJUnit.assertEquals("A", store.getNote(10).getNote());
  }

  @Test
  public void testGetNote() throws Exception {
    for (int i = 0; i < LOG_EVENT_COUNT; i++) {
      AssertJUnit.assertEquals("Note" + i, store.getNote(i).getNote());
    }
  }

  @Test
  public void testRemoveNote() throws Exception {
    store.removeNote(10);
    AssertJUnit.assertEquals(null, store.getNote(10));

  }

  @Test
  public void testClearNotes() throws Exception {
    //given
    //when
    store.clearNotes();
    //then
    AssertJUnit.assertEquals(0, store.getAllNotes().size());
  }

  @Test
  public void testGetAllNotes() throws Exception {
    //given
    //when
    TreeMap<Integer, Note> allNotes = store.getAllNotes();
    //then
    AssertJUnit.assertEquals(LOG_EVENT_COUNT, allNotes.size());
    AssertJUnit.assertEquals("Note100", allNotes.get(Integer.valueOf(100)).getNote());
  }

  @Test
  public void testIterator() throws Exception {
    Iterator<LogData> iterator = store.iterator();

    int count = 0;
    AssertJUnit.assertNotNull(iterator);
    while (iterator.hasNext()) {
      LogData next = iterator.next();
      AssertJUnit.assertEquals(next.getId(), count);
      count++;
    }
    AssertJUnit.assertEquals(LOG_EVENT_COUNT, count);
  }

  @Test
  public void testIsMarked() throws Exception {
    AssertJUnit.assertTrue(store.isMarked(10));
    AssertJUnit.assertTrue(store.isMarked(20));
    AssertJUnit.assertTrue(store.isMarked(300));
    AssertJUnit.assertFalse(store.isMarked(11));
    AssertJUnit.assertFalse(store.isMarked(21));
    AssertJUnit.assertFalse(store.isMarked(301));
  }

  @Test
  public void testGetMarkerColors() throws Exception {
    AssertJUnit.assertEquals(MarkerColors.Aqua, store.getMarkerColors(10));
    AssertJUnit.assertEquals(MarkerColors.Aqua, store.getMarkerColors(20));
    AssertJUnit.assertEquals(MarkerColors.Aqua, store.getMarkerColors(30));
    AssertJUnit.assertEquals(null, store.getMarkerColors(31));
  }

  @Test
  public void testMarkRows() throws Exception {
    //given
    //when
    store.markRows(MarkerColors.Black, 10);
    store.markRows(MarkerColors.Brown, 12);
    //then
    AssertJUnit.assertEquals(MarkerColors.Black, store.getMarkerColors(10));
    AssertJUnit.assertEquals(MarkerColors.Brown, store.getMarkerColors(12));

  }

  @Test
  public void testUnmarkRows() throws Exception {
    //give
    //when
    store.unmarkRows(10, 20);
    //then
    AssertJUnit.assertFalse(store.isMarked(10));
    AssertJUnit.assertFalse(store.isMarked(20));
  }
}
