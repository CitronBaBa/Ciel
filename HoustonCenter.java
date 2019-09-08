import java.util.*;

// maintaing two systems, event subscribe-publish system 
      //and the action redo system 
   // serves as a center class in both systems (as its name)

public class HoustonCenter 
{   // for actions
    private static int index = -1;
    private static List<CielAction> actionList = new ArrayList<>();
    
    // for events
    private static List<CielEvent> eventQueue = new LinkedList<>();
    private static List<CielEventSubscriber> subscribers = new ArrayList<>();
    
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
    public static void clearActionList()
    {   actionList.clear();
        index = -1;
    }

    public static void subscribe(CielEventSubscriber subscriber)
    {   subscribers.add(subscriber);
    }

    public static void propagateEvent(CielEvent event)
    {   for(CielEventSubscriber s : subscribers)
        {   s.reactOnEvent(event);
        }
    }
    
}
