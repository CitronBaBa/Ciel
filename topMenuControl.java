import java.util.*;
import java.io.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.input.*;
import javafx.scene.Scene;
import javafx.stage.*;
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
    private GlobalSatellite globals;
    //fxml
    public Node menuPanel;
    public MenuItem save;
    public MenuItem read;

    public MenuItem remove;

    public Node getPanel()
    {   return menuPanel;
    }

    public topMenuControl(CielControl cielControl) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("topMenu.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.load();
        this.cielControl = cielControl;
        this.globals = GlobalSatellite.getSatellite();
        initialization();
    }

    private void initialization()
    {   save.setOnAction(e->saving());
        read.setOnAction(e->reading());
        remove.setOnAction(e->removing());
    }

    private void saving()
    {   Ciel cielModel = cielControl.getCielModel();
        String path = askSaveFileName();
        if(path==null) return;
        FileSystem fileHandler = new FileSystem("");
        fileHandler.writeObjectTo(path,cielModel);
    }
    private String askSaveFileName()
    {   FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./data/"));
        File selectedFile = fileChooser.showSaveDialog(globals.getStage());
        if(selectedFile==null) return null;
        return selectedFile.getPath();
    }

    private void reading()
    {   String path = askReadFileName();
        if(path==null) return;
        FileSystem fileHandler = new FileSystem("");
        Ciel cielModel = (Ciel) fileHandler.readObjectFrom(path);
        cielControl.loadFromCielModel(cielModel);
    }
    private String askReadFileName()
    {   FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./data/"));
        File selectedFile = fileChooser.showOpenDialog(globals.getStage());
        if(selectedFile==null) return null;
        return selectedFile.getPath();
    }


    private void removing()
    {   cielControl.removeSelected();
    }

}
