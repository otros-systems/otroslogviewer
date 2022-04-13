package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.Note;

import java.util.concurrent.Callable;

public class OpeartionAddNoteToRow implements Callable<Void> {
  private final LogDataStore logDataStore;
  private final int row;
  private final Note note;

  public OpeartionAddNoteToRow(LogDataStore logDataStore, int row, Note note) {
    this.logDataStore = logDataStore;
    this.row = row;
    this.note = note;
  }

  @Override
  public Void call() throws Exception {
    logDataStore.addNoteToRow(row, note);
    return null;
  }
}
