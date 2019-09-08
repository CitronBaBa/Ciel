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

// holding global values
// is a singleton

public class GlobalSatellite 
{   private static GlobalSatellite satellite;

    //globals
    private Stage mainStage;
    private Ciel cielModel; 
    private String filePath = null;
    public void setFilePath(String path)
    {   this.filePath = path;
        mainStage.setTitle("Ciel: "+path);
    }
    public String getFilePath() { return filePath;}

    private Map<Etoile,EtoileControl> globalStarReferences = new HashMap<>();
    public static void putStarReference(Etoile e, EtoileControl eC)
    {   getSatellite().globalStarReferences.put(e,eC);
    }
    public static void removeStarReference(Etoile e, EtoileControl eC)
    {   getSatellite().globalStarReferences.remove(e);
    }
    public static EtoileControl getStarControl(Etoile e)
    {   return getSatellite().globalStarReferences.get(e);
    }

    private GlobalSatellite()
    {   cielModel = new Ciel();
    }

    public static GlobalSatellite getSatellite()
    {   if(satellite == null) satellite = new GlobalSatellite();
        return satellite;
    }

    public Stage getStage() {  return mainStage;}
    public void setStage(Stage mainStage) {  this.mainStage = mainStage;}
    public void setCielModel(Ciel cielModel){ this.cielModel = cielModel;}
    public Ciel getCielModel(){   return cielModel;}
}
