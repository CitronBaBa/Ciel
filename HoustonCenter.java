import java.util.*;

public class HoustonCenter 
{   // for actions
    private static int index = -1;
    private static List<CielAction> actionList = new ArrayList<>();
    
    // for events
    private static List<CielEvent> eventQueue = new LinkedList<>();
        
    public static void recordAction(CielAction action)
    {   actionList.add(++index,action);
        actionList = actionList.subList(0,index+1);
    }
    public static void undoAction()
    {   if(index==-1) return;
        actionList.get(index).undo();
        index--;
    }
    public static void redoAction()
    {   if(index==actionList.size()-1) return;
        actionList.get(++index).redo();
    }
    
}
