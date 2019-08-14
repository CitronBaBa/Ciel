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
import javafx.beans.property.*;
import javafx.application.Platform;
public class CielControl
{   //fxml
    private Pane cielArea;
    private VBox cielBox;
    private ScrollPane cielScrolPane;


    private double zoomIntensity = 0.002;
    private double maxScale = 5;
    private double minScale = 0.2;

    private EtoileControl selectedEtoile;
    public EtoileControl getSelectedStar() {   return selectedEtoile;}
    public Pane getCielArea() {   return cielArea;}

    private GlobalSatellite globals;
    private Ciel cielModel;
    private CielRobot robot;
    public CielRobot getRobot() {   return robot;}

// mapping between model object and graph object
// used to quickly find controller
    private Map<Etoile,EtoileControl> etoileControls = new HashMap<>();
    private Map<Align,AlignControl> alignControls = new HashMap<>();
    public Map<Etoile,EtoileControl> getEtoileControls()
    {   return etoileControls;
    }

    private VBox backgroundPopUp;
    private VBox starPopUp;

// for linking operation
    private QuadCurve newAlignCurve;
    private EtoileControl fromStar;
    private EtoileControl nearsetStar;


    public CielControl(Pane cielArea, VBox cielBox, ScrollPane cielScrolPane)
    {   globals = GlobalSatellite.getSatellite();
        cielModel = globals.getCielModel();
        this.cielArea = cielArea;
        this.cielBox = cielBox;
        this.cielScrolPane = cielScrolPane;
        this.robot = new CielRobot(etoileControls,cielScrolPane,cielArea);
        initialization();
    }

    public void loadFromCielModel(Ciel cielModel)
    {   // clear up
        removeEverything();

        // loading
        this.cielModel = cielModel;
        for(Etoile e : cielModel.getParentEtoiles())
        {   EtoileControl parentStar = drawOneStar(e);
        }
        for(Align a : cielModel.getAligns())
        {   drawOneAlign(a);
        }

        cielScrolPane.layout();
    }

// ideally this should be moved to etoileControl

