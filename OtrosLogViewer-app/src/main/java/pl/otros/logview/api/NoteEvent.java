/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.api;

public class NoteEvent {

  public enum EventType {
    ADD, REMOVE, CLEAR
  }

  private int row;
  private EventType eventType;
  private NotableTableModel source;
  private Note note;

  public NoteEvent(EventType eventType, NotableTableModel source, Note note, int row) {
    super();
    this.row = row;
    this.eventType = eventType;
    this.source = source;
    this.note = note;
  }

  public NoteEvent() {
    super();
  }

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public NotableTableModel getSource() {
    return source;
  }

  public void setSource(NotableTableModel source) {
    this.source = source;
  }

  public Note getNote() {
    return note;
  }

  public void setNote(Note note) {
    this.note = note;
  }

}
