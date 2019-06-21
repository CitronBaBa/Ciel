import java.util.*;

public class Align
{   private Etoile from;
    private Etoile to;
    public Etoile getFromStar() { return from;}
    public Etoile getToStar() { return to;}
    public Align(Etoile from, Etoile to)
    {   this.from = from;
        this.to = to;
    }
}