    private void removeEverything()
    {   etoileControls.clear();
        alignControls.clear();
        cielArea.getChildren().clear();
        clearWrappers();
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
        dragAssistance();
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
    {
         // start at the center
         moveViewPortToCenter();

         // run later to avoid width/height change in intialization
         Platform.runLater(()->anchorContentInScrolPane());

         cielBox.setOnScroll(e ->
         {   e.consume();
             onScroll(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
         });

    }

    /* the listener here aims to anchor the mind map's visual position
     when the size of the viewport of the scrolpane is changed
     (as Hvalue/Vvalue isn't changed, without this adjustment the content will shift)
     some little math is done here
     */
    private void anchorContentInScrolPane()
    {   cielScrolPane.widthProperty().addListener((obs,oldV,newV)->
        {   //scrolw is the width of the viewport
            double scrolW = (double)oldV - cielScrolPane.getPadding().getRight()- cielScrolPane.getPadding().getLeft();
            double scrolW1 = (double)newV - cielScrolPane.getPadding().getRight()- cielScrolPane.getPadding().getLeft();
            double newHvalue = (cielArea.getPrefWidth()-scrolW)/(cielArea.getPrefWidth()-scrolW1) * cielScrolPane.getHvalue();
            cielScrolPane.setHvalue(newHvalue);
            //System.out.println("listen width");
        });
        cielScrolPane.heightProperty().addListener((obs,oldV,newV)->
        {   double scrolH = (double)oldV - cielScrolPane.getPadding().getTop() - cielScrolPane.getPadding().getBottom();
            double scrolH1 = (double)newV - cielScrolPane.getPadding().getTop()-cielScrolPane.getPadding().getBottom();
            double newVvalue = (cielArea.getPrefHeight()-scrolH)/(cielArea.getPrefHeight()-scrolH1) * cielScrolPane.getVvalue();
            cielScrolPane.setVvalue(newVvalue);
            //System.out.println("h value listen and changed--" + newVvalue);
        });
    }

    private void moveViewPortToCenter()
    {    Bounds viewB = cielScrolPane.getViewportBounds();

         double targetX = cielArea.getPrefWidth()/2-cielScrolPane.getPrefWidth()/2;
         double targetY = cielArea.getPrefHeight()/2-cielScrolPane.getPrefHeight()/2;
         double Hpercentage = targetX/(cielArea.getPrefWidth()-cielScrolPane.getPrefWidth());
         double Vpercentage = targetY/(cielArea.getPrefHeight()-cielScrolPane.getPrefHeight());

         cielScrolPane.setHvalue(0.5);
         cielScrolPane.setVvalue(0.5);
         // System.out.println(targetX+","+targetY);
         // System.out.println(cielScrolPane.getVvalue()+"--y,"+cielScrolPane.getHvalue()+"--x");
         // System.out.println(cielScrolPane.getVmax()+"--y,"+cielScrolPane.getHmax()+"--x");
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
        double newScaleValue = scaleValue * zoomFactor;
        if(newScaleValue>maxScale || newScaleValue<minScale) newScaleValue = scaleValue;
        cielModel.getScaleProperty().set(newScaleValue);
        //updateScale();
        //cielScrolPane.layout(); // refresh ScrollPane scroll positions & target bounds

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
    {   Rectangle background = new Rectangle(13,9,Color.rgb(252,244,246));
        background.setId("ciel-background");
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
        cielArea.getChildren().remove(backgroundPopUp);
        addingStarOperation(new Coordination(x,y));
    }
    private void addingStarOperation(Coordination newCoor)
    {   Etoile newEtoile = new Etoile("empty");
        newEtoile.updateCoordination(newCoor);
        EtoileControl controller = drawOneStar(newEtoile);
        cielArea.requestFocus();
    }

    private void drawOneAlign(Align align)
    {   AlignControl newAlign = new AlignControl(align,alignControls,cielArea,cielModel);
        newAlign.drawYourself();
    }

    private void addOneAlign(Align align)
    {   AlignControl newAlign = new AlignControl(align,alignControls,cielArea,cielModel);
        newAlign.addAndDrawYourself();
        HoustonCenter.recordAction(new AlignControl.AlignAction(newAlign,false));
    }

    private EtoileControl drawOneStar(Etoile star)
    {   EtoileControl controller;
        if(star.isSubStar())
            controller = new EtoileControl_Rectangle(star,etoileControls,alignControls,cielArea,cielModel,e->starSetUp(e));
        else controller = new EtoileControl_Rectangle(star,etoileControls,alignControls,cielArea,cielModel,e->starSetUp(e));
        HoustonCenter.recordAction(new AddingAction(controller,false));
        return controller;
    }

    private void starSetUp(EtoileControl controller)
    {   starMouseSetUp(controller);
    }


    // private EtoileControl cachedEtoile = null;

    private void dragAssistance()
    {
//cielScrolPane.setPannable(false);
        // cielScrolPane.setOnDragDetected(new EventHandler<MouseEvent>()
        // {   public void handle(MouseEvent event)
        //     {   System.out.println("background drag detected");
        //         if(cachedEtoile!=null)
        //          {
        //              cachedEtoile.getPrimaryView().fireEvent(event);
        //              System.out.println("background drag transfered");
        //
        //          }
        //     }
        // });
    //     cielScrolPane.addEventFilter(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
    //         public void handle(MouseEvent event)
    //         {   if(cachedEtoile!=null)
    //             {   event.consume();
    //                 EtoileControl target = cachedEtoile;
    //                 cachedEtoile = null;
    //                 target.getPrimaryView().fireEvent(event);
    //             }
    //         }
    //     });
    //
    }

// child star should not be dragged
// and ideally have a different mouse behavior

// shuffling sub star (to pane) at the begining of drag will
// cause mouse_drag not delivered to the sub star
// currently shuffling in pressed event;

/*************!!!!!!****/
// a restrucure is needed to get rid of startFullDrag
// and use normal drag event (drag type 1 in javafx)
// and then all the nodes even sub nodes can be dragged relatively
    private final ObjectProperty<EtoileControl> oldParentWrapper = new SimpleObjectProperty<>();
    private final ObjectProperty<EtoileControl> sudoStarWrapper = new SimpleObjectProperty<>();
    private final ObjectProperty<EtoileControl> oriStarWrapper = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> lastPosWrapper = new SimpleObjectProperty<>();

    private void clearWrappers()
    {
       oldParentWrapper.setValue(null);
        sudoStarWrapper.setValue(null);
        oriStarWrapper.setValue(null);
        lastPosWrapper.setValue(null);
    }

    private void starMouseSetUp(EtoileControl controller)
    {   final Coordination oldCoor = new Coordination(0,0);

        controller.getPrimaryView().setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
            EtoileControl target = controller;

            if(sudoStarWrapper.get()!=null) target = sudoStarWrapper.get();

            //if(lastPosWrapper.get()==null) lastPosWrapper.setValue(new Point2D(newCoor.getX(),newCoor.getY()));
            Point2D newPoint = new Point2D(newCoor.getX(),newCoor.getY());
            Point2D oldPoint = lastPosWrapper.get();
            Point2D adjust = newPoint.subtract(oldPoint);
            lastPosWrapper.setValue(newPoint);
            target.updateStarPosRelatively(new Coordination(adjust.getX(),adjust.getY()));
            //flying over something
            robot.highlightHoveredStar(target);
            event.consume();
            //System.out.println("star:"+controller.getEtoile().getName()+" dragged");
        }
        });

        controller.getPrimaryView().setOnMousePressed(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   //shuffle to the top
            oldCoor.setX(controller.getEtoile().getCoordination().getX());
            oldCoor.setY(controller.getEtoile().getCoordination().getY());

            Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
            lastPosWrapper.setValue(new Point2D(newCoor.getX(),newCoor.getY()));

            if(!controller.getEtoile().isSubStar()) controller.shuffleToTheTop();
            //System.out.println("star:"+controller.getEtoile().getName()+" pressed");
            event.consume();
        }
        });

        controller.getPrimaryView().setOnDragDetected(new EventHandler<MouseEvent>()
        {   public void handle(MouseEvent event)
            {   controller.getPrimaryView().startFullDrag();
                if(controller.getEtoile().isSubStar())
                {   EtoileControl oldParent = etoileControls.get(controller.getEtoile().getParent());
                    if(oldParent==null) System.out.println("no parent is found");
                    oldParentWrapper.setValue(oldParent);
                    oriStarWrapper.setValue(controller);

                    EtoileControl sudoStar = controller.giveADeepCopy();
                    controller.hideStarRecursively();
                    Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                    lastPosWrapper.setValue(new Point2D(newCoor.getX(),newCoor.getY()));
                    sudoStar.updateStarPos(newCoor);
                    sudoStarWrapper.set(sudoStar);
                }
                System.out.println("star:"+controller.getEtoile().getName()+" drag detected");
                event.consume();
            }
        });

        controller.getPrimaryView().setOnMouseDragEntered(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   //System.out.println("star:"+controller.getEtoile().getName()+" drag entered");
                event.consume();
              }
        });

        controller.getPrimaryView().setOnMouseDragReleased(new EventHandler<MouseDragEvent>()
        {   public void handle(MouseDragEvent event)
            {   if(event.getEventType()!=MouseDragEvent.MOUSE_DRAG_RELEASED) return;
                if(controller.getEtoile().isSubStar()) return;
                Coordination newCoor = getCielRelativeCoor(new Coordination(event.getSceneX(),event.getSceneY()));
                Coordination oriCoor = new Coordination(oldCoor.getX(),oldCoor.getY());

                EtoileControl target = controller;
                if(oriStarWrapper.get()!=null) target = oriStarWrapper.get();


                boolean isMerged = robot.stopFlyingAndTryMerge(target,oldParentWrapper.get(),sudoStarWrapper.get(),oriCoor);
                if(!isMerged)
                {   int index = 0;
                    if(sudoStarWrapper.get()!=null)
                    {   index = oldParentWrapper.get().getEtoile().getChildren().indexOf(target.getEtoile());
                        target.showStarRecursively();
                        target.becomeFreeStar();
                        target.updateStarPos(newCoor);
                        sudoStarWrapper.get().removeYourGroup();
                    }
                    HoustonCenter.recordAction(new MovingAction(oriCoor,newCoor,target,oldParentWrapper.get(),index));
                }
                else if(sudoStarWrapper.get()!=null)
                {   oriStarWrapper.get().getView().setVisible(true);
                    sudoStarWrapper.get().removeYourGroup();
                }

                sudoStarWrapper.setValue(null);
                oldParentWrapper.setValue(null);
                oriStarWrapper.setValue(null);
                lastPosWrapper.setValue(null);
                //cachedEtoile = null;
                System.out.println("star:"+controller.getEtoile().getName()+" drag realeased");
                event.consume();
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
            {   selectStar(controller);
            }
            event.consume();
        }
        });
    }

    private void selectStar(EtoileControl targetEtoile)
    {   if(selectedEtoile == targetEtoile) return;
        if(selectedEtoile != null) unSelectStarWithoutPropagte();

        selectedEtoile = targetEtoile;
        selectedEtoile.addEffect();
        showDependency(selectedEtoile);
        HoustonCenter.propagateEvent(CielEvent.ChangeFocus);
    }
    private void showDependency(EtoileControl targetEtoile)
    {   String className = targetEtoile.getEtoile().getName();
        Set<Etoile> dependents = cielModel.getJavaManager().getDependentEtoiles(className);
        if(dependents == null) return;
        for(Etoile e : dependents)
        {   EtoileControl ec = etoileControls.get(e);
            if(ec!=null) ec.addEffect();
        }
    }
    private void unSelectStarWithoutPropagte()
    {   if(selectedEtoile == null) return;
        selectedEtoile.removeEffect();
        unShowDependency(selectedEtoile);
        selectedEtoile = null;
    }
    private void unSelectStar()
    {   unSelectStarWithoutPropagte();
        HoustonCenter.propagateEvent(CielEvent.ChangeFocus);
    }
    private void unShowDependency(EtoileControl targetEtoile)
    {   String className = targetEtoile.getEtoile().getName();
        Set<Etoile> dependents = cielModel.getJavaManager().getDependentEtoiles(className);
        if(dependents == null) return;
        for(Etoile e : dependents)
        {   EtoileControl ec = etoileControls.get(e);
            if(ec!=null) ec.removeEffect();
        }
    }
    public void restoreAllColors()
    {   for(EtoileControl e : etoileControls.values())
        {   e.restoreColor();
        }
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
        cielArea.requestFocus();
    }

    public void removeSelected()
    {   if(selectedEtoile!=null)
        removeOperation(selectedEtoile);
    }

    private void removeOperation(EtoileControl targetEtoile)
    {   targetEtoile.removeYourGroup();
        if(selectedEtoile==targetEtoile) unSelectStar();
        HoustonCenter.recordAction(new AddingAction(targetEtoile,true));
        cielArea.getChildren().remove(starPopUp);
        cielArea.requestFocus();
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
            {   clearSelectionOrEffect();
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
            {   clearAnyEffect();
            }
        };
        });
    }

    private void clearSelectionOrEffect()
    {   unSelectStar();
        clearAnyEffect();
    }
    private void clearAnyEffect()
    {   restoreAllColors();
        discardAlignOperation();
        cielArea.getChildren().remove(starPopUp);
        cielArea.getChildren().remove(backgroundPopUp);
        for(AlignControl aC : alignControls.values())
        {   aC.clearUp();
        }
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
    }

    private void discardAlignOperation()
    {   if(nearsetStar!=null) nearsetStar.removeEffect();
        if(newAlignCurve!=null) cielArea.getChildren().remove(newAlignCurve);
        newAlignCurve = null;
        fromStar = null;
    }

    private void alignOneStar(Coordination targetCoor)
    {   if(fromStar!=nearsetStar)
        {   Align newAlign = new Align(fromStar.getEtoile(),nearsetStar.getEtoile());
            addOneAlign(newAlign);
        }
        discardAlignOperation();
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
        {   if(!inverse) target.removeYourGroup();
            else target.addYourGroup();
        }
        public void redo()
        {   if(!inverse) target.addYourGroup();
            else target.removeYourGroup();
        }
    }

    private static class MovingAction implements CielAction
    {   Coordination oldCoor;
        Coordination newCoor;
        EtoileControl controller;

        //only when star is a sub star before moving to free places
        EtoileControl oldParent = null;
        int index;

        public MovingAction(Coordination oldCoor, Coordination newCoor, EtoileControl controller, EtoileControl oldParent, int index)
        {   this.oldCoor = oldCoor;
            this.newCoor = newCoor;
            this.controller = controller;
            this.oldParent = oldParent;
            this.index = index;
        }

        public void undo()
        {   if(oldParent==null)
            {   controller.updateStarPos(oldCoor);
                //System.out.println("normal undo");
            }
            else
            {       oldParent.insertChild(controller,index);
                    //System.out.println("abnormal undo");
            }

        }
        public void redo()
        {   if(oldParent==null) controller.updateStarPos(newCoor);
            else
            {   controller.becomeFreeStar();
                controller.updateStarPos(newCoor);
            }
        }
    }

}
