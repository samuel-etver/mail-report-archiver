package reportarchiver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;


public class Common {
  public static final String APP_VERSION = "11.12.2018";
  public static final String MAIN_ICON_FILE_NAME = "report-user-icon.png";
  private static final String CFG_FILE_NAME = "ReportArchiver.config";
  public static final int EMAIL_READ_PERIOD_NOM = 2*60; // minutes  
  public static final int EMAIL_READ_PERIOD_MIN = 60; // minutes
  public static final int EMAIL_READ_PERIOD_IF_FAILED_NOM = 20; // minutes
  public static final int EMAIL_READ_PERIOD_IF_FAILED_MIN = 15; // minutes
  
  public static String sAppFolder = "";
  public static String sCfgFolder = "";  
  
  public static String sEmailHost = "";    
  public static String sEmailUser = "";
  public static String sEmailPassword = "";
  public static String sEmailSender = "";
  public static String sEmailArchivePath = "";
  public static EmailClientStatus sEmailClientStatus = EmailClientStatus.Idle;
  private static final ArrayList<ClientLogHandler> sClientLogHandlers =
   new ArrayList();
  
  public static int sEmailRead;
  public static int sEmailAdded;
  public static int sEmailDeleted;
  public static int sEmailTextAdded;
  public static int sEmailAttachmentAdded;
  public static int sEmailReadPeriod = EMAIL_READ_PERIOD_NOM;
  public static int sEmailReadPeriodIfFailed = EMAIL_READ_PERIOD_IF_FAILED_NOM;
  
  public static Stage sPrimaryStage;
  
  public static Log sLog;
  

  public static void init() {       
    try {
      final Path p = Paths.get(Common.class.getProtectionDomain().getCodeSource().
       getLocation().toURI());
      sAppFolder = p.getParent().toString() + File.separator;
    }
    catch(Exception e) {    
    }    
    
    sCfgFolder = sAppFolder;
    
    sLog = Log.getInstance();
    sLog.setPath(sAppFolder);   
    sLog.load();
    
    
    java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {  
        finit();
      }
    });
  }


  public static void load() {     
    final ConfigFile cfg = new ConfigFile();
    
    cfg.load(sCfgFolder + CFG_FILE_NAME);
    
    sEmailHost = cfg.read("EmailHost", sEmailHost).trim();
    sEmailUser = cfg.read("EmailUser", sEmailUser).trim();
    sEmailPassword = cfg.read("EmailPassword", sEmailPassword).trim();
    sEmailSender = cfg.read("EmailSender", sEmailSender).trim();
    sEmailArchivePath = cfg.read("EmailArchivePath", sEmailArchivePath).trim();
    int tmpInt = cfg.read("EmailReadPeriod", sEmailReadPeriod);
    if (tmpInt >= EMAIL_READ_PERIOD_MIN)
      sEmailReadPeriod = tmpInt;
    tmpInt = cfg.read("EmailReadPeriodIfFailed", sEmailReadPeriodIfFailed);
    if (tmpInt >= EMAIL_READ_PERIOD_IF_FAILED_MIN)
      sEmailReadPeriodIfFailed = tmpInt;
    
    final LocalDateTime dttm = LocalDateTime.now();
    Common.writelnClientLog(
     "Сервер стартовал: ",
     dttm.toLocalDate().toString(),
     " ",
     dttm.toLocalTime().toString(),
     "\n",
     "======\n");
  }
  

  public static void save() {    
    final ConfigFile cfg = new ConfigFile();
    
    cfg.load(sCfgFolder + CFG_FILE_NAME);
    
    cfg.write("EmailHost", sEmailHost);
    cfg.write("EmailUser", sEmailUser);
    cfg.write("EmailPassword", sEmailPassword);
    cfg.write("EmailSender", sEmailSender);
    cfg.write("EmailArchivePath", sEmailArchivePath);
    cfg.write("EmailReadPeriod", sEmailReadPeriod);
    cfg.write("EmailReadPeriodIfFailed", sEmailReadPeriodIfFailed);
    
    cfg.save(sCfgFolder + CFG_FILE_NAME);
  }
  

  private static void finit() {
    final LocalDateTime dttm = LocalDateTime.now();
    Common.writelnClientLog(
     "Сервер остановлен: ",
     dttm.toLocalDate().toString(),
     " ",
     dttm.toLocalTime().toString(),
     "\n",
     "======\n");  
    sLog.close();
  }
  

  public static void InvokeFx(Runnable r) {
    Platform.runLater(r);
  }
  

  public static void InvokeAwt(Runnable r) {
    InvokeSwing(r);
  }
  

  public static void InvokeSwing(Runnable r) {
    javax.swing.SwingUtilities.invokeLater(r);
  }
  

  public static String getStatus() {
    return sLog.getContent();
  }
  
  
  public static Double toDouble(Object o) {
    return o != null && o instanceof Number 
            ? ((Number)o).doubleValue() 
            : null;
  }
  

  private static void notifyLogUpdated() {
    final ActionEvent event = new ActionEvent();   
    sClientLogHandlers.forEach((handler) -> {
      handler.handle(event);
    });
  }
  

  public static void writeClientLog(String... lines) {    
    sLog.write(lines);
    notifyLogUpdated();
  }
  

  public static void writelnClientLog(String... lines) {
    sLog.writeln(lines);
    notifyLogUpdated();
  }
  

  public static void writeExceptionClientLog(Exception ex) {  
    sLog.writeEx(ex);
    notifyLogUpdated();
  }
  

  public static String getClientLogText() {
    return getStatus();
  }
  

  public static void addClientLogHandler(ClientLogHandler handler) {
    sClientLogHandlers.add(handler);
  }
  

  public static void removeClientLogHandler(ClientLogHandler handler) {
    sClientLogHandlers.remove(handler);
  }


  public enum EmailClientStatus {
    Idle, Start, Reading, Terminate
  };
  

  public interface ClientLogHandler extends EventHandler<ActionEvent> {  
  }
}
