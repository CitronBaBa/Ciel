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
    private VBox cielBox;
    private ScrollPane cielScrolPane;

    private double zoomIntensity = 0.002;

    private EtoileControl selectedEtoile;
    public EtoileControl getSelectedStar() {   return selectedEtoile;}
    private Ciel cielModel;
    public Ciel getCielModel() {   return cielModel;}

// mapping between model object and graph object
// used to quickly find controller
    private Map<Etoile,EtoileControl> etoileControls = new HashMap<>();
    private Map<Align,AlignControl> alignControls = new HashMap<>();

    private VBox backgroundPopUp;
    private VBox starPopUp;

// for linking operation
    private QuadCurve newAlignCurve;
    private EtoileControl fromStar;
    private EtoileControl nearsetStar;


    public CielControl(Pane cielArea, VBox cielBox, ScrollPane cielScrolPane)
    {   cielModel = new Ciel();
        this.cielArea = cielArea;
        this.cielBox = cielBox;
        this.cielScrolPane = cielScrolPane;
        initialization();
    }

    public void loadFromCielModel(Ciel cielModel)
    {   // clear up
        removeEverything();

        // loading
        this.cielModel = cielModel;
        for(Etoile e : cielModel.getParentEtoiles())
        {   EtoileControl parentStar = drawOneStar(e);
            drawChildStar(parentStar);
        }
        for(Align a : cielModel.getAligns())
        {   drawOneAlign(a);
        }
    }

