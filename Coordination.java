import javafx.beans.property.*;
import java.io.*;

public class Coordination implements Serializable
{   private transient DoubleProperty x = new SimpleDoubleProperty(); 
    private transient DoubleProperty y = new SimpleDoubleProperty();
    public Coordination(double x, double y)
    {   this.x.set(x); 
        this.y.set(y);
    }
    public double getX() { return x.getValue();}
    public double getY() { return y.getValue();}
    public DoubleProperty getXProperty(){ return x;}
    public DoubleProperty getYProperty(){ return y;}
    public void setX(double x) { this.x.set(x);}
    public void setY(double y) { this.y.set(y);}

    private void writeObject(ObjectOutputStream out)
    throws IOException 
    {   out.defaultWriteObject();   // always call this first
        out.writeObject(x.get());
        out.writeObject(y.get());
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException 
    {   in.defaultReadObject();    // always call this first
        x = new SimpleDoubleProperty((double) in.readObject());
        y = new SimpleDoubleProperty((double) in.readObject());
    }

    public void print()
    {   System.out.println("x = " + x.get() + ", y = "+y.get());
    }
}
