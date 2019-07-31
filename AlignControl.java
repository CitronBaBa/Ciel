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

public class AlignControl 
{   private Align align;
    private QuadCurve curve;
    private Map<Align, AlignControl> alignControls;
    private Pane cielArea;
    private Ciel cielModel;

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

        curve = new QuadCurve();
        setCurveCoor(curve,from,to);
        cielArea.getChildren().add(1,curve);
        alignControls.put(align,this);
    }

    public void removeYourself()
    {   cielArea.getChildren().remove(curve);
        alignControls.remove(align);
        cielModel.getAligns().remove(align);
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
