package reportarchiver;

import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class StatusDialog extends Stage {
  private final static String PROPERTY_X = "x";
  private final static String PROPERTY_Y = "y";
  private final static String PROPERTY_W = "w";
  private final static String PROPERTY_H = "h";
  
  private final TextArea mStatusTextArea = new TextArea();
  private final Button mReadBtn = new Button("Считать");  
  private final Button mCloseBtn = new Button("Закрыть");
  private EventHandler<ActionEvent> mReadEventHandler;
  
  
  private StatusDialog() {      
  }
  

  private void onCloseClick() {
    close();
  }
  

  private void onReadClick() {
    if (mReadEventHandler != null)
      mReadEventHandler.handle(null);
  }
  

  public void setOnRead(EventHandler<ActionEvent> handler) {
    mReadEventHandler = handler;
  }
  

  public static StatusDialog createDialog(Map<String, Object> storage, boolean enableReadButton) {
    final StatusDialog dlg = new StatusDialog();
    
    dlg.setTitle("Статус");
    
    final TextArea ta = dlg.mStatusTextArea;
    ta.setEditable(false);

    dlg.mReadBtn.setOnAction(e -> dlg.onReadClick());
    dlg.mCloseBtn.setDefaultButton(true);
    dlg.mCloseBtn.setOnAction(e -> dlg.onCloseClick());
    
    final HBox btnsBox = new HBox();
    btnsBox.setSpacing(8);
    btnsBox.setPadding(new Insets(8, 8, 8, 8));
    btnsBox.setAlignment(Pos.BASELINE_RIGHT);
    
    btnsBox.getChildren().addAll(dlg.mReadBtn, dlg.mCloseBtn);
    
    
    final BorderPane rootLa = new BorderPane();
    
    rootLa.setCenter(dlg.mStatusTextArea);
    rootLa.setBottom(btnsBox);
    
    final Scene scene = new Scene(rootLa, 480, 400);    
    scene.getStylesheets().add("resources/Status.css");
    dlg.setScene(scene);
    
    Double dlgX = null;
    Double dlgY = null;
    Double dlgW = null;
    Double dlgH = null;
    if(storage != null) {        
      dlgX = Common.toDouble(storage.get(PROPERTY_X));
      dlgY = Common.toDouble(storage.get(PROPERTY_Y));
      dlgW = Common.toDouble(storage.get(PROPERTY_W));
      dlgH = Common.toDouble(storage.get(PROPERTY_H));
    }

    if (dlgX == null || dlgY == null) {
      dlg.centerOnScreen();
    }
    else {
      dlg.setX(dlgX);
      dlg.setY(dlgY);
      if (dlgW != null) {
        dlg.setWidth(dlgW);
      }
      if (dlgH != null) {
        dlg.setHeight(dlgH);
      }
    }
    
    dlg.enableReadButton(enableReadButton);
    
    return dlg;
  }


  public void updateStatusText(String txt) {
    mStatusTextArea.setText(txt);
    mStatusTextArea.positionCaret(txt.length());
    mStatusTextArea.setScrollLeft(0);
    mStatusTextArea.setScrollTop(Double.MAX_VALUE);
  }
  

  public void enableReadButton(boolean enable) {
    mReadBtn.setDisable(!enable);
  }
  
  
  public void saveSettings(HashMap<String, Object> storage) {
    storage.put(PROPERTY_X, getX());
    storage.put(PROPERTY_Y, getY());
    storage.put(PROPERTY_W, getWidth());
    storage.put(PROPERTY_H, getHeight());      
  }
}
