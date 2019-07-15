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
import javafx.scene.paint.*;
import javafx.scene.effect.*;

public class StylePanel
{   private CielControl cielControl;
    private HBox bottomBox = new HBox();
    private GridPane colorGird = new GridPane();
    private int x = 0; private int y = 0;

    public Node getPanel()
    {   return bottomBox;
    }

    public StylePanel(CielControl cielControl)
    {   this.cielControl = cielControl;
        init();
    }
    
    private void init()
    {   ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e->chooseColor(colorPicker.getValue()));

        createColorGird();
        bottomBox.getChildren().addAll(colorGird,colorPicker);
    }

    private void createColorGird()
    {   for(int i=0; i<50;i++)
        {   drawOneColorBox(Color.rgb(i*5,i*2,i*2));
        }
    }
    private void drawOneColorBox(Color color)
    {   Rectangle box = new Rectangle(40,30,color);
        setBoxBehavior(box);
        placeOneColorBox(box);
    }

    private void placeOneColorBox(Node node)
    {   GridPane.setConstraints(node,x++,y);
        if(x>9)
        {   x=0;
        	y++;
        }
        colorGird.getChildren().add(node);
    }

    private void setBoxBehavior(Shape box)
    {   box.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 2)
            {   chooseColor(box.getFill());
            }   
        }
        });

        box.setOnMouseEntered(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getEventType() == MouseEvent.MOUSE_ENTERED)
            {   box.setEffect(new Glow(0.8));
            }   
        }
        });

        box.setOnMouseExited(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getEventType() == MouseEvent.MOUSE_EXITED)
            {   box.setEffect(null);
            }   
        }
        });
        
    }

    private void chooseColor(Paint color)
    {   EtoileControl selectedEtoile = cielControl.getSelectedStar();
        if(selectedEtoile==null) return;
        Paint oldColor = selectedEtoile.getColor();
        selectedEtoile.setColor(color);
        ColorAction action = new ColorAction(color,oldColor,selectedEtoile);
        HoustonCenter.recordAction(action);
    }

    public static class ColorAction implements CielAction
    {   Paint newColor;
        Paint oldColor;
        EtoileControl target;
    
        public ColorAction(Paint newColor, Paint oldColor, EtoileControl target)
        {   this.newColor = newColor;
            this.oldColor = oldColor;
            this.target = target;
        }
        public void undo()
        {   target.setColor(oldColor);
        }
        public void redo()
        {   target.setColor(newColor);
        }
    }
    
}   
