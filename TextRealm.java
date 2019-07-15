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
public class TextRealm implements Initializable,CielEventSubscriber
{   private Node realm;
    EtoileControl targetEtoile;
    private CielControl cielControl;

    //fxml
    public TextArea textArea;
    public Button save;

    public void initialize(URL location, ResourceBundle resources) 
    {   saveButton();
    }

    public void reactOnEvent(CielEvent event)
    {   if(event == CielEvent.ChangeFocus)
        {   changeStar();
        }
    }

    public TextRealm(CielControl cielControl) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TextRealm.fxml"));
        fxmlLoader.setController(this);
        realm = fxmlLoader.load();
        this.cielControl = cielControl;
        HoustonCenter.subscribe(this);
    }
    public Node getRealm() {  return realm;}

    private void changeStar ()
    {   targetEtoile = cielControl.getSelectedStar();
        if(targetEtoile!=null)
        {   textArea.setText(targetEtoile.getEtoile().getText());
        }
        else deactivate();
    }
    private void deactivate()
    {   ;
    }

    private void saveButton()
    {   save.setOnAction(e->saveText());
    }
    private void saveText()
    {   if(targetEtoile==null) return;
        targetEtoile.getEtoile().setText(textArea.getText());
    }
    
}   
