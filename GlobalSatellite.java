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

//holding global value
public class GlobalSatellite
{   private static GlobalSatellite satellite;

    //globals
    private Stage mainStage;

    private GlobalSatellite()
    {  ;
    }

    public static GlobalSatellite getSatellite()
    {   if(satellite == null) satellite = new GlobalSatellite();
        return satellite;
    }

    public Stage getStage() {  return mainStage;}
    public void setStage(Stage mainStage) {  this.mainStage = mainStage;}

}
