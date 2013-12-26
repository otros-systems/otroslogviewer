package pl.otros.logview.store.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;
import sun.misc.ProxyGenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import static org.junit.Assert.fail;

public class MemoryAsyncLogDataStoreTest {

  public static final int LOG_EVENT_COUNT = 1000;
  public static final String TEST_THREAD_POOL_NAME = "TestThreadPoolName";
  MemoryAsyncLogDataStore store;
  private String[] classes = new String[]{
       "com.package.p1.Class",
       "com.package.p1.Dao",
       "com.package.p1.Bean",
       "com.package.p2/ExtraBean",
       "com.package.p2.Class",
  }   ;
  private String[] threads = new String[]{
       "t1",
       "t3",
       "tsfsa",
       "tsdf",
       "tsf",
  }   ;
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
        Assert.assertEquals("Async operation was performed out of designed thread pool","TestThreadPoolName",logStoreThreadName);
        return method.invoke(memorylogDataStore, args);
      }
    });


    store = new MemoryAsyncLogDataStore(service, logDataStore);
    for (int i=0;i<LOG_EVENT_COUNT;i++){
      LogDataBuilder builder = new LogDataBuilder();
      builder.withDate(new Date(i * 1000 * 60 * 60l));
      builder.withLevel(Level.INFO);
      builder.withClass(classes[i%classes.length]);
      builder.withMessage("My message " + i);
      builder.withThread(threads[i%threads.length]);
      store.add(builder.build());
    }

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetCount() throws Exception {
    Assert.assertEquals(LOG_EVENT_COUNT,store.getCount());

  }

  @Test
  public void testRemove() throws Exception {
    LogData logDataRow1 = store.getLogData(1);
    ListenableFuture remove = store.remove(0);
    remove.get();

    Assert.assertEquals(logDataRow1,store.getLogData(0));
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
    Assert.assertEquals(classes[0],logData.getClazz());
  }

  @Test
  public void testGetLogDataIdInRow() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testGetLimit() throws Exception {
    store.setLimit(100);
    Assert.assertEquals(100,store.getLimit());
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
    Assert.assertEquals(LOG_EVENT_COUNT+1,store.getCount());
    Assert.assertEquals(testMessage,store.getLogData(store.getCount()-1).getMessage());
  }

  @Test
  public void testGetLogData1() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testClear() throws Exception {
    int size = store.clear();

    Assert.assertEquals(LOG_EVENT_COUNT,size);
    Assert.assertEquals(0,store.getCount());

  }

  @Test
  public void testAddNoteToRow() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testGetNote() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testRemoveNote() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testClearNotes() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testGetAllNotes() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testIterator() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testIsMarked() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testGetMarkerColors() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testMarkRows() throws Exception {
    fail("not implemented");
  }

  @Test
  public void testUnmarkRows() throws Exception {
    fail("not implemented");
  }
}
