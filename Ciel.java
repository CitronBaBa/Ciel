import java.util.*;
import java.io.*;
import javafx.beans.property.*;

public class Ciel implements Serializable
{   private List<Etoile> etoilesOnSky = new ArrayList<>();
    private List<Align> alignsOnSky = new ArrayList<>();
    private transient DoubleProperty scaleCoeeficent = new SimpleDoubleProperty(1.0f);
    private CielJavaManager javaManager = new CielJavaManager();

    public CielJavaManager getJavaManager() {   return javaManager;}
    public List<Etoile> getParentEtoiles(){   return etoilesOnSky;}
    public List<Align> getAligns(){   return alignsOnSky;}

    public void addParentStar(Etoile node)
    {   etoilesOnSky.add(node);
    }
    public double getScale()
    {   return scaleCoeeficent.get();
    }
    public DoubleProperty getScaleProperty()
    {   return scaleCoeeficent;
    }
    private void writeObject(ObjectOutputStream out)
    throws IOException
    {   out.defaultWriteObject();   // always call this first
        out.writeObject(scaleCoeeficent.get());
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {   in.defaultReadObject();    // always call this first
        scaleCoeeficent = new SimpleDoubleProperty((double) in.readObject());
    }

    public void removeStar(Etoile star)
    {   if(etoilesOnSky.remove(star)) return;
        for(Etoile e : etoilesOnSky)
        {  if(e.removeOneInChildren(star)) return;
        }
    }

    public List<Align> giveRelatedAligns(Etoile targetStar)
    {   List<Align> results = new ArrayList<>();
        for(Align a : alignsOnSky)
        {   if(targetStar == a.getFromStar() || targetStar == a.getToStar())
            {   results.add(a);
            }
        }
        return results;
    }

    public void readJavaFiles(List<File> javaFiles)
    {   List<Etoile> resultedEtoiles = javaManager.readJavaFiles(javaFiles);
        for(Etoile e : resultedEtoiles)
        {   if(!e.isSubStar()) etoilesOnSky.add(e);
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
