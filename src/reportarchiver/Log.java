package reportarchiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Log {
  private static final int DAYS_MAX = 30;
  private static final String NL = "\n";
  public static final String FOLDER = "Log";
  private static final Log sInstance = new Log();
  private final List<LogDay> mLogDays = new ArrayList<>(DAYS_MAX);  
  private String mPath;
  private BufferedOutputStream mOutputStream;
  private LocalDate mCurrDate;
  

  private Log() {    
  }
  

  public static Log getInstance() {
    return sInstance;
  }
  

  @SuppressWarnings("CallToPrintStackTrace")
  public void close() {
    final BufferedOutputStream stream = mOutputStream;
    mOutputStream = null;
    
    try {
      stream.close();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  

  public String getPath() {
    return mPath;
  }
  

  public void setPath(String path) {
    mPath = path;
  }
  

  public String getContent() {
    final StringBuilder builder = new StringBuilder();
    synchronized(mLogDays) {
      mLogDays.forEach(logDay -> builder.append(logDay.getContent()));
    }
    return builder.toString();
  }
  

  private Path buildFilePath(LocalDate date) {
    final String fileName = String.format(
      "ReportArchiver (%04d-%02d).log",
      date.getYear(),
      date.getMonthValue());
    return Paths.get(mPath, FOLDER, fileName);
  }
  

  public void load() {        
    final LocalDate currDate = LocalDate.now();    
    final Path filePath = buildFilePath(currDate);
    mCurrDate = currDate;
    
    String content = null;
    
    if(Files.exists(filePath)) {
      try {
        final byte[] buff = Files.readAllBytes(filePath);
        content = new String(buff, "UTF8");        
      }
      catch(IOException ex) {   
        System.out.println("Не удалось считать лог-файл (" +
         filePath.toString() + ")");
        System.out.println(ex.getMessage());
      }
    }            

    final LogDay logDay = new LogDay();
    logDay.setDate(currDate);
    if(content != null) {
      logDay.setContent(content);
    }
    
    synchronized(mLogDays) {
      mLogDays.clear();
      mLogDays.add(logDay);
    }
  }
  

  private void createOutputStream(Path filePath) {
    final BufferedOutputStream outputStream = mOutputStream;
    mOutputStream = null;
    
    if(outputStream != null) {
      try {
        outputStream.close();
      }
      catch(IOException ex) {        
        System.out.println(ex);
      }
    }
    
    final File file = filePath.toFile();
    final Path folder = file.getParentFile().toPath();
    if(!Files.exists(folder)) {
      try {
        Files.createDirectories(folder);
      }
      catch(IOException ex) {  
        System.out.println(ex);       
      }
    }
    
    try {
      mOutputStream = 
       new BufferedOutputStream(new FileOutputStream(file, true));
    }
    catch(Exception ex) {      
      System.out.println(ex);
    }
  }
  

  public void removeOld() {
    synchronized(mLogDays) {
      final int count = mLogDays.size();
      for(int i = 0; i < count - DAYS_MAX; i++) {
        mLogDays.remove(0);
      }
    }
  }
  

  public void write(String... txt) {       
    writeImpl(false, txt);
  }
  

  public void writeln(String... txt) {  
    writeImpl(true, txt);
  }
  

  public void writeEx(Exception ex) {
    final LocalDateTime dttm = LocalDateTime.now();
    final StringBuilder builder = new StringBuilder();    
    builder.append(NL)
           .append("EXCEPTION (")
           .append(dttm.toLocalDate().toString())
           .append(" ")
           .append(dttm.toLocalTime().toString())
           .append(")")
           .append(NL) 
           .append(ex.toString())
           .append(NL); 
    final StackTraceElement[] stackTrace = ex.getStackTrace();
    for(StackTraceElement elem: stackTrace) {
      builder.append(elem.toString())
             .append(NL);
    }
    writeln(builder.toString());
  }
  

  private void writeImpl(boolean nl, String... txt) { 
    final LocalDate currDate = LocalDate.now();
    LogDay currLogDay = null;
    final StringBuilder builder = new StringBuilder();
    
    synchronized(mLogDays) {
      if(!mLogDays.isEmpty()) {
        final LogDay lastLogDay = mLogDays.get(mLogDays.size() - 1);
        if(currDate.isEqual(lastLogDay.getDate())) {
          currLogDay = lastLogDay;
        }
      }
      
      if(currLogDay == null) {
        currLogDay = new LogDay();
        currLogDay.setDate(currDate);
        mLogDays.add(currLogDay);
      }

      for(String s: txt) {
        builder.append(s);
      }
      if(nl) {
        builder.append(NL);
      }      
      
      final String builtTxt = builder.toString();
      currLogDay.appendContent(builtTxt);      
      
      if(mOutputStream == null 
         || mCurrDate == null
         || mCurrDate.getMonth() != currDate.getMonth()) {
        mCurrDate = currDate;
        createOutputStream(buildFilePath(currDate));
      }
      
      if(mOutputStream != null) {
        try {
          mOutputStream.write(builtTxt.getBytes("UTF8"));
          mOutputStream.flush();
        }
        catch(IOException ex) { 
          System.out.println(ex);
        }
      }        
    }
    
    removeOld();
  }
  

  private class LogDay {
    LocalDate mDate;
    final StringBuilder mContent = new StringBuilder();
    

    LocalDate getDate() {
      return mDate;
    }
    

    void setDate(LocalDate date) {
      mDate = date;
    }
    

    String getContent() {      
      return mContent.toString();
    }
    

    void setContent(String content) {
      clearContent();
      mContent.append(content);
    }
    

    void appendContent(String content) {
      mContent.append(content);
    }
    

    void clearContent() {
      mContent.setLength(0);
    }
  }
}
