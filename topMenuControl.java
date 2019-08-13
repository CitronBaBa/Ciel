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
import javafx.scene.image.*;
import javafx.beans.binding.*;
import javafx.beans.value.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

public class topMenuControl
{   private CielControl cielControl;
    private GlobalSatellite globals;
    //fxml
    public Node menuPanel;
    public MenuItem save;
    public MenuItem saveAs;
    public MenuItem saveAsPhoto;
    public MenuItem read;
    public MenuItem readJava;
    public MenuItem remove;
    public MenuItem arrange;

    public Slider slider;
    private void sliderSettings()
    {   slider.setMax(0.8f);
        slider.setMin(0.35f);
        slider.setValue(0.7f);
    }

    public Node getPanel()
    {   return menuPanel;
    }
    public DoubleProperty getSlideValue()
    {   return slider.valueProperty();
    }

    public topMenuControl(CielControl cielControl) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("topMenu.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.load();
        this.cielControl = cielControl;
        this.globals = GlobalSatellite.getSatellite();
        initialization();
        menuPanel.setId("top-menu");
    }

    private void initialization()
    {   save.setOnAction(e->initialSave());
        saveAs.setOnAction(e->savingAs());
        read.setOnAction(e->reading());
        remove.setOnAction(e->removing());
        readJava.setOnAction(e->readingjava());
        arrange.setOnAction(e->cielControl.getRobot().arrangeAllStars());
        saveAsPhoto.setOnAction(e->savingPhoto());
        sliderSettings();
    }

    public void initialSave()
    {   if(globals.getFilePath()==null) savingAs();
        else saving(globals.getFilePath());
    }

    private void savingAs()
    {   String path = askSaveFileName("./data/","Ciel File","*.data");
        if(path==null) return;
        saving(path);
        globals.setFilePath(path);
    }

    private void saving(String path)
    {   if(path==null) return;
        if(!path.endsWith(".data")) path = path + ".data";
        HoustonCenter.propagateEvent(CielEvent.SaveModel);
        FileSystem fileHandler = new FileSystem("");
        Ciel cielModel = globals.getCielModel();
        fileHandler.writeObjectTo(path,cielModel);
    }

    private String askSaveFileName(String defaultDir,String name, String extension)
    {   FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(defaultDir));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(name, extension));
        File selectedFile = fileChooser.showSaveDialog(globals.getStage());
        if(selectedFile==null) return null;
        return selectedFile.getPath();
    }

    private void reading()
    {   String path = askReadFileName();
        if(path==null) return;
        FileSystem fileHandler = new FileSystem("");
        Ciel cielModel = (Ciel) fileHandler.readObjectFrom(path);
        updateModel(cielModel);
        globals.setFilePath(path);
    }
    private String askReadFileName()
    {   FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./data/"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Ciel File", "*.data"));
        File selectedFile = fileChooser.showOpenDialog(globals.getStage());
        if(selectedFile==null) return null;
        return selectedFile.getPath();
    }

    private void readingjava()
    {   List<File> javaFiles = askReadJavaFiles();
        if(javaFiles==null) return;
        Ciel cielModel = new Ciel();

        // a nasty quick way to set parse environment
        // ideally should open another dialog
        File parseDir = javaFiles.get(0).getParentFile();
        cielModel.readJavaFiles(javaFiles,parseDir);
        updateModel(cielModel);
        Platform.runLater(()->{cielControl.getRobot().arrangeAllStars();});
    }

    private List<File> askReadJavaFiles()
    {   FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("java File", "*.java"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(globals.getStage());
        if(selectedFiles==null) return null;
        return selectedFiles;
    }

    private void updateModel(Ciel cielModel)
    {   globals.setCielModel(cielModel);
        HoustonCenter.clearActionList();
        HoustonCenter.propagateEvent(CielEvent.LoadNewModel);
        cielControl.loadFromCielModel(cielModel);
    }

    private void removing()
    {   cielControl.removeSelected();
    }
    
    private void savingPhoto()
    {   File dir = new File ("./data/image/");
        if(!dir.exists()) dir.mkdir();
        String path = askSaveFileName("./data/image/","png file","*.png");
        if(path==null) return;
        if(!path.endsWith("png")) path +=".png";
        final File outputFile = new File(path);

        Node target = cielControl.getCielArea();
        WritableImage wImage = null;//new WritableImage(600,800);
        target.snapshot( (p)->
        {   writePhotoFile(p.getImage(),outputFile);
            return null;
        }
        ,new SnapshotParameters(),wImage);
    }

    private void writePhotoFile(Image image,File outputFile)
    {   BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {   ImageIO.write(bImage, "png", outputFile);} 
        catch (IOException e) 
        {   throw new RuntimeException(e);
        }
    }

}
