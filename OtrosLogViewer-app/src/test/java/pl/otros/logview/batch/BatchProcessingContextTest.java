package pl.otros.logview.batch;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.LogInvestiagionPersitanceUtil;
import pl.otros.logview.batch.BatchProcessingContext.LogDataStoreType;
import pl.otros.logview.gui.LogDataTableModel.Memento;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;
import pl.otros.logview.store.file.FileLogDataStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

public class BatchProcessingContextTest {

  private BatchProcessingContext batchProcessingContext;

  @BeforeMethod
public void prepare() {
    batchProcessingContext = new BatchProcessingContext();
  }

  @Test
  public void testCreateLogDataStoreMemory() throws IOException {
    LogDataStore createLogDataStore = batchProcessingContext.createLogDataStore(LogDataStoreType.MEMORY);

    AssertJUnit.assertEquals(MemoryLogDataStore.class.getName(), createLogDataStore.getClass().getName());
    AssertJUnit.assertEquals(createLogDataStore, batchProcessingContext.getDataStore());
  }

  @Test
  public void testCreateLogDataStoreFile() throws IOException {
    LogDataStore createLogDataStore = batchProcessingContext.createLogDataStore(LogDataStoreType.FILE);

    AssertJUnit.assertEquals(FileLogDataStore.class.getName(), createLogDataStore.getClass().getName());
    AssertJUnit.assertEquals(createLogDataStore, batchProcessingContext.getDataStore());
  }

  @Test
  public void testSaveLogDataStoreOutputStreamString() throws Exception {
    // givne
    LogDataStore logDataStore = batchProcessingContext.createLogDataStore(LogDataStoreType.MEMORY);
    logDataStore.add(new LogDataBuilder().withId(1).withDate(new Date(1)).withClass("a.B").withMessage("m1").withLevel(Level.INFO).build());
    logDataStore.add(new LogDataBuilder().withId(2).withDate(new Date(2)).withClass("a.C").withMessage("m2").withLevel(Level.WARNING).build());
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    // when
    batchProcessingContext.saveLogDataStore(bout, "logName");

    // then
    InputStream in = new ByteArrayInputStream(bout.toByteArray());
    Memento loadMemento = LogInvestiagionPersitanceUtil.loadMemento(in);
    ArrayList<LogData> list = loadMemento.getList();
    AssertJUnit.assertEquals(2, list.size());
    AssertJUnit.assertEquals(logDataStore.getLogData(0), list.get(0));
    AssertJUnit.assertEquals(logDataStore.getLogData(1), list.get(1));
  }

  @Test
  public void testGetAttributeStringClassOfT() {
    batchProcessingContext.setAttribute("A", "C");

    AssertJUnit.assertEquals("C", batchProcessingContext.getAttribute("A", String.class));
  }

  @Test
  public void testGetAttributeStringClassOfTT() {

    AssertJUnit.assertFalse(batchProcessingContext.containsAttribute("A"));
    AssertJUnit.assertEquals("D", batchProcessingContext.getAttribute("A", String.class, "D"));
  }

  @Test
  public void testContainsAttribute() {
    batchProcessingContext.setAttribute("A", "C");

    AssertJUnit.assertTrue(batchProcessingContext.containsAttribute("A"));
    AssertJUnit.assertFalse(batchProcessingContext.containsAttribute("B"));
  }

}
