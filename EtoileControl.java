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

// etoileview - primaryview - shape are essential
// and now their relations can be aribtrary
// which will not affect the program

public class EtoileControl implements Initializable 
{   // total view
    public Node etoileView;
    private Node childDraw;

    // details
    public Ellipse etoileShape;
    public StackPane primaryView;
    public VBox childArea;
    public Label name;
    public Pane backgroundPane;
    
    private Pane cielArea;
    private Etoile monEtoile;
    private Map<Etoile,EtoileControl> etoileMap;
    private Ciel cielModel;



    public void initialize(URL location, ResourceBundle resources) 
    {   name.setText(monEtoile.getName());
        innerMouseSetUp();
        autoUpdatePostion();
        
        // the first adjusting itself will not have the reference of its
        // layout position in the parent, as it has not been added
        // locateWithAdjustment(monEtoile.getCoordination()); 
        // , therefore, should not be used
        initalLocate();
        addYourself();
    }
    private void initalLocate()
    {   if(monEtoile.getViewCoor()!=null) // when it is reloaded to the scene
        locateDirectly(monEtoile.getViewCoor());
        else  // when it's created from blank fxml
        {   double x0 = monEtoile.getCoordination().getX() - etoileShape.getRadiusX();
            double y0 = monEtoile.getCoordination().getY() - etoileShape.getRadiusY();
            Coordination newCoor = new Coordination(x0,y0);
            locateDirectly(newCoor);
        }
    }

    public Etoile getEtoile() {   return monEtoile;}
    public Node getView(){   return this.etoileView;}
    public Node getPrimaryView() {  return primaryView;}
    public VBox getChildArea() {  return childArea;}
    public Node getShape() {  return etoileShape;}
    public DoubleProperty getShapeYProperty()
    {   return etoileShape.centerYProperty();
    }
    public DoubleProperty getShapeXProperty()
    {   return etoileShape.centerXProperty();
    }
    public DoubleProperty getShapeRaidusXProperty()
    {   return etoileShape.radiusXProperty();
    }
    public DoubleProperty getShapeRaidusYProperty()
    {   return etoileShape.radiusYProperty();
    }
    public void setChildDraw(Node childDraw)
    {   this.childDraw = childDraw;
    }
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
    
    public void addChildDrawing(EtoileControl childStar)
    {   Path childPath = new Path();
        childPath.relocate(0,0);
        //System.out.println(etoileShape.getLayoutX()+", "+etoileShape.getLayoutY());
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(monEtoile.getCoordination().getXProperty().add(getShapeRaidusXProperty()));
        moveTo.yProperty().bind(monEtoile.getCoordination().getYProperty());

        QuadCurveTo quadCurveTo = new QuadCurveTo();
        quadCurveTo.xProperty().bind(
                childStar.getEtoile().getCoordination().getXProperty()
                .subtract(childStar.getShapeRaidusXProperty()));
        quadCurveTo.yProperty().bind(
                childStar.getEtoile().getCoordination().getYProperty()
                .add(childStar.getShapeRaidusYProperty())
        );
        quadCurveTo.controlXProperty().bind(
                childStar.getEtoile().getCoordination().getXProperty()
                .add(monEtoile.getCoordination().getXProperty()).divide(2.0f)  );
        quadCurveTo.controlYProperty().bind(
                childStar.getEtoile().getCoordination().getYProperty()
                .add(monEtoile.getCoordination().getYProperty()).divide(2.0f)  );

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(
                childStar.getEtoile().getCoordination().getXProperty()
                .add(childStar.getShapeRaidusXProperty())
        );
        lineTo.yProperty().bind(  
                childStar.getEtoile().getCoordination().getYProperty()
                .add(childStar.getShapeRaidusYProperty())
        );

        childPath.getElements().add(moveTo);
        childPath.getElements().add(quadCurveTo);
        childPath.getElements().add(lineTo);
        childStar.setChildDraw(childPath);
        cielArea.getChildren().add(childPath);
    }

    public void setColor(Paint color)
    {   etoileShape.setFill(color);
    }
    public Paint getColor()
    {   return etoileShape.getFill();
    }

    public EtoileControl(Etoile etoile, Map<Etoile,EtoileControl> etoileMap, Pane cielArea,
            Ciel cielModel)
    {   this.monEtoile = etoile;
        this.etoileMap = etoileMap;
        this.cielArea = cielArea;
        this.cielModel = cielModel;
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

    private void autoUpdatePostion()
    {   etoileShape.localToSceneTransformProperty().addListener((obs, oldT, newT) -> 
        {   Point2D originalPosInScene = newT.transform(new Point2D(0,0));
            Bounds cielBound = cielArea.localToScene(cielArea.getBoundsInLocal());
            double cielX = originalPosInScene.getX() - cielBound.getMinX(); 
            double cielY = originalPosInScene.getY() - cielBound.getMinY();    
            Coordination newCoor = new Coordination(cielX,cielY);
            // System.out.println(monEtoile.getName()+"///");newCoor.print();
            // System.out.println("ciel ori: "+cielBound.getMinX()+", "+cielBound.getMinY());
            // System.out.println(originalPosInScene);
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
    {   Coordination original = newCoor;
        double oriX = original.getX();
        double oriY = original.getY();
        double finalX = oriX - etoileShape.getLayoutX();
        double finalY = oriY - etoileShape.getLayoutY();
        return new Coordination(finalX,finalY);
    }

    private void innerMouseSetUp()
    {   name.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 2)
            {   innerMouseAction();
            }   
        }
        });
    }

    private void innerMouseAction()
    {   primaryView.getChildren().remove(name);
        TextField input = new TextField();
        input.setPrefWidth(50);
        input.setOnAction(e->
        {   name.setText(input.getText());
            monEtoile.setName(input.getText());
            primaryView.getChildren().remove(input);
            primaryView.getChildren().add(name);
        });
        primaryView.getChildren().add(input);
    }

    public void addEffect()
    {   //System.out.println(monEtoile.getName()+" adding effect");
        Node targetNode = this.getPrimaryView();
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
        Node targetNode = this.getPrimaryView();
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

    // private void resizeListen()
    // {   childArea.widthProperty().addListener((obs, oldVal, newVal) -> 
    //     {   updateStarPos(monEtoile.getCoordination());  System.out.println("changing width");
    //     });
    // 
    //     childArea.heightProperty().addListener((obs, oldVal, newVal) -> 
    //     {   updateStarPos(monEtoile.getCoordination());  System.out.println("changing height");
    //     });
    // }
    // private void updateChildCoorinations()
    // {   for(Etoile childEtoie: monEtoile.getChildren())
    //     {   updateOneChildCoor(childEtoie);
    //     }
    // }
    // private void updateOneChildCoor(Etoile childEtoie)
    // {   Node childNode = etoileMap.get(childEtoie).getView();
    //     double childX = monEtoile.getCoordination().getX()-etoileShape.getLayoutX()+
    //                 childArea.getLayoutX()+childNode.getLayoutX();
    //     double childY = monEtoile.getCoordination().getY()-etoileShape.getLayoutY()+
    //                 childArea.getLayoutY()+childNode.getLayoutY();
    //     Coordination topLeftCoor = new Coordination(childX,childY);
    //     etoileMap.get(childEtoie).updateCoorFromTopLeft(topLeftCoor);
    // }
    // public void updateCoorFromTopLeft(Coordination topLeftCoor)
    // {   double finalX = topLeftCoor.getX() + etoileShape.getLayoutX();
    //     double finalY = topLeftCoor.getY() + etoileShape.getLayoutY();
    //     updateCoor(new Coordination(finalX,finalY));
    // }
