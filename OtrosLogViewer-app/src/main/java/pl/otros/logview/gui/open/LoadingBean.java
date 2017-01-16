package pl.otros.logview.gui.open;

import pl.otros.logview.api.io.LoadingInfo;

class LoadingBean {

  LoadingBean(FileObjectToImport fileObjectToImport, LoadingInfo loadingInfo) {
    this.fileObjectToImport = fileObjectToImport;
    this.loadingInfo = loadingInfo;
  }

  FileObjectToImport fileObjectToImport;
  LoadingInfo loadingInfo;
}
