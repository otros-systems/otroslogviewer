package pl.otros.logview.singleinstance;

import com.negusoft.singleinstance.RequestDelegate;
import com.negusoft.singleinstance.ResponseDelegate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import pl.otros.logview.gui.AppProperties;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.TailMultipleFilesIntoOneView;
import pl.otros.swing.OtrosSwingUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleInstanceRequestResponseDelegate implements RequestDelegate, ResponseDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleInstanceRequestResponseDelegate.class.getName());
  private OtrosApplication otrosApplication;

  private static SingleInstanceRequestResponseDelegate instance;

  private SingleInstanceRequestResponseDelegate() {

  }

  public static SingleInstanceRequestResponseDelegate getInstance() {
    if (instance == null) {
      synchronized (SingleInstanceRequestResponseDelegate.class) {
        if (instance == null) {
          instance = new SingleInstanceRequestResponseDelegate();
        }
      }
    }
    return instance;
  }

  @Override
  public void requestAction(Socket socket, String... params) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("OPEN ");
      sb.append(params.length).append("\n");
      for (String p : params) {
        sb.append(p);
        sb.append("\n");
      }
      sb.append("PATH " + new AppProperties().getCurrentDir()).append("\n");
      LOGGER.info("writing to socket \"" + sb.toString() + "\n");
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
      printWriter.print(sb.toString());
      printWriter.flush();
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String response = reader.readLine();
      if ("QUIT".equalsIgnoreCase(response)) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.error( "Can't write params to socket", e);
      e.printStackTrace();
    }
  }

  @Override
  public void responseAction(Socket socket) {
    final StringBuilder sb = new StringBuilder();
    List<String> filesList = new ArrayList<String>();
    String path = null;
    try {
      LOGGER.info("Someone is calling us!");
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String line = reader.readLine();
      LOGGER.info("Received command " + line);
      if (StringUtils.startsWithIgnoreCase(line, "OPEN")) {
        int count = Integer.parseInt(line.split(" ")[1].trim());
        for (int i = 0; i < count; i++) {
          line = reader.readLine();
          LOGGER.info("Received param: " + line);
          sb.append(i).append("\t").append(line).append("\n");
          filesList.add(line);
        }
        line = reader.readLine();
        if (StringUtils.startsWithIgnoreCase(line, "PATH")) {
          path = line.split(" ", 2)[1];
          sb.append("Path = ").append(path);
        }
      } else {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.error( "Can't read params from socket", e);
    }

    openFilesFromStartArgs(otrosApplication,filesList, path);
    try {
      socket.getOutputStream().write("QUIT\n".getBytes());
    } catch (IOException e) {
      LOGGER.info("Error sending QUIT message to client");
    }
  }

  public static void openFilesFromStartArgs(final OtrosApplication otrosApplication, List<String> filesList, String path) {
    ArrayList<FileObject> fileObjects = new ArrayList<FileObject>();
    for (String file : filesList) {
      try {
        FileObject fo = VFS.getManager().resolveFile(new File(path), file);
        fileObjects.add(fo);
      } catch (FileSystemException e) {
        LOGGER.error( "Cant resolve " + file + " in path " + path, e);
      }
    }
    final FileObject[] files = fileObjects.toArray(new FileObject[0]);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame applicationJFrame = null;
        if (otrosApplication != null) {
          applicationJFrame = otrosApplication.getApplicationJFrame();
          OtrosSwingUtils.frameToFront(applicationJFrame);
        }
        if (files.length > 0) {
          new TailMultipleFilesIntoOneView(otrosApplication).openFileObjectsIntoOneView(files, applicationJFrame);
        }
      }
    });
  }

  public OtrosApplication getOtrosApplication() {
    return otrosApplication;
  }

  public void setOtrosApplication(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }
}
