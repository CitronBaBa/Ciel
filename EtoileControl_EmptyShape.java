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

// an etoile without anyshape (only text)

public class EtoileControl_EmptyShape extends EtoileControl implements Initializable
{   public EtoileControl_EmptyShape(Etoile etoile, Map<Etoile,EtoileControl> etoileMap,
            Map<Align,AlignControl> alignMap, Pane cielArea, Ciel cielModel)
    {   super(etoile,etoileMap,alignMap,cielArea,cielModel);
        primaryView.getChildren().remove(etoileShape);
    }

    protected void dynamicSizing()
    {   ;
    }


    @Override
    protected Point2D giveCenterPoint() //local coordinate system
    {   return new Point2D(name.getWidth()/2,name.getHeight()/2);
    }
    protected Node getMainShape() { return name;}
    public void setShapeColor(Paint color)
    {   name.setTextFill(color);
    }
    public Color getShapeColor()
    {   return (Color)name.getTextFill();
    }

    @Override
    protected String getFxmlName() 
    {   if(getEtoile().isSubStar()) return "EtoileSub.fxml";
        else return "EtoileMain.fxml";
    }

    protected ObservableDoubleValue getWidthProperty()
    {   return name.widthProperty();
    }

    //ciel coordinate system
    @Override
    protected DoubleBinding bottomRightX()
    {   Coordination coor = getEtoile().getCoordination();
        return coor.getXProperty().add
                (name.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomRightY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (name.heightProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomLeftX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().subtract
                (name.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomLeftY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (name.heightProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding centerRightX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().add
                (name.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected ObservableDoubleValue centerRightY()
    {   Coordination coor = getEtoile().getCoordination();
        return coor.getYProperty();
    }
}
