import java.util.*;

// this is use to undo/redo user operations
// history actions recorded in HoustonCenter class 
public interface CielAction
{   public void undo();
    public void redo();
}
