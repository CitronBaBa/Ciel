import java.util.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.input.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.shape.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.*;

// a subscription system may be introduced for the interaction
// between this and cielControl 
public class TextRealm implements Initializable
{   private Node realm;
    EtoileControl targetEtoile;
    //fxml
    public TextArea textArea;
    public Button save;
    public void initialize(URL location, ResourceBundle resources) 
    {   saveButton();
    }

    public TextRealm() throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TextRealm.fxml"));
        fxmlLoader.setController(this);
        realm = fxmlLoader.load();
    }
    public Node getRealm() {  return realm;}
    public void changeStar (EtoileControl newEtoile)
    {   targetEtoile = newEtoile;
        textArea.setText(newEtoile.getEtoile().getText());
    }
    private void saveButton()
    {   save.setOnAction(e->saveText());
    }
    private void saveText()
    {   targetEtoile.getEtoile().setText(textArea.getText());
    }
    
}   
