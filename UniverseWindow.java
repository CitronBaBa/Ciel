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

// this is the main window 
// it holds refernce of all UI components' controllers



/// develop notes:
/*  javafx calculates everything(layout, coordinate) after executing your program
    it also calculates the layout only after it is added to the scene
    or when you specifically tells it to
    not when it is added to its parent in fxml
*/
public class UniverseWindow extends Application implements Initializable,CielEventSubscriber
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
    public void reactOnEvent(CielEvent event)
    {
        /* dynamic textRealm display, currently not used*/
        // if(event == CielEvent.ChangeFocus)
        // {   if(cielControl.getSelectedStar()==null)
        //     splitView.getItems().remove(textRealm.getRealm());
        //     else if(!splitView.getItems().contains(textRealm.getRealm()))
        //     {   splitView.getItems().add(textRealm.getRealm());
        //         setUpDivder();
        //     }
        // }
        // if(event == CielEvent.LoadNewModel)
        // {   splitView.getItems().remove(textRealm.getRealm());
        // }
    }

// this part can be prettified 
    @Override
    public void start(Stage primaryStage) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CielWindow.fxml"));
        fxmlLoader.setController(this);
        Parent view = fxmlLoader.load();

        Scene scene = new Scene(view, 1100, 800);
        scene.getStylesheets().add("style/style.css");
        scene.getStylesheets().add("style/cielStyle.css");
        scene.getStylesheets().add("style/javaKeyword.css");

        keyControl(scene);
        HoustonCenter.subscribe(this);
        primaryStage.setTitle("Ciel");
        primaryStage.setScene(scene);

        globals = GlobalSatellite.getSatellite();
        globals.setStage(primaryStage);

        cielControl = new CielControl(cielArea, cielBox, cielScrolPane);

        textRealm = new TextRealm(cielControl);

        StackPane ciellayers = new StackPane();
        AnchorPane controlAnchors = new AnchorPane();
        controlAnchors.setPickOnBounds(false);
        ciellayers.getChildren().addAll(cielScrolPane,controlAnchors);

        splitView = new SplitPane(ciellayers,textRealm.getRealm());
        root.setCenter(splitView);


        stylePanel = new StylePanel(cielControl);
        AnchorPane.setBottomAnchor(stylePanel.getPanel(),0d);
        AnchorPane.setLeftAnchor(stylePanel.getPanel(),0d);
        controlAnchors.getChildren().add(stylePanel.getPanel());

        topMenu = new topMenuControl(cielControl);
        BorderPane.setAlignment(topMenu.getPanel(),Pos.CENTER_RIGHT);
        root.setTop(topMenu.getPanel());

        setUpDivder();
        dynamicSizing();
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.show();
    }

    private void setUpDivder()
    {   if(splitView.getDividers().size()==0) return;
        SplitPane.Divider divd = splitView.getDividers().get(0);
        divd.positionProperty().bindBidirectional(topMenu.getSlideValue());
    }

    private void dynamicSizing()
    {    
        // topMenu.getSlideValue().addListener((obs,oldV,newV)->
        //  {   if(Math.abs((double)newV-1.0f)<0.0001)
        //       {   splitView.getItems().remove(textRealm.getRealm());
        //       }
        //       else if(!splitView.getItems().contains(textRealm.getRealm()))
        //       {   splitView.getItems().add(textRealm.getRealm());
        //           setUpDivder();
        //       }
        //  });
    }

    private void keyControl(Scene scene)
    {   final KeyCombination keyCombinationShift1 = new KeyCodeCombination(
        KeyCode.Z, KeyCombination.CONTROL_DOWN);
        final KeyCombination keyCombinationShift2 = new KeyCodeCombination(
            KeyCode.Z, KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN);
        final KeyCombination keyCombinationShift3 = new KeyCodeCombination(
        KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
        public void handle(KeyEvent event)
        {   if(keyCombinationShift2.match(event))
            HoustonCenter.redoAction();
            if(keyCombinationShift1.match(event))
            HoustonCenter.undoAction();
            if(keyCombinationShift3.match(event))
            topMenu.initialSave();
        }});

        cielScrolPane.addEventFilter(KeyEvent.KEY_PRESSED,e->{
            EtoileControl target = cielControl.getSelectedStar();
            if(target==null) return;
            if(e.getCode()==KeyCode.UP)
            {   target.shiftInChildren(true);
                e.consume();
            }
            if(e.getCode()==KeyCode.DOWN)
            {   target.shiftInChildren(false);
                e.consume();
            }
        });
    }

}
