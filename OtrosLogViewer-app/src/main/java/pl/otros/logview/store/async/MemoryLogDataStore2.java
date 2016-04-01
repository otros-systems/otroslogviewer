package pl.otros.logview.store.async;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.store.MemoryLogDataStore;
import pl.otros.logview.gui.actions.search.SearchResult;

public class MemoryLogDataStore2 extends MemoryLogDataStore implements LogDataStore2 {

  private int[] viewToModel = new int[0];
  private int modelAfterFiltersCount;


  //TODO pass object to report progress
  //TODO pass current selection to return index after filtering
  //TODO incremental filtering when new LogData is added
  @Override
  public FilterResult filter(LogDataFilter logDataFilter) {
    return doFiltering(logDataFilter);
  }

  protected FilterResult doFiltering(LogDataFilter logDataFilter) {
    String string = logDataFilter == null ? "" : logDataFilter.getString();
    //TODO init mapping arrays
    int[] modelToView = new int[this.getCountWithoutFilters()];
    int[] viewToModelTmp = new int[modelToView.length];
    int filteredOut = 0;

    for (int i = 0; i < modelToView.length; i++) {
      LogData logData = getLogData(i);
      if (logDataFilter == null || StringUtils.containsIgnoreCase(logData.getMessage(), string)) {
        modelToView[i] = i - filteredOut;
        viewToModelTmp[i - filteredOut] = i;
      } else {
        modelToView[i] = -1;
        filteredOut++;
      }
    }
    if (viewToModel.length * 0.7 < filteredOut) {
      viewToModel = new int[viewToModelTmp.length - filteredOut];
      System.arraycopy(viewToModelTmp, 0, viewToModel, 0, viewToModelTmp.length - filteredOut);
    } else {
      viewToModel = viewToModelTmp;
    }
    modelAfterFiltersCount = modelToView.length - filteredOut;
    return null;
  }

  @Override
  public SearchResult search(SearchCriteria searchCriteria) {
    return null;
  }

  @Override
  public int getCountWithoutFilters() {
    return super.getCount();
  }

  @Override
  public int getCount() {
    return modelAfterFiltersCount;
  }
}
