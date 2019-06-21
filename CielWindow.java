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

public class CielWindow extends Application implements Initializable 
{   public Button Submit;
    public Pane cielArea;

// mapping between model object and graph object
// used to quickly find controller
    private Map<Etoile,EtoileControl> etoileControls = new HashMap<>();
    private Map<Align,QuadCurve> alignControls = new HashMap<>();
   
    private VBox backgroundPopUp;
    private VBox starPopUp;

// for linking operation
    private QuadCurve newAlignCurve;
    private EtoileControl fromStar;
    private EtoileControl nearsetStar;

    public static void CielWindow(String[] args) 
    {   launch(args);
    }

    public void initialize(URL location, ResourceBundle resources) 
    {   backgroundSetUp();
        Etoile star0 = new Etoile("Node");
        loadOneStar(star0,"Etoile.fxml");

        cielMouseSetUp();
        popUpMenu();
    }

//background must be the first element of cielarea
    private void backgroundSetUp()
    {   Rectangle background = new Rectangle(1300,900,Color.WHITE);
        backgroundMouseSetUp(background);
        cielArea.getChildren().add(background);
    }

    private void popUpMenu()
    {   backgroundPopUp = new VBox();
        starPopUp = new VBox();
        addingButton();
    }
    private void addingButton()
    {   Button adding = new Button("add");
        adding.setOnAction(e->addingOperation());
        backgroundPopUp.getChildren().add(adding);
    }
    private void addingOperation()
    {   EtoileControl controller = loadOneStar(new Etoile("empty"),"Etoile.fxml");
        double x = backgroundPopUp.getLayoutX(); double y = backgroundPopUp.getLayoutY();
        controller.updateStarPos(new Coordination(x,y));
        cielArea.getChildren().remove(backgroundPopUp);
    }

// this could be refined by only updating relevant aligns 
    private void updateAllAlign()
    {  
    }
    private void updateAlign(Align align, QuadCurve curve)
    {  
    }
    private void drawOneAlign(Align align)
    {   Coordination from = align.getFromStar().getCoordination(); 
        Coordination to = align.getToStar().getCoordination();

        QuadCurve curveLine = new QuadCurve();
        setCurveCoor(curveLine,from,to);
        cielArea.getChildren().add(curveLine);
        alignControls.put(align,curveLine);
    }

    private void setCurveCoor(QuadCurve curveLine, Coordination from, Coordination to)
    {   //double deltaX = to.getX()-from.getX();
        //double deltaY = to.getY()-from.getY();
        curveLine.endXProperty().bind(to.getXProperty().subtract(from.getXProperty()));
        curveLine.endYProperty().bind(to.getYProperty().subtract(from.getYProperty()));
        curveLine.layoutXProperty().bind(from.getXProperty());
        curveLine.layoutYProperty().bind(from.getYProperty());
        curveLine.setStartX(0);
        curveLine.setStartY(0);
        curveLine.setControlX(50);
        curveLine.setControlX(50);
        curveLine.setFill(null);
        curveLine.setStroke(Color.BLACK);
    }


    private EtoileControl loadOneStar(Etoile star, String viewName)
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(viewName));
        EtoileControl controller = new EtoileControl(star,etoileControls,cielArea);
        fxmlLoader.setController(controller);
        try 
        {   Node starNode = fxmlLoader.load();
            etoileControls.put(star,controller);
            putStarOnSky(starNode);
            starSetUp(controller);
            return controller;
        }
        catch(Exception e) {  e.printStackTrace(); return null; }
    }

    private void putStarOnSky(Node starNode)
    {   cielArea.getChildren().add(starNode);
    }


    @Override
    public void start(Stage primaryStage) throws Exception
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CielWindow.fxml"));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Ciel");
        primaryStage.setScene(new Scene(root, 1300, 900));
        primaryStage.show();
    }

    private void starSetUp(EtoileControl controller)
    {   starMouseSetUp(controller);
    }

