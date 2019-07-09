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

public class CielControl 
{   //fxml
    private Pane cielArea;
    private ScrollPane cielScrolPane;
    TextRealm textRealm;

    private EtoileControl selectedEtoile;
    public EtoileControl getSelectedStar() {   return selectedEtoile;}
    private Ciel cielModel;
    public Ciel getCielModel() {   return cielModel;}

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


    public CielControl(Pane cielArea,  ScrollPane cielScrolPane, TextRealm textRealm)
    {   cielModel = new Ciel();
        this.cielArea = cielArea;
        this.textRealm = textRealm;
        this.cielScrolPane = cielScrolPane;
        initialization();
    }

    public void loadFromCielModel(Ciel cielModel)
    {   // clear up 
        removeEverything();
        
        // loading
        this.cielModel = cielModel;
        for(Etoile e : cielModel.getParentEtoiles())
        {   EtoileControl parentStar = drawOneStar(e, "Etoile.fxml");
            drawChildStar(parentStar);
        }
        for(Align a : cielModel.getAligns())
        {   drawOneAlign(a);
        }
    }
    private void drawChildStar(EtoileControl parentStar)
    {   for(Etoile eSub : parentStar.getEtoile().getChildren())
        {   EtoileControl childStar = drawOneStar(eSub,"EtoileSub.fxml");
            if(eSub.getChildren().size()>0)
            drawChildStar(childStar);
        }   
    }

    private void removeEverything()
    {   etoileControls.clear();
        alignControls.clear();
        cielArea.getChildren().clear();
        newAlignCurve = null;
        fromStar = null;
        nearsetStar = null;
        selectedEtoile = null;

        //reset background
        backgroundSetUp();
    }

    private void initialization() 
    {   backgroundSetUp();
        cielMouseSetUp();
        cielBoundSetUp();
        popUpMenu();

        Etoile star0 = new Etoile("Node");
        drawOneStar(star0, "Etoile.fxml");
    }
    private void cielBoundSetUp()
    {   cielArea.minWidthProperty().bind(cielScrolPane.widthProperty());
        cielArea.minHeightProperty().bind(cielScrolPane.heightProperty());
      // cielScrolPane.setPannable(true);
        Rectangle clipRec = new Rectangle();
        cielArea.setClip(clipRec);
        cielArea.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            //System.out.println(newValue);
            clipRec.setWidth(newValue.getWidth());
            clipRec.setHeight(newValue.getHeight());
        });  
        // cielScrolPane.setFitToHeight(true);
        // cielScrolPane.setFitToWidth(true);
    }

//background must be the first element of cielarea
    private void backgroundSetUp()
    {   Rectangle background = new Rectangle(1300,900,Color.WHITE);
        background.widthProperty().bind(cielArea.widthProperty());
        background.heightProperty().bind(cielArea.heightProperty());
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
        adding.setOnAction(e->addingStar());
        backgroundPopUp.getChildren().add(adding);
    }
    private void addingStar()
    {   double x = backgroundPopUp.getLayoutX(); 
        double y = backgroundPopUp.getLayoutY();
        addingStarOperation(new Coordination(x,y));
        cielArea.getChildren().remove(backgroundPopUp);
    }
    private void addingStarOperation(Coordination newCoor)
    {   Etoile newEtoile = new Etoile("empty");
        newEtoile.updateCoordination(newCoor);
        EtoileControl controller = drawOneStar(newEtoile,"Etoile.fxml");
    }

    private void drawOneAlign(Align align)
    {   Coordination from = align.getFromStar().getCoordination(); 
        Coordination to = align.getToStar().getCoordination();

        QuadCurve curveLine = new QuadCurve();
        setCurveCoor(curveLine,from,to);
        cielArea.getChildren().add(1,curveLine);
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


    private EtoileControl drawOneStar(Etoile star, String viewName)
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(viewName));
        EtoileControl controller = new EtoileControl(star,etoileControls,cielArea,cielModel);
        fxmlLoader.setController(controller);
        try {   fxmlLoader.load(); }
        catch(Exception e) {  e.printStackTrace(); return null; }

        starSetUp(controller);
        HoustonCenter.recordAction(new AddingAction(controller,false));
        return controller;
    }

    private void starSetUp(EtoileControl controller)
    {   starMouseSetUp(controller);
    }

