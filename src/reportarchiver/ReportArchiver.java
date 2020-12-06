package reportarchiver;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class ReportArchiver extends Application {
  private static final String MIME_TEXT = "text/plain";
  private static final String MIME_HTML = "text/html";
  private static final String MIME_RTF  = "text/richtext";
  
  private SystemTray mTray;
  private TrayIcon mTrayIcon;
  private AboutDialog mAboutDialog;
  private HashMap<String, Object> mAboutDialogSettings;
  private OptionsDialog mOptionsDialog;
  private HashMap<String, Object> mOptionsDialogSettings;
  private StatusDialog mStatusDialog;
  private HashMap<String, Object> mStatusDialogSettings;
  private Timer mReadTimer;
  private final EventHandler<ActionEvent> mReadEventHandler = e -> onReadClick();
  private boolean mReadControlEnabled = true;
  private final ReadRunner mReadRunner = new ReadRunner();
  private final Thread mReadThread = new Thread(mReadRunner);
  private LocalDateTime mLastReadTime;
  private boolean mLastReadSuccess = true;
  private final Common.ClientLogHandler mClientLogHandler =
   e -> onClientLogUpdated();
  private boolean mClientLogUpdated = false;
  private MenuItem mReadMenuItem;
  
  
  @Override
  public void start(Stage primaryStage) {
    Platform.setImplicitExit(false);
    
    Common.addClientLogHandler(mClientLogHandler);
    mLastReadTime = LocalDateTime.now().minusMinutes(Common.sEmailReadPeriod - 5);
    
    Common.InvokeAwt(() -> {
      createTray();
      mReadTimer = new Timer(1000, e -> onReadTimer());
      mReadTimer.start();
    });
    
    final StackPane root = new StackPane();    
    final Scene scene = new Scene(root, 300, 250);
    
    Common.sPrimaryStage = primaryStage;
    primaryStage.setTitle("Архиватор отчетов сменных инженеров");
    primaryStage.getIcons().add(
     new javafx.scene.image.Image("/resources/" + Common.MAIN_ICON_FILE_NAME));
    primaryStage.setScene(scene);    
        
    mReadThread.start();
  }
  

  private void createTray() {
    if (!SystemTray.isSupported())
      return;
    
    mTray = SystemTray.getSystemTray();    
    try {
      final URL url = getClass().getResource("/resources/" + 
       Common.MAIN_ICON_FILE_NAME);      
      mTrayIcon = new TrayIcon(ImageIO.read((url)));
      
      final PopupMenu menu = new PopupMenu();
      MenuItem mi;
      mi = new MenuItem("О программе...");
      mi.addActionListener(e -> onAboutClick());
      menu.add(mi);
      mi = new MenuItem("-");      
      menu.add(mi);
      mReadMenuItem =
      mi = new MenuItem("Считать сейчас");
      mi.addActionListener(e -> onReadClick());
      menu.add(mi);
      mi = new MenuItem("-");
      menu.add(mi);
      mi = new MenuItem("Настройки...");
      mi.addActionListener(e -> onOptionsClick());
      menu.add(mi);
      mi = new MenuItem("Статус...");
      mi.addActionListener(e -> onStatusClick());
      menu.add(mi);
      mi = new MenuItem("-");
      menu.add(mi);
      mi = new MenuItem("Выход");
      mi.addActionListener(e -> onExitClick());
      menu.add(mi);
      
      mTrayIcon.setPopupMenu(menu);      
      mTray.add(mTrayIcon);
    }
    catch(Exception ex) {  
      Common.writeExceptionClientLog(ex);
    }
  }
  

  private void onAboutClick() {
    Common.InvokeFx(() -> {
      if (mAboutDialog == null) {
        mAboutDialog = AboutDialog.createDialog(mAboutDialogSettings);
        mAboutDialog.showAndWait();
        if (mAboutDialogSettings == null) {
          mAboutDialogSettings = new HashMap<>();
        }
        mAboutDialog.saveSettings(mAboutDialogSettings);
        mAboutDialog = null;
      }
      else {
        if (mAboutDialog.isIconified())
          mAboutDialog.setIconified(false);
        mAboutDialog.requestFocus();
      }
    }); 
  }
  

  private void onReadClick() {
    if (mReadControlEnabled) {
      if (Common.sEmailClientStatus == Common.EmailClientStatus.Idle)
        Common.sEmailClientStatus = Common.EmailClientStatus.Start;
    }
  }
  

  private void onStatusClick() {
    Common.InvokeFx(() -> {
      if (mStatusDialog == null) {
        mStatusDialog = StatusDialog.createDialog(mStatusDialogSettings, mReadControlEnabled);
        mStatusDialog.setOnRead(mReadEventHandler);
        updateStatusDlg();
        mStatusDialog.showAndWait();
        if (mStatusDialogSettings == null) {
          mStatusDialogSettings = new HashMap<>();
        }
        mStatusDialog.saveSettings(mStatusDialogSettings);
        mStatusDialog = null;
      }
      else {
        updateStatusDlg();
        if (mStatusDialog.isIconified())
          mStatusDialog.setIconified(false);
        mStatusDialog.requestFocus();
      }
    });
  }

  
  private void onOptionsClick() {
    Common.InvokeFx(() -> {
      if (mOptionsDialog == null) {
        mOptionsDialog = OptionsDialog.createDialog(mOptionsDialogSettings);
        mOptionsDialog.showAndWait();
        if (mOptionsDialogSettings == null) {
          mOptionsDialogSettings = new HashMap<>();
        }
        mOptionsDialog.saveSettings(mOptionsDialogSettings);
        mOptionsDialog = null;
      }
      else {
        if (mOptionsDialog.isIconified())
          mOptionsDialog.setIconified(false);
        mOptionsDialog.requestFocus();
      }
    });
  }
  

  private void onExitClick() {    
    mReadRunner.terminate();
    try {
        mReadThread.join();
    }
    catch(Exception ex) {        
      Common.writeExceptionClientLog(ex);
    }
    
    Common.InvokeFx(() -> {
      Stage[] wins = {
        mAboutDialog, mOptionsDialog, mStatusDialog
      };
      for (Stage w: wins) {
        if (w != null && w.isShowing())
          w.hide();
      }      
      Platform.exit();      
    });
    
    mTray.remove(mTrayIcon);
  }
  

  private void updateStatusDlg() {
    final StatusDialog dlg = this.mStatusDialog;
    if (dlg != null) 
      dlg.updateStatusText(Common.getClientLogText());
  }
  

  private void onClientLogUpdated() {
    mClientLogUpdated = true;
  }
  

  private void onReadTimer() {
    if (Common.sEmailClientStatus == Common.EmailClientStatus.Idle) {
      if (!mReadControlEnabled) {
        mReadControlEnabled = true;
      }
    }   
    
    boolean reqRead = false;
    final LocalDateTime currTime = LocalDateTime.now();
    if (currTime.isBefore(mLastReadTime)) 
      reqRead = true;
    else {
      final int period = mLastReadSuccess ?
       Common.sEmailReadPeriod : Common.sEmailReadPeriodIfFailed;
      if (currTime.isAfter(mLastReadTime.plusMinutes(period)))
        reqRead = true;
    }
    if (reqRead && Common.sEmailClientStatus == Common.EmailClientStatus.Idle) {      
      Common.sEmailClientStatus = Common.EmailClientStatus.Start;    
      mLastReadTime = currTime;
    }
    
    if (mClientLogUpdated) {
      mClientLogUpdated = false;
      Common.InvokeFx(() -> updateStatusDlg());
    }
  }
  

  public boolean readMail() {
    boolean result = false;
    
    disableReadControl();
    
    mLastReadTime = LocalDateTime.now();
    
    Common.sEmailClientStatus = Common.EmailClientStatus.Reading;
    
    Common.sEmailRead = 0;
    Common.sEmailAdded = 0;
    Common.sEmailDeleted = 0;
    Common.sEmailTextAdded = 0;
    Common.sEmailAttachmentAdded = 0;
    
    logReadStarted();
    
    boolean emailHostEmpty = Common.sEmailHost.length() == 0;
    boolean emailUserEmpty = Common.sEmailUser.length() == 0;
    boolean emailPasswordEmpty = Common.sEmailPassword.length() == 0;
    boolean emailSenderEmpty = Common.sEmailSender.length() == 0;
    boolean emailArchivePathEmpty = Common.sEmailArchivePath.length() == 0;
    boolean emailArchivePathExists = true;
    
    boolean canRead = !(     
        emailHostEmpty 
     || emailUserEmpty
     || emailPasswordEmpty 
     || emailSenderEmpty
     || emailArchivePathEmpty
     );       
    
    if(canRead) {
      File file = new File(Common.sEmailArchivePath);
      emailArchivePathExists = file.exists() && file.isDirectory();
      if(!emailArchivePathExists)
        canRead = false;
    }
    
    if(canRead) {
      result = readEmailImpl();
    }
    else {
      final CannotReadCause cause = new CannotReadCause();
      cause.emailHostEmpty = emailHostEmpty;
      cause.emailUserEmpty = emailUserEmpty;
      cause.emailPasswordEmpty = emailPasswordEmpty;
      cause.emailSenderEmpty = emailSenderEmpty;
      cause.emailArchivePathEmpty = emailArchivePathEmpty;
      cause.emailArchivePathExists = emailArchivePathExists;
      logCannotReadCause(cause);
    }
    
    logReadResult();    
    try {
      Thread.sleep(5000);    
    }
    catch(InterruptedException ex) {        
    }
    enableReadControl();    
    return result;
  }
  

  static class CannotReadCause {
    boolean emailHostEmpty;
    boolean emailUserEmpty;
    boolean emailPasswordEmpty;
    boolean emailSenderEmpty;
    boolean emailArchivePathEmpty;
    boolean emailArchivePathExists;
  }
  

  static void logCannotReadCause(CannotReadCause cause) {
      Common.writelnClientLog("Ошибка чтения:");
      if (cause.emailHostEmpty)
        Common.writelnClientLog("-Не указан сервер почты.");
      if (cause.emailUserEmpty)
        Common.writelnClientLog("-Не указан логин");
      if (cause.emailPasswordEmpty)
        Common.writelnClientLog("-Не указан пароль");
      if (cause.emailSenderEmpty) 
        Common.writelnClientLog("-Не указан отправитель");
      if (cause.emailArchivePathEmpty) 
        Common.writelnClientLog("-Не указана папка архива");        
      else if (!cause.emailArchivePathExists)
        Common.writelnClientLog("-Папка архива не существует");
  }
  

  static void logReadStarted() {
    final LocalDateTime dttm = LocalDateTime.now();
    Common.writelnClientLog(
     "-----\n",       
     "Считывание начато (",
     dttm.toLocalDate().toString(),
     " ",
     dttm.toLocalTime().toString(),
     ")");
  }
  

  static void logReadResult() {
    final LocalDateTime dttm = LocalDateTime.now();
    Common.writelnClientLog(
     "\t", Integer.toString(Common.sEmailRead), " писем ",
      Integer.toString(Common.sEmailAttachmentAdded), " вложений считано\n",
     "\t", Integer.toString(Common.sEmailAdded),   " писем добавлено\n",
     "\t", Integer.toString(Common.sEmailDeleted), " писем удалено\n",
     "Считывание закончено (",
      dttm.toLocalDate().toString(), " ", dttm.toLocalTime().toString(), ")\n",
     "-----\n");
  }
  

  private boolean readEmailImpl() {
    final String host = Common.sEmailHost;
    final String user = Common.sEmailUser;
    final String password = Common.sEmailPassword;
    boolean success = false;
    final Message[] movingMessages = new Message[1];
    boolean notSavedFolderExists = false;
    
    try {
      final Properties props = System.getProperties();
      final Session session = Session.getDefaultInstance(props);
      try (Store store = session.getStore("imap")) {
        store.connect(host, user, password);      
        final Folder trashFolder = store.getFolder("Удаленные");
        final Folder notSavedFolder = store.getFolder("Не сохраненные");
        if (!notSavedFolder.exists()) {
          try {
            notSavedFolderExists = notSavedFolder.create(Folder.READ_WRITE);
          }
          catch(Exception ex) {
            Common.writeExceptionClientLog(ex);
          }
        }
        try (Folder inboxFolder = store.getFolder("inbox")) {
          inboxFolder.open(Folder.READ_WRITE);
          final Message[] messages = inboxFolder.getMessages();
          for(Message msg: messages) {             
            boolean tryToSave = false;
            boolean saved = false;
            if (Common.sEmailClientStatus == Common.EmailClientStatus.Terminate) 
              throw new UserBreakException("Чтение прервано");

            Common.sEmailRead++;

            final Object[] addresses = msg.getFrom();
            if (addresses != null &&
                addresses.length >= 0 &&
                addresses[0] instanceof InternetAddress) {
              InternetAddress inetAddr = (InternetAddress)msg.getFrom()[0];
              if (Common.sEmailSender.equalsIgnoreCase(inetAddr.getAddress())) {
                tryToSave = true;
                saved = saveEmailMsg(msg);
              }
            }
            movingMessages[0] = msg;
            Folder dstFolder = trashFolder;
            if (notSavedFolderExists && tryToSave && !saved)
              dstFolder = notSavedFolder;
            inboxFolder.copyMessages(movingMessages, dstFolder);
            msg.setFlag(Flags.Flag.DELETED, true);
            if (dstFolder == trashFolder)
              Common.sEmailDeleted++;
          }
        }
      }
      
      success = true;
    }
    catch (Exception ex) {
      Common.writeExceptionClientLog(ex);
    }
    
    return success;
  }

  
  private boolean saveEmailMsg(Message msg) {
    final StringBuilder pathBuff = new StringBuilder();    
    boolean result = false;
    
    try {
      final LocalDateTime msgDtTm = msg.getSentDate().toInstant().
       atZone(ZoneId.systemDefault()).toLocalDateTime();
      final String fileNamePrefix = "[" + 
       msgDtTm.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) +
       "]";
      final Path archivePath = Paths.get(Common.sEmailArchivePath,
       msgDtTm.format(DateTimeFormatter.ofPattern("uuuu")),
       msgDtTm.format(DateTimeFormatter.ofPattern("MM")));
      
      if (!Files.exists(archivePath))
        Files.createDirectories(archivePath);
      
      final Object content = msg.getContent();
      if (content instanceof String) {
        final String msgTxt = content.toString();  
        if (msg.isMimeType(MIME_TEXT))
          result |= saveEmailPlainMsg(archivePath, fileNamePrefix, msgTxt);
        else if (msg.isMimeType(MIME_HTML))
          result |= saveEmailHtmlMsg(archivePath, fileNamePrefix, msgTxt);
        else if (msg.isMimeType(MIME_RTF))
          result |= saveEmailRtfMsg(archivePath, fileNamePrefix, msgTxt);
        else
          throw new UnknownMimeException();          
      }
      else if (content instanceof Multipart) {
        final Multipart multi = (Multipart)msg.getContent();
        for (int body_ind = 0; body_ind < multi.getCount(); body_ind++) {
          final BodyPart bp = multi.getBodyPart(body_ind);
          if (bp.isMimeType(MIME_TEXT)) {  
            result |= saveEmailPlainMsg(archivePath, fileNamePrefix,
             bp.getContent().toString());
          }
          else if (bp.isMimeType(MIME_HTML)) {            
            result |= saveEmailHtmlMsg(archivePath, fileNamePrefix, 
             bp.getContent().toString());
          }
          else if (bp.isMimeType(MIME_RTF)) {            
            result |= saveEmailRtfMsg(archivePath, fileNamePrefix,
             bp.getContent().toString());
          }
          else {
            String fileName = bp.getFileName();
            if (fileName == null)
              throw new UnknownMimeException();
            fileName = MimeUtility.decodeText(fileName);
            final String leadingPart = Paths.get(archivePath.toString(), 
               fileNamePrefix + "(").toString();            
            final String trailingPart = ") " + fileName;            
            pathBuff.setLength(0);
            pathBuff.append(leadingPart);
            boolean done = false;
            for(int i = 1; i < 100; i++) {
              pathBuff.setLength(leadingPart.length());
              pathBuff.append(Integer.toString(i));
              pathBuff.append(trailingPart);
              Path filePath = Paths.get(pathBuff.toString());
              if (!Files.exists(filePath)) {
                Files.copy(bp.getInputStream(), filePath);
                Common.sEmailAttachmentAdded++;
                done = true;
                break;
              }
            }
            if (!done) 
              throw new FileSaveFailureException();
            result |= done;
          }
        }
      }
      else {
        if (content == null)
          throw new NullMimeException();
        else
          throw new UnknownMimeException();
      }
            
      Common.sEmailAdded++;
    }
    catch(Exception ex) {      
      Common.writeExceptionClientLog(ex);
    }   
    
    return result;
  }
  

  private boolean saveEmailPlainMsg(Path path, String prefix, String msg) {    
    if(!isPlainMsgEmpty(msg)) {
      final Path filePath = Paths.get(path.toString(), prefix);
      return saveEmailTextMsg(filePath, "txt", msg, "UTF8");
    }
    return true;
  }
  

  public static final boolean isPlainMsgEmpty(String msg) {
    return msg == null || msg.trim().isEmpty();
  }
  

  private boolean saveEmailHtmlMsg(Path path, String prefix, String msg) {
    if(!isHtmlMsgEmpty(msg)) {      
      final Path filePath = Paths.get(path.toString(), prefix);
      return saveEmailTextMsg(filePath, "html", msg, null);
    }
    return true;
  }
  

  public static final boolean isHtmlMsgEmpty(String msg) {
    if(msg == null)
      return true;
    
    boolean empty = false;
    
    try {
      final Document doc = Jsoup.parse(msg);
      final Element body = doc.body();
      empty = body.text().isEmpty();
    }
    catch(Exception ex) {
      Common.writelnClientLog("HTML email is not parsed");
      Common.writeExceptionClientLog(ex);
    }
    
    return empty;
  }
  

  private boolean saveEmailRtfMsg(Path path, String prefix, String msg) {
    final Path filePath = Paths.get(path.toString(), prefix);
    return saveEmailTextMsg(filePath, "rtf", msg, null);
  }
  

  private boolean saveEmailTextMsg(Path path, String ext, String msg, 
     String enc) {
    boolean result = false;
    
    try  {
      final String leadingPart = path.toString() + "(";
      final String trailingPart = ") - msg." + ext;
      boolean done = false;
      for (int i = 1; i < 100; i++) {
        final File file = new File(leadingPart + Integer.toString(i) + trailingPart);
        if (!file.exists()) {
          try(FileOutputStream stream = new FileOutputStream(file)) {
            if (enc != null && enc.length() > 0)
              stream.write(msg.getBytes(enc));
            else
              stream.write(msg.getBytes());
          }        
          done = true;
          break;
        }
      }

      if(!done)
        throw new FileSaveFailureException();
      
      result = true;
    }
    catch(Exception ex) {
      Common.writeExceptionClientLog(ex);
    }
    
    return result;
  }
  

  private void enableReadControl() {
    enableReadControl(true);
  }
  

  private void enableReadControl(boolean enabled) {
    if (mReadControlEnabled != enabled) {
      mReadControlEnabled = enabled;
      Common.InvokeAwt(() -> {
        mReadMenuItem.setEnabled(enabled);
      });
      Common.InvokeFx(() -> {
        StatusDialog dlg = mStatusDialog;
        if (dlg != null)
          dlg.enableReadButton(enabled);
      });
    }
  }
  

  private void disableReadControl() {
    enableReadControl(false);
  }


  public static void main(String[] args) {
    Common.init();
    Common.load();
    launch(args);
  }
    


  class ReadRunner implements Runnable {
    private volatile boolean terminated = true;
    

    public void terminate() {
      Common.sEmailClientStatus = Common.EmailClientStatus.Terminate;
    }
    

    public boolean isTerminated() {
      return terminated;
    }
    

    @Override
    public void run() {
      terminated = false;
      
      quit: while (true) {
        switch(Common.sEmailClientStatus) {
          case Terminate:
            break quit;
          case Start:
            mLastReadSuccess = readMail();
            if (Common.sEmailClientStatus == Common.EmailClientStatus.Reading)
              Common.sEmailClientStatus = Common.EmailClientStatus.Idle;
        }
      }
      
      terminated = true;
    }
  }
  

  class UserBreakException extends Exception {  
    public UserBreakException(String msg) {
      super(msg);
    }
  }
  

  class UnknownMimeException extends Exception {
    public UnknownMimeException() {
      super("Неизвестное содержимое письма");
    }
    
    public UnknownMimeException(String msg) {
      super(msg);
    }
  }
  

  class NullMimeException extends Exception {
    public NullMimeException() {
      super("Null-содержимое письма");
    }
    public NullMimeException(String msg) {
      super(msg);
    }
  }
  

  class FileSaveFailureException extends Exception {
    public FileSaveFailureException() {
      super("Не удалось сохранить файл.");
    }
  }
}
