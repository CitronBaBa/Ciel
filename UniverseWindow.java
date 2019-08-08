import java.util.*;
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
import javafx.scene.paint.Color;
import javafx.scene.effect.*;
import javafx.beans.binding.*;
import javafx.beans.value.*;

/// develop notes:
/*  javafx calculates everything(layout, coordinate) after executing your program
    it also calculates the layout only after it is added to the scene
    or when you specifically tells it to
    not when it is added to its parent in fxml
*/
public class UniverseWindow extends Application implements Initializable
{   private GlobalSatellite globals;
    //fxml
    public Pane cielArea;
    public VBox cielBox;
    public ScrollPane cielScrolPane;
    public BorderPane root;

    //main parts
    private SplitPane splitView;
    private CielControl cielControl;
    private TextRealm textRealm;
    private StylePanel stylePanel;
    private topMenuControl topMenu;

    public static void UniverseWindow(String[] args)
    {   launch(args);
    }
    public void initialize(URL location, ResourceBundle resources)
    {   ;
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CielWindow.fxml"));
        fxmlLoader.setController(this);
        Parent view = fxmlLoader.load();

        Scene scene = new Scene(view, 1100, 800);
        scene.getStylesheets().add("style/style.css");
        scene.getStylesheets().add("style/javaKeyword.css");
        keyControl(scene);
        primaryStage.setTitle("Ciel");
        primaryStage.setScene(scene);
        globals = GlobalSatellite.getSatellite();
        globals.setStage(primaryStage);

        cielControl = new CielControl(cielArea, cielBox, cielScrolPane);
    
        textRealm = new TextRealm(cielControl);
        
        splitView = new SplitPane(cielScrolPane,textRealm.getRealm());
        root.setCenter(splitView);

        stylePanel = new StylePanel(cielControl);
        root.setBottom(stylePanel.getPanel());

        topMenu = new topMenuControl(cielControl);
        BorderPane.setAlignment(topMenu.getPanel(),Pos.CENTER_RIGHT);
        root.setTop(topMenu.getPanel());

        dynamicSizing();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.show();
    }

    private void dynamicSizing()
    {   //Scene mainScene = globals.getStage().getScene();
        //ObservableDoubleValue slideValue = topMenu.getSlideValue();
        //textRealm.getRealm().prefWidthProperty().bind(mainScene.widthProperty().multiply(
            // Bindings.subtract(1,slideValue)    ));
        //textRealm.getRealm().maxHeightProperty().bind(cielScrolPane.prefHeightProperty());
        //cielScrolPane.prefWidthProperty().bind(mainScene.widthProperty().multiply(slideValue));
    }

    private void keyControl(Scene scene)
    {   final KeyCombination keyCombinationShift1 = new KeyCodeCombination(
        KeyCode.Z, KeyCombination.CONTROL_DOWN);
        final KeyCombination keyCombinationShift2 = new KeyCodeCombination(
            KeyCode.Z, KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
        public void handle(KeyEvent event)
        {   if(keyCombinationShift2.match(event))
            HoustonCenter.redoAction();
            if(keyCombinationShift1.match(event))
            HoustonCenter.undoAction();
        }});
    }
}
