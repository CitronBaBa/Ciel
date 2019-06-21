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
// it is only correct when etoileview - primaryview - shape hierachy is kept
// with shape representing the centre of the etoile graph
public class EtoileControl implements Initializable 
{   // total view
    public Node etoileView;

    // details
    public Node etoileShape;
    public StackPane primaryView;
    public VBox childArea;
    public Label name;
    
    private Pane cielArea;
    private Etoile monEtoile;
    private Map<Etoile,EtoileControl> etoileMap;

    public void initialize(URL location, ResourceBundle resources) 
    {   name.setText(monEtoile.getName());
        locateItself();
        innerMouseSetUp();
        autoPositioning();
    }
    public Etoile getEtoile() {   return monEtoile;}
    public Node getView(){   return this.etoileView;}
    public Node getPrimaryView() {  return primaryView;}
    public Region getChildArea() {  return childArea;}

    public void addChild(EtoileControl childStar)
    {   monEtoile.addChild(childStar.getEtoile());
        childArea.getChildren().add(childStar.getView());

        // Etoile originalStar = monEtoile; 
        // while(originalStar.isSubStar()) originalStar = originalStar.getParent();      
        // etoileMap.get(originalStar).updateStarPos(originalStar.getCoordination());
    }

    public EtoileControl(Etoile etoile, Map<Etoile,EtoileControl> etoileMap, Pane cielArea)
    {   this.monEtoile = etoile;
        this.etoileMap = etoileMap;
        this.cielArea = cielArea;
    }

// it overlaps with autoPositioning when calling from outside
// not a big problem
    public void updateStarPos(Coordination newCoor)
    {   updateCoor(newCoor);
        locateItself();
    }
    private void updateCoor(Coordination newPos)
    {   monEtoile.updateCoordination(newPos);
    }

    private void locateItself()
    {   if(monEtoile.isSubStar()) return;
        Coordination centerCoor = giveCenterCoordination();
        etoileView.setLayoutX(centerCoor.getX());
        etoileView.setLayoutY(centerCoor.getY());
    }

    private void autoPositioning()
    {   etoileShape.localToSceneTransformProperty().addListener((obs, oldT, newT) -> 
        {   Point2D originalPosInScene = newT.transform(new Point2D(0,0));
            Bounds cielBound = cielArea.localToScene(cielArea.getBoundsInLocal());
            double cielX = originalPosInScene.getX() - cielBound.getMinX(); 
            double cielY = originalPosInScene.getY() - cielBound.getMinY();    
            Coordination newCoor = new Coordination(cielX,cielY);
            updateStarPos(newCoor);
        });
    }

    public Coordination getCoordination()
    {   return monEtoile.getCoordination();
    }
    private Coordination giveCenterCoordination()
    {   Coordination original = monEtoile.getCoordination();
        double topX = original.getX();
        double topY = original.getY();
        double finalX = topX - etoileShape.getLayoutX();
        double finalY = topY - etoileShape.getLayoutY();
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
