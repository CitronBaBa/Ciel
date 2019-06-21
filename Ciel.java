import java.util.*;

public class Ciel
{   private List<Etoile> etoilesOnSky = new ArrayList<>();
    
    public static void main(String[] args) 
    {   Ciel newMap = new Ciel();
        Etoile newNode = new Etoile("hahaha");
        Etoile newNode2 = new Etoile("hahahas");
        newMap.addStar(newNode);newMap.addStar(newNode2);
        newMap.printMap();
    }

    public void addStar(Etoile node)
    {   etoilesOnSky.add(node);
    }

    public void printMap()
    {   for(Etoile n : etoilesOnSky)
        System.out.println(n.getName());
    }
}
