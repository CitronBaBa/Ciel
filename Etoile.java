import java.util.*;
import java.io.Serializable;

// this class is a mind map node
// etoile actually means star  

public class Etoile implements Serializable
{   private String name = "Node";
    private String text = "";
    private double[] color = null;

    // remebering two coordinates to facilitate operations in javafx
    private Coordination etoilePos = new Coordination(100,100);
    private Coordination viewPos = null;

    private List<Etoile> children = new ArrayList<>();
    private Etoile parentStar;
    private boolean isSubStar = false;

    // java
    private String className;
    public void setClassName(String className) { this.className = className; }
    public String getClassName() {   return className;}

    public boolean isSubStar() {   return isSubStar;}
    public void becomeSubStar(Etoile parentStar) 
    {   isSubStar = true;
        this.parentStar = parentStar;
    }
    public void detachFromParent()
    {   isSubStar = false;
        this.parentStar = null;
    }
    public void sendChildAway(Etoile child)
    {   children.remove(child);
    }
    public Etoile getParent() {   return parentStar;}

    public Etoile(String name)
    {   this.name = name;
    }
    public Etoile(String name, boolean isSubStar, Etoile parentStar)
    {   this.name = name;
        this.isSubStar = isSubStar;
        this.parentStar = parentStar;
    }
    public void updateCoordination(Coordination newPos)
    {   this.etoilePos.setX(newPos.getX());
        this.etoilePos.setY(newPos.getY());
    }
    public void updateCoordination(Coordination newPos, Coordination viewPos)
    {   this.etoilePos.setX(newPos.getX());
        this.etoilePos.setY(newPos.getY());
        this.viewPos = viewPos;
    }
    
    public void setColor(double[] color){   this.color = color;}
    public double[] getColor() {  return this.color;}
    public String getName() {   return name;}
    public void setName(String name) { this.name = name;}
    public String getText() {   return text;}
    public void setText(String text) { this.text = text;}

    public Coordination getCoordination() {   return etoilePos;}
    public Coordination getViewCoor() {   return viewPos;}
    public void addChild(Etoile newChild) {   children.add(newChild);}
    public List<Etoile> getChildren() {   return children;}

    public boolean removeOneInChildren(Etoile star)
    {   if(children.remove(star)) return true;
        for(Etoile e : children)
        {   if(e.removeOneInChildren(star)) 
            return true;
        }
        return false;
    }
    
    public String toString()
    {   if(className!=null) return "ClassName:"+className;
        return "EName:"+name;
    }

// create a brand new kindergarten recursively
// but the first caller will not copy its parent and return as a free star
    public Etoile giveADeepCopy()
    {   Etoile deepCopy = this.giveACopy();
        for(Etoile child : this.getChildren())
        {   Etoile childDeepCopy = child.giveADeepCopy();
            childDeepCopy.becomeSubStar(deepCopy);
            deepCopy.addChild(childDeepCopy);
        }
        return deepCopy;
    }

// give a free copy (discard parent-child relation)
    protected Etoile giveACopy()
    {   Etoile copy = new Etoile(name);
        copy.setText(text);
        copy.setColor(color);
        copy.updateCoordination(etoilePos,viewPos);
        copy.setClassName(className);
        return copy;
    }

        // Etoile originalStar = monEtoile; 
        // while(originalStar.isSubStar()) originalStar = originalStar.getParent();      
        // etoileMap.get(originalStar).updateStarPos(originalStar.getCoordination());
}
