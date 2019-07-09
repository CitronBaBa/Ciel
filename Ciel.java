import java.util.*;
import java.io.Serializable;

public class Ciel implements Serializable
{   private List<Etoile> etoilesOnSky = new ArrayList<>();
    private List<Align> alignsOnSky = new ArrayList<>();

    public List<Etoile> getParentEtoiles(){   return etoilesOnSky;}
    public List<Align> getAligns(){   return alignsOnSky;}

    public void addParentStar(Etoile node)
    {   etoilesOnSky.add(node);
    }
    public void removeStar(Etoile star)
    {   if(etoilesOnSky.remove(star)) return;
        for(Etoile e : etoilesOnSky)
        {  if(e.removeOneInChildren(star)) return;
        }
    }

    public void printMap()
    {   for(Etoile n : etoilesOnSky)
        System.out.println(n.getName());
    }
    public static void main(String[] args) 
    {   Ciel newMap = new Ciel();
        Etoile newNode = new Etoile("hahaha");
        Etoile newNode2 = new Etoile("hahahas");
        newMap.addParentStar(newNode);newMap.addParentStar(newNode2);
        newMap.printMap();
    }
}
