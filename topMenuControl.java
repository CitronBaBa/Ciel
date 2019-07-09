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
import javafx.scene.paint.*;
import javafx.scene.effect.*;

public class topMenuControl
{   private CielControl cielControl;
    private HBox menuPanel;

    public Node getPanel()
    {   return menuPanel;
    }

    public topMenuControl(CielControl cielControl)
    {   this.cielControl = cielControl;
        initialization();
    }

    private void initialization()
    {   menuPanel = new HBox();
        Button btn0 = new Button("Save");
        btn0.setOnAction(e->saving());
        Button btn1 = new Button("Read");
        btn1.setOnAction(e->reading());
        menuPanel.getChildren().addAll(btn0,btn1);
    }

    private void saving()
    {   Ciel cielModel = cielControl.getCielModel();
        FileSystem fileHandler = new FileSystem("./data/");
        fileHandler.writeObjectTo("testing",cielModel);
    }

    private void reading()
    {   FileSystem fileHandler = new FileSystem("./data/");
        Ciel cielModel = (Ciel) fileHandler.readObjectFrom("testing");
        cielControl.loadFromCielModel(cielModel);
    }
    
}   
