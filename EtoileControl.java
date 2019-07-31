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

// "etoileview - primaryview - shape" hierarchy and childArea are essential
// however their relations can be aribtrary
// with minor or zero modification it will still work

// when inherited, all protected methods need to be override or checked
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

    //caching for related aligns
    List<AlignControl> previousAligns = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources)
    {   innerMouseSetUp();
        textSetUp();
        scalingSetUp();
        addYourself();
        initalLocate();
        autoUpdatePostion();
        initialStyling();
        dynamicSizing();
    }

    public EtoileControl(Etoile etoile, Map<Etoile,EtoileControl> etoileMap,
            Map<Align,AlignControl> alignMap, Pane cielArea, Ciel cielModel)
    {   this.monEtoile = etoile;
        this.etoileMap = etoileMap;
        this.alignMap = alignMap;
        this.cielArea = cielArea;
        this.cielModel = cielModel;
        loadFromFxml();
        //loadChildren();
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
    public Node getChildDraw() { return childDraw;}

    public void addYourself()
    {   //parent
        if(!monEtoile.isSubStar())
        {   cielArea.getChildren().add(etoileView);

            if(!this.cielModel.getParentEtoiles().contains(monEtoile))
            this.cielModel.getParentEtoiles().add(monEtoile);
        }
        //child
        else
        {   EtoileControl parent = etoileMap.get(monEtoile.getParent());
            parent.getChildArea().getChildren().add(etoileView);
            parent.addChildDrawing(this);
            if(!parent.getEtoile().getChildren().contains(monEtoile))
            parent.getEtoile().getChildren().add(monEtoile);
        }
        etoileMap.put(monEtoile,this);
        addAllChildDrawing();
        addRelatedAligns();
    }

    public void removeYourself()
    {   //parent
        if(!monEtoile.isSubStar())
        {   cielArea.getChildren().remove(etoileView);
            cielModel.getParentEtoiles().remove(monEtoile);
        }
        //child
        else
        {   EtoileControl parent = etoileMap.get(monEtoile.getParent());
            parent.getChildArea().getChildren().remove(etoileView);
            cielArea.getChildren().remove(childDraw);
            parent.getEtoile().getChildren().remove(monEtoile);
        }
        etoileMap.remove(monEtoile);
        removeAllChildDrawing();
        removeRelatedAligns();
    }

    private void addRelatedAligns()
    {   for(AlignControl a : previousAligns)
        a.addAndDrawYourself();
        previousAligns.clear();
    }

    private void removeRelatedAligns()
    {   List<Align> targets = cielModel.giveRelatedAligns(monEtoile);
        for(Align a : targets)
        {   AlignControl targetALign = alignMap.get(a);
            previousAligns.add(targetALign);
            targetALign.removeYourself();
        }
    }

    public void addChild(EtoileControl childStar)
    {   monEtoile.addChild(childStar.getEtoile());
        childArea.getChildren().add(childStar.getView());
        addChildDrawing(childStar);
    }

    public void addAllChildDrawing()
    {   for(Etoile e : monEtoile.getChildren())
        {   EtoileControl childStar = etoileMap.get(e);
            if(childStar!=null)
            {   addChildDrawing(childStar);
                childStar.addAllChildDrawing();
            }
        }
    }
    public void removeAllChildDrawing()
    {   for(Etoile e : monEtoile.getChildren())
        {   EtoileControl childStar = etoileMap.get(e);
            cielArea.getChildren().remove(childStar.getChildDraw());
            childStar.removeAllChildDrawing();
        }
    }

    private void initalLocate()
    {   if(monEtoile.isSubStar()) return;


        // loading from file
        // this case can be eliminated once child is loaded recursively from inside
        // (instead of sepaately in cielControl)
        if(monEtoile.getViewCoor()!=null)
        {   locateDirectly(monEtoile.getViewCoor());
            return;
        }

        etoileView.layout();
        updateStarPos(monEtoile.getCoordination());
    }

    private void scalingSetUp()
    {  if(monEtoile.isSubStar()) return;
      etoileView.scaleXProperty().bind(cielModel.getScaleProperty());
      etoileView.scaleYProperty().bind(cielModel.getScaleProperty());
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

        QuadCurveTo quadCurveTo = new QuadCurveTo();
        quadCurveTo.xProperty().bind(childStar.bottomLeftX());
        quadCurveTo.yProperty().bind(childStar.bottomLeftY());

        quadCurveTo.controlXProperty().bind(
                childStar.getEtoile().getCoordination().getXProperty()
                .add(coor.getXProperty()).divide(2.0f)  );
        quadCurveTo.controlYProperty().bind(
                childStar.getEtoile().getCoordination().getYProperty()
                .add(coor.getYProperty()).divide(2.0f)  );

        // ArcTo arcTo = new ArcTo();
        // arcTo.xProperty().bind(childStar.bottomLeftX());
        // arcTo.yProperty().bind(childStar.bottomLeftY());
        // arcTo.radiusXProperty().bind(calculateRadius(childStar));
        // arcTo.radiusYProperty().bind(calculateRadius(childStar));

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(childStar.bottomRightX());
        lineTo.yProperty().bind(childStar.bottomRightY());

        childPath.getElements().add(moveTo);
        childPath.getElements().add(quadCurveTo);
        childPath.getElements().add(lineTo);
        childPath.setId("curve");
        childStar.setChildDraw(childPath);
        cielArea.getChildren().add(childPath);
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

        QuadCurveTo quadCurveTo = new QuadCurveTo();
        quadCurveTo.xProperty().bind(childStar.bottomLeftX());
        quadCurveTo.yProperty().bind(childStar.bottomLeftY());

        quadCurveTo.controlXProperty().bind(
                childStar.getEtoile().getCoordination().getXProperty()
                .add(coor.getXProperty()).divide(2.0f)  );
        quadCurveTo.controlYProperty().bind(
                childStar.getEtoile().getCoordination().getYProperty()
                .add(coor.getYProperty()).divide(2.0f)  );

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(childStar.bottomRightX());
        lineTo.yProperty().bind(childStar.bottomRightY());

        childPath.getElements().add(moveTo);
        childPath.getElements().add(quadCurveTo);
        childPath.getElements().add(lineTo);
        childPath.setId("curve");
        childStar.setChildDraw(childPath);
        cielArea.getChildren().add(childPath);
    }

    private void initialStyling()
    {   setShapeColor(getColor());
    }

    public void setColor(Color color)
    {   setShapeColor(color);
        double[] colorFigures = {color.getRed(),color.getGreen(),color.getBlue(),color.getOpacity()};
        monEtoile.setColor(colorFigures);
    }
    public Color getColor()
    {   double[] colorFigures = monEtoile.getColor();
        if(colorFigures == null) return getShapeColor();
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

    // update the center point to model
    private void autoUpdatePostion()
    {   getMainShape().localToSceneTransformProperty().addListener((obs, oldT, newT) ->
        {   Point2D originalPosInScene = newT.transform(giveCenterPoint());
            Bounds cielBound = cielArea.localToScene(cielArea.getBoundsInLocal());
            double cielX = originalPosInScene.getX() - cielBound.getMinX();
            double cielY = originalPosInScene.getY() - cielBound.getMinY();
            Coordination newCoor = new Coordination(cielX,cielY);
            updateModelCoor(newCoor);
        });
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
        //System.out.println(inParent);
        return new Coordination(finalX,finalY);
    }
    // {   Point2D ShapeOri = etoileShape.getLocalToSceneTransform().transform(new Point2D(0,0));
    //     Point2D overallOri = etoileView.getLocalToSceneTransform().transform(new Point2D(0,0));
    //     Point2D adjust = new Point2D(ShapeOri.getX()-overallOri.getX(),ShapeOri.getY()-overallOri.getY());
    //     double finalX = newCoor.getX() - adjust.getX();
    //     double finalY = newCoor.getY() - adjust.getY();
    //     System.out.println("adjustment:"+adjust);
    //     return new Coordination(finalX,finalY);
    private void textSetUp()
    {   nameField.setVisible(false);
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
    {   monEtoile.setName(nameField.getText());
        nameField.setVisible(false);
        name.setVisible(true);
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
}
