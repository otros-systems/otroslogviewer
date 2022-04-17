/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.uml;

import pl.otros.logview.api.model.LogData;

import java.io.PrintWriter;
import java.util.*;

public class LogdataConverter {

  public UMLModel createJComponent(LogData[] datas) {
    LinkedList<String> stack = new LinkedList<>();
    LinkedList<String> actors = getActors2(datas);
    HashMap<String, Integer> actorNesting = new HashMap<>();
    actors.addFirst("User");
    stack.add("User");

    for (String a : actors) {
      actorNesting.put(a, 0);
    }

    UMLModel model = new UMLModel(actors);
    model.step(80);
    model.activate("User", datas[0].getId());
    for (LogData logData : datas) {
      String actor = logData.getClazz();
      if (actor.equalsIgnoreCase("ITMinstall.CandleInstall$3")) {
        continue;
      }
      Message m = new Message(logData.getMessage());
      String actorFrom = "";
      String actorTo = "";

      if (m.getType().equals(Message.MessageType.TYPE_ENTRY)) {
        actorFrom = stack.getLast();
        stack.addLast(logData.getClazz());

        actorTo = actor;

        if (actorNesting.get(actorTo) == 0) {
          model.activate(actorTo, logData.getId());
        }
        actorNesting.put(actorTo, actorNesting.get(actorTo).intValue() + 1);

        model.message(actorFrom, actorTo, logData.getMethod() + " " + m.getMessage(), logData.getId());
        model.step();

      } else if (m.getType().equals(Message.MessageType.TYPE_EXIT)) {
        actorFrom = stack.removeLast();
        actorTo = stack.getLast();
        model.step();
        model.rmessage(actorTo, actorFrom, "return:" + m.getMessage(), logData.getId());
        // model.step();
        actorNesting.put(actorFrom, actorNesting.get(actorFrom).intValue() - 1);
        if (actorNesting.get(actorFrom) == 0) {
          model.deactivate(actorFrom, logData.getId());
        }

        model.step();
      }

    }
    for (String actor : actors) {
      model.deactivate(actor, datas[datas.length - 1].getId());
    }
    // model.deactivate("User");

    return model;

  }

  public void createPsFile(LogData[] datas, PrintWriter out) {

    LinkedList<String> stack = new LinkedList<>();
    Map<String, String> actors = getActors(datas);
    // StringBuffer sb = new StringBuffer();

    out.append(".PS\n");
    out.append("copy \"sequence.pic\";\n");

    out.append("#boxwid = 3.3;\n#boxht = 2;\n");
    out.append("#spacing	= 0.35;\n");
    out.append("#movewid	= 1.75;\n");
    out.append("maxpswid= 722;\n");
    out.append("maxpsht = 15000;\n");
    out.append("# Object definition\n");
    out.append("pobject(E,\"External Messages\");\n");
    actors.put("User", "E");
    stack.add("User");

    for (String key : actors.keySet()) {
      out.append("object(").append(actors.get(key)).append(",\"").append(key).append("\");\n");
    }

    out.append("#Creating events\n");
    for (LogData logData : datas) {
      // logData.getMessage()
      // message(O,O,"callbackLoop()");
      // active(P);
      // rmessage(P,O,"");
      // inactive(P);
      String actorCode = actors.get(getShortClassName(logData.getClazz()));
      Message m = new Message(logData.getMessage());
      String actorFrom = "";
      String actorTo = "";

      if (m.getType().equals(Message.MessageType.TYPE_ENTRY)) {
        stack.addLast(logData.getClazz());
        actorFrom = stack.get(stack.size() - 2);
        actorFrom = actors.get(getShortClassName(actorFrom));
        actorTo = actorCode;
        out.append("message(");
        out.append(actorFrom);
        out.append(",");
        out.append(actorTo);
        out.append(",\"");
        out.append(logData.getMethod() + " " + m.getMessage());
        out.append("\");\n");
        out.append("active(" + actorCode + ");\n");

      } else if (m.getType().equals(Message.MessageType.TYPE_EXIT)) {
        actorFrom = actors.get(getShortClassName(stack.removeLast()));
        actorTo = actors.get(getShortClassName(stack.getLast()));
        out.append("rmessage(");
        out.append(actorFrom);
        out.append(",");
        out.append(actorTo);
        out.append(",\"");
        out.append(m.getMessage());
        out.append("\");\n");
        out.append("inactive(" + actorCode + ");\n");

      } else {
        // zlewamy
        // actorFrom = actorCode;
        // actorTo = actorCode;
        // out.append("message(");
        // out.append(actorCode);
        // out.append(",");
        // out.append(actorCode);
        // out.append(",\"");
        // out.append(m.getMessage());
        // out.append("\");\n");

      }

    }

    out.append("# Object lifeline completion\n");
    for (Object key : actors.keySet()) {
      out.append("complete(" + actors.get(key) + ");\n");

    }

    out.append(".PE\n");
    out.flush();
    out.close();

  }

  private Map<String, String> getActors(LogData[] datas) {
    Map<String, String> list = new LinkedHashMap<>();
    for (int i = 0; i < datas.length; i++) {
      String shortName = getShortClassName(datas[i].getClazz());
      if (!list.containsKey(shortName)) {
        list.put(shortName, "A" + i);
      }
    }
    return list;
  }

  private LinkedList<String> getActors2(LogData[] datas) {
    LinkedHashSet<String> map = new LinkedHashSet<>();
    for (LogData data : datas) {
      String clazz = data.getClazz();
      if (!map.contains(clazz)) {
        map.add(clazz);
      }
    }

    return new LinkedList<>(map);
  }

  private String getShortClassName(String className) {
    return className.substring(className.lastIndexOf('.') + 1);
  }


}
