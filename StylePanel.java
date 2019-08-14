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

public class StylePanel implements CielEventSubscriber
{   private CielControl cielControl;
    private GlobalSatellite globals;
    private TabPane bottomTabs = new TabPane();
    private Tab interfaceTab;

    private HBox bottomBox = new HBox();
    private GridPane colorGird = new GridPane();
    private int x = 0; private int y = 0;

    private GridPane interfaceGrid = new GridPane();
    private int x1 = 0; private int y1 = 0;

    private final double recHeight = 30;
    public Node getPanel()
    {   return bottomTabs;
    }

    public void reactOnEvent(CielEvent event)
    {   if(event==CielEvent.LoadNewModel)
        {   updateInterfacePanel();
        }
    }

    public StylePanel(CielControl cielControl)
    {   this.cielControl = cielControl;
        this.globals = GlobalSatellite.getSatellite();
        HoustonCenter.subscribe(this);
        init();
    }

    private void init()
    {   ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e->chooseColor(colorPicker.getValue()));
        createColorGird();
        colorPicker.setPrefHeight(recHeight);
        bottomBox.getChildren().addAll(colorGird,colorPicker);
        bottomBox.setId("bottom_color_box");
        bottomBox.setAlignment(Pos.TOP_LEFT);
        colorPicker.getStyleClass().add("button");
        Tab colorTab = new Tab("Color",bottomBox);
        interfaceTab = new Tab("interfaces",interfaceGrid);
        colorTab.setClosable(false);
        interfaceTab.setClosable(false);
        bottomTabs.getTabs().addAll(colorTab,interfaceTab);
        bottomTabs.setPickOnBounds(false);
        updateInterfacePanel();
    }

    private void updateInterfacePanel()
    {   interfaceGrid.getChildren().clear();
        x1 = 0; y1 = 0;
        //Map<String,List<Etoile>> implementations = globals.getCielModel().getJavaManager().getImplementations();
        Map<String,double[]> interfaces = globals.getCielModel().getJavaManager().getInterfaces();
        if(globals.getCielModel().getJavaManager().getLoadedClassesCount()==0)
        {   bottomTabs.getTabs().remove(interfaceTab); 
            return;
        }
        else if(!bottomTabs.getTabs().contains(interfaceTab))
        {   bottomTabs.getTabs().add(interfaceTab);
        }
        for(Map.Entry<String,double[]> pair: interfaces.entrySet())
        {   String interfaceName = pair.getKey();
            double[] colorFigures = pair.getValue();
            // Color = new Color(colorFigures[0],colorFigures[1],colorFigures[2],colorFigures[3]);
            Label label = new Label(interfaceName);
            label.setPrefHeight(recHeight);
            label.setId("interfaceLabel");
            label.setStyle("-fx-background-color:rgb("+colorFigures[0]*255+","+colorFigures[1]*255+
                ","+colorFigures[2]*255+","+colorFigures[3]+")");
            setInterfaceBoxBehavior(label);
            placeOneInterfaceBox(label);
        }
    }

    private void placeOneInterfaceBox(Node node)
    {   GridPane.setConstraints(node,x++,y);
        if(x1>9)
        {   x1=0;
        	y1++;
        }
        interfaceGrid.getChildren().add(node);
    }

    private void createColorGird()
    {   for(Color c : colorPalette)
        {   drawOneColorBox(c);
        }
    }
    private void drawOneColorBox(Color color)
    {   Rectangle box = new Rectangle(40,recHeight,color);
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

    private void setInterfaceBoxBehavior(Label box)
    {   box.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   highlightRelatedStars(box.getText());
            }
        }
        });
        setMovingEffect(box);
    }

    private void highlightRelatedStars(String interfaceName)
    {   Map<String,List<Etoile>> implementations = globals.getCielModel().getJavaManager().getImplementations();
        Map<String,double[]> interfaces = globals.getCielModel().getJavaManager().getInterfaces();

        List<Etoile> targets = implementations.get(interfaceName);
        double[] colorFigures = interfaces.get(interfaceName);
        Color targetColor = new Color(colorFigures[0],colorFigures[1],colorFigures[2],colorFigures[3]);


        Map<EtoileControl,Color> oldColors = new HashMap<>();
        Map<EtoileControl,Color> newColors = new HashMap<>();


        //actions
        cielControl.restoreAllColors();
        for(Etoile e : targets)
        {   EtoileControl eController = cielControl.getEtoileControls().get(e);
            if(eController==null) continue;
            oldColors.put(eController,eController.getShapeColor());
            newColors.put(eController,targetColor);
            eController.setShapeColor(targetColor);
        }

        //HoustonCenter.recordAction(new MultiColorAction(oldColors,newColors));
    }


    private void setBoxBehavior(Shape box)
    {   box.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event)
        {   if(event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 1)
            {   chooseColor((Color)box.getFill());
            }
        }
        });

        setMovingEffect(box);
    }

    private void setMovingEffect(Node box)
    {   box.setOnMouseEntered(new EventHandler<MouseEvent>() {
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

    private void chooseColor(Color color)
    {   EtoileControl selectedEtoile = cielControl.getSelectedStar();
        if(selectedEtoile==null) return;
        Color oldColor = selectedEtoile.getColor();
        selectedEtoile.setColor(color);
        ColorAction action = new ColorAction(color,oldColor,selectedEtoile);
        HoustonCenter.recordAction(action);
    }

    public static class ColorAction implements CielAction
    {   Color newColor;
        Color oldColor;
        EtoileControl target;

        public ColorAction(Color newColor, Color oldColor, EtoileControl target)
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

    public static class MultiColorAction implements CielAction
    {   Map<EtoileControl,Color> oldColors;
        Map<EtoileControl,Color> newColors;

        public MultiColorAction(Map<EtoileControl,Color> oldColors, Map<EtoileControl,Color> newColors)
        {   this.newColors = newColors;
            this.oldColors = oldColors;
        }
        public void undo()
        {   for(Map.Entry<EtoileControl,Color> pair: oldColors.entrySet())
            {   pair.getKey().setShapeColor(pair.getValue());
            }
        }
        public void redo()
        {   for(Map.Entry<EtoileControl,Color> pair: newColors.entrySet())
            {   pair.getKey().setShapeColor(pair.getValue());
            }
        }
    }

    private static Color[] colorPalette = {
      Color.rgb(255, 255, 255, 1),
      Color.rgb(132, 220, 198, 1),
      Color.rgb(165, 255, 214, 1),
      Color.rgb(255, 166, 158, 1),
      Color.rgb(255, 104, 107, 1),
      Color.rgb(150,206,180),
      Color.rgb(255,238,173),
      Color.rgb(255,111,105),
      Color.rgb(255,204,92),
      Color.rgb(136,216,176),
   };

}
