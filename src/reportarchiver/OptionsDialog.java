package reportarchiver;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class OptionsDialog extends Stage {
  private final static String PROPERTY_X = "x";
  private final static String PROPERTY_Y = "y";  
  
  private final Label mEmailHostLbl = new Label();
  private final Label mEmailUserLbl = new Label();
  private final Label mEmailPasswordLbl = new Label();
  private final Label mEmailSenderLbl = new Label();
  private final Label mEmailArchivePathLbl = new Label();
  private final TextField mEmailHostEdit = new TextField();
  private final TextField mEmailUserEdit = new TextField();
  private final PasswordField mEmailPasswordEdit = new PasswordField();
  private final TextField mEmailSenderEdit = new TextField();
  private final TextField mEmailArchivePathEdit = new TextField();
  private final Button mOkBtn = new Button();
  private final Button mCancelBtn = new Button();
  
  
  private OptionsDialog() {      
  }
  

  private void onOkClick() {
    Common.sEmailHost = mEmailHostEdit.getText().trim();
    Common.sEmailUser = mEmailUserEdit.getText().trim();
    Common.sEmailPassword = mEmailPasswordEdit.getText().trim();
    Common.sEmailSender = mEmailSenderEdit.getText().trim();
    Common.sEmailArchivePath = mEmailArchivePathEdit.getText().trim();
    
    close();
    Common.save();
  }
  

  private void onCancelClick() {
    close();
  }
  

  public static OptionsDialog createDialog(Map<String, Object> storage) {
    final OptionsDialog dlg = new OptionsDialog();
    
    dlg.setTitle("Настройки");
    dlg.getIcons().add(Common.sPrimaryStage.getIcons().get(0));
    dlg.setResizable(false);
    
    final VBox mainLa = new VBox();
    
    final Scene scene = new Scene(mainLa);    
    
    final GridPane fieldsLa = new GridPane();
    fieldsLa.setVgap(8);
    fieldsLa.setHgap(8);
    fieldsLa.setPadding(new Insets(8, 8, 8, 8));
    
    dlg.mEmailHostLbl.setText("Сервер почты:");
    dlg.mEmailUserLbl.setText("Логин:");
    dlg.mEmailPasswordLbl.setText("Пароль:");
    dlg.mEmailSenderLbl.setText("Отправитель:");
    dlg.mEmailArchivePathLbl.setText("Папка архива:");
    final double editPrefW = 240;
    dlg.mEmailHostEdit.setPrefWidth(editPrefW);
    dlg.mEmailUserEdit.setPrefWidth(editPrefW);
    dlg.mEmailPasswordEdit.setPrefWidth(editPrefW);
    dlg.mEmailSenderEdit.setPrefWidth(editPrefW);
    dlg.mEmailArchivePathEdit.setPrefWidth(editPrefW);
    
    dlg.mOkBtn.setText("Ввод");
    dlg.mOkBtn.setAlignment(Pos.TOP_RIGHT);
    dlg.mOkBtn.setDefaultButton(true);
    dlg.mOkBtn.setOnAction(e -> dlg.onOkClick());
    dlg.mCancelBtn.setText("Отмена");
    dlg.mCancelBtn.setAlignment(Pos.TOP_RIGHT);
    dlg.mCancelBtn.setOnAction(e -> dlg.onCancelClick());
    
    fieldsLa.add(dlg.mEmailHostLbl, 0, 0);
    fieldsLa.add(dlg.mEmailUserLbl, 0, 1);
    fieldsLa.add(dlg.mEmailPasswordLbl, 0, 2);
    fieldsLa.add(dlg.mEmailSenderLbl, 0, 3);
    fieldsLa.add(dlg.mEmailArchivePathLbl, 0, 4);
    fieldsLa.add(dlg.mEmailHostEdit, 1, 0);
    fieldsLa.add(dlg.mEmailUserEdit, 1, 1);
    fieldsLa.add(dlg.mEmailPasswordEdit, 1, 2);
    fieldsLa.add(dlg.mEmailSenderEdit, 1, 3);
    fieldsLa.add(dlg.mEmailArchivePathEdit, 1, 4);
    
    
    final HBox btnsLa = new HBox();
    btnsLa.setAlignment(Pos.TOP_RIGHT);
    btnsLa.setPadding(new Insets(0, 8, 8, 8));
    btnsLa.setSpacing(8);
    btnsLa.getChildren().addAll(
      dlg.mOkBtn,
      dlg.mCancelBtn
    );
    
    mainLa.getChildren().addAll(
      fieldsLa,      
      btnsLa      
    );
    
    dlg.setScene(scene);    
    dlg.sizeToScene();
    
    dlg.mEmailHostEdit.setText(Common.sEmailHost);
    dlg.mEmailUserEdit.setText(Common.sEmailUser);
    dlg.mEmailPasswordEdit.setText(Common.sEmailPassword);
    dlg.mEmailSenderEdit.setText(Common.sEmailSender);
    dlg.mEmailArchivePathEdit.setText(Common.sEmailArchivePath);
    
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
