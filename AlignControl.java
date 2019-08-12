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
import javafx.scene.transform.*;

public class AlignControl 
{   private Align align;
    private Path curve;
    private Map<Align, AlignControl> alignControls;
    private Pane cielArea;
    private Ciel cielModel;
    private Polygon arrow;
    private Rotate rotate;

    public AlignControl(Align align, Map<Align,AlignControl> alignControls, Pane cielArea, Ciel cielModel)
    {   this.align = align;
        this.alignControls = alignControls;
        this.cielArea = cielArea;
        this.cielModel = cielModel;
    }
    public Align getAlign() {  return align;}

    public void addAndDrawYourself()
    {   drawYourself();
        cielModel.getAligns().add(align);
    }

    public void drawYourself()
    {   Coordination from = align.getFromStar().getCoordination(); 
        Coordination to = align.getToStar().getCoordination();

        drawAlignCurve(from,to);
        cielArea.getChildren().add(1,curve);
        cielArea.getChildren().add(arrow);
        alignControls.put(align,this);
    }

    public void removeYourself()
    {   cielArea.getChildren().removeAll(curve,arrow);
        alignControls.remove(align);
        cielModel.getAligns().remove(align);
    }

    public void drawAlignCurve(Coordination from, Coordination to)
    {   curve = new Path();
        curve.setId("curve");
        curve.relocate(0,0);
        
        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(from.getXProperty());
        moveTo.yProperty().bind(from.getYProperty());

        CubicCurveTo curveTo = new CubicCurveTo();
        curveTo.xProperty().bind(to.getXProperty());
        curveTo.yProperty().bind(to.getYProperty());

        curve.getElements().addAll(moveTo,curveTo);

        //arrow
        double c = 16;
        double adjust = 2;
        arrow = new Polygon(0,0-c*Math.pow(3,0.5)/3*adjust,
                    c/2,Math.pow(3,0.5)/6,
                    0-c/2,Math.pow(3,0.5)/6);
        rotate = new Rotate();
        Scale scale = new Scale();
        scale.xProperty().bind(cielModel.getScaleProperty());
        scale.yProperty().bind(cielModel.getScaleProperty());
        scale.setPivotX(0);
        scale.setPivotY(0);
        arrow.setId("arrow");
        // arrow.scaleXProperty().bind(cielModel.getScaleProperty());
        // arrow.scaleYProperty().bind(cielModel.getScaleProperty());
        rotate.setAxis(Rotate.Z_AXIS);
        arrow.getTransforms().addAll(rotate,scale);

        updateArrow(curveTo,from,to);
        from.getXProperty().addListener((obs,oldV,newV)->{updateArrow(curveTo,from,to);});
        from.getYProperty().addListener((obs,oldV,newV)->{updateArrow(curveTo,from,to);});
        to.getXProperty().addListener((obs,oldV,newV)->{updateArrow(curveTo,from,to);});
        to.getYProperty().addListener((obs,oldV,newV)->{updateArrow(curveTo,from,to);});
    }

    private void updateArrow(CubicCurveTo curveTo, Coordination from, Coordination to)
    {   float t = 0.5f;
        
        double xMiddle = (to.getX()+from.getX())/2;
        double yMiddle = (to.getY()+from.getY())/2;
        double k = (from.getX()-to.getX())/(to.getY()-from.getY());
        // this distance can well be anything else
                double offLineDistance = (xMiddle-from.getX());
        double cX1 = xMiddle - offLineDistance/(1+Math.pow(k,2));
        double cX2 = xMiddle + offLineDistance/(1+Math.pow(k,2));
        double cY1 = k*(cX1-xMiddle)+yMiddle;
        double cY2 = k*(cX2-xMiddle)+yMiddle;
        curveTo.setControlX2(cX2); curveTo.setControlY2(cY2);
        curveTo.setControlX1(cX1); curveTo.setControlY1(cY1);

        arrow.setTranslateX(Math.pow(1-t,3)*from.getX()+
                3*t*Math.pow(1-t,2)*curveTo.getControlX1()+
                3*(1-t)*t*t*curveTo.getControlX2()+
                Math.pow(t, 3)*to.getX());
        arrow.setTranslateY(Math.pow(1-t,3)*from.getY()+
                3*t*Math.pow(1-t, 2)*curveTo.getControlY1()+
                3*(1-t)*t*t*curveTo.getControlY2()+
                Math.pow(t, 3)*to.getY());
        Point2D tan = new Point2D(-3*Math.pow(1-t,2)*from.getX()+
                 3*(Math.pow(1-t, 2)-2*t*(1-t))*curveTo.getControlX1()+
                 3*((1-t)*2*t-t*t)*curveTo.getControlX2()+
                 3*Math.pow(t, 2)*to.getX(),
                 -3*Math.pow(1-t,2)*from.getY()+
                 3*(Math.pow(1-t, 2)-2*t*(1-t))*curveTo.getControlY1()+
                 3*((1-t)*2*t-t*t)*curveTo.getControlY2()+
                 3*Math.pow(t, 2)*to.getY());

        double size = Math.max(curve.getBoundsInLocal().getWidth(), curve.getBoundsInLocal().getHeight());
        double scale = size / 4d;
        tan = tan.normalize().multiply(scale);
        double angle = Math.atan2( tan.getY(), tan.getX());
        angle = Math.toDegrees(angle);
        double offset = -90;
        if( t > 0.4) offset = +90;
        rotate.setAngle(angle + offset);

        
    }
    

    public static void setCurveCoor(QuadCurve curveLine, Coordination from, Coordination to)
    {   curveLine.endXProperty().bind(to.getXProperty().subtract(from.getXProperty()));
        curveLine.endYProperty().bind(to.getYProperty().subtract(from.getYProperty()));
        curveLine.layoutXProperty().bind(from.getXProperty());
        curveLine.layoutYProperty().bind(from.getYProperty());
        curveLine.setStartX(0);
        curveLine.setStartY(0);
        curveLine.setControlX(50);
        curveLine.setControlX(50);
        curveLine.setId("curve");
    }


}
