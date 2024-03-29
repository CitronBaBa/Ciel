import java.util.*;
import java.io.*;
import javafx.beans.property.*;

// this class is the representation of the mind map as a whole
// it includes parent nodes (stars) and links
// child nodes' reference is hold by its parent

/* notic the 'Ciel' is actually a french word meaning the sky
   and 'Etoile' actually means star
   sorry for my spoiled naming practice
   understand these words will make it a lot easier to understand the program
*/

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
    public void readJavaFiles(List<File> javaFiles, File parseDir)
    {   List<Etoile> resultedEtoiles = javaManager.readJavaFiles(javaFiles,parseDir);
        for(Etoile e : resultedEtoiles)
        {   if(!e.isSubStar()) etoilesOnSky.add(e);
        }
    }
    public void readJavaFiles(File targetDir)
    {   List<Etoile> resultedEtoiles = javaManager.readJavaFiles(targetDir);
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