// ideally this should be moved to etoileControl
    private void drawChildStar(EtoileControl parentStar)
    {   for(Etoile eSub : parentStar.getEtoile().getChildren())
        {   EtoileControl childStar = drawOneStar(eSub);
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
        unSelectStar();

        //reset background
        backgroundSetUp();
    }

    private void initialization()
    {   backgroundSetUp();
        cielMouseSetUp();
        cielBoundSetUp();
        scrollAndZoomSetUp();
        popUpMenu();

        Etoile star0 = new Etoile("Node");
        drawOneStar(star0);
    }
    private void cielBoundSetUp()
    {   //cielArea.prefWidthProperty().bind(cielBox.widthProperty());
        //cielArea.prefHeightProperty().bind(cielBox.heightProperty());
        cielScrolPane.setPannable(true);
        Rectangle clipRec = new Rectangle();
        cielArea.setClip(clipRec);
        cielArea.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            clipRec.setWidth(newValue.getWidth());
             clipRec.setHeight(newValue.getHeight());
         });
         // cielScrolPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    }

    private void scrollAndZoomSetUp()
    {   cielBox.setOnScroll(e ->
        {   e.consume();
            onScroll(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
        });
    }

    private void updateScale()
    {   double scaleValue = cielModel.getScaleProperty().get();
        for(Map.Entry<Etoile,EtoileControl> pair: etoileControls.entrySet())
        {   if(!pair.getValue().getEtoile().isSubStar())
            {   pair.getValue().getView().setScaleX(scaleValue);
                pair.getValue().getView().setScaleY(scaleValue);
            }
        }
    }

    private void onScroll(double wheelDelta, Point2D mousePoint)
    {   double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Node zoomNode = cielBox.getChildren().get(0);
        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = cielScrolPane.getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = cielScrolPane.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = cielScrolPane.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        double scaleValue = cielModel.getScaleProperty().get();
        scaleValue = scaleValue * zoomFactor;
        cielModel.getScaleProperty().set(scaleValue);
        //updateScale();
        cielScrolPane.layout(); // refresh ScrollPane scroll positions & target bounds

        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = cielArea.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = cielArea.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        //cielScrolPane.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        //cielScrolPane.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));

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
        EtoileControl controller = drawOneStar(newEtoile);
    }

    private void drawOneAlign(Align align)
    {   AlignControl newAlign = new AlignControl(align,alignControls,cielArea,cielModel);
        newAlign.drawYourself();
    }

    private void addOneAlign(Align align)
    {   AlignControl newAlign = new AlignControl(align,alignControls,cielArea,cielModel);
        newAlign.addAndDrawYourself();
        HoustonCenter.recordAction(new AlignAction(newAlign,false));
    }

    private EtoileControl drawOneStar(Etoile star)
    {   EtoileControl controller;
        if(star.isSubStar()) 
            controller = new EtoileControl_EmptyShape(star,etoileControls,alignControls,cielArea,cielModel);
        else controller = new EtoileControl(star,etoileControls,alignControls,cielArea,cielModel);
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
            event.consume();
        }
        });

        final Coordination oldCoor = new Coordination(0,0);
        controller.getPrimaryView().setOnDragDetected(new EventHandler<MouseEvent>()
        {   public void handle(MouseEvent event)
            {   controller.getPrimaryView().startFullDrag();
                event.consume();
            }
        });
        controller.getPrimaryView().setOnMouseDragEntered(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   oldCoor.setX(controller.getEtoile().getCoordination().getX());
                oldCoor.setY(controller.getEtoile().getCoordination().getY());
                event.consume();
            }
        });

        controller.getPrimaryView().setOnMouseDragExited(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                Coordination oriCoor = new Coordination(oldCoor.getX(),oldCoor.getY());
                HoustonCenter.recordAction(new MovingAction(oriCoor,newCoor,controller));
                event.consume();
            }
        });
        // controller.getPrimaryView().addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED, new EventHandler<MouseDragEvent>() 
        // {   public void handle(MouseDragEvent event)
        //     {   oldCoor.setX(controller.getEtoile().getCoordination().getX());
        //         oldCoor.setY(controller.getEtoile().getCoordination().getY());
        //         event.consume(); 
        //     }
        // });
        // 
        // controller.getPrimaryView().addEventFilter(MouseDragEvent.MOUSE_DRAG_EXITED, new EventHandler<MouseDragEvent>() 
        // {   public void handle(MouseDragEvent event)
        //     {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
        //         Coordination oriCoor = new Coordination(oldCoor.getX(),oldCoor.getY());
        //         HoustonCenter.recordAction(new MovingAction(oriCoor,newCoor,controller));
        //         event.consume();   
        //     }
        // });

        controller.getPrimaryView().setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.SECONDARY && event.getClickCount() == 1)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                popStarPopUp(newCoor,controller);
            }
            if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   selectStar(controller);
            }
            event.consume();
        }
        });
    }

    private void selectStar(EtoileControl targetEtoile)
    {   if(selectedEtoile == targetEtoile) return;
        if(selectedEtoile != null) selectedEtoile.removeEffect();
        selectedEtoile = targetEtoile;
        selectedEtoile.addEffect();
        HoustonCenter.propagateEvent(CielEvent.ChangeFocus);
    }
    private void unSelectStar()
    {   if(selectedEtoile == null) return;
        selectedEtoile.removeEffect();
        selectedEtoile = null;
        HoustonCenter.propagateEvent(CielEvent.ChangeFocus);
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
        AlignControl.setCurveCoor(newAlignCurve,from,from);
        cielArea.getChildren().add(1,newAlignCurve);
        cielArea.getChildren().remove(starPopUp);
    }
    private void branchingOperation(EtoileControl parentStar)
    {   EtoileControl newStar = drawOneStar(new Etoile("empty",true,parentStar.getEtoile()));
        cielArea.getChildren().remove(starPopUp);
    }

    public void removeSelected()
    {   if(selectedEtoile!=null)
        removeOperation(selectedEtoile);
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
            AlignControl.setCurveCoor(newAlignCurve,from,newCoor);
            focuseEffect(newCoor);
        }
        });

        cielArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   unSelectStar();
            }
            if(event.getButton()==MouseButton.SECONDARY && event.getClickCount()==1)
            {   double x = event.getX();
                double y = event.getY();
                if(!cielArea.getChildren().contains(backgroundPopUp))
                cielArea.getChildren().add(backgroundPopUp);
                backgroundPopUp.relocate(x,y);
            }
        }
        });

        cielArea.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event)
        {   if(newAlignCurve!=null && event.getButton()==MouseButton.PRIMARY)
            {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                alignOneStar(newCoor);
                event.consume();
            }
            if(event.getClickCount() == 1)
            {   cielArea.getChildren().remove(starPopUp);
                cielArea.getChildren().remove(backgroundPopUp);
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
    {
        // background.setOnMouseClicked(new EventHandler<MouseEvent>() {
        // @Override
        // public void handle(MouseEvent event)
        // {   if(event.getButton()==MouseButton.SECONDARY && event.getClickCount()==1)
        //     {   double x = event.getX();
        //         double y = event.getY();
        //         if(!cielArea.getChildren().contains(backgroundPopUp))
        //         cielArea.getChildren().add(backgroundPopUp);
        //         backgroundPopUp.relocate(x,y);
        //     }
        // }
        // });
    }

    private void alignOneStar(Coordination targetCoor)
    {   Align newAlign = new Align(fromStar.getEtoile(),nearsetStar.getEtoile());
        addOneAlign(newAlign);
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
        private void removeStar()
        {
        }
    }

   private class AlignAction implements CielAction
   {    private AlignControl target;
        private final boolean inverse;
        public AlignAction(AlignControl target, boolean inverse)
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
        {   target.removeYourself();
        }
        private void addAlign()
        {   target.addAndDrawYourself();
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
