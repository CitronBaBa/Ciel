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
{   private VBox realm;
    private EtoileControl targetEtoile;
    private CielControl cielControl;
    private GlobalSatellite globals;

    private JavaArea javaArea;
    //fxml
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
    {   globals = GlobalSatellite.getSatellite();
        this.cielControl = cielControl;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TextRealm.fxml"));
        fxmlLoader.setController(this);
        realm = fxmlLoader.load();
        HoustonCenter.subscribe(this);

        codeAreaSetUp();
        dynamicSizing();
    }
    public Region getRealm() {  return realm;}

    private void codeAreaSetUp()
    {   javaArea = new JavaArea();
        realm.getChildren().add(0,javaArea.getArea());
        //String code = javaArea.getCode();
    }

    private void dynamicSizing()
    {   javaArea.getArea().prefWidthProperty().bind(realm.prefWidthProperty());
        javaArea.getArea().prefHeightProperty().bind(realm.prefHeightProperty());
    }

    private void changeStar ()
    {   targetEtoile = cielControl.getSelectedStar();
        if(targetEtoile!=null)
        {   javaArea.loadText(targetEtoile.getEtoile().getText());
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
        targetEtoile.getEtoile().setText(javaArea.getCode());
    }

}
