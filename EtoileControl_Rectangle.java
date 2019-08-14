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

public class EtoileControl_Rectangle extends EtoileControl implements Initializable
{
    Rectangle rec;
    //the gap between the curveline and the shape itsself
    private final double curvePosVerticalOffset = 1.2d;

    public EtoileControl_Rectangle(Etoile etoile, Map<Etoile,EtoileControl> etoileMap,
            Map<Align,AlignControl> alignMap, Pane cielArea, Ciel cielModel, BehaviorInjection outerSetup)
    {   super(etoile,etoileMap,alignMap,cielArea,cielModel,outerSetup);
        primaryView.getChildren().remove(etoileShape);
        primaryView.getChildren().add(0,rec);
    }

    protected void initialSetUp()
    {   rec = new Rectangle();
        rec.setArcHeight(15);
        rec.setArcWidth(15);
        rec.setFill(Color.rgb(132, 220, 198, 1));
    }

    protected void dynamicSizing()
    {   rec.widthProperty().bind(name.widthProperty().multiply(1.2f));
        rec.heightProperty().bind(name.heightProperty().multiply(1.2f));
    }

/// initally the width value is 0
//  thanks to the bloody Javafx
    @Override
    protected Point2D giveCenterPoint() //local coordinate system
    {   if(rec.getWidth()==0) return new Point2D(20,10);
        return new Point2D(rec.getWidth()/2,rec.getHeight()/2);
    }
    protected Node getMainShape() { return rec;}
    public void setShapeColor(Paint color)
    {   rec.setFill(color);
    }
    public Color getShapeColor()
    {   return (Color)rec.getFill();
    }

    @Override
    protected String getFxmlName()
    {   if(getEtoile().isSubStar()) return "EtoileSub.fxml";
        else return "EtoileMain.fxml";
    }

    protected ObservableDoubleValue getWidthProperty()
    {   return rec.widthProperty();
    }

    //ciel coordinate system
    @Override
    protected DoubleBinding bottomRightX()
    {   Coordination coor = getEtoile().getCoordination();
        return coor.getXProperty().add
                (rec.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomRightY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (rec.heightProperty().divide(2.0f).add(curvePosVerticalOffset).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomLeftX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().subtract
                (rec.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding bottomLeftY()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getYProperty().add
                (rec.heightProperty().divide(2.0f).add(curvePosVerticalOffset).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected DoubleBinding centerRightX()
    {   Coordination coor = monEtoile.getCoordination();
        return coor.getXProperty().add
                (rec.widthProperty().divide(2.0f).multiply(cielModel.getScaleProperty()));
    }

    @Override
    protected ObservableDoubleValue centerRightY()
    {   Coordination coor = getEtoile().getCoordination();
        return coor.getYProperty();
    }
}
