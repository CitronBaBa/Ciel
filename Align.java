import java.util.*;
import java.io.Serializable;

// this class represents links between mind map node 

public class Align implements Serializable
{   private Etoile from;
    private Etoile to;
    public Etoile getFromStar() { return from;}
    public Etoile getToStar() { return to;}
    public Align(Etoile from, Etoile to)
    {   this.from = from;
        this.to = to;
    }
}
