package pl.otros.logview.gui.open;

import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.ContentProbe;

class AddingDetail {
  private CanParse canParse;
  private PossibleLogImporters possibleLogImporters;
  private ContentProbe contentProbe;

  AddingDetail(CanParse canParse, PossibleLogImporters possibleLogImporters, ContentProbe contentProbe) {
    this.canParse = canParse;
    this.possibleLogImporters = possibleLogImporters;
    this.contentProbe = contentProbe;
  }

  public CanParse getCanParse() {
    return canParse;
  }

  public PossibleLogImporters getPossibleLogImporters() {
    return possibleLogImporters;
  }

  public ContentProbe getContentProbe() {
    return contentProbe;
  }
}
