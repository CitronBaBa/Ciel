import java.util.*;

public class Etoile
{   private String name;
    private Coordination etoilePos = new Coordination(100,100);
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
    public void updateCoordination(Coordination newPos)
    {   this.etoilePos.setX(newPos.getX());
        this.etoilePos.setY(newPos.getY());
    }
    public String getName() {   return name;}
    public void setName(String name) { this.name = name;}
    public Coordination getCoordination() {   return etoilePos;}
    public void addChild(Etoile newChild) {   children.add(newChild);}
    public List<Etoile> getChildren() {   return children;}
}
