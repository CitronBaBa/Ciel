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
import javafx.beans.property.*;
import javafx.scene.effect.*;
import javafx.scene.paint.*;
import javafx.beans.binding.*;
import javafx.beans.value.*;
import javafx.application.Platform;
import javafx.scene.transform.*;

// this class holds the graphical object of etoile 
// and controls it 

// "etoileview - primaryview - shape" hierarchy and childArea are essential
// however their relations can be aribtrary
// with minor or zero modification it will still work

// when inherited, all protected methods need to be override or checked
// arbitrary shapes can be achieved using inheritance 

/**** Warning: global star reference needs to be cleared when etoile is definitely
removed for memory relieving ***/
/* currently this is not adressed */

public class EtoileControl implements Initializable
{
    // total view
    public Region etoileView;
    private Node childDraw;

    // details (should use @fxml to avoid public)
    public Ellipse etoileShape;
    public StackPane primaryView;
    public VBox childArea;
    public Label name;
    public TextField nameField;

    private Pane cielArea;
    protected Etoile monEtoile;
    private Map<Etoile,EtoileControl> etoileMap;
    private Map<Align,AlignControl> alignMap;
    protected Ciel cielModel;
    private BehaviorInjection outerSetup;

    //caching for related aligns
    List<AlignControl> previousAligns = new ArrayList<>();

    public static interface BehaviorInjection
    {   public void injectBehavior(EtoileControl etoileControl);
    }

    public void initialize(URL location, ResourceBundle resources)
    {   innerMouseSetUp();
        textSetUp();
        if(monEtoile.isSubStar()) addYourself();
        else addYourselfRecursively();
        initalLocate();
        autoUpdatePostion();
        initialStyling();
        dynamicSizing();

        outerSetup.injectBehavior(this);
    }

    public EtoileControl(Etoile etoile, Map<Etoile,EtoileControl> etoileMap,
            Map<Align,AlignControl> alignMap, Pane cielArea, Ciel cielModel, BehaviorInjection outerSetup)
    {   this.monEtoile = etoile;
        this.etoileMap = etoileMap;
        this.alignMap = alignMap;
        this.cielArea = cielArea;
        this.cielModel = cielModel;
        this.outerSetup = outerSetup;
        initialSetUp();
        loadFromFxml();
        //loadChildren();
    }

    protected void initialSetUp()
    {   ;
    }