// child star should not be dragged 
// and ideally have a different mouse behavior
    private void starMouseSetUp(EtoileControl controller)
    {   
        controller.getPrimaryView().setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getEventType()!= MouseEvent.MOUSE_DRAGGED) return;
            if(controller.getEtoile().isSubStar()) return;
            Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
            controller.updateStarPos(newCoor);
        }
        });

        final Coordination oldCoor = new Coordination(0,0);
        controller.getPrimaryView().setOnDragDetected(new EventHandler<MouseEvent>()
        {   public void handle(MouseEvent event)
            {   controller.getPrimaryView().startFullDrag();
            }
        });
        controller.getPrimaryView().setOnMouseDragEntered(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   oldCoor.setX(controller.getEtoile().getCoordination().getX());
                oldCoor.setY(controller.getEtoile().getCoordination().getY());
            }
        });
        // controller.getPrimaryView().setOnMouseDragOver(new EventHandler<MouseDragEvent>()
        // {   public void handle(MouseDragEvent event)
        //     {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
        //         controller.updateStarPos(newCoor);
        //     }
        // });
        controller.getPrimaryView().setOnMouseDragExited(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                Coordination oriCoor = new Coordination(oldCoor.getX(),oldCoor.getY());
                HoustonCenter.recordAction(new MovingAction(oriCoor,newCoor,controller));
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
            {  selectStar(controller);
            }
        }
        }); 
        controller.getPrimaryView().setOnMouseEntered(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   controller.addEffect();
        }
        }); 
        controller.getPrimaryView().setOnMouseExited(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   controller.removeEffect();
        }
        }); 
    }

    private void selectStar(EtoileControl targetEtoile)
    {   if(selectedEtoile == targetEtoile) return;
        if(selectedEtoile != null) selectedEtoile.removeEffect();
        selectedEtoile = targetEtoile;
        selectedEtoile.addEffect();
        textRealm.changeStar(selectedEtoile);
    }

    private void popStarPopUp(Coordination coor, EtoileControl etoileController)
    {   starPopUp.relocate(coor.getX(),coor.getY());
        starPopUp.getChildren().clear();
        if(!cielArea.getChildren().contains(starPopUp))
        cielArea.getChildren().add(starPopUp);
        
        Button linking = new Button("link");
        linking.setOnAction(new EventHandler<ActionEvent>(){
        public void handle(ActionEvent event)
        {   linkingOperation(etoileController);
        }
        });
        starPopUp.getChildren().add(linking);
        Button addChild = new Button("addChild");
        addChild.setOnAction(e->branchingOperation(etoileController));
        starPopUp.getChildren().add(addChild);
        Button removing = new Button("remove");
        removing.setOnAction(e->removeOperation(etoileController));
        starPopUp.getChildren().add(removing);
    }
    private void linkingOperation(EtoileControl etoileController)
    {   Coordination from = etoileController.getCoordination();
        newAlignCurve = new QuadCurve();
        fromStar = etoileController;
        setCurveCoor(newAlignCurve,from,from);
        cielArea.getChildren().add(1,newAlignCurve);
        cielArea.getChildren().remove(starPopUp);
    }
    private void branchingOperation(EtoileControl parentStar)
    {   EtoileControl newStar = drawOneStar(new Etoile("empty",true,parentStar.getEtoile()),"EtoileSub.fxml");
        cielArea.getChildren().remove(starPopUp);
    }
    private void removeOperation(EtoileControl targetEtoile)
    {   targetEtoile.removeYourself();
        HoustonCenter.recordAction(new AddingAction(targetEtoile,true));
        cielArea.getChildren().remove(starPopUp);
    }
   
    private Coordination getCielRelativeCoor(Coordination sceneCoor)
    {   Point2D ori = cielArea.localToScene(0.0f,0.0f);
        double cielX = sceneCoor.getX() - ori.getX(); 
        double cielY = sceneCoor.getY() - ori.getY();
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
                
                // if(newAlignCurve==null) return;
                // Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));         
                // alignOneStar(newCoor);
            }
        }
        });

        cielArea.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) 
        {   if(newAlignCurve!=null)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                alignOneStar(newCoor);
                event.consume();
            }
        };
        });
    }

    private void focuseEffect(Coordination newCoor)
    {   EtoileControl newNearsetStar = findNearestStar(newCoor.getX(),newCoor.getY());
        if(newNearsetStar == nearsetStar) return;
        if(nearsetStar != null) nearsetStar.removeEffect();
        newNearsetStar.addEffect();
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
        drawOneAlign(newAlign);
        cielModel.getAligns().add(newAlign);
        cielArea.getChildren().remove(newAlignCurve);
        nearsetStar.removeEffect();
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

   private static class AddingAction implements CielAction
   {    private EtoileControl target;
        private final boolean inverse;
        public AddingAction(EtoileControl target, boolean inverse)
        {   this.target = target;
            this.inverse = inverse;
        }
        public void undo()
        {   if(!inverse) target.removeYourself();
            else target.addYourself();
        }
        public void redo()
        {   if(!inverse) target.addYourself();
            else target.removeYourself();
        }
    }

   private class AlignAction implements CielAction
   {    private Align target;
        private final boolean inverse;
        public AlignAction(Align target, boolean inverse)
        {   this.target = target;
            this.inverse = inverse;
        }
        public void undo()
        {   if(!inverse) removeAlign();
            else addAlign();
        }
        public void redo()
        {   if(!inverse) addAlign();
            else removeAlign();
        }
        private void removeAlign()
        {   
        }
        private void addAlign()
        {
        }
    }

    private static class MovingAction implements CielAction
    {   Coordination oldCoor;
        Coordination newCoor;
        EtoileControl controller;
        public MovingAction(Coordination oldCoor, Coordination newCoor, EtoileControl controller)
        {   this.oldCoor = oldCoor;
            this.newCoor = newCoor;
            this.controller = controller;
        }
        public void undo()
        {   controller.updateStarPos(oldCoor);
        }
        public void redo()
        {   controller.updateStarPos(newCoor);
        }
    }

}   