// child star should not be dragged 
// and ideally have a different mouse behavior
    private void starMouseSetUp(EtoileControl controller)
    {   controller.getPrimaryView().setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getEventType()!= MouseEvent.MOUSE_DRAGGED) return;
            if(controller.getEtoile().isSubStar()) return;
            Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
            controller.updateStarPos(newCoor);
            updateAllAlign();
        }
        });

        controller.getPrimaryView().setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.SECONDARY && event.getClickCount() == 1)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));         
                popStarPopUp(newCoor,controller);
            }
            if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   controller.updateStarPos(controller.getCoordination());
                updateAllAlign();
            }
        }
        });     
    }

    private void popStarPopUp(Coordination coor, EtoileControl etoileController)
    {   starPopUp.relocate(coor.getX(),coor.getY());
        starPopUp.getChildren().clear();
        if(!cielArea.getChildren().contains(starPopUp))
        cielArea.getChildren().add(starPopUp);
        
        Button linking = new Button("link");
        linking.setOnAction(e->linkingOperation(etoileController));
        starPopUp.getChildren().add(linking);
        Button addChild = new Button("addChild");
        addChild.setOnAction(e->branchingOperation(etoileController));
        starPopUp.getChildren().add(addChild);
    }
    private void linkingOperation(EtoileControl etoileController)
    {   Coordination from = etoileController.getCoordination();
        newAlignCurve = new QuadCurve();
        fromStar = etoileController;
        setCurveCoor(newAlignCurve,from,from);
        cielArea.getChildren().add(newAlignCurve);
        cielArea.getChildren().remove(starPopUp);
    }
    private void branchingOperation(EtoileControl parentStar)
    {   EtoileControl newStar = loadOneStar(new Etoile("empty"),"EtoileSub.fxml");
        newStar.getEtoile().becomeSubStar(parentStar.getEtoile());
        parentStar.addChild(newStar);
        cielArea.getChildren().remove(starPopUp);
    }
   
    private Coordination getCielRelativeCoor(Coordination sceneCoor)
    {   Bounds cielBound = cielArea.localToScene(cielArea.getBoundsInLocal());
        double cielX = sceneCoor.getX() - cielBound.getMinX(); 
        double cielY = sceneCoor.getY() - cielBound.getMinY();
        return new Coordination(cielX,cielY);
    }

    private void cielMouseSetUp()
    {   cielArea.setOnMouseMoved(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(newAlignCurve==null) return;
            Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY())); 
            Coordination from = new Coordination(newAlignCurve.getLayoutX(),newAlignCurve.getLayoutY());
            setCurveCoor(newAlignCurve,from,newCoor);
            focuseEffect(newCoor);
        }
        });

        cielArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   cielArea.getChildren().remove(starPopUp);
                cielArea.getChildren().remove(backgroundPopUp);
                
                if(newAlignCurve==null) return;
                Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));         
                alignOneStar(newCoor);
            }
        }
        });     
    }

    private void focuseEffect(Coordination newCoor)
    {   EtoileControl newNearsetStar = findNearestStar(newCoor.getX(),newCoor.getY());
        if(newNearsetStar == nearsetStar) return;
        if(nearsetStar != null) nearsetStar.getPrimaryView().setEffect(null);
        newNearsetStar.getPrimaryView().setEffect(new Glow(0.8));
        nearsetStar = newNearsetStar;
    }

    private void backgroundMouseSetUp(Node background)
    {   background.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.SECONDARY && event.getClickCount()==1)
            {   double x = event.getX();
                double y = event.getY();
                if(!cielArea.getChildren().contains(backgroundPopUp))
                cielArea.getChildren().add(backgroundPopUp);
                backgroundPopUp.relocate(x,y);
            }
        }
        });
    }

    private void alignOneStar(Coordination targetCoor)
    {   Align newAlign = new Align(fromStar.getEtoile(),nearsetStar.getEtoile());
        nearsetStar.getPrimaryView().setEffect(null);
        drawOneAlign(newAlign);
        cielArea.getChildren().remove(newAlignCurve);
        newAlignCurve = null;
        fromStar = null;
    }

    private EtoileControl findNearestStar(double x, double y) 
    {   Point2D pClick = new Point2D(x, y);
        pClick = cielArea.localToScene(pClick);
        EtoileControl nearestEtoile = null;
        double closestDistance = Double.POSITIVE_INFINITY;

        for (Map.Entry<Etoile,EtoileControl> pair : etoileControls.entrySet()) 
        {   
            Node etoilePrimaryNode = pair.getValue().getPrimaryView();
            Bounds bounds = etoilePrimaryNode.localToScene(etoilePrimaryNode.getBoundsInLocal());

            Point2D[] corners = new Point2D[] {
                    new Point2D((bounds.getMinX()+bounds.getMaxX())/2
                                , (bounds.getMinY()+bounds.getMaxY())/2),
                    // new Point2D(bounds.getMaxX(), bounds.getMinY()),
                    // new Point2D(bounds.getMaxX(), bounds.getMaxY()),
                    // new Point2D(bounds.getMinX(), bounds.getMaxY()),
            };

            for (Point2D pCompare: corners) {
                double nextDist = pClick.distance(pCompare);
                if (nextDist < closestDistance) {
                    closestDistance = nextDist;
                    nearestEtoile = pair.getValue();
                }
            }
        }

       return nearestEtoile;
   }

}