    private void loadFromFxml()
    {   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(getFxmlName()));
        fxmlLoader.setController(this);
        try {   fxmlLoader.load(); }
        catch(Exception e) {  e.printStackTrace();}
    }

    protected String getFxmlName()
    {   if(monEtoile.isSubStar()) return "EtoileSub.fxml";
        else return "EtoileMain.fxml";
    }

    protected void dynamicSizing()
    {   etoileShape.radiusXProperty().bind(name.widthProperty().divide(1.2f));
        etoileShape.radiusYProperty().bind(name.heightProperty().divide(0.9f));
    }


    /*  ideally child loading should be in this class
        however, because mousesetting partly sits in the cielControl
        it is currently too complicated to do that (child star doesn't gets full mouse setup)
    */

    // private void loadChildren()
    // {   for(Etoile eSub : monEtoile.getChildren())
    //     {   EtoileControl_EmptyShap
    //     }
    // }


    public Etoile getEtoile() {   return monEtoile;}
    public Region getView(){   return this.etoileView;}
    public Node getPrimaryView() {  return primaryView;}
    public VBox getChildArea() {  return childArea;}
    public void setChildDraw(Node childDraw){   this.childDraw = childDraw; }
    public void removeMyChildDraw()
    {   cielArea.getChildren().remove(childDraw);
        childDraw = null;
    }
    public void addMyChildDraw()
    {   if(childDraw==null)
        etoileMap.get(monEtoile.getParent()).addChildDrawing(this);
    }
    public Node getChildDraw() { return childDraw;}

    public EtoileControl giveADeepCopy()
    {   Etoile modelDeepCopy = monEtoile.giveADeepCopy();
        EtoileControl deepCopy = new EtoileControl_Rectangle(modelDeepCopy,etoileMap,alignMap,cielArea,cielModel,outerSetup);

        // visually more approperiate, but nasty
        deepCopy.etoileView.setId("sub-hbox");
        deepCopy.name.setId("sub-etoile");

        return deepCopy;
    }

    public void addYourselfRecursively()
    {   addYourself();
        drawChildStarRecursively();
    }

    public void addYourGroup()
    {   addYourself();
        addChildStarRef();
        addAllChildDrawing();
    }

    public void addYourself()
    {   etoileMap.put(monEtoile,this);
        GlobalSatellite.putStarReference(monEtoile,this);

        //parent
        if(!monEtoile.isSubStar())
        {   name.setId("parent-etoile");
            etoileView.setId("parent-hbox");
            cielArea.getChildren().add(etoileView);
            if(!this.cielModel.getParentEtoiles().contains(monEtoile))
            this.cielModel.getParentEtoiles().add(monEtoile);
        }
        //child
        else
        {   name.setId("sub-etoile");
            etoileView.setId("sub-hbox");
            EtoileControl parent = etoileMap.get(monEtoile.getParent());
            if(monEtoile.getParent()==null) throw new RuntimeException("parent model cannot be found");
            if(parent==null)
            {   System.out.println("child is: " + monEtoile.getName());
                System.out.println("parent is: " + monEtoile.getParent().getName());
                throw new RuntimeException("parent view controller cannot be found");
            }
            parent.getChildArea().getChildren().add(etoileView);
            if(this.childDraw==null) addMyChildDraw();
            if(!parent.getEtoile().getChildren().contains(monEtoile))
            parent.getEtoile().getChildren().add(monEtoile);
        }
        scalingSetUp();
        addRelatedAligns();
    }

    public void drawChildStarRecursively()
    {   for(Etoile eSub : monEtoile.getChildren())
        {   EtoileControl childStar = new EtoileControl_Rectangle(eSub,etoileMap,alignMap,cielArea,cielModel,outerSetup);
            childStar.drawChildStarRecursively();
        }
    }

    private void addChildStarRef()
    {   for(Etoile eSub : monEtoile.getChildren())
        {   EtoileControl childStar = GlobalSatellite.getStarControl(eSub);
            etoileMap.put(eSub,childStar);
            childStar.addChildStarRef();
        }
        addRelatedAligns();
    }

    public void removeYourGroup()
    {   removeYourself();
        removeAllChildDrawing();
        removeChildStarRef();
    }

    public void removeYourself()
    {   //parent
        if(!monEtoile.isSubStar())
        {   cielArea.getChildren().remove(etoileView);
            cielModel.getParentEtoiles().remove(monEtoile);
        }
        //child
        else
        {   detachFromParentKeepInHeart();
        }
        etoileMap.remove(monEtoile);
        removeMyChildDraw();
        removeRelatedAligns();
    }

    private void removeChildStarRef()
    {   for(Etoile eSub : monEtoile.getChildren())
        {   EtoileControl childStar = etoileMap.get(eSub);
            etoileMap.remove(eSub);
            childStar.removeChildStarRef();
        }
        removeRelatedAligns();
    }

    private void addRelatedAligns()
    {   for(AlignControl a : previousAligns)
        a.addAndDrawYourself();
        previousAligns.clear();
    }

    public void removeRelatedAligns()
    {   List<Align> targets = cielModel.giveRelatedAligns(monEtoile);
        for(Align a : targets)
        {   AlignControl targetALign = alignMap.get(a);
            previousAligns.add(targetALign);
            targetALign.removeYourself();
        }
    }

    private void detachFromParentKeepInHeart()
    {   EtoileControl parent = etoileMap.get(monEtoile.getParent());
        parent.getChildArea().getChildren().remove(etoileView);
        removeMyChildDraw();

        // model
        parent.getEtoile().sendChildAway(monEtoile);
        // let parent knows, but don't change monEtoile itself
        // for redo operation (keep in heart)
    }

    public void addChild(EtoileControl childStar)
    {   childStar.removeYourGroup();
        childStar.getEtoile().becomeSubStar(monEtoile);
        childStar.addYourGroup();
    }

    public void becomeFreeStar()
    {   removeYourGroup();
        monEtoile.detachFromParent();
        addYourGroup();
    }

    // child star must not be a sub star
    // should be already freed
    public void insertChild(EtoileControl childStar, int targetPos)
    {   //defensive
        if(childStar.getEtoile().isSubStar()) childStar.becomeFreeStar();

        addChild(childStar);

        // adjust to target position
        childArea.getChildren().remove(childStar.getView());
        childArea.getChildren().add(targetPos,childStar.getView());
        // update model
        monEtoile.getChildren().remove(childStar.getEtoile());
        monEtoile.getChildren().add(targetPos,childStar.getEtoile());
    }

    public void shiftInChildren(boolean isMovingUp)
    {   if(!monEtoile.isSubStar()) return;
        List<Etoile> childrenList = monEtoile.getParent().getChildren();
        int oldPos = childrenList.indexOf(monEtoile);
        if(!isMovingUp && oldPos==childrenList.size()-1) return;
        if(isMovingUp && oldPos==0) return;

        EtoileControl parentControl = etoileMap.get(monEtoile.getParent());
        this.becomeFreeStar();
        if(isMovingUp) parentControl.insertChild(this,oldPos-1);
        else parentControl.insertChild(this,oldPos+1);
    }

    public void hideStarRecursively()
    {   etoileView.setVisible(false);
        removeMyChildDraw();
        removeRelatedAligns();
        for(Etoile e : monEtoile.getChildren())
        {   etoileMap.get(e).hideStarRecursively();
        }
    }
    public void showStarRecursively()
    {   etoileView.setVisible(true);
        addMyChildDraw();
        addRelatedAligns();
        for(Etoile e : monEtoile.getChildren())
        {   etoileMap.get(e).showStarRecursively();
        }
    }

    public void addAllChildDrawing()
    {   for(Etoile e : monEtoile.getChildren())
        {   EtoileControl childStar = etoileMap.get(e);
            if(childStar!=null)
            {    if(childStar.getChildDraw()==null)
                {   this.addChildDrawing(childStar);
                }
                childStar.addAllChildDrawing();
            }
        }
    }
    public void removeAllChildDrawing()
    {   for(Etoile e : monEtoile.getChildren())
        {   EtoileControl childStar = etoileMap.get(e);
            childStar.removeMyChildDraw();
            childStar.removeAllChildDrawing();
        }
    }

    private void initalLocate()
    {   if(monEtoile.isSubStar()) return;


        // loading from file
        // this case can be eliminated once child is loaded recursively from inside
        // (instead of sepaately in cielControl)
        // if(monEtoile.getViewCoor()!=null)
        // {   locateDirectly(monEtoile.getViewCoor());
        //     return;
        // }

        etoileView.layout();
        //System.out.println(primaryView.getLayoutY());
        updateStarPos(monEtoile.getCoordination());
    }

    private void scalingSetUp()
    {   if(monEtoile.isSubStar())
        {   etoileView.scaleXProperty().unbind();
            etoileView.scaleYProperty().unbind();
            etoileView.scaleXProperty().setValue(1.0);
            etoileView.scaleYProperty().setValue(1.0);
        }
        else
        {   etoileView.scaleXProperty().bind(cielModel.getScaleProperty());
            etoileView.scaleYProperty().bind(cielModel.getScaleProperty());
        }
    }

    private void test(EtoileControl childStar)
    {   Path path = new Path();
        path.relocate(0,0);
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(childStar.getCoordination().getXProperty());
        moveTo.yProperty().bind(childStar.getCoordination().getYProperty());
        path.getElements().add(moveTo);
        path.getElements().add(new LineTo(300,300));
        cielArea.getChildren().add(path);
    }

    private void addChildDrawing(EtoileControl childStar)
    {   if(monEtoile.isSubStar()) childToChildDraw(childStar);
        else parentToChildDraw(childStar);
    }

    protected Point2D giveCenterPoint(){ return new Point2D(0,0);}
    protected Node getMainShape() { return etoileShape;}
    protected DoubleBinding bottomRightX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().add
                (etoileShape.radiusXProperty().multiply(cielModel.getScaleProperty()));
    }

    protected DoubleBinding bottomRightY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (etoileShape.radiusYProperty().multiply(cielModel.getScaleProperty()));
    }

    protected DoubleBinding bottomLeftX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().subtract
                (etoileShape.radiusXProperty().multiply(cielModel.getScaleProperty()));
    }

    protected DoubleBinding bottomLeftY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (etoileShape.radiusYProperty().multiply(cielModel.getScaleProperty()));
    }

    protected DoubleBinding centerRightX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().add
                (etoileShape.radiusXProperty().multiply(cielModel.getScaleProperty()));
    }

    protected ObservableDoubleValue centerRightY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty();
    }

    protected ObservableDoubleValue getWidthProperty()
    {   return etoileShape.radiusXProperty().multiply(2.0f);
    }

    private void childToChildDraw(EtoileControl childStar)
    {   Path childPath = new Path();
        childPath.relocate(0,0);
        Coordination coor = monEtoile.getCoordination();

        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(bottomRightX());
        moveTo.yProperty().bind(bottomRightY());

        CubicCurveTo cubicTo = drawACubicCurve(childStar,bottomRightX(),bottomRightY());

        // ArcTo arcTo = new ArcTo();
        // arcTo.xProperty().bind(childStar.bottomLeftX());
        // arcTo.yProperty().bind(childStar.bottomLeftY());
        // arcTo.radiusXProperty().bind(calculateRadius(childStar));
        // arcTo.radiusYProperty().bind(calculateRadius(childStar));

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(childStar.bottomRightX());
        lineTo.yProperty().bind(childStar.bottomRightY());

        childPath.getElements().add(moveTo);
        childPath.getElements().add(cubicTo);
        childPath.getElements().add(lineTo);
        childPath.setId("curve");
        childStar.setChildDraw(childPath);
        cielArea.getChildren().add(childPath);
    }

    private CubicCurveTo drawACubicCurve(EtoileControl childStar, ObservableValue<Number> oriX, ObservableValue<Number> oriY)
    {   CubicCurveTo curve = new CubicCurveTo();

        curve.xProperty().bind(childStar.bottomLeftX());
        curve.yProperty().bind(childStar.bottomLeftY());

        curve.controlX2Property().bind(oriX);
        curve.controlY2Property().bind(childStar.bottomLeftY());
        curve.controlX1Property().bind(childStar.bottomLeftX());
        curve.controlY1Property().bind(oriY);
        return curve;
    }

    // private ObservableNumberValue calculateRadius(EtoileControl childStar)
    // {   NumberBinding deltaX = bottomRightX().subtract(childStar.bottomLeftX());
    //     NumberBinding deltaY = bottomRightY().subtract(childStar.bottomLeftY());
    //     NumberBinding squareSum = deltaX.multiply(deltaX).add(deltaY.multiply(deltaY));
    //     NumberBinding sumPlus = squareSum.divide(deltaY).divide(2.0f);
    //     NumberBinding sumNeg = squareSum.divide(deltaY).divide(-2.0f);
    //     NumberBinding radius = Bindings.when(deltaY.greaterThan(0.0f))
    //              .then(sumPlus)
    //              .otherwise(sumNeg);
    //     return radius;
    // }

    private void parentToChildDraw(EtoileControl childStar)

    {   Path childPath = new Path();
        childPath.relocate(0,0);
        Coordination coor = monEtoile.getCoordination();

        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(centerRightX());
        moveTo.yProperty().bind(centerRightY());

        CubicCurveTo cubicTo = drawACubicCurve(childStar,centerRightX(),centerRightY());

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(childStar.bottomRightX());
        lineTo.yProperty().bind(childStar.bottomRightY());

        childPath.getElements().add(moveTo);
        childPath.getElements().add(cubicTo);
        childPath.getElements().add(lineTo);
        childPath.setId("curve");
        childStar.setChildDraw(childPath);
        cielArea.getChildren().add(childPath);
    }

    private void initialStyling()
    {   //read fxml color to model
        if(monEtoile.getColor()==null)setColor(getShapeColor());
        else restoreColor();
    }
    public void restoreColor()
    {   setShapeColor(getColor());
    }

    public void setColor(Color color)
    {   setShapeColor(color);
        double[] colorFigures = {color.getRed(),color.getGreen(),color.getBlue(),color.getOpacity()};
        monEtoile.setColor(colorFigures);
    }
    public Color getColor()
    {   double[] colorFigures = monEtoile.getColor();
        return new Color(colorFigures[0],colorFigures[1],colorFigures[2],colorFigures[3]);
    }
    public void setShapeColor(Paint color)
    {   etoileShape.setFill(color);
    }
    public Color getShapeColor()
    {   return (Color)etoileShape.getFill();
    }


    public void updateStarPos(Coordination newCoor)
    {   locateWithAdjustment(newCoor);
    }
    public void updateStarPosRelatively(Coordination adjust)
    {   double x = etoileView.getLayoutX() + adjust.getX();
        double y = etoileView.getLayoutY() + adjust.getY();
        locateDirectly(new Coordination(x,y));
    }

    public void shuffleToTheTop()
    {   if(monEtoile.isSubStar()) throw new RuntimeException("you are shuffling a sub star, which is illegal");
        cielArea.getChildren().remove(etoileView);
        cielArea.getChildren().add(etoileView);
    }

