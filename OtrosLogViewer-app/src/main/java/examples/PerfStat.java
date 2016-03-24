package examples;

import pl.otros.logview.api.LogData;
import pl.otros.logview.api.batch.BatchProcessingContext;
import pl.otros.logview.api.batch.LogDataParsedListener;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PerfStat implements LogDataParsedListener {


  private Map<String,List<Event>> eventsOnStations;

  @Override
  public void logDataParsed(LogData data, BatchProcessingContext context) throws Exception {

  }


  public Optional<String> stationId(String msg){
    String[] stationNames = "AD01A06,AD01A06.AD01A08.AD01A09".split(",");
    for (String s:stationNames){
      if (msg.contains(s)){
        return Optional.of(s);
      }
    }
    return Optional.empty();
  }

  public Optional<Event> event(LogData logData){
    final String msg = logData.getMessage();
    final Date date = logData.getDate();
    final String logger = logData.getLoggerName();
    if (logger.equalsIgnoreCase("com.ocado.cfc.stations.scan.ScanManager") && msg.contains("Received LpnScanned(StationId")) {
      return Optional.of(new Event("scanned",date));
    } else if (logger.contentEquals("") && msg.contains("")){
      //occupiedLocations=[RT1057511 (UNKNOWN) @37881963982091670
      return Optional.of(new Event("",date));
    }



//    Logger name: com.ocado.cfc.stations.externalsystems.wcs.WcsServiceClient
//    Message: None Sending request 7707dd7e-9ee0-479b-81d8-4e74acc40ec5 to http://cr2-decant-ambient-wcs.rainbow.os.andover.ocado.com:8091/v1/stations/AD01A07/container-weight-request: WeighContainerRequestV1(56fa3582-e61c-4631-9d49-b651fab0983a,AD01A07,RT1057511,37881963982091670)[0m




    return Optional.empty();
  }

  class Action {
    private String station;
    private String lpn;
    private ActionType actionType;

    Action(String station, String lpn, ActionType actionType) {
      this.station = station;
      this.lpn = lpn;
      this.actionType = actionType;
    }
  }

  private class Event{
    String event;
    Date date;

    public Event(String event, Date date) {
      this.event = event;
      this.date = date;
    }

    public String getEvent() {
      return event;
    }

    public Date getDate() {
      return date;
    }
  }
}

enum ActionType {
  NO_ACTION,
  CREATED_BIN,
  PLACED_ON_WEIGHT,
  SCANNED,
  UPDATE_ID_REQUEST,
  UPDATE_ID_RESPONSE,
  WEIGHT_REQUEST,
  WEIGHT_RESPONSE,
  UPDATE_PROPERTY_REQUEST,
  UPDATE_PROPERTY_RESPONSE,
  BIN_IS_RELEASABLE,
  BUTTON_PRESSED,
  RELEASE_REQUEST,
  BIN_AFTER_INTERACTION
}


//  log processing ->
//  stworzono koszyk
//  zeskanowano
//  wyslano zmiane id
//  SC otrzymano nowe LSE
//  ZM otrzymano nowe LSE
//  koszyk jest releaseable
//  User whisk przycisk
//  koszyk wyjezdza z interaction point
