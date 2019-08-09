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

    private EtoileControl oldHighlightedStar = null;


    public CielRobot(Map<Etoile,EtoileControl> etoileControls, ScrollPane cielScrolPane)
    {   this.etoileControls = etoileControls;
        this.cielScrolPane = cielScrolPane;
    }

    public boolean stopFlyingAndTryMerge(EtoileControl main, Coordination oldCoor)
    {   if(oldHighlightedStar!=null) oldHighlightedStar.removeEffect();
        boolean result = tryMerge(main,oldCoor);
        oldHighlightedStar = null;
        return result;
    }

    private boolean tryMerge(EtoileControl main, Coordination oldCoor)
    {   if(oldHighlightedStar==main) throw new RuntimeException("merge with itself");
        if(oldHighlightedStar!=null)
        {   oldHighlightedStar.insertChild(main);
            System.out.println(main.getEtoile().getName()+"----"+oldHighlightedStar.getEtoile().getName()+" merged");
            HoustonCenter.recordAction(new MergeAction(oldHighlightedStar,main,oldCoor));
            return true;            
        }
        return false;
    }

    public void highlightHoveredStar(EtoileControl main)
    {   EtoileControl e = findHoveredOverStar(main);
        if(e==oldHighlightedStar) return;
        if(e==null)
        {   removeHighlight();
            oldHighlightedStar = null;
            return;
        }
        
        removeHighlight();
        oldHighlightedStar = e;
        e.addEffect();
    }
    private void removeHighlight()
    {   if(oldHighlightedStar!=null) oldHighlightedStar.removeEffect();
    }

    private EtoileControl findHoveredOverStar(EtoileControl yourself)
    {   Bounds flyingEtoileBound = getBoundsInScene(yourself.getPrimaryView());
        for(EtoileControl e : etoileControls.values())
        {   if(e==yourself) continue;
            Node viewBeneath = e.getPrimaryView();
            Bounds beneathBounds = viewBeneath.localToScene(viewBeneath.getBoundsInLocal());
            if(beneathBounds.intersects(flyingEtoileBound))
            {   return e;
            }
        }
        return null;
    }

    private Bounds getBoundsInScene(Node node)
    {   return node.localToScene(node.getBoundsInLocal());
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


// it is only correct when child star is a parent star before merging
    private static class MergeAction implements CielAction
    {   EtoileControl parent,child;
        Coordination oldPlace;
        EtoileControl oldParent = null;

        public MergeAction(EtoileControl parent, EtoileControl child, Coordination oldPlace)
        {   if(oldParent==null) System.out.println("normal merge");
            this.parent = parent;
            this.child = child;
            this.oldPlace = oldPlace;
        }
        public void undo()
        {   child.becomeFreeStar();
            if(oldParent==null) child.updateStarPos(oldPlace);
            else
            {   System.out.println("old parent inserting");oldParent.insertChild(child);
            }
        }
        public void redo()
        {   if(oldParent==null) parent.insertChild(child);
            else
            {   child.becomeFreeStar();
                parent.insertChild(child);
            }
        }
    }

    

}