// locate its self and auto autoPositioning at the same time
// may cause recursive call and stack over flow
// try to change the location with bindings
    private void locateWithAdjustment(Coordination newCoor)
    {   if(monEtoile.isSubStar()) return;
        Coordination adjustedCoor = giveAdjustedCoordination(newCoor);
        locateDirectly(adjustedCoor);
    }
    private void locateDirectly(Coordination adjustedCoor)
    {   etoileView.relocate(adjustedCoor.getX(),adjustedCoor.getY());
    }

    // when system shifts the layout by itself,
    // lock the place of the primary view;
    // it's nasty
    private double layoutY = 0;
    // update the center point to model
    private void autoUpdatePostion()
    {   getMainShape().localToSceneTransformProperty().addListener((obs, oldT, newT) ->
        {   selfUpdatePos(newT);
        });
    }
    private void selfUpdatePos(Transform newT)
    {   if(layoutY!=primaryView.getLayoutY() && !monEtoile.isSubStar())
        {   updateStarPos(monEtoile.getCoordination());
            layoutY = primaryView.getLayoutY();
            return;
        }
        Point2D originalPosInScene = newT.transform(giveCenterPoint());
        Bounds cielBound = cielArea.localToScene(cielArea.getBoundsInLocal());
        double cielX = originalPosInScene.getX() - cielBound.getMinX();
        double cielY = originalPosInScene.getY() - cielBound.getMinY();
        Coordination newCoor = new Coordination(cielX,cielY);
        updateModelCoor(newCoor);
    }
    private void updateModelCoor(Coordination newPos)
    {   Coordination viewPos = giveAdjustedCoordination(newPos);
        //star coor and view coor
        monEtoile.updateCoordination(newPos,viewPos);
    }
    public Coordination getCoordination()
    {   return monEtoile.getCoordination();
    }

    private Coordination giveAdjustedCoordination(Coordination newCoor)
    {   Point2D inParent = getMainShape().getLocalToParentTransform().transform(giveCenterPoint());
        Point2D inEtoile = primaryView.getLocalToParentTransform().transform(inParent);
        double shapeCenterToStarCenter = etoileView.getLayoutBounds().getWidth()/2-inEtoile.getX();

        double oriX = newCoor.getX();
        double oriY = newCoor.getY();
        oriX = oriX + (cielModel.getScale()-1)*shapeCenterToStarCenter;
        double finalX = oriX - inEtoile.getX();
        double finalY = oriY - inEtoile.getY();
        //System.out.println(giveCenterPoint());
        return new Coordination(finalX,finalY);
    }
    private void textSetUp()
    {   nameField.setVisible(false);
        nameField.setStyle("-fx-padding: 0 0 0 0;");
        // primaryView.getChildren().remove(nameField);
        nameField.setText(monEtoile.getName());
        name.textProperty().bind(nameField.textProperty());
    }

    private void innerMouseSetUp()
    {   nameField.focusedProperty().addListener((obs, oldVal, newVal) ->
        {   if(newVal == false) finishEditting();
        });
        nameField.setOnAction(e->
        {   finishEditting();
        });

        name.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 2)
            {   innerMouseAction();
            }
        }
        });

        primaryView.setOnMouseEntered(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   addEffect();
        }
        });
        primaryView.setOnMouseExited(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   removeEffect();
        }
        });
    }

    private void innerMouseAction()
    {   name.setVisible(false);
        nameField.setVisible(true);
        nameField.requestFocus();
    }
    private void finishEditting()
    {   String oldName = monEtoile.getName();
        monEtoile.setName(nameField.getText());
        nameField.setVisible(false);
        name.setVisible(true);
        HoustonCenter.recordAction(new ChangeNameAction(oldName,nameField.getText(),this));
        selfUpdatePos(getMainShape().getLocalToSceneTransform());
    }
    public void setName(String name)
    {   nameField.setText(name);
        monEtoile.setName(name);
    }

    public void addEffect()
    {   //System.out.println(monEtoile.getName()+" adding effect");
        Node targetNode = this.getMainShape();
        if(targetNode.getEffect()==null)
        targetNode.setEffect(new Glow(0.5));
        else
        {   Glow glowEffect = (Glow)targetNode.getEffect();
            double initialLevel = glowEffect.getLevel();
            glowEffect.setLevel(initialLevel+0.5);
        }
        //System.out.println(((Glow)targetNode.getEffect()).getLevel());
    }
    public void removeEffect()
    {   //System.out.println(monEtoile.getName()+" exit effect");
        Node targetNode = this.getMainShape();
        if(targetNode.getEffect()==null)
        {   targetNode.setEffect(new Glow(0));
            System.out.println("removing null effect");
        }
        else
        {   Glow glowEffect = (Glow)targetNode.getEffect();
            double initialLevel = glowEffect.getLevel();
            // exit behavior may have two same consecutive events
            if(initialLevel>0)
            glowEffect.setLevel(initialLevel-0.5);
        }
        //System.out.println(((Glow)targetNode.getEffect()).getLevel());
    }



    private static class ChangeNameAction implements CielAction
    {   private String oldName;
        private String newName;
        private EtoileControl target;

        public ChangeNameAction(String oldName, String newName, EtoileControl target)
        {   this.oldName = oldName;
            this.newName = newName;
            this.target = target;
        }

        public void redo()
        {   target.setName(newName);
        }
        public void undo()
        {   target.setName(oldName);
        }

    }
}
