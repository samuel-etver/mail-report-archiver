package reportarchiver;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light.Distant;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class AboutDialog extends Stage {
  private final static String PROPERTY_X = "x";
  private final static String PROPERTY_Y = "y";
  
  private final Button mOkBtn = new Button("Закрыть");
  private final Label mProgramLbl = new Label("Архиватор отчетов");
  private final Label mDescriptionLbl = new Label();
  private final Label mVersionLbl = new Label(Common.APP_VERSION);

  
  private AboutDialog() {      
  }
  

  public static AboutDialog createDialog(Map<String, Object> storage) {
    final AboutDialog dlg = new AboutDialog();
    dlg.setTitle("О программе");
    dlg.setResizable(false); 
    
    final DropShadow ds = new DropShadow();
    ds.setOffsetX(3.0);
    ds.setColor(Color.color(0.3, 0.3, 0.3));
    final Distant light = new Distant();
    light.setAzimuth(-135.0);
    Lighting lighting = new Lighting();
    lighting.setLight(light);
    lighting.setSurfaceScale(5.0);
    ds.setInput(lighting);
    
    dlg.mProgramLbl.setTextFill(Color.color(0.6, 0, 0));
    dlg.mProgramLbl.setFont(Font.font("Arial", FontWeight.BOLD, 32));
    dlg.mProgramLbl.setEffect(ds);
    
    dlg.mDescriptionLbl.setStyle(
      "-fx-background-color: rgba(0,0,0,0.75)," +
      "linear-gradient(to bottom,#666666 0%, #aaaaaa 15%, #cccccc 100%);" +
      "-fx-padding: 7px;" +
      "-fx-background-radius: 7px;" +
      "-fx-font-size: 13px;" +
      "-fx-font-family: \"Arial\";" +  
      "-fx-font-weight: lighter;" +        
      "-fx-text-fill: black;"        
    );
    dlg.mDescriptionLbl.setText(
      "Программа архивирует отчеты дежурных инженеров,\n" + 
      "присылаемых на почту сервера");
    dlg.mDescriptionLbl.setWrapText(true);
    
    dlg.mVersionLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    dlg.mVersionLbl.setEffect(ds);
    
    
    final VBox vbox = new VBox();
    vbox.getChildren().addAll(
      dlg.mProgramLbl, dlg.mDescriptionLbl, dlg.mVersionLbl, dlg.mOkBtn      
    );
    vbox.setAlignment(Pos.TOP_CENTER);
    vbox.setSpacing(10);
    vbox.setPadding(new Insets(10, 10, 10, 10));
    
    dlg.mOkBtn.setDefaultButton(true);
    dlg.mOkBtn.setOnAction(e -> dlg.hide());
    
    final Scene scene = new Scene(vbox);
    
    dlg.setScene(scene);    

    dlg.sizeToScene();
    
    Double dlgX = null;
    Double dlgY = null;
    if (storage != null) {        
      dlgX = Common.toDouble(storage.get(PROPERTY_X));
      dlgY = Common.toDouble(storage.get(PROPERTY_Y));
    }
    
    if (dlgX == null || dlgY == null) {
      dlg.centerOnScreen();
    }
    else {
      dlg.setX(dlgX);
      dlg.setY(dlgY);
    }
    
    return dlg;
  }
  
  
  public void saveSettings(HashMap<String, Object> storage) {      
    storage.put(PROPERTY_X, getX());
    storage.put(PROPERTY_Y, getY());
  }
}
