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
import javafx.scene.effect.*;

public class CielRobot
{   private Map<Etoile,EtoileControl> etoileControls;
    private ScrollPane cielScrolPane;
    public CielRobot(Map<Etoile,EtoileControl> etoileControls, ScrollPane cielScrolPane)
    {   this.etoileControls = etoileControls;
        this.cielScrolPane = cielScrolPane;
    }

    public void arrangeAllStars()
    {   List<EtoileControl> remainList = new ArrayList<>();
        for(EtoileControl e : etoileControls.values())
        {   if(!e.getEtoile().isSubStar()) remainList.add(e);
        }
        double finalHeight = arrange(remainList,0,false);
        if(remainList.size()!=0) arrange(remainList,finalHeight,true);
    }

    private double arrange(List<EtoileControl> remainList, double startY, boolean extra)
    {   double startX = 0;
        double maxheight = 0;
        List<EtoileControl> arranged = new ArrayList<>();
        for(EtoileControl e: remainList)
        {   //System.out.println(e.getView().getWidth());
            if(startX>cielScrolPane.getWidth()) break;
            double width = e.getView().getBoundsInLocal().getWidth();
            double height = e.getView().getBoundsInLocal().getHeight();
            if((startX+width)>cielScrolPane.getWidth() && !extra) continue;
            e.getView().relocate(startX,startY);
            if(height>maxheight) maxheight = height;
            startX += width;
            arranged.add(e);
        }
        remainList.removeAll(arranged);

        if(arranged.size()==0) return startY+maxheight;
        return arrange(remainList,startY+maxheight,extra);
    }

}
