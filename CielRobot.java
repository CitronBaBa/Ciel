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

    public boolean stopFlyingAndTryMerge(EtoileControl main, EtoileControl oldParent,EtoileControl sudoE, Coordination oldCoor)
    {   if(oldHighlightedStar!=null) oldHighlightedStar.removeEffect();
        boolean result = tryMerge(main,oldParent,sudoE,oldCoor);
        oldHighlightedStar = null;
        return result;
    }

    private boolean tryMerge(EtoileControl main, EtoileControl oldParent, EtoileControl sudoE, Coordination oldCoor)
    {   if(oldHighlightedStar==main) throw new RuntimeException("merge with itself");
        if(oldHighlightedStar!=null)
        {   int oldIndex = 0; int index = 0;
            index = caculateInsertationPos(oldHighlightedStar,main);
            if(oldParent!=null) 
            {   oldIndex = oldParent.getEtoile().getChildren().indexOf(main.getEtoile());
                index = caculateInsertationPos(oldHighlightedStar,sudoE);
            }
            if(main.getEtoile().isSubStar())
            {   main.showStarRecursively();
                main.becomeFreeStar();            
            }
            oldHighlightedStar.insertChild(main,index);
            System.out.println(main.getEtoile().getName()+"----"+oldHighlightedStar.getEtoile().getName()+" merged");
            HoustonCenter.recordAction(new MergeAction(oldHighlightedStar,main,oldParent,oldCoor,oldIndex,index));
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

        EtoileControl resultE = null;
        double maxOverlapPercentage = 0;
        for(EtoileControl e : etoileControls.values())
        {   if(e==yourself) continue;
            if(e.getView().isVisible()==false) continue;
            Bounds beneathBounds = getBoundsInScene(e.getPrimaryView());
            if(beneathBounds.intersects(flyingEtoileBound))
            {   // intersected surface divded by flying surface
                double percent = caculateIntersectPercentage(beneathBounds,flyingEtoileBound); 
                if(percent>maxOverlapPercentage)
                {   maxOverlapPercentage = percent;
                    resultE = e;
                }
            }
        }
        return resultE;
    }

    private Bounds getBoundsInScene(Node node)
    {   return node.localToScene(node.getBoundsInLocal());
    }

    private double caculateIntersectPercentage(Bounds a, Bounds b)
    {   double leftX = Math.max(a.getMinX(),b.getMinX());
        double rightX = Math.min(a.getMaxX(),b.getMaxX());
        double topY = Math.max(a.getMinY(),b.getMinY());
        double bottomY = Math.min(a.getMaxY(),b.getMaxY());
        if(leftX>rightX) return 0;
        return (rightX-leftX)*(bottomY-topY)/(b.getWidth()*b.getHeight());
    }

// using center point may be more intuitive
    private int caculateInsertationPos(EtoileControl parent, EtoileControl futureChild)
    {   int num = parent.getEtoile().getChildren().size();
        double targetY = getBoundsInScene(futureChild.getPrimaryView()).getMinY();
        for(int index=0; index<num; index++)
        {   EtoileControl currentChild = etoileControls.get(parent.getEtoile().getChildren().get(index));
            double currentY = getBoundsInScene(currentChild.getPrimaryView()).getMinY();
            if(targetY<currentY) return index;
        }
        if(num==0) return 0;
        return (num-1);
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
        int oldIndex;
        int newIndex;

        public MergeAction(EtoileControl parent, EtoileControl child, EtoileControl oldParent, Coordination oldPlace, int oldIndex, int newIndex)
        {   this.parent = parent;
            this.child = child;
            this.oldPlace = oldPlace;
            this.oldParent = oldParent;
            this.oldIndex = oldIndex;     
            this.newIndex = newIndex;
        }

        public void undo()
        {   child.becomeFreeStar();
            if(oldParent==null) child.updateStarPos(oldPlace);
            else oldParent.insertChild(child,oldIndex);
            
        }
        public void redo()
        {   if(oldParent==null) parent.insertChild(child,newIndex);
            else
            {   child.becomeFreeStar();
                parent.insertChild(child,newIndex);
            }
        }
    }

    

}
