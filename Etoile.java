import java.util.*;
import java.io.Serializable;

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
    public boolean isSubStar() {   return isSubStar;}
    public void becomeSubStar(Etoile parentStar) 
    {   isSubStar = true;
        this.parentStar = parentStar;
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
        // Etoile originalStar = monEtoile; 
        // while(originalStar.isSubStar()) originalStar = originalStar.getParent();      
        // etoileMap.get(originalStar).updateStarPos(originalStar.getCoordination());
}
